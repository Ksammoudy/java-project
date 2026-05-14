package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.entities.Wallet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.OpenMeteoWeatherService;
import org.example.services.SessionManager;
import org.example.services.WalletJdbcService;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;
import org.example.utils.WeatherLocationState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashboardCitizenController {

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final WalletJdbcService walletService = new WalletJdbcService();
    private final OpenMeteoWeatherService weatherService = new OpenMeteoWeatherService();
    private static final double DEFAULT_TUNIS_LAT = 36.8065;
    private static final double DEFAULT_TUNIS_LON = 10.1815;
    private final AtomicBoolean weatherLoading = new AtomicBoolean(false);
    private Timeline weatherAutoRefreshTimeline;

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
    private Label weatherTitleLabel;
    @FXML
    private Label weatherTemperatureLabel;
    @FXML
    private Label weatherDescriptionLabel;
    @FXML
    private Label weatherUpdatedLabel;
    @FXML
    private Label weatherFeelsLikeLabel;
    @FXML
    private Label weatherHumidityLabel;
    @FXML
    private Label weatherWindLabel;
    @FXML
    private Label weatherRainLabel;
    @FXML
    private Button refreshWeatherButton;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        if (citizenNameLabel != null) {
            citizenNameLabel.setText(CitizenSession.fullName(user));
        }
        if (headerEmailLabel != null) {
            headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "--");
        }

        if (navHome != null) {
            CitizenSidebarHelper.applyActive(navHome,
                    navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);
        }

        loadStatsAndCharts();
        initWeatherCard();
        showFlashIfPresent();
    }

    private void initWeatherCard() {
        if (weatherTemperatureLabel == null) {
            return;
        }
        setWeatherUnavailable();
        fetchWeatherAsync();

        weatherAutoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> fetchWeatherAsync()));
        weatherAutoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        weatherAutoRefreshTimeline.play();

        if (weatherTemperatureLabel.getScene() != null) {
            weatherTemperatureLabel.getScene().windowProperty().addListener((obs, oldWindow, newWindow) -> {
                if (newWindow == null && weatherAutoRefreshTimeline != null) {
                    weatherAutoRefreshTimeline.stop();
                }
            });
        } else {
            weatherTemperatureLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (oldScene != null && newScene == null && weatherAutoRefreshTimeline != null) {
                    weatherAutoRefreshTimeline.stop();
                }
            });
        }
    }

    @FXML
    public void handleRefreshWeather() {
        fetchWeatherAsync();
    }

    private void fetchWeatherAsync() {
        if (weatherTemperatureLabel == null) {
            return;
        }
        if (!weatherLoading.compareAndSet(false, true)) {
            return;
        }
        if (refreshWeatherButton != null) {
            refreshWeatherButton.setDisable(true);
        }

        WeatherLocationState.Coordinates selected = WeatherLocationState.getSelectedLocationOrNull();
        double latitude = selected != null ? selected.latitude() : DEFAULT_TUNIS_LAT;
        double longitude = selected != null ? selected.longitude() : DEFAULT_TUNIS_LON;
        String locationName = selected != null
                ? String.format(Locale.ROOT, "%.4f, %.4f", latitude, longitude)
                : "Tunis";
        if (weatherTitleLabel != null) {
            weatherTitleLabel.setText("Meteo actuelle - " + locationName);
        }

        Thread worker = new Thread(() -> {
            try {
                OpenMeteoWeatherService.WeatherReading reading = weatherService.fetch(latitude, longitude);
                Platform.runLater(() -> applyWeather(reading));
            } catch (Exception ex) {
                Platform.runLater(this::setWeatherUnavailable);
            } finally {
                Platform.runLater(() -> {
                    weatherLoading.set(false);
                    if (refreshWeatherButton != null) {
                        refreshWeatherButton.setDisable(false);
                    }
                });
            }
        }, "weather-refresh");
        worker.setDaemon(true);
        worker.start();
    }

    private void applyWeather(OpenMeteoWeatherService.WeatherReading reading) {
        weatherTemperatureLabel.setText(String.format(Locale.ROOT, "%.1f°C", reading.temperatureC()));
        weatherFeelsLikeLabel.setText(String.format(Locale.ROOT, "Ressenti: %.1f°C", reading.feelsLikeC()));
        weatherHumidityLabel.setText(String.format(Locale.ROOT, "Humidite: %.0f%%", reading.humidityPercent()));
        weatherWindLabel.setText(String.format(Locale.ROOT, "Vent: %.1f km/h", reading.windKmh()));
        weatherRainLabel.setText(String.format(Locale.ROOT, "Pluie: %.1f mm", reading.rainMm()));
        weatherDescriptionLabel.setText(reading.weatherText());
        weatherUpdatedLabel.setText("Maj: " + reading.updatedAt());
    }

    private void setWeatherUnavailable() {
        if (weatherTemperatureLabel != null) {
            weatherTemperatureLabel.setText("--°C");
        }
        if (weatherFeelsLikeLabel != null) {
            weatherFeelsLikeLabel.setText("Ressenti: --°C");
        }
        if (weatherHumidityLabel != null) {
            weatherHumidityLabel.setText("Humidite: --%");
        }
        if (weatherWindLabel != null) {
            weatherWindLabel.setText("Vent: -- km/h");
        }
        if (weatherRainLabel != null) {
            weatherRainLabel.setText("Pluie: -- mm");
        }
        if (weatherDescriptionLabel != null) {
            weatherDescriptionLabel.setText("Meteo indisponible");
        }
        if (weatherUpdatedLabel != null) {
            weatherUpdatedLabel.setText("Maj: --");
        }
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
            long approved = list.stream().filter(d -> isValidatedStatus(d.getStatut())).count();
            long pending = list.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
            int pointsEarned = list.stream()
                    .mapToInt(d -> d.getPointsAttribues() == null ? 0 : d.getPointsAttribues())
                    .sum();

            Wallet syncedWallet = walletService.syncCitizenWalletPoints(cid);
            int walletBalance = syncedWallet.getSoldeActuel() == null ? pointsEarned : syncedWallet.getSoldeActuel();

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
        if (totalDeclarationsLabel != null) {
            totalDeclarationsLabel.setText(String.valueOf(total));
        }
        if (validatedLabel != null) {
            validatedLabel.setText(String.valueOf(approved));
        }
        if (pendingLabel != null) {
            pendingLabel.setText(String.valueOf(pending));
        }
        if (earnedPointsLabel != null) {
            earnedPointsLabel.setText(String.valueOf(ecoPoints));
        }
    }

    private void setPills(long total, long approved, int ecoPoints) {
        if (declarationsPillLabel != null) {
            declarationsPillLabel.setText("Declarations: " + total);
        }
        if (approvedPillLabel != null) {
            approvedPillLabel.setText("Validees: " + approved);
        }
        if (pointsPillLabel != null) {
            pointsPillLabel.setText("EcoPoints: " + ecoPoints);
        }
    }

    private void setEmptyCharts() {
        if (declarationTrendChart != null) {
            declarationTrendChart.getData().clear();
        }
        if (statusChart != null) {
            statusChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Aucune donnee", 1)
            ));
        }
    }

    private void populateCharts(List<DeclarationDechet> list) {
        long approved = list.stream().filter(d -> isValidatedStatus(d.getStatut())).count();
        long pending = list.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
        long refused = list.stream().filter(d -> "REFUSEE".equals(normalizeStatus(d.getStatut()))).count();

        if (statusChart != null) {
            if (approved + pending + refused == 0) {
                statusChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Aucune declaration", 1)
                ));
            } else {
                statusChart.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Validees", approved),
                        new PieChart.Data("En attente", pending),
                        new PieChart.Data("Refusees", refused)
                ));
            }
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
        if (declarationTrendChart != null) {
            declarationTrendChart.getData().setAll(series);
        }
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isValidatedStatus(String status) {
        String normalized = normalizeStatus(status);
        return "APPROUVEE".equals(normalized) || "VALIDATED".equals(normalized);
    }

    private void showFlashIfPresent() {
        if (flashMessageBox == null || flashMessageLabel == null) {
            return;
        }

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
        if (flashMessageBox != null) {
            flashMessageBox.setManaged(false);
            flashMessageBox.setVisible(false);
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
        SessionManager.logout();
        Main.showLoginPage();
    }
}
