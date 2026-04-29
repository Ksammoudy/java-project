package controllers.admin;

import controllers.reponseoffre.ReponseOffreFlowState;
import entities.ReponseOffre;
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
import main.navigation.ViewNavigator;
import services.ServiceReponseOffre;

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
        lblInfo.setText("Export PDF prevu dans la prochaine etape des fonctionnalites avancees.");
    }

    @FXML
    private void onExporterExcel() {
        lblInfo.setText("Export Excel prevu dans la prochaine etape des fonctionnalites avancees.");
    }

    @FXML
    private void onExporterWord() {
        lblInfo.setText("Export Word prevu dans la prochaine etape des fonctionnalites avancees.");
    }

    @FXML
    private void onOpenAdminDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/admin/AdminDashboard.fxml", "WasteWise - Back Office");
    }

    @FXML
    private void onOpenStats(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/admin/AdminDashboard.fxml", "WasteWise - Statistiques admin");
    }

    @FXML
    private void onOpenHome(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
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
        ViewNavigator.navigate(tableReponses, "/fxml/reponseoffre/ReponseOffreEdit.fxml", "WasteWise - Modifier la reponse d'offre");
    }

    private void changerStatut(ReponseOffre r, boolean valider) {
        if (r == null) {
            return;
        }
        try {
            if (valider) {
                serviceReponseOffre.accepterReponse(r.getId());
                lblInfo.setText("Reponse #" + r.getId() + " validee.");
            } else {
                serviceReponseOffre.refuserReponse(r.getId());
                lblInfo.setText("Reponse #" + r.getId() + " refusee.");
            }
            chargerDonnees();
        } catch (Exception e) {
            lblInfo.setText("Erreur moderation: " + e.getMessage());
        }
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
