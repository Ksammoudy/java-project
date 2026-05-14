package main.controllers;

import entities.ReponseOffre;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;
import org.example.utils.CitizenSession;
import services.ServiceReponseOffre;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    private static final String CHATBASE_URL = "https://www.chatbase.co/chatbot-iframe/WAO818oBk6Ity1yhCsPT8";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Stat cards ──────────────────────────────────────────────────────────
    @FXML private Label lblReponsesTotales;
    @FXML private Label lblReponseValidees;
    @FXML private Label lblReponseEnAttente;
    @FXML private Label lblEcoPoints;

    // ── Dernières réponses table ─────────────────────────────────────────────
    @FXML private TableView<ReponseOffre> tableReponses;
    @FXML private TableColumn<ReponseOffre, String> colDate;
    @FXML private TableColumn<ReponseOffre, String> colOffre;
    @FXML private TableColumn<ReponseOffre, String> colQuantite;
    @FXML private TableColumn<ReponseOffre, String> colStatut;

    // ── Chatbot ──────────────────────────────────────────────────────────────
    @FXML private VBox chatbaseWindow;
    @FXML private WebView chatbaseWebView;

    private boolean chatbaseLoaded;
    private final ServiceReponseOffre serviceReponse = new ServiceReponseOffre();

    @FXML
    public void initialize() {
        configurerTable();
        chargerDonnees();
    }

    // ── Table setup ──────────────────────────────────────────────────────────

    private void configurerTable() {
        colDate.setCellValueFactory(cell -> {
            ReponseOffre r = cell.getValue();
            if (r == null || r.getDateSoumis() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(
                    r.getDateSoumis().toLocalDateTime().format(DATE_FMT));
        });

        colOffre.setCellValueFactory(cell -> {
            ReponseOffre r = cell.getValue();
            if (r == null) return new SimpleStringProperty("");
            return new SimpleStringProperty("Appel #" + r.getAppelOffreId());
        });

        colQuantite.setCellValueFactory(cell -> {
            ReponseOffre r = cell.getValue();
            if (r == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(r.getQuantiteProposee() + " kg");
        });

        colStatut.setCellValueFactory(cell -> {
            ReponseOffre r = cell.getValue();
            if (r == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(r.getStatut());
        });
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private void chargerDonnees() {
        try {
            Integer citoyenId = CitizenSession.resolveCitizenDatabaseId();

            int total, validees, enAttente;
            List<ReponseOffre> reponses;

            if (citoyenId != null && citoyenId > 0) {
                total     = serviceReponse.compterTotal(citoyenId);
                validees  = serviceReponse.compterParStatut(citoyenId, ReponseOffre.STATUT_VALIDE);
                enAttente = serviceReponse.compterParStatut(citoyenId, ReponseOffre.STATUT_EN_ATTENTE);
                reponses  = serviceReponse.recupererParCitoyen(citoyenId);
            } else {
                // Fallback: show global stats when no session user is resolved
                List<ReponseOffre> toutes = serviceReponse.recupererTout();
                total     = toutes.size();
                validees  = (int) toutes.stream()
                        .filter(r -> ReponseOffre.STATUT_VALIDE.equals(r.getStatut())).count();
                enAttente = (int) toutes.stream()
                        .filter(r -> ReponseOffre.STATUT_EN_ATTENTE.equals(r.getStatut())).count();
                reponses  = toutes.size() > 5 ? toutes.subList(0, 5) : toutes;
            }

            long ecoPoints = serviceReponse.sumEcoPoints(citoyenId != null ? citoyenId : 0);

            lblReponsesTotales.setText(String.valueOf(total));
            lblReponseValidees.setText(String.valueOf(validees));
            lblReponseEnAttente.setText(String.valueOf(enAttente));
            lblEcoPoints.setText(String.valueOf(ecoPoints));

            ObservableList<ReponseOffre> data = FXCollections.observableArrayList(reponses);
            tableReponses.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
            lblReponsesTotales.setText("—");
            lblReponseValidees.setText("—");
            lblReponseEnAttente.setText("—");
            lblEcoPoints.setText("—");
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.DASHBOARD, AppRoutes.TITLE_DASHBOARD);
    }

    @FXML
    private void onOpenAppelOffre(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.APPEL_OFFRE_CREATE, "WasteWise - Creer un appel d'offre");
    }

    @FXML
    private void onOpenReponseOffre(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.REPONSE_OFFRE_CREATE, "WasteWise - Creer une reponse d'offre");
    }

    @FXML
    private void onOpenBackOffice(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_DASHBOARD, AppRoutes.TITLE_ADMIN_DASHBOARD);
    }

    // ── Chatbot ──────────────────────────────────────────────────────────────

    @FXML
    private void onOpenAssistant() {
        if (!chatbaseLoaded) {
            chatbaseWebView.getEngine().load(CHATBASE_URL);
            chatbaseLoaded = true;
        }
        chatbaseWindow.setManaged(true);
        chatbaseWindow.setVisible(true);
    }

    @FXML
    private void onCloseAssistant() {
        chatbaseWindow.setVisible(false);
        chatbaseWindow.setManaged(false);
    }
}
