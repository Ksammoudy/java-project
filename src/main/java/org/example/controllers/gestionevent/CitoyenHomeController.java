package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class CitoyenHomeController {

    @FXML
    private StackPane contentArea; // Mrigla m3a el FXML tawa

    @FXML
    public void initialize() {
        // Chargement de la page par défaut
        showBienvenue();
    }

    @FXML
    private void showBienvenue() {
        loadPage("BienvenueCitoyen");
    }

    @FXML
    private void showEvents() {
        loadPage("ActionsEcologiques");
    }

    @FXML
    private void showMyParticipations() {
        loadPage("ParticipationsFront");
    }

    @FXML
    private void showBadges() {
        loadPage("BadgesFront");
    }

    @FXML
    private void showNotifications() {
        loadPage("NotificationFront");
    }

    private void loadPage(String fxml) {
        String path = "/org/example/views/event/" + fxml + ".fxml";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("❌ Erreur: Fichier introuvable -> " + path);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // StackPane ya3mel remplaçement lel contenu wa7dou
            contentArea.getChildren().setAll(root);
            System.out.println("✅ Page " + fxml + " chargée.");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page: " + fxml);
            e.printStackTrace();
        }
    }
}