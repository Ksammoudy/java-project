package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

public class RoleSelectionController {

    @FXML
    private void handleCitoyen(ActionEvent event) {

        loadStage(event, "/org/example/views/event/CitoyenHome.fxml");
    }

    @FXML
    private void handleOrganisateur(ActionEvent event) {

        loadStage(event, "/org/example/views/event/OrganisateurHome.fxml");
    }

    private void loadStage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Fichier FXML non trouvé : " + fxmlPath);
            e.printStackTrace();
        }
    }
}
