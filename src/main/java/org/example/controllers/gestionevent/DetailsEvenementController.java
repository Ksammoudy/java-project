package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.example.models.gestionevent.Evenement; // Yelzem hedha bech ya3raf Evenement
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.models.gestionevent.Evenement;

public class DetailsEvenementController {
    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblDate;
    @FXML private Label lblOrganisateur;

    // Hedhi el method elli n3aytouلها mel AfficherController
    public void setEvenement(Evenement ev) {
        if (ev != null) {
            lblTitre.setText(ev.getTitre());
            lblDescription.setText(ev.getDescription());
            lblDate.setText(ev.getDate().toString());
            lblOrganisateur.setText(ev.getNomOrganisateur());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/AfficherEvenement.fxml"));
            Parent root = loader.load();
            // Yelzem labels mte3ek ikounou declared bech el lookup tekhdem
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) lblTitre.getScene().lookup("#contentArea");
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
