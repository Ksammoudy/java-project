package main.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import main.navigation.ViewNavigator;

public class DashboardController {

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
    }

    @FXML
    private void onOpenAppelOffre(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreCreate.fxml", "WasteWise - Creer un appel d'offre");
    }

    @FXML
    private void onOpenReponseOffre(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreCreate.fxml", "WasteWise - Creer une reponse d'offre");
    }
}
