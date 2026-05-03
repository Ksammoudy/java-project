package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.models.gestionevent.Participation;
import org.example.services.gestionevent.ParticipationServices;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class AjouterParticipationControllerFront {

    @FXML private TextField nomInput;
    @FXML private TextField emailInput;

    private int eventId;
    private String nomEvenement;

    public void setEventData(int id, String titre) {
        this.eventId = id;
        this.nomEvenement = titre;
        System.out.println("✅ Données reçues: ID=" + id + " | Titre=" + titre);
    }

    @FXML
    void handleValiderParticipation(ActionEvent event) {
        String nom = nomInput.getText().trim();
        String email = emailInput.getText().trim();

        // --- 1. Contrôle de saisie: Champs vides ---
        if (nom.isEmpty() || email.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // --- 2. Contrôle de saisie: Nom (Lettres et espaces uniquement) ---
        // Regex: ^[a-zA-Z\\s]+$ (hrouf kbar w sghar w espace barka)
        if (!nom.matches("^[a-zA-Z\\s]+$")) {
            showAlert("Erreur de saisie", "Le nom ne doit contenir que des lettres et des espaces.");
            return;
        }

        // --- 3. Contrôle de saisie: Email (doit contenir @) ---
        if (!email.contains("@")) {
            showAlert("Erreur de saisie", "L'adresse email doit être valide (contient @).");
            return;
        }

        if (this.eventId == 0) {
            showAlert("Erreur Critique", "ID Événement introuvable.");
            return;
        }

        try {
            Participation p = new Participation();
            p.setDateInscription(new Date(System.currentTimeMillis()));
            p.setIdEvenement(this.eventId);
            p.setIdCitoyen(1);
            p.setNomCitoyen(nom);
            p.setNomEvenement(this.nomEvenement);
            p.setEmail(email);

            ParticipationServices ps = new ParticipationServices();
            ps.create(p);

            System.out.println("✅ Participation validée pour: " + this.nomEvenement);

            // Notification de succès avant de quitter
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("Votre participation a été enregistrée !");
            success.showAndWait();

            handleRetour(event);

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Problème lors de l'enregistrement dans la base.");
            e.printStackTrace();
        }
    }

    // Méthode simple bech t-affichi erreur lel user
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/RoleSelection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur Navigation: " + e.getMessage());
        }
    }
}