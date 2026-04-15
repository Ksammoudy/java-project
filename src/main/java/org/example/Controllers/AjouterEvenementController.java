package org.example.Controllers;

import org.example.entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;

public class AjouterEvenementController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtDescription;
    @FXML private TextField txtLieu;
    @FXML private DatePicker datePicker;

    private EvenementControllers mainController;

    public void setMainController(EvenementControllers controller) {
        this.mainController = controller;
    }

    @FXML
    private void ajouterEvent() {

        Evenement ev = new Evenement(
                txtTitre.getText(),
                txtDescription.getText(),
                txtLieu.getText(),
                Date.valueOf(datePicker.getValue()),
                1
        );

        mainController.addEvent(ev);

        Stage stage = (Stage) txtTitre.getScene().getWindow();
        stage.close();
    }
}