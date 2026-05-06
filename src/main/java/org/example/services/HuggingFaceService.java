package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HuggingFaceService {
    private static final String ENDPOINT = "https://router.huggingface.co/hf-inference/models/google/vit-base-patch16-224";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public record Result(boolean success, String label, Double score, String error) {
    }

    public Result classifyImage(Path imagePath) {
        String token = ApiConfig.huggingFaceApiKey();
        if (token.isBlank()) {
            return new Result(false, null, null, "Token Hugging Face manquant.");
        }
        try {
            byte[] data = Files.readAllBytes(imagePath);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/octet-stream")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            JsonObject errorObj = tryParseObject(response.body());
            if (code == 401) {
                return new Result(false, null, null, "401 Unauthorized: token Hugging Face invalide.");
            }
            if (code == 429) {
                return new Result(false, null, null, "429 Rate limit: quota Hugging Face depasse.");
            }
            if (code == 503) {
                String msg = errorObj != null && errorObj.has("error") ? errorObj.get("error").getAsString() : "Model loading";
                return new Result(false, null, null, "503 Model loading: " + msg);
            }
            if (code >= 500) {
                String msg = errorObj != null && errorObj.has("error") ? errorObj.get("error").getAsString() : "Erreur serveur Hugging Face.";
                return new Result(false, null, null, code + " Server error: " + msg);
            }

            JsonArray predictions = JsonParser.parseString(response.body()).getAsJsonArray();
            if (predictions.isEmpty() || !predictions.get(0).isJsonObject()) {
                return new Result(false, null, null, "Aucune prediction exploitable dans la reponse.");
            }
            JsonObject top = predictions.get(0).getAsJsonObject();
            String label = top.has("label") && !top.get("label").isJsonNull() ? top.get("label").getAsString() : null;
            Double score = top.has("score") && !top.get("score").isJsonNull() ? top.get("score").getAsDouble() : null;
            if (label == null || score == null) {
                return new Result(false, null, null, "Prediction incomplete: label ou score manquant.");
            }
            return new Result(true, label, score, null);
        } catch (IOException e) {
            return new Result(false, null, null, "Impossible de lire le fichier image.");
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "Erreur Hugging Face." : e.getMessage();
            if (msg.toLowerCase().contains("timed out") || msg.toLowerCase().contains("timeout")) {
                msg = "Timeout pendant l appel Hugging Face.";
            } else {
                msg = "Erreur reseau Hugging Face: " + msg;
            }
            return new Result(false, null, null, msg);
        }
    }

    private JsonObject tryParseObject(String body) {
        try {
            if (body == null || body.isBlank()) {
                return null;
            }
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}

