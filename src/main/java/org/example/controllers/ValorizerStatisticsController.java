package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;

import java.sql.SQLException;
import java.util.Locale;

public class ValorizerStatisticsController {

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();

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
    private Label valorizerNameLabel;
    @FXML
    private Label headerEmailLabel;

    @FXML
    private Label totalReceivedLabel;
    @FXML
    private Label totalValorizedLabel;
    @FXML
    private Label totalPointsLabel;
    @FXML
    private Label pendingLabel;

    @FXML
    private BarChart<String, Number> estadisticsChart;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");

        updateNavigation("statistics");

        loadStatistics();
        populateChart();
    }

    private void loadStatistics() {
        try {
            var all = declarationService.findAll();
            var valorized = all.stream().filter(d -> isValorizedStatus(d.getStatut())).toList();
            var pending = all.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).toList();

            totalReceivedLabel.setText(String.valueOf(all.size()));
            totalValorizedLabel.setText(String.valueOf(valorized.size()));
            pendingLabel.setText(String.valueOf(pending.size()));

            int totalPoints = valorized.stream().mapToInt(d -> d.getPointsAttribues() != null ? d.getPointsAttribues() : 0).sum();
            totalPointsLabel.setText(String.valueOf(totalPoints));
        } catch (SQLException e) {
            System.err.println("Erreur au chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValorizedStatus(String status) {
        String normalized = normalizeStatus(status);
        return "APPROUVEE".equals(normalized) || "VALIDATED".equals(normalized);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void populateChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        series.getData().add(new XYChart.Data<>("Janvier", 45));
        series.getData().add(new XYChart.Data<>("Fevrier", 62));
        series.getData().add(new XYChart.Data<>("Mars", 58));
        series.getData().add(new XYChart.Data<>("Avril", 71));
        series.getData().add(new XYChart.Data<>("Mai", 89));
        estadisticsChart.getData().setAll(series);
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleWasteReceived() {
        Main.showValorizerWasteReceivedPage();
    }

    @FXML
    public void handleValorization() {
        Main.showValorizerValorizationPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showValorizerStatisticsPage();
    }

    @FXML
    public void handleSettings() {
        Main.showValorizerSettingsPage();
    }

    private void updateNavigation(String page) {
        if (navHome != null) navHome.getStyleClass().remove("active");
        if (navWasteReceived != null) navWasteReceived.getStyleClass().remove("active");
        if (navValorization != null) navValorization.getStyleClass().remove("active");
        if (navStatistics != null) navStatistics.getStyleClass().remove("active");
        if (navSettings != null) navSettings.getStyleClass().remove("active");

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
            default:
                break;
        }
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
