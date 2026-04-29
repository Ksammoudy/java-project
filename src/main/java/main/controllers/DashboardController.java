package main.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;

public class DashboardController {

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.DASHBOARD, AppRoutes.TITLE_DASHBOARD);
    }

    @FXML
    private void onOpenAppelOffre(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.APPEL_OFFRE_CREATE, "WasteWise - Creer un appel d'offre");
    }

    @FXML
    private void onOpenReponseOffre(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.REPONSE_OFFRE_CREATE, "WasteWise - Creer une reponse d'offre");
    }

    @FXML
    private void onOpenBackOffice(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_DASHBOARD, AppRoutes.TITLE_ADMIN_DASHBOARD);
    }
}
