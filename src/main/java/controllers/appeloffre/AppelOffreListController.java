package controllers.appeloffre;

import entities.AppelOffre;
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
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import utils.ExportDocumentService;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AppelOffreListController {

    private static final String STYLE_FILTER_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10;";
    private static final String STYLE_FILTER_ERROR =
            "-fx-background-color: #FFF6F6; -fx-border-color: #DC2626; -fx-border-radius: 10; -fx-background-radius: 10;";

    @FXML
    private TextField txtRecherche;
    @FXML
    private TextField txtQuantiteMin;
    @FXML
    private ComboBox<String> cbEtat;
    @FXML
    private ComboBox<String> cbTri;
    @FXML
    private ComboBox<String> cbOrdre;
    @FXML
    private Label lblInfo;
    @FXML
    private Label lblCount;
    @FXML
    private Label lblTotalAppels;
    @FXML
    private Label lblTotalActifs;
    @FXML
    private Label lblTotalExpires;
    @FXML
    private Label lblTauxActifs;
    @FXML
    private Label lblQuantiteTotale;

    @FXML
    private TableView<AppelOffre> tableAppelOffres;
    @FXML
    private TableColumn<AppelOffre, Integer> colId;
    @FXML
    private TableColumn<AppelOffre, String> colTitre;
    @FXML
    private TableColumn<AppelOffre, String> colDescription;
    @FXML
    private TableColumn<AppelOffre, Double> colQuantite;
    @FXML
    private TableColumn<AppelOffre, String> colDateLimite;
    @FXML
    private TableColumn<AppelOffre, String> colEtat;
    @FXML
    private TableColumn<AppelOffre, Void> colActions;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ObservableList<AppelOffre> data = FXCollections.observableArrayList();
    private final List<AppelOffre> filteredData = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        initialiserFiltres();
        initialiserColonnes();
        chargerDonnees();
        initialiserSelectionInfo();

        String flash = AppelOffreFlowState.consumeFlashMessage();
        if (flash != null && !flash.isBlank()) {
            lblInfo.setText(flash);
        }
    }

    private void initialiserFiltres() {
        cbEtat.setItems(FXCollections.observableArrayList("Tous", "Actifs", "Expires"));
        cbEtat.setValue("Tous");

        cbTri.setItems(FXCollections.observableArrayList("Date limite", "Titre", "Quantite"));
        cbTri.setValue("Date limite");

        cbOrdre.setItems(FXCollections.observableArrayList("Decroissant", "Croissant"));
        cbOrdre.setValue("Decroissant");
        txtQuantiteMin.setStyle(STYLE_FILTER_OK);
    }

    private void initialiserColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteDemandee"));

        colDateLimite.setCellValueFactory(cell -> {
            Timestamp ts = cell.getValue().getDateLimite();
            String value = ts == null ? "" : formatter.format(ts.toLocalDateTime());
            return new SimpleStringProperty(value);
        });

        colEtat.setCellValueFactory(cell -> {
            AppelOffre a = cell.getValue();
            boolean active = a.getDateLimite() != null && a.getDateLimite().after(new Timestamp(System.currentTimeMillis()));
            return new SimpleStringProperty(active ? "Active" : "Expire");
        });

        colEtat.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean isActive = "Active".equalsIgnoreCase(item.trim());
                badge.setText(isActive ? "Active" : "Expire");
                badge.setStyle(isActive
                        ? "-fx-background-color: #D8F3E5; -fx-text-fill: #107C41; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 11 5 11;"
                        : "-fx-background-color: #FBE4E4; -fx-text-fill: #C0392B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 11 5 11;");

                setText(null);
                setGraphic(badge);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnModifier = new Button("Modifier");
            private final HBox container = new HBox(6, btnVoir, btnModifier);

            {
                btnVoir.setStyle("-fx-background-color: #E8F0FF; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 6 12 6 12;");
                btnModifier.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E3A8A; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 6 12 6 12;");

                btnVoir.setOnAction(evt -> {
                    AppelOffre a = getTableView().getItems().get(getIndex());
                    showDetails(a);
                });

                btnModifier.setOnAction(evt -> {
                    AppelOffre a = getTableView().getItems().get(getIndex());
                    AppelOffreFlowState.setSelectedAppelId(a.getId());
                    ViewNavigator.navigate(btnModifier, "/fxml/appeloffre/AppelOffreEdit.fxml", "WasteWise - Modifier l'appel d'offre");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tableAppelOffres.setItems(data);
    }

    private void showDetails(AppelOffre a) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details Appel d'offre");
        alert.setHeaderText("Appel #" + a.getId() + " - " + a.getTitre());
        alert.setContentText("Description: " + a.getDescription()
                + "\nQuantite: " + a.getQuantiteDemandee() + " kg"
                + "\nDate limite: " + formatter.format(a.getDateLimite().toLocalDateTime())
                + "\nValorisateur ID: " + a.getValorisateurId());
        alert.showAndWait();
    }

    private void chargerDonnees() {
        try {
            List<AppelOffre> list = serviceAppelOffre.recupererTout();
            filteredData.clear();
            filteredData.addAll(list);
            data.setAll(filteredData);
            updateCount();
        } catch (Exception e) {
            lblInfo.setText("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void onAppliquerFiltres() {
        try {
            List<AppelOffre> source = new ArrayList<>(serviceAppelOffre.recupererTout());
            String search = safeLower(txtRecherche.getText());
            Double min = parseMinOrNull(txtQuantiteMin.getText());
            if (min == null) {
                txtQuantiteMin.setStyle(STYLE_FILTER_ERROR);
                lblInfo.setText("Quantite min invalide. Entrez un nombre >= 0.");
                return;
            }
            txtQuantiteMin.setStyle(STYLE_FILTER_OK);
            String etat = cbEtat.getValue();

            List<AppelOffre> filtered = source.stream()
                    .filter(a -> search.isEmpty()
                            || a.getTitre().toLowerCase(Locale.ROOT).contains(search)
                            || a.getDescription().toLowerCase(Locale.ROOT).contains(search))
                    .filter(a -> a.getQuantiteDemandee() >= min)
                    .filter(a -> filterEtat(a, etat))
                    .collect(Collectors.toList());

            sort(filtered, cbTri.getValue(), cbOrdre.getValue());
            filteredData.clear();
            filteredData.addAll(filtered);
            data.setAll(filteredData);
            updateCount();
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
        cbEtat.setValue("Tous");
        cbTri.setValue("Date limite");
        cbOrdre.setValue("Decroissant");
        chargerDonnees();
        lblInfo.setText("Filtres reinitialises.");
    }

    @FXML
    private void onNouveau(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreCreate.fxml", "WasteWise - Creer un appel d'offre");
    }

    @FXML
    private void onModifierSelection(ActionEvent event) {
        AppelOffre selected = tableAppelOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblInfo.setText("Selectionnez un appel a modifier.");
            return;
        }

        AppelOffreFlowState.setSelectedAppelId(selected.getId());
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreEdit.fxml", "WasteWise - Modifier l'appel d'offre");
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
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
    }

    @FXML
    private void onOpenList(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
    }

    @FXML
    private void onOpenCreate(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreCreate.fxml", "WasteWise - Creer un appel d'offre");
    }

    private boolean filterEtat(AppelOffre a, String etat) {
        if (etat == null || etat.equals("Tous")) {
            return true;
        }
        boolean active = a.getDateLimite() != null && a.getDateLimite().after(new Timestamp(System.currentTimeMillis()));
        return (etat.equals("Actifs") && active) || (etat.equals("Expires") && !active);
    }

    private void sort(List<AppelOffre> list, String tri, String ordre) {
        Comparator<AppelOffre> comparator;
        if ("Titre".equals(tri)) {
            comparator = Comparator.comparing(AppelOffre::getTitre, String.CASE_INSENSITIVE_ORDER);
        } else if ("Quantite".equals(tri)) {
            comparator = Comparator.comparingDouble(AppelOffre::getQuantiteDemandee);
        } else {
            comparator = Comparator.comparing(AppelOffre::getDateLimite);
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

    private void updateCount() {
        int total = data.size();
        long actifs = data.stream()
                .filter(a -> a != null && a.getDateLimite() != null && a.getDateLimite().after(new Timestamp(System.currentTimeMillis())))
                .count();
        long expires = total - actifs;
        double quantiteTotale = data.stream().mapToDouble(AppelOffre::getQuantiteDemandee).sum();
        double tauxActifs = total == 0 ? 0d : (actifs * 100d) / total;

        lblCount.setText(total + " appel(s)");
        if (lblTotalAppels != null) {
            lblTotalAppels.setText(String.valueOf(total));
        }
        if (lblTotalActifs != null) {
            lblTotalActifs.setText(String.valueOf(actifs));
        }
        if (lblTotalExpires != null) {
            lblTotalExpires.setText(String.valueOf(expires));
        }
        if (lblTauxActifs != null) {
            lblTauxActifs.setText(String.format(Locale.ROOT, "%.1f%%", tauxActifs));
        }
        if (lblQuantiteTotale != null) {
            lblQuantiteTotale.setText(String.format(Locale.ROOT, "%.1f kg", quantiteTotale));
        }
    }

    private void exporter(String type, String extension, ExportAction action) {
        if (filteredData.isEmpty()) {
            lblInfo.setText("Aucun appel a exporter avec les filtres actuels.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les appels - " + type);
        chooser.setInitialFileName("appels_offre_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + extension);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(type + " (*" + extension + ")", "*" + extension));

        File selected = chooser.showSaveDialog(tableAppelOffres.getScene() == null ? null : tableAppelOffres.getScene().getWindow());
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
        return "Gestion des appels d'offre";
    }

    private List<String> exportHeaders() {
        return List.of("Id", "Titre", "Description", "Quantite", "Date limite", "Etat", "Valorisateur", "Reponses");
    }

    private List<List<String>> exportRows() {
        return filteredData.stream()
                .map(a -> List.of(
                        String.valueOf(a.getId()),
                        safeExport(a.getTitre()),
                        safeExport(a.getDescription()),
                        String.format(Locale.ROOT, "%.2f kg", a.getQuantiteDemandee()),
                        a.getDateLimite() == null ? "" : formatter.format(a.getDateLimite().toLocalDateTime()),
                        isActive(a) ? "Actif" : "Expire",
                        "#" + a.getValorisateurId(),
                        compterReponsesSafely(a)
                ))
                .collect(Collectors.toList());
    }

    private String compterReponsesSafely(AppelOffre appelOffre) {
        try {
            return String.valueOf(serviceAppelOffre.compterReponsesLiees(appelOffre.getId()));
        } catch (Exception e) {
            return "-";
        }
    }

    private boolean isActive(AppelOffre appelOffre) {
        return appelOffre.getDateLimite() != null && appelOffre.getDateLimite().after(new Timestamp(System.currentTimeMillis()));
    }

    private File ensureExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if (path.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            return file;
        }
        return new File(path + extension);
    }

    private String safeExport(String value) {
        return value == null ? "" : value;
    }

    @FunctionalInterface
    private interface ExportAction {
        void export(File file) throws IOException;
    }

    private void initialiserSelectionInfo() {
        tableAppelOffres.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            if (selected == null) {
                return;
            }
            lblInfo.setText("Appel #" + selected.getId() + " selectionne. Vous pouvez cliquer sur Modifier.");
        });
    }
}
