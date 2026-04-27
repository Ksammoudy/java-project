package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class OrganisateurHomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Awel page d-maredj hiya el liste mta3 el recherche
        showMyEvents();
    }

    @FXML
    private void showMyEvents() {
        loadPage("AfficherEvenement"); // El page elli fiha el TableView wel Recherche
    }

    @FXML
    private void showAjouter() {
        loadPage("AjouterEvenement"); // Page el formulaire
    }

    @FXML
    private void showStats() {
        loadPage("StatsOrganisateur");
    }

    private void loadPage(String fxml) {
        String path = "/org/example/views/event/" + fxml + ".fxml";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("❌ Fichier introuvable: " + path);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Logic bech d-maredj lel RoleSelection
    }
}