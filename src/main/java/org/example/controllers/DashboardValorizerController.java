package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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
    public void initialize() {
        User user = resolveValorizerUser();
        String name = fullName(user);
        valorizerNameLabel.setText(name);
        valorizerHeaderNameLabel.setText(name);
        populateChart();
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleReceivedWaste() {
        Main.showDeclarationListPage();
    }

    @FXML
    public void handleValorisation() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleStats() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleProfile() {
        Main.showProfileViewPage();
    }

    @FXML
    public void handleEditProfile() {
        Main.showProfileEditPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }

    private void populateChart() {
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
