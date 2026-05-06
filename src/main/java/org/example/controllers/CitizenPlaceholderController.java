package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;
import org.example.utils.CitizenUiState;

/**
 * Ecran temporaire pour les sections citoyen non encore finalisees (stats, news, air, withdraw).
 */
public class CitizenPlaceholderController {

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
    private Label sectionCrumbLabel;
    @FXML
    private Label heroTitleLabel;
    @FXML
    private Label sectionHintLabel;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        String title = CitizenUiState.getPlaceholderTitle();
        sectionCrumbLabel.setText(title);
        heroTitleLabel.setText(title);
        sectionHintLabel.setText("Cette section sera alignee sur le Symfony PiDev. Navigation citoyen active.");

        Button active = switch (title) {
            case "Statistiques" -> navStatistics;
            case "Nouveautes" -> navNews;
            case "Air Quality" -> navAir;
            case "Withdraw" -> navWithdraw;
            default -> navHome;
        };
        CitizenSidebarHelper.applyActive(active,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);
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
