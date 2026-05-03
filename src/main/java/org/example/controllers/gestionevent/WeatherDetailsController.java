package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class WeatherDetailsController {

    @FXML private Label lblCity, lblTemp, lblDescription, lblHumidity, lblWind, lblPressure, lblFeelsLike;

    @FXML
    public void initialize() {
        // Hne dhibet t-najem d-3ayyet lel API mte3ek bech d-maredj el data el s7i7a
        // Lil tawa, hani khallithom kima fil image elli b3at-ha
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        // Hedhi d-sakker el pop-up w d-rajja3 el user
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}