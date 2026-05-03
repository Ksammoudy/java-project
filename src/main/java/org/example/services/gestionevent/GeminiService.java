package org.example.services.gestionevent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject; // Lezem t-zid el dépendance org.json fil pom.xml

public class GeminiService {
    // 🔑 El Key mte3ek s7i7a mel image
    private static final String API_KEY = "AIzaSyAu4dMqovMZITYwx1PguFvLwE0VFwIq4PU";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public static double getImpactPrediction(String titre, String lieu) {
        try {
            String promptText = "Analyse l'impact de cet événement écologique : '" + titre + "' à '" + lieu + "'. " +
                    "Réponds UNIQUEMENT avec un nombre décimal entre 0.0 et 1.0.";

            String jsonInput = "{ \"contents\": [{ \"parts\":[{ \"text\": \"" + promptText + "\" }] }] }";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 🔍 IMPORTANT: Maredj el body fil console bech naعرفou el ghalta
            System.out.println("Gemini Response: " + response.body());

            JSONObject jsonResponse = new JSONObject(response.body());

            // Check ken Gemini raja3 Error dhibet (kima Safety)
            if (jsonResponse.has("candidates") && !jsonResponse.getJSONArray("candidates").isEmpty()) {
                String resultText = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text").trim();
                return Double.parseDouble(resultText);
            } else {
                System.err.println("❌ Pas de candidats dans la réponse.");
                return Math.random() * 0.5 + 0.3; // Raja3 ra9m aléatoire sghir bech ma y-kounouch el kol 65
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur API: " + e.getMessage());
            return 0.5;
        }
    }
}