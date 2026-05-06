package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.OpenAqService;
import org.example.services.SessionManager;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

public class CitizenAirQualityController {

    private static final double TUNIS_LAT = 36.8065;
    private static final double TUNIS_LON = 10.1815;
    private final OpenAqService openAqService = new OpenAqService();

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
    private Label pm25Label;
    @FXML
    private Label pm10Label;
    @FXML
    private Label updatedLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button refreshButton;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");
        CitizenSidebarHelper.applyActive(navAir, navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        pm25Label.setText("-");
        pm10Label.setText("-");
        updatedLabel.setText("-");
        statusLabel.setText("Cliquez sur Actualiser pour charger les donnees (OpenAQ).");
        fetchAsync();
    }

    @FXML
    public void handleRefresh() {
        fetchAsync();
    }

    private void fetchAsync() {
        refreshButton.setDisable(true);
        statusLabel.setText("Chargement...");
        Thread worker = new Thread(() -> {
            try {
                OpenAqService.Result result = openAqService.getLocations(TUNIS_LAT, TUNIS_LON, 25_000, 150);
                Platform.runLater(() -> {
                    refreshButton.setDisable(false);
                    if (!result.success()) {
                        pm25Label.setText("-");
                        pm10Label.setText("-");
                        updatedLabel.setText("-");
                        statusLabel.setText(result.message() == null ? "Impossible de charger OpenAQ." : result.message());
                        return;
                    }

                    int stationCount = result.stations().size();
                    long pm25Count = result.stations().stream().filter(s -> s.parameters().stream().anyMatch(p -> p.equalsIgnoreCase("pm25"))).count();
                    long pm10Count = result.stations().stream().filter(s -> s.parameters().stream().anyMatch(p -> p.equalsIgnoreCase("pm10"))).count();
                    pm25Label.setText(pm25Count > 0 ? pm25Count + " stations" : "Aucune");
                    pm10Label.setText(pm10Count > 0 ? pm10Count + " stations" : "Aucune");
                    updatedLabel.setText(String.valueOf(stationCount));
                    statusLabel.setText("OpenAQ charge (" + stationCount + " stations).");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    refreshButton.setDisable(false);
                    statusLabel.setText("Erreur : " + (ex.getMessage() != null ? ex.getMessage() : "inconnue"));
                });
            }
        }, "openaq-air");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    public void handleDashboard() { Main.showDashboardCitizen(); }
    @FXML
    public void handleDeclareWaste() { Main.showDeclarationCitizenFormPage(); }
    @FXML
    public void handleMyDeclarations() { Main.showCitizenMyDeclarationsPage(); }
    @FXML
    public void handleStatistics() { Main.showCitizenStatisticsPage(); }
    @FXML
    public void handleNews() { Main.showCitizenNewsPage(); }
    @FXML
    public void handleAirQuality() { Main.showCitizenAirQualityPage(); }
    @FXML
    public void handleWithdraw() { Main.showCitizenWithdrawPage(); }
    @FXML
    public void handleProfile() { Main.showCitizenSettingsPage(); }
    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}

