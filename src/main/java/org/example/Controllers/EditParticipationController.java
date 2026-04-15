package org.example.Controllers;

import org.example.entities.Participation;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.sql.Date;

public class EditParticipationController {

    @FXML private TextField txtEvent;
    @FXML private TextField txtCitoyen;
    @FXML private DatePicker datePicker;

    private Participation participation;

    // 🔥 main controller reference (for refresh)
    private ParticipationController mainController;

    public void setMainController(ParticipationController controller) {
        this.mainController = controller;
    }

    // 🔥 receive selected object
    public void setParticipation(Participation p) {
        this.participation = p;

        txtEvent.setText(String.valueOf(p.getIdEvenement()));
        txtCitoyen.setText(String.valueOf(p.getIdCitoyen()));

        if (p.getDateInscription() != null) {
            datePicker.setValue(p.getDateInscription().toLocalDate());
        }
    }

    // 💾 SAVE
    @FXML
    private void saveParticipation() {

        if (participation == null) return;

        participation.setIdEvenement(Integer.parseInt(txtEvent.getText()));
        participation.setIdCitoyen(Integer.parseInt(txtCitoyen.getText()));

        if (datePicker.getValue() != null) {
            participation.setDateInscription(
                    Date.valueOf(datePicker.getValue())
            );
        }

        System.out.println("Participation updated ✔");

        // 🔥 REFRESH TABLE
        if (mainController != null) {
            mainController.refreshTable();
        }

        // close window
        txtEvent.getScene().getWindow().hide();
    }
}