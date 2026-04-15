package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class DashboardAdminController {

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label adminHeaderNameLabel;

    @FXML
    private Label weatherValueLabel;

    @FXML
    private Label weatherMetaLabel;

    @FXML
    private Label declarationsCountLabel;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private Label typesCountLabel;

    @FXML
    private Label topTypeLabel;

    @FXML
    private BarChart<String, Number> declarationTypeChart;

    @FXML
    private PieChart statusPieChart;

    @FXML
    public void initialize() {
        User user = resolveAdminUser();
        adminNameLabel.setText(fullName(user));
        adminHeaderNameLabel.setText(fullName(user));
        weatherValueLabel.setText("16C");
        weatherMetaLabel.setText("Vent: 2.5 km/h | Code meteo: 0 | Maj: 2026-02-26T09:30");
        declarationsCountLabel.setText("19");
        pendingCountLabel.setText("7");
        typesCountLabel.setText("3");
        topTypeLabel.setText("Plastique (14)");
        populateCharts();
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    public void handleUsers() {
        Main.showAdminUsersPage();
    }

    @FXML
    public void handleDeclarations() {
        Main.showDeclarationListPage();
    }

    @FXML
    public void handleTypeDechets() {
        Main.showTypeDechetWorkshopPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }

    private void populateCharts() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        series.getData().add(new XYChart.Data<>("Plastique", 14));
        series.getData().add(new XYChart.Data<>("Papier / Carton", 3));
        series.getData().add(new XYChart.Data<>("Verre", 2));
        declarationTypeChart.getData().setAll(series);

        statusPieChart.setData(FXCollections.observableArrayList(
            new PieChart.Data("Approuvees", 12),
            new PieChart.Data("En attente", 7),
            new PieChart.Data("Refusees", 0)
        ));
        statusPieChart.setLabelsVisible(true);
    }

    private User resolveAdminUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return user;
        }

        User demo = new User();
        demo.setNom("Utilisateur");
        demo.setPrenom("Demo");
        demo.setEmail("demo@wastewise.tn");
        demo.setType("ADMIN");
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
