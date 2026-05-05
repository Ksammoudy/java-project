package org.example.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.Main;
import org.example.models.ZonePolluee;
import org.example.services.PDFExportService;
import org.example.services.ZonePollueeDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdvancedDashboardController {

    @FXML private Label totalZonesLabel;
    @FXML private Label avgPollutionLabel;
    @FXML private Label criticalZonesLabel;
    @FXML private Label mediumZonesLabel;
    @FXML private Label lowZonesLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label healthScoreLabel;

    @FXML private PieChart riskPieChart;
    @FXML private BarChart<String, Number> pollutionBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private StackedBarChart<String, Number> comparisonChart;

    @FXML private TableView<ZonePolluee> zonesTableView;
    @FXML private TableColumn<ZonePolluee, String> nameColumn;
    @FXML private TableColumn<ZonePolluee, Integer> levelColumn;
    @FXML private TableColumn<ZonePolluee, String> riskColumn;
    @FXML private TableColumn<ZonePolluee, String> gpsColumn;

    @FXML private ProgressBar healthProgressBar;
    @FXML private Label healthStatusLabel;
    @FXML private Text recommendationText;
    @FXML private VBox alertBox;
    @FXML private GridPane statsGrid;
    @FXML private Label riskIndexLabel;
    @FXML private Label improvementLabel;
    @FXML private Label criticalPercentageLabel;

    private ZonePollueeDAO zoneDAO = new ZonePollueeDAO();
    private List<ZonePolluee> zones;
    private Timeline refreshTimeline;
    private Timeline animationTimeline;
    private Random random = new Random();

    @FXML
    public void initialize() {
        loadData();
        setupCharts();
        setupTableView();
        setupAutoRefresh();
        startAdvancedAnimations();
        startRealTimeUpdates();
    }

    private void loadData() {
        zones = zoneDAO.getAllZones();
        updateStatistics();
        updateCharts();
        updateTableView();
        updateHealthRecommendations();
        updateAlerts();
        updateAdvancedMetrics();
        lastUpdatedLabel.setText("Dernière mise à jour: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void updateAdvancedMetrics() {
        double avgPollution = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(0);
        int criticalCount = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        double riskIndex = (avgPollution * 10) + (criticalCount * 5);
        riskIndex = Math.min(100, riskIndex);

        riskIndexLabel.setText(String.format("%.1f", riskIndex));

        double improvement = random.nextDouble() * 20 - 5;
        String improvementText = improvement >= 0 ? "▲ +" + String.format("%.1f", improvement) + "%" : "▼ " + String.format("%.1f", Math.abs(improvement)) + "%";
        improvementLabel.setText(improvementText);
        improvementLabel.setStyle(improvement >= 0 ? "-fx-text-fill: #4caf50;" : "-fx-text-fill: #dc3545;");

        double criticalPercentage = (criticalCount * 100.0) / zones.size();
        criticalPercentageLabel.setText(String.format("%.1f%%", criticalPercentage));

        double healthScore = 100 - riskIndex;
        healthScoreLabel.setText(String.format("%.0f/100", healthScore));
    }

    private void updateStatistics() {
        int total = zones.size();
        int critical = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        int medium = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
        int low = (int) zones.stream().filter(z -> z.getNiveauPollution() < 4).count();
        double avg = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(0);

        totalZonesLabel.setText(String.valueOf(total));
        criticalZonesLabel.setText(String.valueOf(critical));
        mediumZonesLabel.setText(String.valueOf(medium));
        lowZonesLabel.setText(String.valueOf(low));
        avgPollutionLabel.setText(String.format("%.1f/10", avg));

        if (critical > total / 2) {
            healthProgressBar.setStyle("-fx-accent: #dc3545;");
            healthProgressBar.setProgress(0.2);
            healthStatusLabel.setText("⚠️ CRITIQUE");
        } else if (critical > 0) {
            healthProgressBar.setStyle("-fx-accent: #ff9800;");
            healthProgressBar.setProgress(0.5);
            healthStatusLabel.setText("🟡 ATTENTION");
        } else {
            healthProgressBar.setStyle("-fx-accent: #4caf50;");
            healthProgressBar.setProgress(0.9);
            healthStatusLabel.setText("✅ SÛR");
        }
    }

    private void setupCharts() {
        riskPieChart.setTitle("Distribution des Risques");
        riskPieChart.setLegendSide(Side.RIGHT);
        riskPieChart.setLabelsVisible(true);

        pollutionBarChart.setTitle("Niveaux de Pollution par Zone");
        pollutionBarChart.setLegendVisible(false);
        pollutionBarChart.setAnimated(true);

        trendLineChart.setTitle("Tendance de Pollution (6 mois)");
        trendLineChart.setCreateSymbols(true);
        trendLineChart.setAnimated(true);

        comparisonChart.setTitle("Comparaison par Niveau de Risque");
        comparisonChart.setLegendVisible(true);
        comparisonChart.setAnimated(true);
    }

    private void updateCharts() {
        updatePieChart();
        updateBarChart();
        updateTrendChart();
        updateComparisonChart();
    }

    private void updatePieChart() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        int critical = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        int medium = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
        int low = (int) zones.stream().filter(z -> z.getNiveauPollution() < 4).count();

        pieData.addAll(
                new PieChart.Data("Critique (" + critical + ")", critical),
                new PieChart.Data("Moyen (" + medium + ")", medium),
                new PieChart.Data("Faible (" + low + ")", low)
        );
        riskPieChart.setData(pieData);
    }

    private void updateBarChart() {
        pollutionBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Niveau de pollution");

        List<ZonePolluee> sortedZones = zones.stream()
                .sorted(Comparator.comparingInt(ZonePolluee::getNiveauPollution).reversed())
                .limit(8)
                .collect(Collectors.toList());

        for (ZonePolluee zone : sortedZones) {
            String zoneName = zone.getNomZone();
            if (zoneName.length() > 12) {
                zoneName = zoneName.substring(0, 10) + "...";
            }
            series.getData().add(new XYChart.Data<>(zoneName, zone.getNiveauPollution()));
        }
        pollutionBarChart.getData().add(series);
    }

    private void updateTrendChart() {
        trendLineChart.getData().clear();
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Évolution de la pollution");

        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        double baseAvg = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(5);

        for (int i = 0; i < months.length; i++) {
            double trend = baseAvg + Math.sin(i * Math.PI / 3) * 1.5;
            trend = Math.max(1, Math.min(10, trend));
            trendSeries.getData().add(new XYChart.Data<>(months[i], trend));
        }
        trendLineChart.getData().add(trendSeries);
    }

    private void updateComparisonChart() {
        comparisonChart.getData().clear();

        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Niveau actuel");

        XYChart.Series<String, Number> thresholdSeries = new XYChart.Series<>();
        thresholdSeries.setName("Seuil critique");

        List<ZonePolluee> topZones = zones.stream()
                .sorted(Comparator.comparingInt(ZonePolluee::getNiveauPollution).reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (ZonePolluee zone : topZones) {
            String zoneName = zone.getNomZone();
            if (zoneName.length() > 8) {
                zoneName = zoneName.substring(0, 7) + "...";
            }
            currentSeries.getData().add(new XYChart.Data<>(zoneName, zone.getNiveauPollution()));
            thresholdSeries.getData().add(new XYChart.Data<>(zoneName, 7.0));
        }

        comparisonChart.getData().addAll(currentSeries, thresholdSeries);
    }

    private void setupTableView() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomZone"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("niveauPollution"));
        gpsColumn.setCellValueFactory(new PropertyValueFactory<>("coordonneesGps"));

        riskColumn.setCellValueFactory(cellData -> {
            int level = cellData.getValue().getNiveauPollution();
            String risk;
            if (level >= 7) risk = "🔴 CRITIQUE";
            else if (level >= 4) risk = "🟡 MOYEN";
            else risk = "🟢 FAIBLE";
            return new javafx.beans.property.SimpleStringProperty(risk);
        });

        levelColumn.setCellFactory(column -> new TableCell<ZonePolluee, Integer>() {
            @Override
            protected void updateItem(Integer level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(level.toString() + "/10");
                    setAlignment(Pos.CENTER);
                    if (level >= 7) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-background-color: #ffebee;");
                    } else if (level >= 4) {
                        setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold; -fx-background-color: #fff3e0;");
                    } else {
                        setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold; -fx-background-color: #e8f5e9;");
                    }
                }
            }
        });
    }

    private void updateTableView() {
        zonesTableView.getItems().clear();
        zonesTableView.getItems().addAll(zones);
        levelColumn.setSortType(TableColumn.SortType.DESCENDING);
        zonesTableView.getSortOrder().add(levelColumn);
    }

    private void updateHealthRecommendations() {
        int criticalCount = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();

        if (criticalCount > zones.size() / 2) {
            recommendationText.setText("🚨 **URGENCE SANITAIRE MAJEURE** 🚨\n\n• ÉVACUATION recommandée\n• Port du masque OBLIGATOIRE\n• Restez à l'intérieur\n• Consultez un médecin");
        } else if (criticalCount > 0) {
            recommendationText.setText("⚠️ **RECOMMANDATIONS SANITAIRES** ⚠️\n\n• Portez un masque\n• Évitez les activités extérieures\n• Lavez-vous les mains\n• Surveillez les symptômes");
        } else {
            recommendationText.setText("✅ **ENVIRONNEMENT SAIN** ✅\n\n• Activités normales\n• Continuez la surveillance\n• Signalez toute pollution");
        }
    }

    private void updateAlerts() {
        alertBox.getChildren().clear();

        List<ZonePolluee> criticalZones = zones.stream()
                .filter(z -> z.getNiveauPollution() >= 8)
                .collect(Collectors.toList());

        if (!criticalZones.isEmpty()) {
            Label alertTitle = new Label("🔴 ALERTES CRITIQUES 🔴");
            alertTitle.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            alertBox.getChildren().add(alertTitle);

            for (ZonePolluee zone : criticalZones) {
                Label alert = new Label("⚠️ " + zone.getNomZone() + " - Niveau " + zone.getNiveauPollution() + "/10");
                alert.setStyle("-fx-text-fill: #dc3545;");
                alertBox.getChildren().add(alert);
            }
        }
    }

    private void setupAutoRefresh() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> refreshData())
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void startRealTimeUpdates() {
        animationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> {
                    Platform.runLater(() -> updateAdvancedMetrics());
                })
        );
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    private void startAdvancedAnimations() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), statsGrid);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void refreshData() {
        loadData();
        // Pas de popup — refresh silencieux
    }

    // ==================== EXPORT METHODS ====================

    @FXML
    private void exportFullReport() {
        showExportDialog("Rapport Complet", "Génération du rapport complet en cours...");
        String filepath = PDFExportService.generateReport(zones, "FULL");
        showExportResult(filepath);
    }

    @FXML
    private void exportExecutiveSummary() {
        showExportDialog("Résumé Exécutif", "Génération du résumé exécutif en cours...");
        String filepath = PDFExportService.generateReport(zones, "EXECUTIVE");
        showExportResult(filepath);
    }

    @FXML
    private void exportStatisticsOnly() {
        showExportDialog("Statistiques", "Génération des statistiques en cours...");
        String filepath = PDFExportService.generateReport(zones, "STATISTICS");
        showExportResult(filepath);
    }

    @FXML
    private void exportZonesList() {
        showExportDialog("Liste des Zones", "Génération de la liste des zones en cours...");
        String filepath = PDFExportService.generateReport(zones, "ZONES");
        showExportResult(filepath);
    }

    @FXML
    private void exportAndEmail() {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Fonctionnalité à venir");
        infoAlert.setHeaderText("Envoi par email");
        infoAlert.setContentText("Cette fonctionnalité sera disponible prochainement.\n\nPour l'instant, le rapport sera sauvegardé localement.");
        infoAlert.showAndWait();

        exportFullReport();
    }

    private void showExportDialog(String title, String message) {
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Export PDF");
        loadingAlert.setHeaderText(title);
        loadingAlert.setContentText(message);
        loadingAlert.show();

        // Auto-close after 1 second
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> loadingAlert.close());
        delay.play();
    }

    private void showExportResult(String filepath) {
        if (filepath != null && !filepath.isEmpty()) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText("Rapport généré avec succès!");
            successAlert.setContentText("Le fichier a été sauvegardé dans:\n" + filepath);

            ButtonType openButton = new ButtonType("Ouvrir le dossier");
            ButtonType closeButton = new ButtonType("Fermer");
            successAlert.getButtonTypes().setAll(openButton, closeButton);

            successAlert.showAndWait().ifPresent(response -> {
                if (response == openButton) {
                    try {
                        String os = System.getProperty("os.name").toLowerCase();
                        String downloads = System.getProperty("user.home") + "/Downloads";
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("explorer.exe /select," + filepath);
                        } else if (os.contains("mac")) {
                            Runtime.getRuntime().exec("open " + downloads);
                        } else {
                            Runtime.getRuntime().exec("xdg-open " + downloads);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Échec de la génération");
            errorAlert.setContentText("Une erreur est survenue lors de la création du PDF.");
            errorAlert.showAndWait();
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Auto-close after 2 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> alert.close());
        delay.play();

        alert.show();
    }

    // ==================== NAVIGATION METHODS ====================

    @FXML
    private void refreshDashboard() {
        refreshData();
    }

    @FXML
    private void goToZones() {
        Main.showZonePollueeListPage();
    }

    @FXML
    private void goToMap() {
        Main.showMapPage();
    }

    @FXML
    private void goToIndicators() {
        Main.showIndicateurImpactListPage();
    }

    @FXML
    private void goToChatbot() {
        Main.showChatbot();
    }

    public void stop() {
        if (refreshTimeline != null) refreshTimeline.stop();
        if (animationTimeline != null) animationTimeline.stop();
    }
}