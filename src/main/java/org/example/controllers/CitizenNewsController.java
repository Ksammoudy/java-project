package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.Main;
import org.example.models.User;
import org.example.services.NewsApiService;
import org.example.services.SessionManager;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.awt.Desktop;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CitizenNewsController {
    private final NewsApiService newsApiService = new NewsApiService();
    private final List<NewsApiService.Article> loadedArticles = new ArrayList<>();

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
    private TextField searchField;
    @FXML
    private Button refreshNewsButton;
    @FXML
    private Label newsStatusLabel;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "--");

        CitizenSidebarHelper.applyActive(navNews,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applySearchFilter(newValue));
        }

        fetchNewsAsync();
    }

    @FXML
    public void handleRefreshNews() {
        fetchNewsAsync();
    }

    private void fetchNewsAsync() {
        if (refreshNewsButton != null) {
            refreshNewsButton.setDisable(true);
        }
        if (newsStatusLabel != null) {
            newsStatusLabel.setText("Chargement des nouveautés...");
        }
        articlesBox.getChildren().clear();

        Integer citizenId = CitizenSession.resolveCitizenDatabaseId();
        Thread worker = new Thread(() -> {
            NewsApiService.Result result = newsApiService.getPersonalizedWasteWiseNews(20, citizenId);
            Platform.runLater(() -> {
                if (refreshNewsButton != null) {
                    refreshNewsButton.setDisable(false);
                }
                if (!result.available()) {
                    loadedArticles.clear();
                    showUnavailableCard("Impossible de charger les nouveautés.");
                    if (newsStatusLabel != null) {
                        newsStatusLabel.setText("Impossible de charger les nouveautés.");
                    }
                    return;
                }

                loadedArticles.clear();
                loadedArticles.addAll(result.articles());
                if (newsStatusLabel != null) {
                    newsStatusLabel.setText(loadedArticles.size() + " articles chargés.");
                }
                applySearchFilter(searchField != null ? searchField.getText() : "");
            });
        }, "newsapi-fetch");
        worker.setDaemon(true);
        worker.start();
    }

    private void applySearchFilter(String keyword) {
        articlesBox.getChildren().clear();
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<NewsApiService.Article> filtered = loadedArticles.stream()
                .filter(a -> q.isBlank() || matchesQuery(a, q))
                .toList();

        if (filtered.isEmpty()) {
            showUnavailableCard("Aucun article trouvé pour ce filtre.");
            return;
        }

        for (NewsApiService.Article a : filtered) {
            articlesBox.getChildren().add(buildArticleCard(a));
        }
    }

    private boolean matchesQuery(NewsApiService.Article a, String q) {
        String blob = (safe(a.title()) + " " + safe(a.description()) + " " + safe(a.sourceName()) + " " + safe(a.author()))
                .toLowerCase(Locale.ROOT);
        return blob.contains(q);
    }

    private VBox buildArticleCard(NewsApiService.Article a) {
        VBox card = new VBox(10);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(16));

        HBox top = new HBox(12);
        top.setFillHeight(true);

        if (a.urlToImage() != null && !a.urlToImage().isBlank()) {
            ImageView image = new ImageView();
            image.setFitWidth(160);
            image.setFitHeight(96);
            image.setPreserveRatio(true);
            try {
                Image img = new Image(a.urlToImage(), true);
                image.setImage(img);
                top.getChildren().add(image);
            } catch (Exception ignored) {
                // Ignore invalid image URLs.
            }
        }

        VBox textBox = new VBox(8);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label title = new Label(safeOrDefault(a.title(), "Sans titre"));
        title.getStyleClass().add("panel-heading");
        title.setWrapText(true);

        Label desc = new Label(safeOrDefault(a.description(), "Aucune description."));
        desc.getStyleClass().add("body-text");
        desc.setWrapText(true);

        Label meta = new Label(
                safeOrDefault(a.sourceName(), "Source inconnue")
                        + " | "
                        + safeOrDefault(a.author(), "Auteur inconnu")
                        + " | "
                        + formatDate(a.publishedAt())
        );
        meta.getStyleClass().add("workspace-subtitle");

        textBox.getChildren().addAll(title, desc, meta);
        top.getChildren().add(textBox);

        HBox actions = new HBox(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button readMore = new Button("Lire plus");
        readMore.getStyleClass().add("outline-btn");
        readMore.setDisable(a.url() == null || a.url().isBlank());
        readMore.setOnAction(evt -> openArticle(a.url()));
        actions.getChildren().addAll(spacer, readMore);

        card.getChildren().addAll(top, actions);
        return card;
    }

    private void openArticle(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception ignored) {
            // Keep UI stable if desktop browse fails.
        }
    }

    private void showUnavailableCard(String message) {
        VBox card = new VBox(8);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(16));
        Label title = new Label("Nouveautés");
        title.getStyleClass().add("panel-heading");
        Label body = new Label(message);
        body.setWrapText(true);
        body.getStyleClass().add("body-text");
        card.getChildren().addAll(title, body);
        articlesBox.getChildren().add(card);
    }

    private String formatDate(String publishedAt) {
        if (publishedAt == null || publishedAt.isBlank()) {
            return "--";
        }
        try {
            OffsetDateTime dt = OffsetDateTime.parse(publishedAt);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception ex) {
            return publishedAt;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
