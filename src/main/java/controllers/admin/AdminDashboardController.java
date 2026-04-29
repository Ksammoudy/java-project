package controllers.admin;

import entities.AppelOffre;
import entities.ReponseOffre;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminDashboardController {

    private static final int AUTO_REFRESH_SECONDS = 8;

    @FXML
    private BorderPane root;
    @FXML
    private Label lblIndiceSante;
    @FXML
    private Label lblAppelsActifs;
    @FXML
    private Label lblExpirant;
    @FXML
    private Label lblReponsesRecentes;
    @FXML
    private Label lblTauxValidation;
    @FXML
    private Label lblAlerteExpires;
    @FXML
    private Label lblAlerteModeration;
    @FXML
    private Label lblValidees;
    @FXML
    private Label lblEnAttente;
    @FXML
    private Label lblRefusees;
    @FXML
    private Label lblDerniereMaj;
    @FXML
    private ProgressBar barValidees;
    @FXML
    private ProgressBar barEnAttente;
    @FXML
    private ProgressBar barRefusees;
    @FXML
    private Label lblInfo;

    @FXML
    private TableView<AppelOffre> tableAppelsRecents;
    @FXML
    private TableColumn<AppelOffre, String> colAppelTitre;
    @FXML
    private TableColumn<AppelOffre, Double> colAppelQuantite;
    @FXML
    private TableColumn<AppelOffre, String> colAppelDate;
    @FXML
    private TableColumn<AppelOffre, String> colAppelEtat;
    @FXML
    private TableColumn<AppelOffre, String> colAppelJours;

    @FXML
    private TableView<ReponseOffre> tableReponsesRecentes;
    @FXML
    private TableColumn<ReponseOffre, Integer> colReponseCitoyen;
    @FXML
    private TableColumn<ReponseOffre, Integer> colReponseOffre;
    @FXML
    private TableColumn<ReponseOffre, String> colReponseStatut;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Timeline refreshTimeline;
    private boolean refreshInProgress;

    @FXML
    public void initialize() {
        initialiserTables();
        chargerDashboard(false);
        initialiserAutoRefresh();
    }

    private void initialiserTables() {
        colAppelTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAppelQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteDemandee"));
        colAppelDate.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue().getDateLimite())));
        colAppelEtat.setCellValueFactory(cell -> new SimpleStringProperty(isActive(cell.getValue()) ? "Actif" : "Expire"));
        colAppelJours.setCellValueFactory(cell -> new SimpleStringProperty(daysRemainingLabel(cell.getValue())));
        colAppelEtat.setCellFactory(col -> badgeCell());

        colReponseCitoyen.setCellValueFactory(new PropertyValueFactory<>("citoyenId"));
        colReponseOffre.setCellValueFactory(new PropertyValueFactory<>("appelOffreId"));
        colReponseStatut.setCellValueFactory(cell -> new SimpleStringProperty(normaliserStatut(cell.getValue().getStatut())));
        colReponseStatut.setCellFactory(col -> badgeCell());
    }

    private void initialiserAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(AUTO_REFRESH_SECONDS), event -> chargerDashboard(false)));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        if (root != null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null && refreshTimeline != null) {
                    refreshTimeline.stop();
                }
            });
        }
    }

    private void chargerDashboard(boolean manual) {
        if (refreshInProgress) {
            return;
        }

        refreshInProgress = true;
        if (manual) {
            lblInfo.setText("Synchronisation en cours...");
        }

        Task<DashboardSnapshot> task = new Task<>() {
            @Override
            protected DashboardSnapshot call() throws Exception {
                List<AppelOffre> appels = new ArrayList<>(serviceAppelOffre.recupererTout());
                List<ReponseOffre> reponses = new ArrayList<>(serviceReponseOffre.recupererTout());
                return buildSnapshot(appels, reponses);
            }
        };

        task.setOnSucceeded(event -> {
            refreshInProgress = false;
            appliquerSnapshot(task.getValue(), manual);
        });
        task.setOnFailed(event -> {
            refreshInProgress = false;
            Throwable error = task.getException();
            String message = error == null || error.getMessage() == null ? "Erreur inconnue." : error.getMessage();
            lblInfo.setText("Erreur dashboard admin: " + message);
        });

        Thread thread = new Thread(task, "wastewise-admin-dashboard-refresh");
        thread.setDaemon(true);
        thread.start();
    }

    private DashboardSnapshot buildSnapshot(List<AppelOffre> appels, List<ReponseOffre> reponses) {
        long actifs = appels.stream().filter(this::isActive).count();
        long expirant = appels.stream().filter(this::expiresWithinSevenDays).count();
        long expires = appels.size() - actifs;
        long valides = reponses.stream().filter(r -> "valide".equals(normaliserStatut(r.getStatut()))).count();
        long attente = reponses.stream().filter(r -> "en attente".equals(normaliserStatut(r.getStatut()))).count();
        long refusees = reponses.stream().filter(r -> "refuse".equals(normaliserStatut(r.getStatut()))).count();
        long recentes = reponses.stream().filter(this::isRecentResponse).count();
        long moderationRetard = reponses.stream().filter(this::isPendingLate).count();
        double tauxValidation = reponses.isEmpty() ? 0d : (valides * 100d) / reponses.size();
        int indice = calculerIndiceSante(actifs, expires, attente, moderationRetard, tauxValidation);

        List<AppelOffre> appelsRecents = appels.stream()
                .sorted(Comparator.comparing(AppelOffre::getDateLimite, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();
        List<ReponseOffre> reponsesRecentes = reponses.stream()
                .sorted(Comparator.comparing(ReponseOffre::getDateSoumis, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .toList();

        return new DashboardSnapshot(
                appels.size(),
                reponses.size(),
                actifs,
                expirant,
                expires,
                valides,
                attente,
                refusees,
                recentes,
                moderationRetard,
                tauxValidation,
                indice,
                appelsRecents,
                reponsesRecentes,
                LocalDateTime.now()
        );
    }

    private void appliquerSnapshot(DashboardSnapshot snapshot, boolean manual) {
        lblIndiceSante.setText(snapshot.indiceSante() + "/100");
        lblAppelsActifs.setText(snapshot.appelsActifs() + " / " + snapshot.totalAppels());
        lblExpirant.setText(String.valueOf(snapshot.appelsExpirant()));
        lblReponsesRecentes.setText(snapshot.reponsesRecentes7j() + " / " + snapshot.totalReponses());
        lblTauxValidation.setText(String.format(Locale.ROOT, "%.0f%%", snapshot.tauxValidation()));
        lblAlerteExpires.setText(snapshot.appelsExpires() + " expire(s)");
        lblAlerteModeration.setText(snapshot.reponsesEnAttente() + " en attente");
        lblValidees.setText(String.valueOf(snapshot.reponsesValidees()));
        lblEnAttente.setText(String.valueOf(snapshot.reponsesEnAttente()));
        lblRefusees.setText(String.valueOf(snapshot.reponsesRefusees()));

        double total = Math.max(1d, snapshot.totalReponses());
        barValidees.setProgress(snapshot.reponsesValidees() / total);
        barEnAttente.setProgress(snapshot.reponsesEnAttente() / total);
        barRefusees.setProgress(snapshot.reponsesRefusees() / total);

        tableAppelsRecents.setItems(FXCollections.observableArrayList(snapshot.appelsRecents()));
        tableReponsesRecentes.setItems(FXCollections.observableArrayList(snapshot.reponsesRecentes()));
        lblDerniereMaj.setText("Derniere mise a jour: " + dateTimeFormatter.format(snapshot.updatedAt()));
        lblInfo.setText(manual
                ? "Dashboard rafraichi depuis la base."
                : "Dashboard synchronise automatiquement toutes les " + AUTO_REFRESH_SECONDS + "s.");
    }

    @FXML
    private void onOpenHome(ActionEvent event) {
        stopAutoRefresh();
        ViewNavigator.navigate(event, AppRoutes.DASHBOARD, AppRoutes.TITLE_DASHBOARD);
    }

    @FXML
    private void onOpenAdminDashboard(ActionEvent event) {
        chargerDashboard(true);
    }

    @FXML
    private void onOpenStats(ActionEvent event) {
        stopAutoRefresh();
        ViewNavigator.navigate(event, AppRoutes.ADMIN_REPONSE_STATS, AppRoutes.TITLE_ADMIN_STATS);
    }

    @FXML
    private void onOpenAppels(ActionEvent event) {
        stopAutoRefresh();
        ViewNavigator.navigate(event, AppRoutes.APPEL_OFFRE_LIST, AppRoutes.TITLE_APPELS);
    }

    @FXML
    private void onOpenReponses(ActionEvent event) {
        stopAutoRefresh();
        ViewNavigator.navigate(event, AppRoutes.ADMIN_REPONSE_MODERATION, AppRoutes.TITLE_ADMIN_REPONSES);
    }

    @FXML
    private void onRefresh() {
        chargerDashboard(true);
    }

    private void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    private <T> TableCell<T, String> badgeCell() {
        return new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(item);
                badge.setStyle(styleForBadge(item));
                setGraphic(badge);
                setText(null);
            }
        };
    }

    private String styleForBadge(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("actif") || v.contains("valide")) {
            return "-fx-background-color: #D8F3E5; -fx-text-fill: #087044; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 8 4 8;";
        }
        if (v.contains("expire") || v.contains("refuse")) {
            return "-fx-background-color: #FBE4E4; -fx-text-fill: #C0392B; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 8 4 8;";
        }
        return "-fx-background-color: #FFF1C7; -fx-text-fill: #9A6B00; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 8 4 8;";
    }

    private int calculerIndiceSante(long actifs, long expires, long attente, long moderationRetard, double tauxValidation) {
        int score = 50;
        score += Math.min(20, (int) actifs * 4);
        score += Math.min(20, (int) tauxValidation / 5);
        score -= Math.min(20, (int) expires * 2);
        score -= Math.min(15, (int) attente);
        score -= Math.min(15, (int) moderationRetard * 3);
        return Math.max(0, Math.min(100, score));
    }

    private boolean isActive(AppelOffre a) {
        return a.getDateLimite() != null && a.getDateLimite().after(new Timestamp(System.currentTimeMillis()));
    }

    private boolean expiresWithinSevenDays(AppelOffre a) {
        if (!isActive(a)) {
            return false;
        }
        long days = java.time.Duration.between(LocalDateTime.now(), a.getDateLimite().toLocalDateTime()).toDays();
        return days <= 7;
    }

    private boolean isRecentResponse(ReponseOffre r) {
        if (r.getDateSoumis() == null) {
            return false;
        }
        long days = java.time.Duration.between(r.getDateSoumis().toLocalDateTime(), LocalDateTime.now()).toDays();
        return days <= 7;
    }

    private boolean isPendingLate(ReponseOffre r) {
        if (!"en attente".equals(normaliserStatut(r.getStatut())) || r.getDateSoumis() == null) {
            return false;
        }
        long days = java.time.Duration.between(r.getDateSoumis().toLocalDateTime(), LocalDateTime.now()).toDays();
        return days > 3;
    }

    private String daysRemainingLabel(AppelOffre a) {
        if (a.getDateLimite() == null) {
            return "-";
        }
        long days = java.time.Duration.between(LocalDateTime.now(), a.getDateLimite().toLocalDateTime()).toDays();
        if (days < 0) {
            return "depassee";
        }
        return days + " jour(s)";
    }

    private String formatDate(Timestamp ts) {
        return ts == null ? "" : formatter.format(ts.toLocalDateTime());
    }

    private String normaliserStatut(String s) {
        if (s == null) {
            return "en attente";
        }
        String x = s.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        if ("valide".equals(x) || "validee".equals(x) || "acceptee".equals(x)) {
            return "valide";
        }
        if ("refuse".equals(x) || "refusee".equals(x) || "rejetee".equals(x)) {
            return "refuse";
        }
        return "en attente";
    }

    private record DashboardSnapshot(
            int totalAppels,
            int totalReponses,
            long appelsActifs,
            long appelsExpirant,
            long appelsExpires,
            long reponsesValidees,
            long reponsesEnAttente,
            long reponsesRefusees,
            long reponsesRecentes7j,
            long moderationRetard,
            double tauxValidation,
            int indiceSante,
            List<AppelOffre> appelsRecents,
            List<ReponseOffre> reponsesRecentes,
            LocalDateTime updatedAt
    ) {
    }
}
