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

    // Version de test locale
    private final String clientId = "1337612458412063";
    private final String clientSecret = "14f5369e553c329cc2bd761f9bd85c2a";
    private final String redirectUri = "http://localhost:8080/callback";

    public interface AuthCallback {
        void onSuccess(User user);
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
            System.out.println("TOKEN RESPONSE = " + tokenResponse.body());

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
            System.out.println("PROFILE RESPONSE = " + profileResponse.body());

            String email = extractJsonValue(profileResponse.body(), "email");
            String name = extractJsonValue(profileResponse.body(), "name");

            System.out.println("EMAIL DECODED = " + email);
            System.out.println("NAME = " + name);

            if (isBlank(email)) {
                email = "fb_" + UUID.randomUUID() + "@facebook.local";
            }

            User user = loginOrCreateFacebookUser(email, name);

            if (user == null) {
                callback.onError("Impossible de connecter ou créer l’utilisateur Facebook.");
                return;
            }

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
                if (parts.length > 0) {
                    prenom = parts[0];
                }
                if (parts.length > 1) {
                    nom = parts[parts.length - 1];
                }
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

            boolean added = userService.addUser(user);

            if (!added) {
                return null;
            }

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
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], java.nio.charset.StandardCharsets.UTF_8);
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

        String value = json.substring(start, end);
        return decodeUnicodeEscapes(value);
    }

    private String decodeUnicodeEscapes(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c == '\\' && i + 5 < value.length() && value.charAt(i + 1) == 'u') {
                String hex = value.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    result.append((char) code);
                    i += 5;
                } catch (NumberFormatException e) {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}