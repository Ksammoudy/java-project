package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class HuggingFaceService {
    private static final String ENDPOINT = "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224";
    private static final String HUGGINGFACE_API_KEY =
            System.getenv().getOrDefault("HUGGINGFACE_API_KEY", "");

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public record Result(boolean success, String label, Double score, String error) {
    }

    public record DebugResult(
            boolean success,
            int httpStatus,
            String rawResponse,
            String label,
            Double score,
            String message
    ) {
    }

    public Result classifyImage(Path imagePath) {
        DebugResult debug = testImage(imagePath);
        if (!debug.success()) {
            return new Result(false, null, null, debug.message());
        }
        if (debug.label() == null || debug.score() == null) {
            return new Result(false, null, null, "Prediction incomplete (label/score manquant).");
        }
        return new Result(true, debug.label(), debug.score(), null);
    }

    public DebugResult testImage(Path imagePath) {
        if (imagePath == null || !Files.exists(imagePath)) {
            return new DebugResult(false, -1, "", null, null, "Fichier image introuvable.");
        }

        try {
            byte[] data = Files.readAllBytes(imagePath);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + HUGGINGFACE_API_KEY)
                    .header("Content-Type", "application/octet-stream")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            System.out.println("[HF] URL=" + ENDPOINT);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String raw = response.body() == null ? "" : response.body().trim();
            System.out.println("[HF] HTTP status=" + status);
            System.out.println("[HF] Raw response=" + raw);

            String label = null;
            Double score = null;
            boolean success = false;
            String message;

            if (status == 200) {
                JsonElement parsed = tryParseJson(raw);
                if (parsed != null && parsed.isJsonArray()) {
                    JsonArray predictions = parsed.getAsJsonArray();
                    if (!predictions.isEmpty() && predictions.get(0).isJsonObject()) {
                        JsonObject top = predictions.get(0).getAsJsonObject();
                        if (top.has("label") && !top.get("label").isJsonNull()) {
                            label = top.get("label").getAsString();
                        }
                        if (top.has("score") && !top.get("score").isJsonNull()) {
                            score = top.get("score").getAsDouble();
                        }
                    }
                }

                if (label != null && score != null) {
                    success = true;
                    message = "API HuggingFace fonctionne correctement";
                } else {
                    String apiError = extractApiError(raw);
                    if (apiError != null && !apiError.isBlank()) {
                        message = "Erreur IA: " + apiError;
                    } else {
                        message = "Reponse HuggingFace invalide ou non exploitable.";
                    }
                }
                return new DebugResult(success, status, raw, label, score, message);
            }

            message = switch (status) {
                case 401, 403 -> "Token HuggingFace invalide ou non autorise";
                case 404 -> "Endpoint ou modele HuggingFace incorrect";
                case 503 -> "Modele HuggingFace en chargement, reessayez";
                default -> "Service d'analyse indisponible (HTTP " + status + ").";
            };
            String apiError = extractApiError(raw);
            if (apiError != null && !apiError.isBlank()) {
                message = message + " Details: " + apiError;
            }
            return new DebugResult(false, status, raw, null, null, message);
        } catch (IOException e) {
            return new DebugResult(false, -1, "", null, null, "Impossible de lire la photo.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new DebugResult(false, -1, "", null, null, "Appel IA interrompu.");
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Erreur IA inconnue." : e.getMessage();
            return new DebugResult(false, -1, "", null, null, "Erreur HuggingFace: " + message);
        }
    }

    private JsonElement tryParseJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return JsonParser.parseString(body);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractApiError(String body) {
        JsonElement json = tryParseJson(body);
        if (json != null && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("error") && !obj.get("error").isJsonNull()) {
                return obj.get("error").getAsString();
            }
        }
        if (body != null && !body.isBlank() && body.toLowerCase().contains("error")) {
            return body;
        }
        return null;
    }
}
