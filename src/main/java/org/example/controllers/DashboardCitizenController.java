package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.entities.Wallet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;
import org.example.services.WalletJdbcService;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardCitizenController {

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final WalletJdbcService walletService = new WalletJdbcService();

    @FXML
    private Button navHome;
    @FXML
    private Button navDeclare;
    @FXML
    private Button navMyDeclarations;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navNews;
    @FXML
    private Button navAir;
    @FXML
    private Button navWithdraw;
    @FXML
    private Button navSettings;

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
    private javafx.scene.layout.HBox flashMessageBox;
    @FXML
    private Label flashMessageLabel;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        CitizenSidebarHelper.applyActive(navHome,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        loadStatsAndCharts();
        showFlashIfPresent();
    }

    private void loadStatsAndCharts() {
        try {
            Integer cid = CitizenSession.resolveCitizenDatabaseId();
            if (cid == null) {
                setStatLabels(0, 0, 0, 0);
                setPills(0, 0, 0);
                setEmptyCharts();
                return;
            }

            List<DeclarationDechet> list = declarationService.findByCitoyenId(cid);
            long total = list.size();
            long approved = list.stream().filter(d -> "APPROUVEE".equals(normalizeStatus(d.getStatut()))).count();
            long pending = list.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
            int pointsEarned = list.stream()
                    .mapToInt(d -> d.getPointsAttribues() == null ? 0 : d.getPointsAttribues())
                    .sum();

            int walletBalance = walletService.findByUtilisateurId(cid)
                    .map(Wallet::getSoldeActuel)
                    .orElse(pointsEarned);

            setStatLabels(total, approved, pending, walletBalance);
            setPills(total, approved, walletBalance);
            populateCharts(list);
        } catch (SQLException | RuntimeException e) {
            setStatLabels(0, 0, 0, 0);
            setPills(0, 0, 0);
            setEmptyCharts();
        }
    }

    private void setStatLabels(long total, long approved, long pending, int ecoPoints) {
        totalDeclarationsLabel.setText(String.valueOf(total));
        validatedLabel.setText(String.valueOf(approved));
        pendingLabel.setText(String.valueOf(pending));
        earnedPointsLabel.setText(String.valueOf(ecoPoints));
    }

    private void setPills(long total, long approved, int ecoPoints) {
        declarationsPillLabel.setText("Declarations: " + total);
        approvedPillLabel.setText("Validees: " + approved);
        pointsPillLabel.setText("EcoPoints: " + ecoPoints);
    }

    private void setEmptyCharts() {
        declarationTrendChart.getData().clear();
        statusChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Aucune donnee", 1)
        ));
    }

    private void populateCharts(List<DeclarationDechet> list) {
        long approved = list.stream().filter(d -> "APPROUVEE".equals(normalizeStatus(d.getStatut()))).count();
        long pending = list.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
        long refused = list.stream().filter(d -> "REFUSEE".equals(normalizeStatus(d.getStatut()))).count();

        if (approved + pending + refused == 0) {
            statusChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Aucune declaration", 1)
            ));
        } else {
            statusChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Approuvees", approved),
                    new PieChart.Data("En attente", pending),
                    new PieChart.Data("Refusees", refused)
            ));
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        YearMonth now = YearMonth.now();
        Locale fr = Locale.FRENCH;
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, fr) + " " + ym.getYear();
            long count = list.stream()
                    .filter(d -> d.getCreatedAt() != null && YearMonth.from(d.getCreatedAt()).equals(ym))
                    .count();
            series.getData().add(new XYChart.Data<>(label, count));
        }
        declarationTrendChart.getData().setAll(series);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void showFlashIfPresent() {
        String message = AdminUiState.consumeFlashMessage();
        boolean error = AdminUiState.consumeFlashError();
        if (message == null || message.isBlank()) {
            flashMessageBox.setManaged(false);
            flashMessageBox.setVisible(false);
            return;
        }
        flashMessageLabel.setText(message);
        flashMessageBox.setManaged(true);
        flashMessageBox.setVisible(true);
        flashMessageBox.getStyleClass().removeAll("success-banner", "error-banner");
        flashMessageLabel.getStyleClass().removeAll("success-banner-text", "error-banner-text");
        if (error) {
            flashMessageBox.getStyleClass().add("error-banner");
            flashMessageLabel.getStyleClass().add("error-banner-text");
        } else {
            flashMessageBox.getStyleClass().add("success-banner");
            flashMessageLabel.getStyleClass().add("success-banner-text");
        }
    }

    @FXML
    public void closeFlash() {
        flashMessageBox.setManaged(false);
        flashMessageBox.setVisible(false);
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDeclareWaste() {
        Main.showDeclarationCitizenFormPage();
    }

    @FXML
    public void handleMyDeclarations() {
        Main.showCitizenMyDeclarationsPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showCitizenStatisticsPage();
    }

    @FXML
    public void handleNews() {
        Main.showCitizenNewsPage();
    }

    @FXML
    public void handleAirQuality() {
        Main.showCitizenAirQualityPage();
    }

    @FXML
    public void handleWithdraw() {
        Main.showCitizenWithdrawPage();
    }

    @FXML
    public void handleProfile() {
        Main.showCitizenSettingsPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}
