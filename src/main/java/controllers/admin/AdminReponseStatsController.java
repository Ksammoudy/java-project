package controllers.admin;

import entities.AppelOffre;
import entities.ReponseOffre;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;
import services.StatsChartApiService;
import utils.ExportDocumentService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminReponseStatsController {

    @FXML
    private BorderPane root;
    @FXML
    private Button btnExporterPdf;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private Label lblTotalReponses;
    @FXML
    private Label lblTauxAcceptation;
    @FXML
    private Label lblQuantiteTotale;
    @FXML
    private Label lblMoyenneReponse;
    @FXML
    private Label lblInfo;
    @FXML
    private ImageView imgEvolution;
    @FXML
    private ImageView imgTopAppels;
    @FXML
    private ImageView imgStatuts;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final StatsChartApiService statsChartApiService = new StatsChartApiService();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private StatsSnapshot currentSnapshot;

    @FXML
    public void initialize() {
        dateFin.setValue(LocalDate.now());
        dateDebut.setValue(LocalDate.now().minusDays(29));
        chargerStatistiques();
    }

    @FXML
    private void onActualiser() {
        chargerStatistiques();
    }

    @FXML
    private void onReinitialiser() {
        dateFin.setValue(LocalDate.now());
        dateDebut.setValue(LocalDate.now().minusDays(29));
        chargerStatistiques();
    }

    @FXML
    private void onExporterPdf() {
        if (currentSnapshot == null) {
            lblInfo.setText("Aucune statistique a exporter.");
            return;
        }
        if (!chartsReady()) {
            lblInfo.setText("Graphiques API en cours de chargement. Patiente quelques secondes puis relance l'export.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les statistiques - PDF");
        chooser.setInitialFileName("statistiques_reponses_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File selected = chooser.showSaveDialog(lblInfo.getScene() == null ? null : lblInfo.getScene().getWindow());
        if (selected == null) {
            lblInfo.setText("Export PDF annule.");
            return;
        }

        try {
            ExportDocumentService.exportImagePdf(captureStatsScreen(), ensurePdf(selected));
            lblInfo.setText("Export PDF visuel genere comme une capture des statistiques.");
        } catch (Exception e) {
            lblInfo.setText("Erreur export PDF: " + e.getMessage());
        }
    }

    private boolean chartsReady() {
        return imageReady(imgEvolution) && imageReady(imgTopAppels) && imageReady(imgStatuts);
    }

    private boolean imageReady(ImageView imageView) {
        Image image = imageView == null ? null : imageView.getImage();
        return image != null && image.getProgress() >= 1d && !image.isError();
    }

    @FXML
    private void onOpenModeration(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_REPONSE_MODERATION, AppRoutes.TITLE_ADMIN_REPONSES);
    }

    @FXML
    private void onOpenAdminDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_DASHBOARD, AppRoutes.TITLE_ADMIN_DASHBOARD);
    }

    private void chargerStatistiques() {
        LocalDate from = dateDebut.getValue();
        LocalDate to = dateFin.getValue();
        if (from == null || to == null || from.isAfter(to)) {
            lblInfo.setText("Periode invalide.");
            return;
        }
        lblInfo.setText("Chargement des donnees et appel de l'API externe QuickChart...");

        Task<StatsSnapshot> task = new Task<>() {
            @Override
            protected StatsSnapshot call() throws Exception {
                List<ReponseOffre> reponses = serviceReponseOffre.recupererTout();
                List<AppelOffre> appels = serviceAppelOffre.recupererTout();
                return buildSnapshot(reponses, appels, from, to);
            }
        };
        task.setOnSucceeded(event -> appliquerSnapshot(task.getValue()));
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            lblInfo.setText("Erreur statistiques: " + (error == null ? "inconnue" : error.getMessage()));
            showWarning("Erreur statistiques", lblInfo.getText());
        });

        Thread thread = new Thread(task, "wastewise-admin-stats-api");
        thread.setDaemon(true);
        thread.start();
    }

    private StatsSnapshot buildSnapshot(List<ReponseOffre> source, List<AppelOffre> appels, LocalDate from, LocalDate to) {
        Map<Integer, String> titresAppels = appels.stream()
                .collect(Collectors.toMap(AppelOffre::getId, a -> safeTitle(a.getTitre()), (a, b) -> a));

        List<ReponseOffre> filtered = source.stream()
                .filter(r -> isBetween(r.getDateSoumis(), from, to))
                .sorted(Comparator.comparing(ReponseOffre::getDateSoumis, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int total = filtered.size();
        int validated = (int) filtered.stream().filter(r -> "valide".equals(normaliserStatut(r.getStatut()))).count();
        int pending = (int) filtered.stream().filter(r -> "en attente".equals(normaliserStatut(r.getStatut()))).count();
        int refused = (int) filtered.stream().filter(r -> "refuse".equals(normaliserStatut(r.getStatut()))).count();
        double totalQuantity = filtered.stream().mapToDouble(ReponseOffre::getQuantiteProposee).sum();
        double averageQuantity = total == 0 ? 0d : totalQuantity / total;
        double acceptanceRate = total == 0 ? 0d : validated * 100d / total;

        Map<LocalDate, DayPoint> timeline = new LinkedHashMap<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            timeline.put(cursor, new DayPoint());
            cursor = cursor.plusDays(1);
        }
        for (ReponseOffre response : filtered) {
            LocalDate day = toLocalDate(response.getDateSoumis());
            if (day != null && timeline.containsKey(day)) {
                timeline.get(day).add(response.getQuantiteProposee());
            }
        }

        Map<Integer, Integer> countsByAppel = new HashMap<>();
        for (ReponseOffre response : filtered) {
            countsByAppel.merge(response.getAppelOffreId(), 1, Integer::sum);
        }
        List<Map.Entry<Integer, Integer>> topEntries = countsByAppel.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();

        List<String> topLabels = topEntries.stream()
                .map(entry -> titresAppels.getOrDefault(entry.getKey(), "#" + entry.getKey()))
                .toList();
        List<Integer> topCounts = topEntries.stream().map(Map.Entry::getValue).toList();

        if (topLabels.isEmpty()) {
            topLabels = List.of("Aucune donnee");
            topCounts = List.of(0);
        }

        return new StatsSnapshot(
                from,
                to,
                total,
                validated,
                pending,
                refused,
                totalQuantity,
                averageQuantity,
                acceptanceRate,
                new ArrayList<>(timeline.keySet()).stream().map(dayFormatter::format).toList(),
                timeline.values().stream().map(DayPoint::count).toList(),
                timeline.values().stream().map(DayPoint::quantity).toList(),
                topLabels,
                topCounts
        );
    }

    private void appliquerSnapshot(StatsSnapshot snapshot) {
        currentSnapshot = snapshot;
        lblTotalReponses.setText(String.valueOf(snapshot.totalResponses()));
        lblTauxAcceptation.setText(String.format(Locale.FRANCE, "%.1f%%", snapshot.acceptanceRate()));
        lblQuantiteTotale.setText(formatKg(snapshot.totalQuantity()));
        lblMoyenneReponse.setText(formatKg(snapshot.averageQuantity()));

        imgEvolution.setImage(new Image(statsChartApiService.buildEvolutionChartUrl(
                snapshot.timelineLabels(),
                snapshot.timelineCounts(),
                snapshot.timelineQuantities()
        ), true));
        imgTopAppels.setImage(new Image(statsChartApiService.buildTopAppelsChartUrl(
                snapshot.topAppelLabels(),
                snapshot.topAppelCounts()
        ), true));
        imgStatuts.setImage(new Image(statsChartApiService.buildStatusChartUrl(
                snapshot.validated(),
                snapshot.pending(),
                snapshot.refused()
        ), true));
        lblInfo.setText("Statistiques synchronisees avec l'API externe QuickChart.");
    }

    private BufferedImage captureStatsScreen() {
        boolean exportVisible = btnExporterPdf == null || btnExporterPdf.isVisible();
        boolean exportManaged = btnExporterPdf == null || btnExporterPdf.isManaged();
        boolean infoVisible = lblInfo == null || lblInfo.isVisible();
        boolean infoManaged = lblInfo == null || lblInfo.isManaged();

        try {
            if (btnExporterPdf != null) {
                btnExporterPdf.setVisible(false);
                btnExporterPdf.setManaged(false);
            }
            if (lblInfo != null) {
                lblInfo.setVisible(false);
                lblInfo.setManaged(false);
            }

            root.applyCss();
            root.layout();
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(javafx.scene.paint.Color.WHITE);
            WritableImage image = root.snapshot(parameters, null);
            return toBufferedImage(image);
        } finally {
            if (btnExporterPdf != null) {
                btnExporterPdf.setVisible(exportVisible);
                btnExporterPdf.setManaged(exportManaged);
            }
            if (lblInfo != null) {
                lblInfo.setVisible(infoVisible);
                lblInfo.setManaged(infoManaged);
            }
            root.applyCss();
            root.layout();
        }
    }

    private BufferedImage toBufferedImage(WritableImage image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return buffered;
    }

    private List<List<String>> exportRows(StatsSnapshot snapshot) {
        return List.of(
                List.of("Periode", snapshot.from() + " au " + snapshot.to()),
                List.of("API externe", "QuickChart Chart Image API"),
                List.of("Endpoint", "https://quickchart.io/chart"),
                List.of("Reponses sur la periode", String.valueOf(snapshot.totalResponses())),
                List.of("Taux d'acceptation", String.format(Locale.FRANCE, "%.1f%%", snapshot.acceptanceRate())),
                List.of("Quantite totale", formatKg(snapshot.totalQuantity())),
                List.of("Moyenne par reponse", formatKg(snapshot.averageQuantity())),
                List.of("Validees", String.valueOf(snapshot.validated())),
                List.of("En attente", String.valueOf(snapshot.pending())),
                List.of("Refusees", String.valueOf(snapshot.refused()))
        );
    }

    private boolean isBetween(Timestamp timestamp, LocalDate from, LocalDate to) {
        LocalDate day = toLocalDate(timestamp);
        return day != null && !day.isBefore(from) && !day.isAfter(to);
    }

    private LocalDate toLocalDate(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toLocalDate();
    }

    private String normaliserStatut(String value) {
        if (value == null) {
            return "en attente";
        }
        String status = value.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if ("valide".equals(status) || "validee".equals(status) || "acceptee".equals(status)) {
            return "valide";
        }
        if ("refuse".equals(status) || "refusee".equals(status) || "rejetee".equals(status)) {
            return "refuse";
        }
        return "en attente";
    }

    private String formatKg(double value) {
        return String.format(Locale.FRANCE, "%,.2f kg", value).replace('\u00A0', ' ');
    }

    private String safeTitle(String title) {
        String value = title == null || title.isBlank() ? "Appel sans titre" : title.trim();
        return value.length() <= 18 ? value : value.substring(0, 17) + ".";
    }

    private File ensurePdf(File file) {
        String path = file.getAbsolutePath();
        return path.toLowerCase(Locale.ROOT).endsWith(".pdf") ? file : new File(path + ".pdf");
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Statistiques");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final class DayPoint {
        private int count;
        private double quantity;

        private void add(double responseQuantity) {
            count++;
            quantity += responseQuantity;
        }

        private int count() {
            return count;
        }

        private double quantity() {
            return quantity;
        }
    }

    private record StatsSnapshot(
            LocalDate from,
            LocalDate to,
            int totalResponses,
            int validated,
            int pending,
            int refused,
            double totalQuantity,
            double averageQuantity,
            double acceptanceRate,
            List<String> timelineLabels,
            List<Integer> timelineCounts,
            List<Double> timelineQuantities,
            List<String> topAppelLabels,
            List<Integer> topAppelCounts
    ) {
    }
}
