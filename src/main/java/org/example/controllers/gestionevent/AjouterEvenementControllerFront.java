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
    @FXML private TextField txtLieu; // fx:id="txtLieu"
    @FXML private DatePicker datePicker;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtOrganisateur; // fx:id="txtOrganisateur"

    private final EvenementServices service = new EvenementServices();

    @FXML

    private void handleSave() {
        if (txtTitre == null || txtLieu == null || datePicker == null || txtDescription == null || txtOrganisateur == null) {
            showAlert("Erreur", "Liaison FXML échouée. Vérifiez les fx:id.", Alert.AlertType.ERROR);
            return;
        }

        try {
            String titre = txtTitre.getText().trim();
            String desc = txtDescription.getText().trim();
            String lieu = txtLieu.getText().trim();
            String org = txtOrganisateur.getText().trim();
            LocalDate localDate = datePicker.getValue();

            // 1. Verification des champs vides
            if (titre.isEmpty() || lieu.isEmpty() || localDate == null || org.isEmpty() || desc.isEmpty()) {
                showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.WARNING);
                return;
            }

            // 2. Contrôle sur le Titre : Ken des lettres et espaces
            // Regex: ^[a-zA-Z\s]+$ ya3ni mel bideya lel nihaya ken حروف w espace
            if (!titre.matches("^[a-zA-Z\\s]+$")) {
                showAlert("Format Invalide", "Le titre ne doit contenir que des lettres et des espaces.", Alert.AlertType.WARNING);
                return;
            }

            // 3. Contrôle sur la Description : Minimum 20 caractères
            if (desc.length() < 20) {
                showAlert("Description trop courte", "La description doit contenir au moins 20 caractères.", Alert.AlertType.WARNING);
                return;
            }

            // 4. Contrôle sur la Date : Maynajmch ykoun a9al min date lyouma
            if (localDate.isBefore(LocalDate.now())) {
                showAlert("Date Invalide", "La date de l'événement ne peut pas être antérieure à la date d'aujourd'hui.", Alert.AlertType.WARNING);
                return;
            }

            // 5. Contrôle sur l'Organisateur (Optionnel ama ensa7ek bih)
            if (!org.matches("^[a-zA-Z\\s]+$")) {
                showAlert("Format Invalide", "Le nom de l'organisateur ne doit contenir que des lettres.", Alert.AlertType.WARNING);
                return;
            }

            // Si toutes les conditions sont respectées, on continue
            Date sqlDate = Date.valueOf(localDate);
            Evenement ev = new Evenement(titre, desc, lieu, sqlDate, org);

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