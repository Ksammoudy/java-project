package org.example.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.Main;
import org.example.models.User;
import org.example.services.NewsApiService;
import org.example.services.SessionManager;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.awt.Desktop;
import java.net.URI;

public class CitizenNewsController {
    private final NewsApiService newsApiService = new NewsApiService();

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
    private VBox articlesBox;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        CitizenSidebarHelper.applyActive(navNews,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        loadNews();
    }

    private void loadNews() {
        articlesBox.getChildren().clear();
        NewsApiService.Result result = newsApiService.getWasteAndEnergyNews(12);
        if (!result.available()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("panel-card");
            card.setPadding(new Insets(16));
            Label title = new Label("Nouveautes indisponibles");
            title.getStyleClass().add("panel-heading");
            Label body = new Label(result.message() == null ? "Service indisponible." : result.message());
            body.setWrapText(true);
            body.getStyleClass().add("body-text");
            card.getChildren().addAll(title, body);
            articlesBox.getChildren().add(card);
            return;
        }

        for (NewsApiService.Article a : result.articles()) {
            VBox card = new VBox(8);
            card.getStyleClass().add("panel-card");
            card.setPadding(new Insets(16));

            Label title = new Label(a.title());
            title.getStyleClass().add("panel-heading");

            Label date = new Label((a.source() == null ? "Source inconnue" : a.source())
                    + (a.publishedAt() == null || a.publishedAt().isBlank() ? "" : " - " + a.publishedAt()));
            date.getStyleClass().add("workspace-subtitle");

            Label body = new Label(a.description() == null || a.description().isBlank() ? "Aucune description." : a.description());
            body.setWrapText(true);
            body.getStyleClass().add("body-text");

            Button openLink = new Button("Ouvrir l'article");
            openLink.getStyleClass().add("outline-btn");
            openLink.setDisable(a.url() == null || a.url().isBlank());
            openLink.setOnAction(evt -> {
                try {
                    if (Desktop.isDesktopSupported() && a.url() != null && !a.url().isBlank()) {
                        Desktop.getDesktop().browse(URI.create(a.url()));
                    }
                } catch (Exception ignored) {
                }
            });

            card.getChildren().addAll(title, date, body, openLink);
            articlesBox.getChildren().add(card);
        }
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
