package controllers.admin;

import controllers.reponseoffre.ReponseOffreFlowState;
import entities.AppelOffre;
import entities.ReponseOffre;
import entities.UserContact;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import main.navigation.AppRoutes;
import main.navigation.ViewNavigator;
import services.EmailNotificationService;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;
import services.ServiceUserDirectory;
import utils.ExportDocumentService;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminReponseModerationController {

    private static final String STYLE_FILTER_OK =
            "-fx-background-color: #FAFFFC; -fx-border-color: #BFE3CF; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_FILTER_ERROR =
            "-fx-background-color: #FFF6F6; -fx-border-color: #DC2626; -fx-border-radius: 10; -fx-background-radius: 10;";

    @FXML
    private TextField txtRecherche;
    @FXML
    private TextField txtQuantiteMin;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private ComboBox<String> cbTri;
    @FXML
    private ComboBox<String> cbOrdre;
    @FXML
    private ComboBox<Integer> cbPageSize;
    @FXML
    private Label lblInfo;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblCount;
    @FXML
    private Button btnPrecedent;
    @FXML
    private Button btnSuivant;

    @FXML
    private TableView<ReponseOffre> tableReponses;
    @FXML
    private TableColumn<ReponseOffre, Integer> colId;
    @FXML
    private TableColumn<ReponseOffre, Double> colQuantite;
    @FXML
    private TableColumn<ReponseOffre, String> colDateSoumis;
    @FXML
    private TableColumn<ReponseOffre, String> colStatut;
    @FXML
    private TableColumn<ReponseOffre, String> colMessage;
    @FXML
    private TableColumn<ReponseOffre, String> colScore;
    @FXML
    private TableColumn<ReponseOffre, Void> colActions;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceUserDirectory serviceUserDirectory = new ServiceUserDirectory();
    private final EmailNotificationService emailNotificationService = new EmailNotificationService();
    private final ObservableList<ReponseOffre> pageData = FXCollections.observableArrayList();
    private final List<ReponseOffre> allData = new ArrayList<>();
    private final List<ReponseOffre> filteredData = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int currentPage = 0;

    @FXML
    public void initialize() {
        initialiserFiltres();
        initialiserColonnes();
        chargerDonnees();
        tableReponses.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                lblInfo.setText("Reponse #" + selected.getId() + " selectionnee.");
            }
        });
    }

    private void initialiserFiltres() {
        cbStatut.setItems(FXCollections.observableArrayList("Tous", "En attente", "Validee", "Refusee"));
        cbStatut.setValue("Tous");
        cbTri.setItems(FXCollections.observableArrayList("Date soumis", "Quantite", "Statut", "Score"));
        cbTri.setValue("Date soumis");
        cbOrdre.setItems(FXCollections.observableArrayList("Decroissant", "Croissant"));
        cbOrdre.setValue("Decroissant");
        cbPageSize.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
        cbPageSize.setValue(5);
        cbPageSize.valueProperty().addListener((obs, oldValue, newValue) -> {
            currentPage = 0;
            afficherPage();
        });
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);
    }

    private void initialiserColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteProposee"));
        colMessage.setCellValueFactory(cell -> new SimpleStringProperty(blankToEmpty(cell.getValue().getMessage())));
        colDateSoumis.setCellValueFactory(cell -> {
            Timestamp ts = cell.getValue().getDateSoumis();
            return new SimpleStringProperty(ts == null ? "" : formatter.format(ts.toLocalDateTime()));
        });

        colStatut.setCellValueFactory(cell -> new SimpleStringProperty(normaliserStatut(cell.getValue().getStatut())));
        colStatut.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(toDisplayStatut(item));
                badge.setStyle(statutStyle(item));
                setGraphic(badge);
                setText(null);
            }
        });

        colScore.setCellValueFactory(cell -> new SimpleStringProperty(scoreModeration(cell.getValue()) + "/100"));
        colScore.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                int score = Integer.parseInt(item.substring(0, item.indexOf('/')));
                badge.setText(item);
                badge.setStyle(score >= 80
                        ? "-fx-background-color: #D8F3E5; -fx-text-fill: #087044; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 8 5 8;"
                        : "-fx-background-color: #FFF1C7; -fx-text-fill: #9A6B00; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 8 5 8;");
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnValider = new Button("Valider");
            private final Button btnRefuser = new Button("Refuser");
            private final HBox box = new HBox(6, btnVoir, btnModifier, btnValider, btnRefuser);

            {
                btnVoir.setStyle("-fx-background-color: #E9F7EF; -fx-text-fill: #007A47; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 7 13 7 13; -fx-border-color: #BFE3CF; -fx-border-radius: 9;");
                btnModifier.setStyle("-fx-background-color: #E9F7EF; -fx-text-fill: #007A47; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 7 13 7 13; -fx-border-color: #BFE3CF; -fx-border-radius: 9;");
                btnValider.setStyle("-fx-background-color: #1FA466; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 7 13 7 13;");
                btnRefuser.setStyle("-fx-background-color: #D94B4B; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 7 13 7 13;");

                btnVoir.setOnAction(evt -> showDetails(currentRow()));
                btnModifier.setOnAction(evt -> modifier(currentRow()));
                btnValider.setOnAction(evt -> changerStatut(currentRow(), true));
                btnRefuser.setOnAction(evt -> changerStatut(currentRow(), false));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ReponseOffre r = getTableView().getItems().get(getIndex());
                boolean pending = "en attente".equals(normaliserStatut(r.getStatut()));
                btnValider.setVisible(pending);
                btnValider.setManaged(pending);
                btnRefuser.setVisible(pending);
                btnRefuser.setManaged(pending);
                setGraphic(box);
            }

            private ReponseOffre currentRow() {
                return getTableView().getItems().get(getIndex());
            }
        });

        tableReponses.setItems(pageData);
    }

    private void chargerDonnees() {
        try {
            allData.clear();
            allData.addAll(serviceReponseOffre.recupererTout());
            appliquerFiltres(false);
            lblInfo.setText("File de moderation chargee.");
        } catch (Exception e) {
            lblInfo.setText("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void onAppliquerFiltres() {
        appliquerFiltres(true);
    }

    private void appliquerFiltres(boolean showMessage) {
        Double min = parseMinOrNull(txtQuantiteMin.getText());
        if (min == null) {
            txtQuantiteMin.setStyle(STYLE_FILTER_ERROR);
            lblInfo.setText("Quantite min invalide. Entrez un nombre >= 0.");
            return;
        }
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);

        String search = safeLower(txtRecherche.getText());
        String statut = cbStatut.getValue();

        filteredData.clear();
        filteredData.addAll(allData.stream()
                .filter(r -> search.isEmpty()
                        || normaliserStatut(r.getStatut()).contains(search)
                        || blankToEmpty(r.getMessage()).toLowerCase(Locale.ROOT).contains(search))
                .filter(r -> r.getQuantiteProposee() >= min)
                .filter(r -> filterStatut(r, statut))
                .collect(Collectors.toList()));

        sort(filteredData, cbTri.getValue(), cbOrdre.getValue());
        currentPage = 0;
        afficherPage();
        if (showMessage) {
            lblInfo.setText("Filtres appliques.");
        }
    }

    @FXML
    private void onReinitialiserFiltres() {
        txtRecherche.clear();
        txtQuantiteMin.clear();
        cbStatut.setValue("Tous");
        cbTri.setValue("Date soumis");
        cbOrdre.setValue("Decroissant");
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);
        appliquerFiltres(false);
        lblInfo.setText("Filtres reinitialises.");
    }

    @FXML
    private void onPagePrecedente() {
        if (currentPage > 0) {
            currentPage--;
            afficherPage();
        }
    }

    @FXML
    private void onPageSuivante() {
        if (currentPage + 1 < totalPages()) {
            currentPage++;
            afficherPage();
        }
    }

    @FXML
    private void onExporterPdf() {
        exporter("PDF", ".pdf", file -> ExportDocumentService.exportPdf(exportTitle(), exportHeaders(), exportRows(), file));
    }

    @FXML
    private void onExporterExcel() {
        exporter("Excel", ".xlsx", file -> ExportDocumentService.exportXlsx(exportTitle(), exportHeaders(), exportRows(), file));
    }

    @FXML
    private void onExporterWord() {
        exporter("Word", ".docx", file -> ExportDocumentService.exportDocx(exportTitle(), exportHeaders(), exportRows(), file));
    }

    @FXML
    private void onOpenAdminDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_DASHBOARD, AppRoutes.TITLE_ADMIN_DASHBOARD);
    }

    @FXML
    private void onOpenStats(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.ADMIN_REPONSE_STATS, AppRoutes.TITLE_ADMIN_STATS);
    }

    @FXML
    private void onOpenHome(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.DASHBOARD, AppRoutes.TITLE_DASHBOARD);
    }

    @FXML
    private void onOpenAppels(ActionEvent event) {
        ViewNavigator.navigate(event, AppRoutes.APPEL_OFFRE_LIST, AppRoutes.TITLE_APPELS);
    }

    @FXML
    private void onOpenReponses() {
        chargerDonnees();
        lblInfo.setText("File de moderation rafraichie.");
    }

    private void afficherPage() {
        int totalPages = totalPages();
        int pageSize = pageSize();
        int start = Math.min(currentPage * pageSize, filteredData.size());
        int end = Math.min(start + pageSize, filteredData.size());

        pageData.setAll(filteredData.subList(start, end));
        lblCount.setText(filteredData.size() + " ligne(s)");
        lblPage.setText("Page " + (filteredData.isEmpty() ? 0 : currentPage + 1) + " / " + totalPages
                + " (" + filteredData.size() + " lignes)");
        btnPrecedent.setDisable(currentPage <= 0);
        btnSuivant.setDisable(currentPage + 1 >= totalPages);
    }

    private int totalPages() {
        if (filteredData.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil(filteredData.size() / (double) pageSize());
    }

    private int pageSize() {
        Integer value = cbPageSize.getValue();
        return value == null || value <= 0 ? 5 : value;
    }

    private void showDetails(ReponseOffre r) {
        if (r == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details reponse");
        alert.setHeaderText("Reponse #" + r.getId() + " - " + toDisplayStatut(normaliserStatut(r.getStatut())));
        alert.setContentText(
                "Quantite: " + r.getQuantiteProposee() + " kg"
                        + "\nDate soumis: " + formatDate(r.getDateSoumis())
                        + "\nMessage: " + blankToEmpty(r.getMessage())
                        + "\nScore moderation: " + scoreModeration(r) + "/100"
                        + "\nAppel ID: " + r.getAppelOffreId()
                        + "\nCitoyen ID: " + r.getCitoyenId()
        );
        alert.showAndWait();
    }

    private void modifier(ReponseOffre r) {
        if (r == null) {
            return;
        }
        ReponseOffreFlowState.setSelectedReponseId(r.getId());
        ViewNavigator.navigate(tableReponses, AppRoutes.REPONSE_OFFRE_EDIT, AppRoutes.TITLE_REPONSE_EDIT);
    }

    private void changerStatut(ReponseOffre r, boolean valider) {
        if (r == null) {
            return;
        }
        try {
            String resultMessage;
            if (valider) {
                serviceReponseOffre.accepterReponse(r.getId());
                resultMessage = envoyerMailValidation(r.getId());
            } else {
                serviceReponseOffre.refuserReponse(r.getId());
                resultMessage = "Reponse #" + r.getId() + " refusee.";
            }
            chargerDonnees();
            lblInfo.setText(resultMessage);
        } catch (Exception e) {
            lblInfo.setText("Erreur moderation: " + e.getMessage());
        }
    }

    private String envoyerMailValidation(int reponseId) {
        try {
            ReponseOffre reponse = serviceReponseOffre.recupererParId(reponseId);
            if (reponse == null) {
                return "Reponse validee, mail non envoye: reponse introuvable.";
            }
            AppelOffre appelOffre = serviceAppelOffre.recupererParId(reponse.getAppelOffreId());
            UserContact citoyen = serviceUserDirectory.recupererContactCitoyen(reponse.getCitoyenId());
            emailNotificationService.envoyerValidationReponse(citoyen, appelOffre, reponse);
            return "Reponse #" + reponseId + " validee. Email envoye a " + citoyen.getEmail() + ".";
        } catch (Exception e) {
            String message = "Reponse #" + reponseId + " validee, mais email non envoye: " + e.getMessage();
            showWarning("Email non envoye", message);
            return message;
        }
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Notification email");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean filterStatut(ReponseOffre r, String statutFilter) {
        if (statutFilter == null || "Tous".equals(statutFilter)) {
            return true;
        }
        return normaliserStatut(r.getStatut()).equals(normaliserStatut(statutFilter));
    }

    private void sort(List<ReponseOffre> list, String tri, String ordre) {
        Comparator<ReponseOffre> comparator;
        if ("Quantite".equals(tri)) {
            comparator = Comparator.comparingDouble(ReponseOffre::getQuantiteProposee);
        } else if ("Statut".equals(tri)) {
            comparator = Comparator.comparing(r -> normaliserStatut(r.getStatut()));
        } else if ("Score".equals(tri)) {
            comparator = Comparator.comparingInt(this::scoreModeration);
        } else {
            comparator = Comparator.comparing(ReponseOffre::getDateSoumis, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if (!"Croissant".equals(ordre)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);
    }

    private int scoreModeration(ReponseOffre r) {
        int score = 92;
        String message = blankToEmpty(r.getMessage());
        if (message.isBlank()) {
            score -= 18;
        }
        if (r.getQuantiteProposee() < 50) {
            score -= 12;
        }
        if ("refuse".equals(normaliserStatut(r.getStatut()))) {
            score -= 20;
        }
        if (r.getDateSoumis() != null) {
            long ageDays = Duration.between(r.getDateSoumis().toLocalDateTime(), LocalDateTime.now()).toDays();
            if (ageDays > 7) {
                score -= 5;
            }
        }
        return Math.max(0, Math.min(100, score));
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

    private String toDisplayStatut(String statutNormalise) {
        if ("valide".equals(statutNormalise)) {
            return "valide";
        }
        if ("refuse".equals(statutNormalise)) {
            return "refuse";
        }
        return "en attente";
    }

    private String statutStyle(String statut) {
        if ("valide".equals(statut)) {
            return "-fx-background-color: #D8F3E5; -fx-text-fill: #087044; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 8 5 8;";
        }
        if ("refuse".equals(statut)) {
            return "-fx-background-color: #FBE4E4; -fx-text-fill: #C0392B; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 8 5 8;";
        }
        return "-fx-background-color: #FFF1C7; -fx-text-fill: #9A6B00; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 8 5 8;";
    }

    private String formatDate(Timestamp ts) {
        return ts == null ? "" : formatter.format(ts.toLocalDateTime());
    }

    private void exporter(String type, String extension, ExportAction action) {
        if (filteredData.isEmpty()) {
            lblInfo.setText("Aucune reponse a exporter avec les filtres actuels.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les reponses - " + type);
        chooser.setInitialFileName("moderation_reponses_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + extension);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(type + " (*" + extension + ")", "*" + extension));

        File selected = chooser.showSaveDialog(tableReponses.getScene() == null ? null : tableReponses.getScene().getWindow());
        if (selected == null) {
            lblInfo.setText("Export " + type + " annule.");
            return;
        }

        File target = ensureExtension(selected, extension);
        try {
            action.export(target);
            lblInfo.setText("Export " + type + " genere: " + target.getAbsolutePath());
        } catch (Exception e) {
            lblInfo.setText("Erreur export " + type + ": " + e.getMessage());
        }
    }

    private String exportTitle() {
        return "Moderation des reponses d'offre";
    }

    private List<String> exportHeaders() {
        return List.of("Id", "Quantite", "Date soumis", "Statut", "Message", "Score", "Appel", "Citoyen");
    }

    private List<List<String>> exportRows() {
        return filteredData.stream()
                .map(r -> List.of(
                        String.valueOf(r.getId()),
                        String.format(Locale.ROOT, "%.2f kg", r.getQuantiteProposee()),
                        formatDate(r.getDateSoumis()),
                        toDisplayStatut(normaliserStatut(r.getStatut())),
                        blankToEmpty(r.getMessage()),
                        scoreModeration(r) + "/100",
                        "#" + r.getAppelOffreId(),
                        "#" + r.getCitoyenId()
                ))
                .collect(Collectors.toList());
    }

    private File ensureExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            return file;
        }
        return new File(path + extension);
    }

    @FunctionalInterface
    private interface ExportAction {
        void export(File file) throws IOException;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Double parseMinOrNull(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return 0d;
        }
        try {
            return Math.max(0d, Double.parseDouble(text));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
