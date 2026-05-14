package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class DashboardValorizerController {

    @FXML
    private Label valorizerNameLabel;

    @FXML
    private Label valorizerHeaderNameLabel;

    @FXML
    private BarChart<String, Number> valorisationChart;

    @FXML
    private Button navHome;
    @FXML
    private Button navWasteReceived;
    @FXML
    private Button navValorization;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navSettings;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        String name = fullName(user);
        if (valorizerNameLabel != null) {
            valorizerNameLabel.setText(name);
        }
        if (valorizerHeaderNameLabel != null) {
            valorizerHeaderNameLabel.setText(name);
        }
        populateChart();
        updateNavigation("home");
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleReceivedWaste() {
        Main.showValorizerWasteReceivedPage();
    }

    @FXML
    public void handleValorisation() {
        Main.showValorizerValorizationPage();
    }

    @FXML
    public void handleStats() {
        Main.showValorizerStatisticsPage();
    }

    @FXML
    public void handleProfile() {
        Main.showValorizerSettingsPage();
    }

    @FXML
    public void handleEditProfile() {
        Main.showValorizerSettingsPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        Main.showLoginPage();
    }

    /**
     * Marque le bouton courant comme actif dans la navigation.
     * @param page "home", "waste", "valorization", "statistics", "settings"
     */
    private void updateNavigation(String page) {
        // Réinitialise tous les boutons en "muted"
        if (navHome != null) navHome.getStyleClass().remove("active");
        if (navWasteReceived != null) navWasteReceived.getStyleClass().remove("active");
        if (navValorization != null) navValorization.getStyleClass().remove("active");
        if (navStatistics != null) navStatistics.getStyleClass().remove("active");
        if (navSettings != null) navSettings.getStyleClass().remove("active");

        // Active le bouton courant
        switch (page) {
            case "home":
                if (navHome != null) navHome.getStyleClass().add("active");
                break;
            case "waste":
                if (navWasteReceived != null) navWasteReceived.getStyleClass().add("active");
                break;
            case "valorization":
                if (navValorization != null) navValorization.getStyleClass().add("active");
                break;
            case "statistics":
                if (navStatistics != null) navStatistics.getStyleClass().add("active");
                break;
            case "settings":
                if (navSettings != null) navSettings.getStyleClass().add("active");
                break;
        }
    }

    private void populateChart() {
        if (valorisationChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Valorisation");
        series.getData().add(new XYChart.Data<>("Declaration #3", 400));
        series.getData().add(new XYChart.Data<>("Declaration #4", 400));
        series.getData().add(new XYChart.Data<>("Declaration #5", 400));
        series.getData().add(new XYChart.Data<>("Declaration #6", 200));
        series.getData().add(new XYChart.Data<>("Declaration #7", 200));
        series.getData().add(new XYChart.Data<>("Declaration #8", 200));
        valorisationChart.getData().setAll(series);
    }

    private User resolveValorizerUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return user;
        }

        User demo = new User();
        demo.setNom("Utilisateur");
        demo.setPrenom("Demo");
        demo.setEmail("demo@wastewise.tn");
        demo.setType("VALORISATEUR");
        SessionManager.setCurrentUser(demo);
        return demo;
    }

    private String fullName(User user) {
        String prenom = user.getPrenom() == null ? "" : user.getPrenom();
        String nom = user.getNom() == null ? "" : user.getNom();
        String combined = (prenom + " " + nom).trim();
        return combined.isEmpty() ? "Utilisateur Demo" : combined;
    }
}
