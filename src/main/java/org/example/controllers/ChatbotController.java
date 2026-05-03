package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Main;
import org.example.models.ZonePolluee;
import org.example.services.ZonePollueeDAO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatbotController {

    @FXML private VBox chatArea;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button closeButton;
    @FXML private Label chatbotStatus;
    @FXML private ProgressIndicator loadingIndicator;

    private ZonePollueeDAO zoneDAO = new ZonePollueeDAO();
    private List<ZonePolluee> zones;

    private static String API_KEY = "";
    private List<Map<String, String>> conversationHistory = new ArrayList<>();
    private static final int MAX_HISTORY = 10;

    @FXML
    public void initialize() {
        loadApiKey();
        loadZones();
        setupChat();
        addWelcomeMessage();
        updateStatus();
    }

    private void loadApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("❌ config.properties not found");
                API_KEY = "";
                return;
            }
            Properties prop = new Properties();
            prop.load(input);
            API_KEY = prop.getProperty("gemini.api.key");
            if (API_KEY != null && !API_KEY.isEmpty()) {
                String maskedKey = API_KEY.substring(0, Math.min(8, API_KEY.length())) + "...";
                System.out.println("✅ API Key loaded: " + maskedKey);
            } else {
                System.out.println("❌ API Key not found in config.properties");
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading config: " + e.getMessage());
            API_KEY = "";
        }
    }

    private void loadZones() {
        zones = zoneDAO.getAllZones();
        System.out.println("✅ Chargé " + zones.size() + " zones pour l'IA");
    }

    private void setupChat() {
        chatArea.heightProperty().addListener((obs, oldVal, newVal) -> {
            chatScrollPane.setVvalue(1.0);
        });

        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        sendButton.setOnAction(event -> sendMessage());
        closeButton.setOnAction(event -> closeChatbot());

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    private void updateStatus() {
        if (API_KEY != null && !API_KEY.isEmpty()) {
            chatbotStatus.setText("🧠 Gemini AI • " + zones.size() + " zones • Prêt");
            chatbotStatus.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        } else {
            chatbotStatus.setText("⚠️ Clé API manquante • Vérifiez config.properties");
            chatbotStatus.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        }
    }

    private void addWelcomeMessage() {
        String welcome = "🌍 **Bienvenue sur WasteWise Assistant IA!**\n\n" +
                "Je suis un assistant intelligent spécialisé dans les zones polluées.\n\n" +
                "🤖 **Ce que je peux faire:**\n" +
                "• Analyser les zones polluées\n" +
                "• Donner des conseils personnalisés\n" +
                "• Répondre à toutes vos questions\n\n" +
                "**Posez-moi n'importe quelle question!** 💬\n\n" +
                "💡 **Exemples:**\n" +
                "• \"Analyse les statistiques des zones\"\n" +
                "• \"Quelles sont les zones les plus critiques?\"\n" +
                "• \"Donne-moi des conseils santé\"\n" +
                "• \"Quel est le niveau de pollution moyen?\"";

        addBotMessage(welcome);

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> addQuickSuggestions());
        delay.play();
    }

    private void addQuickSuggestions() {
        HBox suggestionsBox = new HBox(10);
        suggestionsBox.setAlignment(Pos.CENTER_LEFT);
        suggestionsBox.setPadding(new Insets(5, 0, 10, 0));

        String[] suggestions = {
                "📊 Analyse complète",
                "🔴 Zones critiques",
                "💡 Conseils santé",
                "📈 Tendances pollution",
                "🌱 Actions recommandées"
        };

        for (String suggestion : suggestions) {
            Button suggestionBtn = new Button(suggestion);
            suggestionBtn.setStyle(
                    "-fx-background-color: #e8f5e9;" +
                            "-fx-text-fill: #2e7d32;" +
                            "-fx-border-color: #4caf50;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 5 15 5 15;" +
                            "-fx-cursor: hand;"
            );
            suggestionBtn.setOnAction(e -> {
                messageInput.setText(suggestion);
                sendMessage();
            });
            suggestionsBox.getChildren().add(suggestionBtn);
        }

        chatArea.getChildren().add(suggestionsBox);
    }

    @FXML
    private void sendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty()) return;

        addUserMessage(userMessage);
        messageInput.clear();

        if (API_KEY == null || API_KEY.isEmpty()) {
            addBotMessage("⚠️ **API non configurée**\n\nVeuillez configurer votre clé API dans `config.properties`");
            return;
        }

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        sendButton.setDisable(true);

        new Thread(() -> {
            String response = callGeminiAPI(userMessage);

            Platform.runLater(() -> {
                addBotMessage(response);
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }
                sendButton.setDisable(false);
            });
        }).start();
    }

    private String callGeminiAPI(String userMessage) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Tu es WasteWise Assistant, un expert IA spécialisé dans les zones polluées en Tunisie.\n");
            prompt.append("Réponds TOUJOURS en français, sois amical, et utilise des émojis.\n");
            prompt.append("Sois concis mais informatif. Donne des conseils pratiques.\n\n");

            prompt.append("Voici les données des zones polluées en Tunisie:\n");
            for (ZonePolluee zone : zones) {
                String riskLevel = zone.getNiveauPollution() >= 7 ? "CRITIQUE ⚠️" :
                        (zone.getNiveauPollution() >= 4 ? "MOYEN 🟡" : "FAIBLE 🟢");
                prompt.append(String.format("- %s: niveau %d/10 (%s)\n",
                        zone.getNomZone(), zone.getNiveauPollution(), riskLevel));
            }

            double avgPollution = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(0);
            long criticalCount = zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
            long mediumCount = zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
            long lowCount = zones.stream().filter(z -> z.getNiveauPollution() < 4).count();

            prompt.append("\nSTATISTIQUES GÉNÉRALES:\n");
            prompt.append(String.format("- Total: %d zones\n", zones.size()));
            prompt.append(String.format("- Niveau moyen: %.1f/10\n", avgPollution));
            prompt.append(String.format("- Zones critiques (≥7): %d\n", criticalCount));
            prompt.append(String.format("- Zones moyennes (4-6): %d\n", mediumCount));
            prompt.append(String.format("- Zones faibles (≤3): %d\n\n", lowCount));

            prompt.append("Question de l'utilisateur: ").append(userMessage);
            prompt.append("\n\nRéponse de l'assistant: ");

            // Try multiple working endpoints
            String[] endpoints = {
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent?key=" + API_KEY,
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY
            };

            String aiResponse = null;
            Exception lastException = null;

            for (String urlString : endpoints) {
                try {
                    System.out.println("Trying endpoint: " + urlString);

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);

                    JsonObject requestBody = new JsonObject();
                    JsonArray contents = new JsonArray();
                    JsonObject content = new JsonObject();
                    JsonArray parts = new JsonArray();
                    JsonObject part = new JsonObject();
                    part.addProperty("text", prompt.toString());
                    parts.add(part);
                    content.add("parts", parts);
                    contents.add(content);
                    requestBody.add("contents", contents);

                    String jsonInputString = new Gson().toJson(requestBody);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    System.out.println("Response code: " + responseCode);

                    if (responseCode == 200) {
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            JsonObject jsonResponse = new Gson().fromJson(response.toString(), JsonObject.class);

                            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                                aiResponse = jsonResponse.getAsJsonArray("candidates")
                                        .get(0).getAsJsonObject()
                                        .getAsJsonObject("content")
                                        .getAsJsonArray("parts")
                                        .get(0).getAsJsonObject()
                                        .get("text").getAsString();

                                // Save to conversation history
                                Map<String, String> userMsg = new HashMap<>();
                                userMsg.put("role", "Utilisateur");
                                userMsg.put("content", userMessage);
                                conversationHistory.add(userMsg);

                                Map<String, String> aiMsg = new HashMap<>();
                                aiMsg.put("role", "Assistant");
                                aiMsg.put("content", aiResponse);
                                conversationHistory.add(aiMsg);

                                while (conversationHistory.size() > MAX_HISTORY) {
                                    conversationHistory.remove(0);
                                }

                                return aiResponse;
                            }
                        }
                    }
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("Failed with endpoint: " + e.getMessage());
                }
            }

            // If all endpoints fail, use intelligent fallback
            return generateSmartResponse(userMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return generateSmartResponse(userMessage);
        }
    }

    private String generateSmartResponse(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();

        // Calculate real-time statistics from your database
        double avgPollution = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(0);
        long criticalCount = zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        long mediumCount = zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
        long lowCount = zones.stream().filter(z -> z.getNiveauPollution() < 4).count();

        // Find most critical zone
        ZonePolluee worstZone = zones.stream()
                .max((z1, z2) -> Integer.compare(z1.getNiveauPollution(), z2.getNiveauPollution()))
                .orElse(null);

        if (lowerMsg.contains("analyse") || lowerMsg.contains("statistique") || lowerMsg.contains("complète")) {
            return String.format(
                    "📊 **ANALYSE COMPLÈTE DES ZONES POLLUÉES**\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📌 **Vue d'ensemble:**\n" +
                            "• Total des zones: %d\n" +
                            "• Niveau moyen: %.1f/10\n\n" +
                            "⚠️ **Répartition par niveau:**\n" +
                            "• 🔴 Critique (≥7/10): %d zone(s)\n" +
                            "• 🟡 Moyen (4-6/10): %d zone(s)\n" +
                            "• 🟢 Faible (≤3/10): %d zone(s)\n\n" +
                            "🎯 **Zone la plus critique:**\n" +
                            "• %s (Niveau %d/10)\n\n" +
                            "💡 **Recommandation prioritaire:**\n" +
                            "Concentrez vos efforts sur les zones critiques pour maximiser l'impact écologique.",
                    zones.size(), avgPollution, criticalCount, mediumCount, lowCount,
                    worstZone != null ? worstZone.getNomZone() : "Aucune",
                    worstZone != null ? worstZone.getNiveauPollution() : 0
            );
        }

        if (lowerMsg.contains("critique") || lowerMsg.contains("urgent") || lowerMsg.contains("grave")) {
            List<ZonePolluee> criticalZones = zones.stream()
                    .filter(z -> z.getNiveauPollution() >= 7)
                    .toList();

            if (criticalZones.isEmpty()) {
                return "✅ **BRAVO!** Aucune zone critique détectée!\n\nContinuez vos bonnes pratiques écologiques pour maintenir cette tendance positive. 🌍";
            }

            StringBuilder response = new StringBuilder();
            response.append("🔴 **ZONES CRITIQUES DÉTECTÉES** 🔴\n\n");
            response.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            for (ZonePolluee zone : criticalZones) {
                response.append(String.format("⚠️ **%s**\n• Niveau: %d/10\n",
                        zone.getNomZone(), zone.getNiveauPollution()));
                response.append("• Action requise: IMMÉDIATE\n\n");
            }

            response.append("🚨 **PLAN D'ACTION URGENT:**\n");
            response.append("1. Augmenter la fréquence des collectes\n");
            response.append("2. Installer des capteurs supplémentaires\n");
            response.append("3. Sensibiliser les habitants via SMS\n");
            response.append("4. Déployer des équipes d'intervention rapide\n");

            return response.toString();
        }

        if (lowerMsg.contains("santé") || lowerMsg.contains("conseil") || lowerMsg.contains("protéger")) {
            return "💚 **CONSEILS SANTÉ - Zones Polluées** 💚\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "😷 **Protection individuelle:**\n" +
                    "• Portez un masque FFP2 dans les zones critiques\n" +
                    "• Lavez-vous les mains régulièrement\n" +
                    "• Évitez les activités extérieures prolongées\n\n" +
                    "🏠 **À la maison:**\n" +
                    "• Plantes dépolluantes (spathiphyllum, sansevieria, ficus)\n" +
                    "• Purificateur d'air recommandé\n" +
                    "• Fermez les fenêtres aux heures de pointe\n\n" +
                    "🥗 **Alimentation:**\n" +
                    "• Augmentez votre consommation d'antioxydants\n" +
                    "• Buvez beaucoup d'eau (2L/jour minimum)\n" +
                    "• Privilégiez les aliments riches en vitamine C\n\n" +
                    "🩺 **Consultation médicale:**\n" +
                    "En cas de toux persistante ou difficultés respiratoires, consultez immédiatement un médecin.";
        }

        if (lowerMsg.contains("action") || lowerMsg.contains("recommandé") || lowerMsg.contains("faire")) {
            return "🌱 **ACTIONS RECOMMANDÉES** 🌱\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    "👥 **Pour les citoyens:**\n" +
                    "• Signaler les zones polluées via l'application\n" +
                    "• Participer aux journées de nettoyage citoyen\n" +
                    "• Utiliser les poubelles de tri sélectif\n" +
                    "• Réduire l'utilisation des véhicules personnels\n\n" +
                    "🏛️ **Pour les autorités:**\n" +
                    "• Augmenter les contrôles industriels\n" +
                    "• Installer plus de points de collecte\n" +
                    "• Organiser des campagnes de sensibilisation\n" +
                    "• Mettre en place des sanctions pour les pollueurs\n\n" +
                    "♻️ **Pour les valorisateurs:**\n" +
                    "• Optimiser les tournées de collecte\n" +
                    "• Former à meilleur tri des déchets\n" +
                    "• Promouvoir l'économie circulaire";
        }

        if (lowerMsg.contains("tendance") || lowerMsg.contains("évolution") || lowerMsg.contains("changement")) {
            return "📈 **TENDANCES & ÉVOLUTIONS** 📈\n\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                    String.format("📊 **Situation actuelle:**\n• Niveau moyen: %.1f/10\n• Zones critiques: %d\n\n",
                            avgPollution, criticalCount) +
                    "🔄 **Projections 2025:**\n" +
                    "• Amélioration prévue de 15% avec les nouvelles initiatives\n" +
                    "• Réduction ciblée dans les zones industrielles\n" +
                    "• Objectif: Passer sous la barre des 5/10 en moyenne\n\n" +
                    "✅ **Signes positifs:**\n" +
                    "• Augmentation des signalements citoyens\n" +
                    "• Meilleure réactivité des autorités\n" +
                    "• Prise de conscience collective grandissante\n\n" +
                    "📅 **Prochain rapport détaillé:** Consultation mensuelle disponible.";
        }

        // Default response for any other question
        return "💬 **Comment puis-je vous aider avec WasteWise?**\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "🤖 **Je suis spécialisé dans les zones polluées. Voici ce que je peux faire:**\n\n" +
                "📊 **Analyses:**\n" +
                "• \"Analyse complète des zones\"\n" +
                "• \"Statistiques détaillées\"\n" +
                "• \"Quelles sont les tendances?\"\n\n" +
                "⚠️ **Alertes:**\n" +
                "• \"Zones critiques\"\n" +
                "• \"Niveaux dangereux\"\n" +
                "• \"Urgence environnementale\"\n\n" +
                "💚 **Conseils:**\n" +
                "• \"Conseils santé\"\n" +
                "• \"Comment me protéger?\"\n" +
                "• \"Actions recommandées\"\n\n" +
                "🌍 Ensemble, faisons la différence pour l'environnement! 🌍";
    }
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.TOP_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContent = new VBox();
        messageContent.setStyle("-fx-background-color: #4caf50; -fx-background-radius: 15 0 15 15; -fx-padding: 10 15 10 15; -fx-max-width: 400px;");

        Label userLabel = new Label("Vous");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-opacity: 0.8;");

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: white; -fx-font-size: 13px;");
        messageText.wrappingWidthProperty().bind(messageContent.widthProperty().subtract(30));

        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-opacity: 0.6;");

        messageContent.getChildren().addAll(userLabel, messageText, timeLabel);
        messageBox.getChildren().add(messageContent);

        Platform.runLater(() -> chatArea.getChildren().add(messageBox));
    }

    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.TOP_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContent = new VBox();
        messageContent.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 0 15 15 15; -fx-padding: 10 15 10 15; -fx-max-width: 450px;");

        Label botLabel = new Label("🤖 WasteWise AI");
        botLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 11px; -fx-font-weight: bold;");

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: #333333; -fx-font-size: 13px;");
        messageText.wrappingWidthProperty().bind(messageContent.widthProperty().subtract(30));

        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 10px;");

        messageContent.getChildren().addAll(botLabel, messageText, timeLabel);
        messageBox.getChildren().add(messageContent);

        Platform.runLater(() -> chatArea.getChildren().add(messageBox));
    }

    private void closeChatbot() {
        addBotMessage("👋 Merci d'avoir utilisé WasteWise AI! À bientôt! 🌍");
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });
        delay.play();
    }

    @FXML
    private void minimizeChatbot() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}