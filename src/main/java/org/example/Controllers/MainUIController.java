package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class MainUIController {

    @FXML private AnchorPane contentArea;

    private void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxml));
            contentArea.getChildren().setAll(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openEvents() {
        loadPage("user.fxml");
    }

    @FXML
    private void openParticipation() {
        loadPage("ParticipationView.fxml");
    }

    @FXML
    private void openOrganisateur() {
        loadPage("OrganisateurView.fxml");
    }
}