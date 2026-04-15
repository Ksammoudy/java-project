package org.example.Controllers;

import org.example.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Date;

public class EditEvenementController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtDescription;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;

    private Evenement currentEvent;

    // 🔥 main controller
    private EvenementControllers mainController;

    public void setMainController(EvenementControllers controller) {
        this.mainController = controller;
    }

    // 🔥 set event
    public void setEvent(Evenement ev) {
        this.currentEvent = ev;

        txtTitre.setText(ev.getTitre());
        txtDescription.setText(ev.getDescription());
        txtLieu.setText(ev.getLieu());

        if (ev.getDate() != null) {
            datePicker.setValue(ev.getDate().toLocalDate());
        }
    }

    @FXML
    private void saveUpdate() {

        // 🔴 CONTROLE DE SAISIE

        // 1. champs vides
        if (txtTitre.getText().isEmpty() ||
                txtDescription.getText().isEmpty() ||
                txtLieu.getText().isEmpty() ||
                datePicker.getValue() == null) {

            showError("Tous les champs sont obligatoires !");
            return;
        }

        // 2. longueur titre
        if (txtTitre.getText().length() < 3) {
            showError("Le titre doit contenir au moins 3 caractères !");
            return;
        }

        // 3. date (ex: pas passée)
        if (datePicker.getValue().isBefore(java.time.LocalDate.now())) {
            showError("La date doit être aujourd’hui ou future !");
            return;
        }

        // ✅ UPDATE
        currentEvent.setTitre(txtTitre.getText());
        currentEvent.setDescription(txtDescription.getText());
        currentEvent.setLieu(txtLieu.getText());
        currentEvent.setDate(Date.valueOf(datePicker.getValue()));

        System.out.println("UPDATED ✔");

        // 🔄 refresh table
        if (mainController != null) {
            mainController.refreshTable();
        }

        // ❌ close window
        txtTitre.getScene().getWindow().hide();
    }

    // 🔴 Alert reusable
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}