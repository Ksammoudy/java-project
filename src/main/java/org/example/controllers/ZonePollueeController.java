package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
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
import java.time.format.DateTimeFormatter;
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

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomZone"));
        colGps.setCellValueFactory(new PropertyValueFactory<>("coordonneesGps"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveauPollution"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateIdentification().format(dateFormatter)));
        colIndicateur.setCellValueFactory(cellData -> {
            IndicateurImpact ind = cellData.getValue().getIndicateur();
            return new SimpleStringProperty(ind != null ? "ID=" + ind.getId() : "Aucun");
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("🔍");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                viewBtn.setOnAction(e -> viewZone(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> editZone(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteZone(getTableView().getItems().get(getIndex())));

                viewBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 30px; -fx-cursor: hand; -fx-background-radius: 5;");
                editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-size: 12px; -fx-min-width: 30px; -fx-cursor: hand; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 30px; -fx-cursor: hand; -fx-background-radius: 5;");

                viewBtn.setTooltip(new Tooltip("Voir les détails"));
                editBtn.setTooltip(new Tooltip("Modifier la zone"));
                deleteBtn.setTooltip(new Tooltip("Supprimer la zone"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, viewBtn, editBtn, deleteBtn);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

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

    public void loadZones() {
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la zone");
        dialog.setHeaderText(zone.getNomZone());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        grid.add(new Label("ID :"), 0, 0);
        grid.add(new Label(String.valueOf(zone.getId())), 1, 0);
        grid.add(new Label("Nom :"), 0, 1);
        grid.add(new Label(zone.getNomZone()), 1, 1);
        grid.add(new Label("Coordonnées GPS :"), 0, 2);
        grid.add(new Label(zone.getCoordonneesGps()), 1, 2);
        grid.add(new Label("Niveau de pollution :"), 0, 3);

        Label niveauLabel = new Label(zone.getNiveauPollution() + "/10");
        if (zone.getNiveauPollution() >= 7) {
            niveauLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        } else if (zone.getNiveauPollution() >= 4) {
            niveauLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
        } else {
            niveauLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        }
        grid.add(niveauLabel, 1, 3);

        grid.add(new Label("Date d'identification :"), 0, 4);
        grid.add(new Label(zone.getDateIdentification().format(dateFormatter)), 1, 4);

        grid.add(new Separator(), 0, 5, 2, 1);
        grid.add(new Label("📊 INDICATEUR ASSOCIÉ :"), 0, 6, 2, 1);
        grid.add(new Label(" "), 0, 7);

        if (zone.getIndicateur() != null) {
            IndicateurImpact ind = zone.getIndicateur();
            grid.add(new Label("ID indicateur :"), 0, 8);
            grid.add(new Label(String.valueOf(ind.getId())), 1, 8);
            grid.add(new Label("Total kg récoltés :"), 0, 9);
            grid.add(new Label(String.format("%.0f kg", ind.getTotalKgRecoltes())), 1, 9);
            grid.add(new Label("CO₂ évité :"), 0, 10);
            grid.add(new Label(String.format("%.0f kg", ind.getCo2Evite())), 1, 10);
            grid.add(new Label("Date de calcul :"), 0, 11);
            grid.add(new Label(ind.getDateCalcul().format(dateFormatter)), 1, 11);
        } else {
            grid.add(new Label("Aucun indicateur associé"), 0, 8, 2, 1);
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        dialog.getDialogPane().lookupButton(closeButton).setStyle("-fx-background-color: #8bd22f; -fx-text-fill: #1a3a2a; -fx-font-weight: bold; -fx-padding: 8 20;");

        dialog.showAndWait();
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

    private void showFormDialog(ZonePolluee zone) {
        Dialog<ZonePolluee> dialog = new Dialog<>();
        dialog.setTitle(zone == null ? "Ajouter une zone" : "Modifier une zone");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/example/styles/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("form-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label titleLabel = new Label(zone == null ? "➕ Nouvelle zone polluée" : "✏️ Modifier la zone");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a3a2a;");
        GridPane.setColumnSpan(titleLabel, 2);
        grid.add(titleLabel, 0, 0);

        Label nomLabel = new Label("Nom de la zone");
        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField nomField = new TextField();
        nomField.setPromptText("Ex: Plage de Hammamet");
        nomField.getStyleClass().add("form-field");
        grid.add(nomLabel, 0, 1);
        grid.add(nomField, 1, 1);

        Label gpsLabel = new Label("Coordonnées GPS");
        gpsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField gpsField = new TextField();
        gpsField.setPromptText("Ex: 36.4025, 10.1817");
        gpsField.getStyleClass().add("form-field");
        grid.add(gpsLabel, 0, 2);
        grid.add(gpsField, 1, 2);

        Label gpsHint = new Label("Format: latitude, longitude");
        gpsHint.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
        GridPane.setColumnSpan(gpsHint, 2);
        grid.add(gpsHint, 0, 3);

        Label niveauLabel = new Label("Niveau de pollution (1-10)");
        niveauLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField niveauField = new TextField();
        niveauField.setPromptText("Entrez un nombre entre 1 et 10");
        niveauField.getStyleClass().add("form-field");
        grid.add(niveauLabel, 0, 4);
        grid.add(niveauField, 1, 4);

        Label indicateurLabel = new Label("Indicateur associé");
        indicateurLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        ComboBox<IndicateurImpact> indicateurCombo = new ComboBox<>();
        indicateurCombo.setPromptText("Sélectionner un indicateur");
        indicateurCombo.getStyleClass().add("form-field");
        indicateurCombo.setPrefWidth(300);
        indicateurCombo.getItems().addAll(indicateurDAO.getAllIndicateurs());
        indicateurCombo.setCellFactory(lv -> new ListCell<IndicateurImpact>() {
            @Override
            protected void updateItem(IndicateurImpact ind, boolean empty) {
                super.updateItem(ind, empty);
                setText(empty || ind == null ? null : ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg, CO₂: " + ind.getCo2Evite());
            }
        });
        indicateurCombo.setButtonCell(new ListCell<IndicateurImpact>() {
            @Override
            protected void updateItem(IndicateurImpact ind, boolean empty) {
                super.updateItem(ind, empty);
                setText(empty || ind == null ? null : ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg");
            }
        });
        grid.add(indicateurLabel, 0, 5);
        grid.add(indicateurCombo, 1, 5);

        if (zone != null) {
            nomField.setText(zone.getNomZone());
            gpsField.setText(zone.getCoordonneesGps());
            niveauField.setText(String.valueOf(zone.getNiveauPollution()));
            indicateurCombo.setValue(zone.getIndicateur());
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("💾 Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        dialog.getDialogPane().lookupButton(saveButton).setStyle("-fx-background-color: #8bd22f; -fx-text-fill: #1a3a2a; -fx-font-weight: bold; -fx-padding: 8 20;");
        dialog.getDialogPane().lookupButton(cancelButton).setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    String nom = nomField.getText();
                    String gps = gpsField.getText();
                    String niveauStr = niveauField.getText();
                    IndicateurImpact indicateur = indicateurCombo.getValue();

                    // ========== CONTROLE DE SAISIE ==========

                    if (nom == null || nom.trim().isEmpty()) {
                        showAlert("Erreur de saisie", "❌ Le nom de la zone est obligatoire.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (nom.length() < 3) {
                        showAlert("Erreur de saisie", "❌ Le nom de la zone doit contenir au moins 3 caractères.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (nom.length() > 100) {
                        showAlert("Erreur de saisie", "❌ Le nom ne doit pas dépasser 100 caractères.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (!nom.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-\\']+$")) {
                        showAlert("Erreur de saisie", "❌ Le nom ne doit contenir que des lettres, chiffres, espaces et tirets.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (gps == null || gps.trim().isEmpty()) {
                        showAlert("Erreur de saisie", "❌ Les coordonnées GPS sont obligatoires.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (!gps.matches("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$")) {
                        showAlert("Erreur de saisie", "❌ Format GPS invalide. Utilisez le format: latitude,longitude (ex: 36.8065,10.1815)", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (niveauStr == null || niveauStr.trim().isEmpty()) {
                        showAlert("Erreur de saisie", "❌ Le niveau de pollution est obligatoire.", Alert.AlertType.ERROR);
                        return null;
                    }

                    int niveau;
                    try {
                        niveau = Integer.parseInt(niveauStr);
                    } catch (NumberFormatException e) {
                        showAlert("Erreur de saisie", "❌ Le niveau doit être un nombre entier.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (niveau < 1 || niveau > 10) {
                        showAlert("Erreur de saisie", "❌ Le niveau doit être compris entre 1 et 10.", Alert.AlertType.ERROR);
                        return null;
                    }

                    if (indicateur == null) {
                        showAlert("Erreur de saisie", "❌ Veuillez sélectionner un indicateur.", Alert.AlertType.ERROR);
                        return null;
                    }

                    // ========== FIN CONTROLE DE SAISIE ==========

                    ZonePolluee newZone = new ZonePolluee(nom.trim(), gps.trim(), niveau, LocalDateTime.now(), indicateur);
                    if (zone != null) {
                        newZone.setId(zone.getId());
                    }
                    return newZone;
                } catch (NumberFormatException e) {
                    showAlert("Erreur de saisie", "❌ Veuillez saisir un niveau valide.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<ZonePolluee> result = dialog.showAndWait();
        result.ifPresent(z -> {
            if (z.getId() == 0) {
                dao.addZone(z);
                showAlert("Succès", "✅ Zone ajoutée avec succès !", Alert.AlertType.INFORMATION);
            } else {
                dao.updateZone(z);
                showAlert("Succès", "✅ Zone modifiée avec succès !", Alert.AlertType.INFORMATION);
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
}