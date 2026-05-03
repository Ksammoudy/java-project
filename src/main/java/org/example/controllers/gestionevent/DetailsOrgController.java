package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.example.models.gestionevent.Evenement;

import java.io.IOException;

public class DetailsOrgController {

    @FXML private Label lblTitre;
    @FXML private Label lblLieu;
    @FXML private Label lblDate;
    @FXML private Label lblOrganisateur;
    @FXML private TextArea txtDescription;

    private Evenement evenement;

    public void setEvenement(Evenement ev) {
        this.evenement = ev;
        if (ev != null) {
            lblTitre.setText(ev.getTitre());
            lblLieu.setText(ev.getLieu());
            lblOrganisateur.setText(ev.getNomOrganisateur());
            if (ev.getDate() != null) {
                lblDate.setText(ev.getDate().toString());
            }
            txtDescription.setText(ev.getDescription());
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            // 1. Chargement de l'interface Home de l'organisateur
            // Thabbet mlih fil path mta3 el FXML mte3ek
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/OrganisateurHome.fxml"));
            Parent root = loader.load();

            // 2. Na5dou el Stage el 7ali
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Nbadlou el Scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Retour réussi vers l'espace Organisateur.");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du retour : " + e.getMessage());
            e.printStackTrace();
        }
    }
}