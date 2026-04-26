package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import org.example.models.gestionevent.Participation;
import org.example.services.gestionevent.ParticipationServices;

import java.sql.SQLException;
import java.util.List;

public class BadgesController {

    @FXML private TextField nomCompletInput;
    @FXML private Label resultLabel;
    @FXML private Circle badgeCircle; // El doura mta3 el badge fil FXML

    private ParticipationServices ps = new ParticipationServices();

    @FXML
    void handleCalculerBadge(ActionEvent event) {
        String nom = nomCompletInput.getText().trim();
        if (nom.isEmpty()) {
            resultLabel.setText("Veuillez entrer votre nom !");
            return;
        }

        try {
            // 1. Njibou el participations l-kol
            List<Participation> allParticipations = ps.read();

            // 2. N7asbou 9adeh men marra el esm hedha mawjoud
            long count = allParticipations.stream()
                    .filter(p -> p.getNomCitoyen().equalsIgnoreCase(nom))
                    .count();

            // 3. Appliquer la logique des couleurs
            updateBadgeUI(count, nom);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBadgeUI(long count, String nom) {
        if (count == 0) {
            resultLabel.setText(nom + ", vous n'avez pas encore de participations.");
            badgeCircle.setStyle("-fx-fill: #cccccc;"); // Gris (mafama chay)
        } else if (count < 5) {
            resultLabel.setText("Badge Bronze: " + count + " Participations");
            badgeCircle.setStyle("-fx-fill: #FF0000;"); // Rouge
        } else if (count >= 5 && count <= 10) {
            resultLabel.setText("Badge Argent: " + count + " Participations");
            badgeCircle.setStyle("-fx-fill: #87CEEB;"); // Bleu Ciel
        } else {
            resultLabel.setText("Badge Or: " + count + " Participations");
            badgeCircle.setStyle("-fx-fill: #FF69B4;"); // Rose
        }
    }
}