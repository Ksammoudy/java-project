package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.example.Main;
import org.example.models.ZonePolluee;
import org.example.models.IndicateurImpact;
import org.example.services.ZonePollueeDAO;
import org.example.services.IndicateurImpactDAO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ZonePollueeController {

    @FXML private TableView<ZonePolluee> zoneTable;
    @FXML private TableColumn<ZonePolluee, Integer> colId;
    @FXML private TableColumn<ZonePolluee, String> colNom;
    @FXML private TableColumn<ZonePolluee, String> colGps;
    @FXML private TableColumn<ZonePolluee, Integer> colNiveau;
    @FXML private TableColumn<ZonePolluee, String> colDate;
    @FXML private TableColumn<ZonePolluee, String> colIndicateur;
    @FXML private TableColumn<ZonePolluee, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Text totalZonesText;
    @FXML private Text hautRisqueText;
    @FXML private Text nouvellesZonesText;

    private ZonePollueeDAO dao = new ZonePollueeDAO();
    private IndicateurImpactDAO indicateurDAO = new IndicateurImpactDAO();
    private ObservableList<ZonePolluee> zoneList = FXCollections.observableArrayList();
    private ZonePolluee currentZone = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomZone"));
        colGps.setCellValueFactory(new PropertyValueFactory<>("coordonneesGps"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveauPollution"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateIdentification().toString()));
        colIndicateur.setCellValueFactory(cellData -> {
            IndicateurImpact ind = cellData.getValue().getIndicateur();
            return new SimpleStringProperty(ind != null ? "ID=" + ind.getId() : "Aucun");
        });

        // Colonne Actions avec boutons
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button qrBtn = new Button("📱");

            {
                viewBtn.setOnAction(e -> viewZone(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> editZone(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteZone(getTableView().getItems().get(getIndex())));
                qrBtn.setOnAction(e -> generateQR(getTableView().getItems().get(getIndex())));
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #ffc107;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #dc3545;");
                qrBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #198754;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, viewBtn, editBtn, deleteBtn, qrBtn));
                }
            }
        });

        // Style des niveaux
        colNiveau.setCellFactory(column -> new TableCell<ZonePolluee, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + "/10");
                    if (item >= 7) setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20;");
                    else if (item >= 4) setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-padding: 4 10; -fx-background-radius: 20;");
                    else setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20;");
                }
            }
        });

        filterCombo.getItems().addAll("Toutes", "Haut risque (≥7)", "Risque moyen (4-6)", "Risque faible (≤3)");
        filterCombo.setValue("Toutes");

        loadZones();

        searchField.textProperty().addListener((obs, old, newVal) -> loadZones());
        filterCombo.valueProperty().addListener((obs, old, newVal) -> loadZones());
    }

    @FXML
    private void loadZones() {
        List<ZonePolluee> zones;
        String search = searchField.getText();
        String filter = filterCombo.getValue();

        if (search != null && !search.isEmpty()) {
            zones = dao.searchByNom(search);
        } else if ("Haut risque (≥7)".equals(filter)) {
            zones = dao.getAllZones().stream().filter(z -> z.getNiveauPollution() >= 7).toList();
        } else if ("Risque moyen (4-6)".equals(filter)) {
            zones = dao.getAllZones().stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() <= 6).toList();
        } else if ("Risque faible (≤3)".equals(filter)) {
            zones = dao.getAllZones().stream().filter(z -> z.getNiveauPollution() <= 3).toList();
        } else {
            zones = dao.getAllZones();
        }

        zoneList.setAll(zones);
        zoneTable.setItems(zoneList);
        updateStats(zones);
    }

    private void updateStats(List<ZonePolluee> zones) {
        totalZonesText.setText(String.valueOf(zones.size()));
        long hautRisque = zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        long nouvelles = zones.stream().filter(z -> z.getDateIdentification().isAfter(LocalDateTime.now().minusDays(7))).count();
        hautRisqueText.setText(String.valueOf(hautRisque));
        nouvellesZonesText.setText(String.valueOf(nouvelles));
    }

    @FXML
    private void addZone() {
        showFormDialog(null);
    }

    private void editZone(ZonePolluee zone) {
        showFormDialog(zone);
    }

    private void viewZone(ZonePolluee zone) {
        showAlert("Détails", zone.getNomZone() + "\nNiveau: " + zone.getNiveauPollution() + "/10\nGPS: " + zone.getCoordonneesGps(), Alert.AlertType.INFORMATION);
    }

    private void deleteZone(ZonePolluee zone) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la zone ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + zone.getNomZone() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.deleteZone(zone.getId());
            loadZones();
            showAlert("Succès", "Zone supprimée avec succès.", Alert.AlertType.INFORMATION);
        }
    }

    private void generateQR(ZonePolluee zone) {
        showAlert("QR Code", "Génération du QR code pour " + zone.getNomZone(), Alert.AlertType.INFORMATION);
    }

    private void showFormDialog(ZonePolluee zone) {
        Dialog<ZonePolluee> dialog = new Dialog<>();
        dialog.setTitle(zone == null ? "Ajouter une zone" : "Modifier une zone");
        dialog.setHeaderText(zone == null ? "Nouvelle zone polluée" : "Modifier la zone");

        // Création du formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom de la zone");
        TextField gpsField = new TextField();
        gpsField.setPromptText("Coordonnées GPS (ex: 36.8065,10.1815)");
        TextField niveauField = new TextField();
        niveauField.setPromptText("Niveau (1-10)");
        ComboBox<IndicateurImpact> indicateurCombo = new ComboBox<>();
        indicateurCombo.setPromptText("Sélectionner un indicateur");
        indicateurCombo.getItems().addAll(indicateurDAO.getAllIndicateurs());
        indicateurCombo.setCellFactory(lv -> new ListCell<IndicateurImpact>() {
            @Override
            protected void updateItem(IndicateurImpact ind, boolean empty) {
                super.updateItem(ind, empty);
                setText(empty || ind == null ? null : ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg");
            }
        });
        indicateurCombo.setButtonCell(new ListCell<IndicateurImpact>() {
            @Override
            protected void updateItem(IndicateurImpact ind, boolean empty) {
                super.updateItem(ind, empty);
                setText(empty || ind == null ? null : ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg");
            }
        });

        if (zone != null) {
            nomField.setText(zone.getNomZone());
            gpsField.setText(zone.getCoordonneesGps());
            niveauField.setText(String.valueOf(zone.getNiveauPollution()));
            indicateurCombo.setValue(zone.getIndicateur());
        }

        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("GPS :"), 0, 1);
        grid.add(gpsField, 1, 1);
        grid.add(new Label("Niveau (1-10) :"), 0, 2);
        grid.add(niveauField, 1, 2);
        grid.add(new Label("Indicateur :"), 0, 3);
        grid.add(indicateurCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    String nom = nomField.getText();
                    String gps = gpsField.getText();
                    int niveau = Integer.parseInt(niveauField.getText());
                    IndicateurImpact indicateur = indicateurCombo.getValue();

                    if (nom == null || nom.trim().isEmpty()) {
                        showAlert("Erreur", "Le nom de la zone est obligatoire.", Alert.AlertType.ERROR);
                        return null;
                    }
                    if (niveau < 1 || niveau > 10) {
                        showAlert("Erreur", "Le niveau doit être compris entre 1 et 10.", Alert.AlertType.ERROR);
                        return null;
                    }
                    if (indicateur == null) {
                        showAlert("Erreur", "Veuillez sélectionner un indicateur.", Alert.AlertType.ERROR);
                        return null;
                    }

                    ZonePolluee newZone = new ZonePolluee(nom, gps, niveau, LocalDateTime.now(), indicateur);
                    if (zone != null) {
                        newZone.setId(zone.getId());
                    }
                    return newZone;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez saisir un niveau valide.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<ZonePolluee> result = dialog.showAndWait();
        result.ifPresent(z -> {
            if (z.getId() == 0) {
                dao.addZone(z);
                showAlert("Succès", "Zone ajoutée avec succès.", Alert.AlertType.INFORMATION);
            } else {
                dao.updateZone(z);
                showAlert("Succès", "Zone modifiée avec succès.", Alert.AlertType.INFORMATION);
            }
            loadZones();
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    private void goToIndicateurs() {
        Main.showIndicateurImpactListPage();
    }

    @FXML
    private void goToMap() {
        showAlert("Info", "Page de la carte en construction", Alert.AlertType.INFORMATION);
    }

}