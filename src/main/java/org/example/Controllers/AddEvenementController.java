package org.example.Controllers;

import org.example.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Date;

public class AddEvenementController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtDescription;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;

    private EvenementControllers mainController;

    public void setMainController(EvenementControllers controller) {
        this.mainController = controller;
    }

    @FXML
    private void addEvent() {

        // 🔴 controle de saisie
        if (txtTitre.getText().isEmpty() ||
                txtDescription.getText().isEmpty() ||
                txtLieu.getText().isEmpty() ||
                datePicker.getValue() == null) {

            showError("Tous les champs sont obligatoires !");
            return;
        }

        if (txtTitre.getText().length() < 3) {
            showError("Titre invalide !");
            return;
        }

        // ✅ create event
        Evenement ev = new Evenement(
                0, // id auto
                txtTitre.getText(),
                txtDescription.getText(),
                txtLieu.getText(),
                Date.valueOf(datePicker.getValue()),
                1
        );

        // 🔥 add to table
        if (mainController != null) {
            mainController.addEvent(ev);
        }

        System.out.println("EVENT ADDED ✔");

        // close window
        txtTitre.getScene().getWindow().hide();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}