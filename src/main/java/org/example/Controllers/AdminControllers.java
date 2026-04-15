package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;


    public class AdminControllers {

    @FXML
    private AnchorPane contentArea;

    @FXML
    public void initialize() {

        loadPage("accueil.fxml");
    }

    private void loadPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/" + fxml)
            );
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showEvenement() {
        loadPage("evenement.fxml");
    }

    @FXML
    public void showParticipation() {
        loadPage("Participation.fxml");
    }
}