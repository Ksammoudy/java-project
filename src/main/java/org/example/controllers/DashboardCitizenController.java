package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class DashboardCitizenController {

    @FXML
    private Label citizenNameLabel;

    @FXML
    private Label headerEmailLabel;

    @FXML
    private Label declarationsPillLabel;

    @FXML
    private Label approvedPillLabel;

    @FXML
    private Label pointsPillLabel;

    @FXML
    private Label totalDeclarationsLabel;

    @FXML
    private Label validatedLabel;

    @FXML
    private Label pendingLabel;

    @FXML
    private Label earnedPointsLabel;

    @FXML
    private LineChart<String, Number> declarationTrendChart;

    @FXML
    private PieChart statusChart;

    @FXML
    public void initialize() {
        User user = resolveCitizenUser();
        citizenNameLabel.setText(fullName(user));
        headerEmailLabel.setText("Utilisateur Demo");
        declarationsPillLabel.setText("Declarations: 14");
        approvedPillLabel.setText("Validees: 13");
        pointsPillLabel.setText("EcoPoints: 8042");
        totalDeclarationsLabel.setText("14");
        validatedLabel.setText("13");
        pendingLabel.setText("1");
        earnedPointsLabel.setText("8042");
        populateCharts();
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDeclarations() {
        Main.showDeclarationListPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleNews() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleAirQuality() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleWithdraw() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleProfile() {
        Main.showProfileViewPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }

    private void populateCharts() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        series.getData().add(new XYChart.Data<>("Sep 2025", 0));
        series.getData().add(new XYChart.Data<>("Oct 2025", 0));
        series.getData().add(new XYChart.Data<>("Nov 2025", 0));
        series.getData().add(new XYChart.Data<>("Dec 2025", 0));
        series.getData().add(new XYChart.Data<>("Jan 2026", 0));
        series.getData().add(new XYChart.Data<>("Feb 2026", 13));
        declarationTrendChart.getData().setAll(series);

        statusChart.setData(FXCollections.observableArrayList(
            new PieChart.Data("Approuvees", 13),
            new PieChart.Data("En attente", 1),
            new PieChart.Data("Refusees", 0)
        ));
    }

    private User resolveCitizenUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return user;
        }

        User demo = new User();
        demo.setNom("Utilisateur");
        demo.setPrenom("Demo");
        demo.setEmail("demo@wastewise.tn");
        demo.setType("CITOYEN");
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
