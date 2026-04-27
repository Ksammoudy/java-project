package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

import java.time.LocalDate;
import java.sql.Date;

public class AjouterEvenementControllerFront {

    @FXML private TextField txtTitre;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtOrganisateur;

    private final EvenementServices service = new EvenementServices();

    @FXML
    private void handleSave() {
        // Verification basic des injections FXML
        if (txtTitre == null || txtLieu == null || datePicker == null || txtDescription == null || txtOrganisateur == null) {
            showAlert("Erreur", "Liaison FXML échouée. Vérifiez les fx:id.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Récupération des données
            String titre = txtTitre.getText();
            String desc = txtDescription.getText();
            String lieu = txtLieu.getText();
            LocalDate localDate = datePicker.getValue();
            String org = txtOrganisateur.getText();

            // Validation des champs vides
            if (titre.isEmpty() || lieu.isEmpty() || localDate == null || org.isEmpty()) {
                showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.WARNING);
                return;
            }

            // Conversion de la date
            Date sqlDate = Date.valueOf(localDate);

            // --- CORRECTION DU CONSTRUCTEUR ---
            // Tartib: 1.Titre, 2.Description, 3.Lieu, 4.Date, 5.Organisateur
            Evenement ev = new Evenement(titre, desc, lieu, sqlDate, org);

            // Appel au service pour l'insertion
            service.create(ev);

            showAlert("Succès", "Votre événement a été publié avec succès !", Alert.AlertType.INFORMATION);
            clearFields();

        } catch (Exception e) {
            showAlert("Erreur", "Problème lors de l'enregistrement : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtTitre.clear();
        txtLieu.clear();
        datePicker.setValue(null);
        txtDescription.clear();
        txtOrganisateur.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}