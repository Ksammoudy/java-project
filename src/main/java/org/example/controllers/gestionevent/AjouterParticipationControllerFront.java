package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    // Hedhom lezem yabdaw m-khabyin (Private)
    private int eventId;
    private String nomEvenement;

    // --- 1. Passation des données ---
    public void setEventData(int id, String titre) {
        this.eventId = id;
        this.nomEvenement = titre;
        System.out.println("✅ Données reçues fil Formulaire: ID=" + id + " | Titre=" + titre);
    }

    @FXML
    void handleValiderParticipation(ActionEvent event) {
        String nom = nomInput.getText();
        String email = emailInput.getText();

        // --- 2. Vérification de sécurité ---
        if (nom.isEmpty() || email.isEmpty()) {
            System.err.println("❌ Erreur: Formulaire vide !");
            return;
        }

        if (this.eventId == 0) {
            System.err.println("❌ Erreur Critique: ID Evenement est 0! Verifier le passage de données.");
            return;
        }

        try {
            // --- 3. Construction de l'objet Participation ---
            Participation p = new Participation();
            p.setDateInscription(new Date(System.currentTimeMillis()));
            p.setIdEvenement(this.eventId);
            p.setIdCitoyen(1); // Simulation d'un citoyen connecté (ID=1)
            p.setNomCitoyen(nom);
            p.setNomEvenement(this.nomEvenement);
            p.setEmail(email);

            // --- 4. Appel au Service ---
            ParticipationServices ps = new ParticipationServices();
            ps.create(p);

            System.out.println("✅ Participation validée pour l'événement: " + this.nomEvenement);

            // --- 5. Retour automatique vers l'accueil ou la liste après succès ---
            handleRetour(event);

        } catch (SQLException e) {
            // Hedhi dhibet t-warrik ken l-ID mahouch mawjoud fil base (Foreign Key Error)
            System.err.println("❌ Erreur SQL: Verifiez que l'ID " + this.eventId + " existe dans la table 'evenement'.");
            e.printStackTrace();
        }
    }

    @FXML
    void handleRetour(ActionEvent event) {
        try {
            // Path salla7tou bech yarja3 lel page mta3 l-evenements (badlou ken t7eb path ekher)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/RoleSelection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("✅ Retour à l'accueil réussi !");
        } catch (IOException e) {
            System.err.println("❌ Erreur Navigation: " + e.getMessage());
        }
    }
}