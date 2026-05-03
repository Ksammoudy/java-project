package main.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;

public class DashboardController {

    private static final String CHATBASE_URL = "https://www.chatbase.co/chatbot-iframe/WAO818oBk6Ity1yhCsPT8";

    @FXML
    private VBox chatbaseWindow;
    @FXML
    private WebView chatbaseWebView;

    private boolean chatbaseLoaded;

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

    @FXML
    private void onOpenAssistant() {
        if (!chatbaseLoaded) {
            chatbaseWebView.getEngine().load(CHATBASE_URL);
            chatbaseLoaded = true;
        }
        chatbaseWindow.setManaged(true);
        chatbaseWindow.setVisible(true);
    }

    @FXML
    private void onCloseAssistant() {
        chatbaseWindow.setVisible(false);
        chatbaseWindow.setManaged(false);
    }
}
