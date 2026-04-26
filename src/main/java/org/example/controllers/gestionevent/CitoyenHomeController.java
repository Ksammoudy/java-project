package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.URL;

public class CitoyenHomeController {

    @FXML private AnchorPane contentArea;

    @FXML
    public void initialize() {
        // Tawa awel ma t-7el el page, todh-hor el Bienvenue
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

    private void loadPage(String fxml) {
        String path = "/org/example/views/event/" + fxml + ".fxml";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("❌ Erreur: Mal9itsh el fichier hne -> " + path);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            contentArea.getChildren().setAll(root);

            // Resizing automatique
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            System.err.println("❌ Erreur fil load mta3: " + fxml);
            e.printStackTrace();
        }
    }
}