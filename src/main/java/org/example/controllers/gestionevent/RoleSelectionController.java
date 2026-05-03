package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;

public class RoleSelectionController {

    @FXML
    private void handleCitoyen(ActionEvent event) {
        // Path mta3 el Citoyen (Front-office)
        loadStage(event, "/org/example/views/event/CitoyenHome.fxml");
    }

    @FXML
    private void handleOrganisateur(ActionEvent event) {
        // Path mta3 el Organisateur (Front-office zeda)
        // Thabbet dhibet elli el fichier esmou OrganisateurHome.fxml w m-7atout fil dossier views/event/
        loadStage(event, "/org/example/views/event/OrganisateurHome.fxml");
    }

    private void loadStage(ActionEvent event, String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("❌ Erreur : Fichier FXML introuvable fil path -> " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // N-badlou el Scene mta3 el Stage el 7ali
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Bechi d-maredj m-rakba m3a el CSS
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Passage vers " + fxmlPath + " réussi.");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement du stage : " + fxmlPath);
            e.printStackTrace();
        }
    }
}