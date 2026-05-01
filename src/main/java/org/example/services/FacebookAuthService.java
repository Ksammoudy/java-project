package org.example.services;

import com.sun.net.httpserver.HttpServer;
import org.example.models.SocialLoginResult;
import org.example.models.User;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class FacebookAuthService {

    private final UserService userService = UserService.getInstance();

    private final String clientId = "1337612458412063";
    private final String clientSecret = "14f5369e553c329cc2bd761f9bd85c2a";
    private final String redirectUri = "http://localhost:8080/callback";

    public interface AuthCallback {
        void onSuccess(SocialLoginResult result);
        void onError(String message);
    }

    public void loginWithFacebook(AuthCallback callback) {
        try {
            URI redirect = URI.create(redirectUri);
            int port = redirect.getPort() == -1 ? 80 : redirect.getPort();
            String path = redirect.getPath();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext(path, exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = extractQueryParam(query, "code");
                String error = extractQueryParam(query, "error");

                if (!isBlank(error)) {
                    sendHtml(exchange, "Connexion Facebook annulée.");
                    server.stop(0);
                    callback.onError("Connexion Facebook annulée.");
                    return;
                }

                if (isBlank(code)) {
                    sendHtml(exchange, "Code OAuth manquant.");
                    server.stop(0);
                    callback.onError("Code Facebook manquant.");
                    return;
                }

                sendHtml(exchange, "Connexion réussie. Vous pouvez fermer cette fenêtre.");
                server.stop(0);

                CompletableFuture.runAsync(() -> handleCode(code, callback));
            });

            server.start();

            String authUrl =
                    "https://www.facebook.com/v19.0/dialog/oauth"
                            + "?client_id=" + urlEncode(clientId)
                            + "&redirect_uri=" + urlEncode(redirectUri)
                            + "&scope=" + urlEncode("email,public_profile")
                            + "&response_type=code";

            Desktop.getDesktop().browse(URI.create(authUrl));

        } catch (Exception e) {
            callback.onError("Erreur Facebook OAuth : " + e.getMessage());
        }
    }

    private void handleCode(String code, AuthCallback callback) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String tokenUrl =
                    "https://graph.facebook.com/v19.0/oauth/access_token"
                            + "?client_id=" + urlEncode(clientId)
                            + "&redirect_uri=" + urlEncode(redirectUri)
                            + "&client_secret=" + urlEncode(clientSecret)
                            + "&code=" + urlEncode(code);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .GET()
                    .build();

            HttpResponse<String> tokenResponse =
                    client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            String accessToken =
                    extractJsonValue(tokenResponse.body(), "access_token");

            if (isBlank(accessToken)) {
                callback.onError("Impossible de récupérer le token Facebook.");
                return;
            }

            String profileUrl =
                    "https://graph.facebook.com/me?fields=id,name,email"
                            + "&access_token=" + urlEncode(accessToken);

            HttpRequest profileRequest = HttpRequest.newBuilder()
                    .uri(URI.create(profileUrl))
                    .GET()
                    .build();

            HttpResponse<String> profileResponse =
                    client.send(profileRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("FACEBOOK PROFILE = " + profileResponse.body());

            String email = extractJsonValue(profileResponse.body(), "email");
            String fullName = extractJsonValue(profileResponse.body(), "name");

            if (isBlank(email)) {
                callback.onError(
                        "Facebook n'a pas fourni l'email réel.\n"
                                + "Ajoutez un email confirmé sur Facebook puis reconnectez-vous."
                );
                return;
            }

            email = email.trim().toLowerCase();

            User existingUser = userService.getUserByEmail(email);

            if (existingUser != null) {
                userService.updateLastSeen(existingUser.getId());

                callback.onSuccess(
                        new SocialLoginResult(
                                true,
                                existingUser,
                                email,
                                fullName,
                                "FACEBOOK"
                        )
                );

            } else {

                callback.onSuccess(
                        new SocialLoginResult(
                                false,
                                null,
                                email,
                                fullName,
                                "FACEBOOK"
                        )
                );
            }

        } catch (Exception e) {
            callback.onError("Erreur Facebook : " + e.getMessage());
        }
    }

    private void sendHtml(com.sun.net.httpserver.HttpExchange exchange, String message)
            throws IOException {

        byte[] bytes =
                ("<html><body><h3>" + message + "</h3></body></html>")
                        .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .add("Content-Type", "text/html; charset=UTF-8");

        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);

            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) {
            return null;
        }

        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);

        if (start == -1) {
            return null;
        }

        start += pattern.length();

        int end = json.indexOf("\"", start);

        if (end == -1) {
            return null;
        }

        return json.substring(start, end);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}