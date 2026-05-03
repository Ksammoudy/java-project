package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class CitoyenHomeController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
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

    // ✅ Méthode Déconnexion
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.setContentText("Vous allez revenir à la page de sélection du rôle.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Thabbet mel path mta3 el ChooseRole mte3ek
                Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/event/RoleSelection.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                System.err.println("❌ Erreur redirection ChooseRole: " + e.getMessage());
            }
        }
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
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page: " + fxml);
            e.printStackTrace();
        }
    }
}