package org.example.services;

import com.sun.net.httpserver.HttpServer;
import org.example.models.SocialLoginResult;
import org.example.models.User;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class GoogleAuthService {

    private final UserService userService = UserService.getInstance();

    private final String clientId = System.getenv("GOOGLE_CLIENT_ID");
    private final String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
    private final String redirectUri = "http://localhost:8000/connect/google/check";

    public interface AuthCallback {
        void onSuccess(SocialLoginResult result);
        void onError(String message);
    }

    public void loginWithGoogle(AuthCallback callback) {
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
                    sendHtml(exchange, "Connexion Google annulée.");
                    server.stop(0);
                    callback.onError("Connexion Google annulée.");
                    return;
                }

                if (isBlank(code)) {
                    sendHtml(exchange, "Code OAuth manquant.");
                    server.stop(0);
                    callback.onError("Code Google manquant.");
                    return;
                }

                sendHtml(exchange, "Connexion Google réussie. Vous pouvez fermer cette fenêtre.");
                server.stop(0);

                CompletableFuture.runAsync(() -> handleCode(code, callback));
            });

            server.start();

            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + urlEncode(clientId)
                    + "&redirect_uri=" + urlEncode(redirectUri)
                    + "&response_type=code"
                    + "&scope=" + urlEncode("openid email profile")
                    + "&access_type=offline"
                    + "&prompt=select_account";

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(authUrl));
            } else {
                callback.onError("Impossible d’ouvrir le navigateur.");
            }

        } catch (Exception e) {
            callback.onError("Erreur Google OAuth : " + e.getMessage());
        }
    }

    private void handleCode(String code, AuthCallback callback) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String form = "code=" + urlEncode(code)
                    + "&client_id=" + urlEncode(clientId)
                    + "&client_secret=" + urlEncode(clientSecret)
                    + "&redirect_uri=" + urlEncode(redirectUri)
                    + "&grant_type=authorization_code";

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            String accessToken = extractJsonValue(tokenResponse.body(), "access_token");

            if (isBlank(accessToken)) {
                callback.onError("Impossible de récupérer le token Google.");
                return;
            }

            HttpRequest profileRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> profileResponse = client.send(profileRequest, HttpResponse.BodyHandlers.ofString());

            String email = extractJsonValue(profileResponse.body(), "email");
            String fullName = extractJsonValue(profileResponse.body(), "name");

            if (isBlank(email)) {
                callback.onError("Impossible de récupérer l'email Google.");
                return;
            }

            email = email.trim().toLowerCase();
            User existingUser = userService.getUserByEmail(email);

            if (existingUser != null) {
                userService.updateLastSeen(existingUser.getId());
                callback.onSuccess(new SocialLoginResult(true, existingUser, email, fullName, "GOOGLE"));
            } else {
                callback.onSuccess(new SocialLoginResult(false, null, email, fullName, "GOOGLE"));
            }

        } catch (Exception e) {
            callback.onError("Erreur Google : " + e.getMessage());
        }
    }

    private void sendHtml(com.sun.net.httpserver.HttpExchange exchange, String message) throws IOException {
        byte[] bytes = ("<html><body><h3>" + message + "</h3></body></html>").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
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

        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);

        if (start == -1) {
            return null;
        }

        start += pattern.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start < json.length() && json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            if (end == -1) {
                return null;
            }
            return json.substring(start, end);
        }

        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }

        return json.substring(start, end).trim();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}