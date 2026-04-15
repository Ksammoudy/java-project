package controllers.reponseoffre;

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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReponseOffreListController {

    private static final String STYLE_FILTER_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10;";
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
    private Label lblInfo;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblTotalReponses;
    @FXML
    private Label lblTotalEnAttente;
    @FXML
    private Label lblTotalValides;
    @FXML
    private Label lblTotalRefusees;

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
    private TableColumn<ReponseOffre, Void> colActions;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ObservableList<ReponseOffre> data = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        initialiserFiltres();
        initialiserColonnes();
        chargerDonnees();
        initialiserSelectionInfo();

        String flash = ReponseOffreFlowState.consumeFlashMessage();
        if (flash != null && !flash.isBlank()) {
            lblInfo.setText(flash);
        }
    }

    private void initialiserFiltres() {
        cbStatut.setItems(FXCollections.observableArrayList("Tous", "En attente", "Valide", "Refuse"));
        cbStatut.setValue("Tous");

        cbTri.setItems(FXCollections.observableArrayList("Date soumis", "Quantite", "Statut"));
        cbTri.setValue("Date soumis");

        cbOrdre.setItems(FXCollections.observableArrayList("Decroissant", "Croissant"));
        cbOrdre.setValue("Decroissant");
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);
    }

    private void initialiserColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteProposee"));
        colMessage.setCellValueFactory(cell -> {
            String msg = cell.getValue().getMessage();
            return new SimpleStringProperty(msg == null ? "" : msg);
        });

        colDateSoumis.setCellValueFactory(cell -> {
            Timestamp ts = cell.getValue().getDateSoumis();
            String value = ts == null ? "" : formatter.format(ts.toLocalDateTime());
            return new SimpleStringProperty(value);
        });

        colStatut.setCellValueFactory(cell -> new SimpleStringProperty(normaliserStatut(cell.getValue().getStatut())));
        colStatut.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String s = normaliserStatut(item);
                badge.setText(s);
                if ("valide".equals(s)) {
                    badge.setStyle("-fx-background-color: #D8F3E5; -fx-text-fill: #107C41; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 10 4 10;");
                } else if ("refuse".equals(s)) {
                    badge.setStyle("-fx-background-color: #FBE4E4; -fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 10 4 10;");
                } else {
                    badge.setStyle("-fx-background-color: #FDEFC8; -fx-text-fill: #9A6B00; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 4 10 4 10;");
                }
                setText(null);
                setGraphic(badge);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final HBox box = new HBox(6, btnVoir, btnModifier);

            {
                btnVoir.setStyle("-fx-background-color: #E0ECFF; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-background-radius: 8;");
                btnModifier.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-background-radius: 8;");

                btnVoir.setOnAction(evt -> {
                    ReponseOffre r = getTableView().getItems().get(getIndex());
                    showDetails(r);
                });

                btnModifier.setOnAction(evt -> {
                    ReponseOffre r = getTableView().getItems().get(getIndex());
                    ReponseOffreFlowState.setSelectedReponseId(r.getId());
                    ViewNavigator.navigate(btnModifier, "/fxml/reponseoffre/ReponseOffreEdit.fxml", "WasteWise - Modifier la reponse d'offre");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableReponses.setItems(data);
    }

    private void showDetails(ReponseOffre r) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details Reponse");
        alert.setHeaderText("Reponse #" + r.getId());
        alert.setContentText(
                "Quantite: " + r.getQuantiteProposee() + " kg"
                        + "\nDate soumis: " + (r.getDateSoumis() == null ? "" : formatter.format(r.getDateSoumis().toLocalDateTime()))
                        + "\nStatut: " + normaliserStatut(r.getStatut())
                        + "\nMessage: " + (r.getMessage() == null ? "" : r.getMessage())
                        + "\nAppel ID: " + r.getAppelOffreId()
                        + "\nCitoyen ID: " + r.getCitoyenId()
        );
        alert.showAndWait();
    }

    private void chargerDonnees() {
        try {
            List<ReponseOffre> list = serviceReponseOffre.recupererTout();
            data.setAll(list);
            updateCountAndSummary();
        } catch (Exception e) {
            lblInfo.setText("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void onAppliquerFiltres() {
        try {
            List<ReponseOffre> source = new ArrayList<>(serviceReponseOffre.recupererTout());
            String search = safeLower(txtRecherche.getText());
            Double min = parseMinOrNull(txtQuantiteMin.getText());
            if (min == null) {
                txtQuantiteMin.setStyle(STYLE_FILTER_ERROR);
                lblInfo.setText("Quantite min invalide. Entrez un nombre >= 0.");
                return;
            }
            txtQuantiteMin.setStyle(STYLE_FILTER_OK);
            String statut = cbStatut.getValue();

            List<ReponseOffre> filtered = source.stream()
                    .filter(r -> search.isEmpty()
                            || normaliserStatut(r.getStatut()).contains(search)
                            || (r.getMessage() != null && r.getMessage().toLowerCase(Locale.ROOT).contains(search)))
                    .filter(r -> r.getQuantiteProposee() >= min)
                    .filter(r -> filterStatut(r, statut))
                    .collect(Collectors.toList());

            sort(filtered, cbTri.getValue(), cbOrdre.getValue());
            data.setAll(filtered);
            updateCountAndSummary();
            lblInfo.setText("Filtres appliques.");
        } catch (Exception e) {
            lblInfo.setText("Erreur filtre: " + e.getMessage());
        }
    }

    @FXML
    private void onReinitialiserFiltres() {
        txtRecherche.clear();
        txtQuantiteMin.clear();
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);
        cbStatut.setValue("Tous");
        cbTri.setValue("Date soumis");
        cbOrdre.setValue("Decroissant");
        chargerDonnees();
        lblInfo.setText("Filtres reinitialises.");
    }

    @FXML
    private void onNouveau(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreCreate.fxml", "WasteWise - Creer une reponse d'offre");
    }

    @FXML
    private void onModifierSelection(ActionEvent event) {
        ReponseOffre selected = tableReponses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblInfo.setText("Selectionnez une reponse a modifier.");
            return;
        }
        ReponseOffreFlowState.setSelectedReponseId(selected.getId());
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreEdit.fxml", "WasteWise - Modifier la reponse d'offre");
    }

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
    }

    @FXML
    private void onOpenAppelOffre(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
    }

    @FXML
    private void onOpenReponseCreate(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreCreate.fxml", "WasteWise - Creer une reponse d'offre");
    }

    @FXML
    private void onOpenReponseList(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
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
        } else {
            comparator = Comparator.comparing(ReponseOffre::getDateSoumis, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if (!"Croissant".equals(ordre)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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

    private void updateCountAndSummary() {
        int total = data.size();
        long attente = data.stream().filter(r -> "en attente".equals(normaliserStatut(r.getStatut()))).count();
        long valides = data.stream().filter(r -> "valide".equals(normaliserStatut(r.getStatut()))).count();
        long refusees = data.stream().filter(r -> "refuse".equals(normaliserStatut(r.getStatut()))).count();

        lblCount.setText(total + " reponse(s)");
        if (lblTotalReponses != null) {
            lblTotalReponses.setText(String.valueOf(total));
        }
        if (lblTotalEnAttente != null) {
            lblTotalEnAttente.setText(String.valueOf(attente));
        }
        if (lblTotalValides != null) {
            lblTotalValides.setText(String.valueOf(valides));
        }
        if (lblTotalRefusees != null) {
            lblTotalRefusees.setText(String.valueOf(refusees));
        }
    }

    private void initialiserSelectionInfo() {
        tableReponses.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            lblInfo.setText("Reponse #" + selected.getId() + " selectionnee. Vous pouvez cliquer sur Modifier.");
        });
    }
}
