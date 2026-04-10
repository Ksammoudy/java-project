package org.example.services;

import com.sun.net.httpserver.HttpServer;
import org.example.models.User;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FacebookAuthService {

    private final UserService userService = UserService.getInstance();

    private final String clientId = System.getenv("OAUTH_FACEBOOK_ID");
    private final String clientSecret = System.getenv("OAUTH_FACEBOOK_SECRET");
    private final String redirectUri = System.getenv("OAUTH_FACEBOOK_REDIRECT_URI");

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void loginWithFacebook(AuthCallback callback) {
        if (isBlank(clientId) || isBlank(clientSecret) || isBlank(redirectUri)) {
            callback.onError("Variables Facebook OAuth manquantes.");
            return;
        }

        try {
            URI redirect = URI.create(redirectUri);
            int port = redirect.getPort() == -1 ? 80 : redirect.getPort();
            String path = redirect.getPath();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext(path, exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = extractQueryParam(query, "code");
                String error = extractQueryParam(query, "error");

                String responseText;

                if (!isBlank(error)) {
                    responseText = "Connexion Facebook annulée.";
                    sendHtml(exchange, responseText);
                    server.stop(0);
                    callback.onError("Connexion Facebook annulée.");
                    return;
                }

                if (isBlank(code)) {
                    responseText = "Code OAuth manquant.";
                    sendHtml(exchange, responseText);
                    server.stop(0);
                    callback.onError("Code Facebook manquant.");
                    return;
                }

                responseText = "Connexion réussie. Vous pouvez fermer cette fenêtre.";
                sendHtml(exchange, responseText);
                server.stop(0);

                CompletableFuture.runAsync(() -> handleCode(code, callback));
            });

            server.start();

            String authUrl = "https://www.facebook.com/v19.0/dialog/oauth"
                    + "?client_id=" + urlEncode(clientId)
                    + "&redirect_uri=" + urlEncode(redirectUri)
                    + "&scope=" + urlEncode("email,public_profile")
                    + "&response_type=code";

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(authUrl));
            } else {
                callback.onError("Impossible d’ouvrir le navigateur.");
            }

        } catch (Exception e) {
            callback.onError("Erreur Facebook OAuth : " + e.getMessage());
        }
    }

    private void handleCode(String code, AuthCallback callback) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String tokenUrl = "https://graph.facebook.com/v19.0/oauth/access_token"
                    + "?client_id=" + urlEncode(clientId)
                    + "&redirect_uri=" + urlEncode(redirectUri)
                    + "&client_secret=" + urlEncode(clientSecret)
                    + "&code=" + urlEncode(code);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .GET()
                    .build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            String accessToken = extractJsonValue(tokenResponse.body(), "access_token");
            if (isBlank(accessToken)) {
                callback.onError("Impossible de récupérer le token Facebook.");
                return;
            }

            String profileUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + urlEncode(accessToken);

            HttpRequest profileRequest = HttpRequest.newBuilder()
                    .uri(URI.create(profileUrl))
                    .GET()
                    .build();

            HttpResponse<String> profileResponse = client.send(profileRequest, HttpResponse.BodyHandlers.ofString());

            String email = extractJsonValue(profileResponse.body(), "email");
            String name = extractJsonValue(profileResponse.body(), "name");

            if (isBlank(email)) {
                callback.onError("Facebook ne fournit pas l'email.");
                return;
            }

            User user = loginOrCreateFacebookUser(email, name);
            callback.onSuccess(user);

        } catch (Exception e) {
            callback.onError("Erreur Facebook : " + e.getMessage());
        }
    }

    private User loginOrCreateFacebookUser(String email, String fullName) {
        email = email.trim().toLowerCase();

        User user = userService.getUserByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);

            String prenom = "User";
            String nom = "Facebook";

            if (!isBlank(fullName)) {
                String[] parts = fullName.trim().split("\\s+");
                if (parts.length > 0) prenom = parts[0];
                if (parts.length > 1) nom = parts[1];
            }

            user.setPrenom(prenom);
            user.setNom(nom);
            user.setPassword(UUID.randomUUID().toString());
            user.setRoles("[]");
            user.setType("CITIZEN");
            user.setCreatedAt(LocalDateTime.now());
            user.setActive(true);
            user.setVerified(true);
            user.setTwoFactorEnabled(false);
            user.setFaceEmbedding(null);
            user.setFaceUpdatedAt(null);
            user.setLastSeenAt(null);
            user.setGoogleAuthenticatorSecret(null);

            userService.addUser(user);
            return userService.getUserByEmail(email);
        }

        boolean changed = false;

        if (user.getNom() == null || user.getNom().isBlank()) {
            user.setNom("Facebook");
            changed = true;
        }

        if (user.getPrenom() == null || user.getPrenom().isBlank()) {
            user.setPrenom("User");
            changed = true;
        }

        if (changed) {
            userService.updateUser(user);
        }

        return user;
    }

    private void sendHtml(com.sun.net.httpserver.HttpExchange exchange, String message) throws IOException {
        byte[] bytes = ("<html><body><h3>" + message + "</h3></body></html>").getBytes();
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || query.isBlank()) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}