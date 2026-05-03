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
import org.example.models.IndicateurImpact;
import org.example.services.IndicateurImpactDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class IndicateurImpactController {

    @FXML private TableView<IndicateurImpact> indicateurTable;
    @FXML private TableColumn<IndicateurImpact, Integer> colId;
    @FXML private TableColumn<IndicateurImpact, Double> colTotalKg;
    @FXML private TableColumn<IndicateurImpact, Double> colCo2;
    @FXML private TableColumn<IndicateurImpact, String> colDate;
    @FXML private TableColumn<IndicateurImpact, Void> colActions;

    @FXML private Text totalIndicateursText;
    @FXML private Text totalDechetsText;
    @FXML private Text totalCo2Text;

    private IndicateurImpactDAO dao = new IndicateurImpactDAO();
    private ObservableList<IndicateurImpact> indicateurList = FXCollections.observableArrayList();
    private Runnable zoneRefreshCallback;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setZoneRefreshCallback(Runnable callback) {
        this.zoneRefreshCallback = callback;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotalKg.setCellValueFactory(new PropertyValueFactory<>("totalKgRecoltes"));
        colCo2.setCellValueFactory(new PropertyValueFactory<>("co2Evite"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateCalcul().format(dateFormatter)));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setOnAction(e -> editIndicateur(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteIndicateur(getTableView().getItems().get(getIndex())));
                editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-font-size: 12px; -fx-min-width: 30px; -fx-cursor: hand; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-min-width: 30px; -fx-cursor: hand; -fx-background-radius: 5;");
                editBtn.setTooltip(new Tooltip("Modifier l'indicateur"));
                deleteBtn.setTooltip(new Tooltip("Supprimer l'indicateur"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(8, editBtn, deleteBtn));
                }
            }
        });

        loadIndicateurs();
    }

    @FXML
    private void loadIndicateurs() {
        java.util.List<IndicateurImpact> indicateurs = dao.getAllIndicateurs();
        indicateurList.setAll(indicateurs);
        indicateurTable.setItems(indicateurList);
        updateStats(indicateurs);
    }

    private void updateStats(java.util.List<IndicateurImpact> indicateurs) {
        totalIndicateursText.setText(String.valueOf(indicateurs.size()));
        double totalKg = indicateurs.stream().mapToDouble(IndicateurImpact::getTotalKgRecoltes).sum();
        double totalCo2 = indicateurs.stream().mapToDouble(IndicateurImpact::getCo2Evite).sum();
        totalDechetsText.setText(String.format("%.0f kg", totalKg));
        totalCo2Text.setText(String.format("%.0f kg", totalCo2));
    }

    @FXML
    private void addIndicateur() {
        showFormDialog(null);
    }

    private void editIndicateur(IndicateurImpact indicateur) {
        showFormDialog(indicateur);
    }

    private void deleteIndicateur(IndicateurImpact indicateur) {
        int zonesLiees = dao.countZonesByIndicateurId(indicateur.getId());

        String message;
        if (zonesLiees > 0) {
            message = "⚠️ ATTENTION : " + zonesLiees + " zone(s) sont liées à cet indicateur.\n\n" +
                    "La suppression de cet indicateur entraînera la suppression de ces zones.\n\n" +
                    "Êtes-vous sûr de vouloir continuer ?";
        } else {
            message = "Supprimer cet indicateur ?";
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'indicateur ?");
        confirm.setContentText(message);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.deleteIndicateurWithZones(indicateur.getId());
            loadIndicateurs();

            if (zoneRefreshCallback != null) {
                zoneRefreshCallback.run();
            }

            String successMessage = zonesLiees > 0 ?
                    "Indicateur et " + zonesLiees + " zone(s) associée(s) supprimés." :
                    "Indicateur supprimé avec succès.";
            showAlert("Succès", successMessage, Alert.AlertType.INFORMATION);
        }
    }

    private void showFormDialog(IndicateurImpact indicateur) {
        Dialog<IndicateurImpact> dialog = new Dialog<>();
        dialog.setTitle(indicateur == null ? "Ajouter un indicateur" : "Modifier un indicateur");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/example/styles/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("form-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(25));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        // Titre
        Label titleLabel = new Label(indicateur == null ? "➕ Nouvel indicateur" : "✏️ Modifier l'indicateur");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a3a2a;");
        GridPane.setColumnSpan(titleLabel, 2);
        grid.add(titleLabel, 0, 0);

        // Champ Total kg
        Label kgLabel = new Label("Total kg récoltés");
        kgLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField kgField = new TextField();
        kgField.setPromptText("Ex: 150");
        kgField.getStyleClass().add("form-field");
        Label errorKgLabel = new Label();
        errorKgLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px;");
        errorKgLabel.setVisible(false);

        GridPane.setConstraints(kgLabel, 0, 1);
        GridPane.setConstraints(kgField, 1, 1);
        GridPane.setConstraints(errorKgLabel, 1, 2);
        grid.getChildren().addAll(kgLabel, kgField, errorKgLabel);

        // Champ CO₂
        Label co2Label = new Label("CO₂ évité (kg)");
        co2Label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        TextField co2Field = new TextField();
        co2Field.setPromptText("Ex: 25");
        co2Field.getStyleClass().add("form-field");
        Label errorCo2Label = new Label();
        errorCo2Label.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 11px;");
        errorCo2Label.setVisible(false);

        GridPane.setConstraints(co2Label, 0, 3);
        GridPane.setConstraints(co2Field, 1, 3);
        GridPane.setConstraints(errorCo2Label, 1, 4);
        grid.getChildren().addAll(co2Label, co2Field, errorCo2Label);

        if (indicateur != null) {
            kgField.setText(String.valueOf(indicateur.getTotalKgRecoltes()));
            co2Field.setText(String.valueOf(indicateur.getCo2Evite()));
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("💾 Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        dialog.getDialogPane().lookupButton(saveButton).setStyle("-fx-background-color: #8bd22f; -fx-text-fill: #1a3a2a; -fx-font-weight: bold; -fx-padding: 8 20;");
        dialog.getDialogPane().lookupButton(cancelButton).setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        Button saveButtonNode = (Button) dialog.getDialogPane().lookupButton(saveButton);
        saveButtonNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            if (validateAndSave(indicateur, kgField, co2Field, errorKgLabel, errorCo2Label)) {
                dialog.close();
            }
        });

        dialog.showAndWait();
    }

    private boolean validateAndSave(IndicateurImpact indicateur,
                                    TextField kgField, TextField co2Field,
                                    Label errorKgLabel, Label errorCo2Label) {

        boolean isValid = true;

        errorKgLabel.setVisible(false);
        errorCo2Label.setVisible(false);

        String kgStr = kgField.getText();
        String co2Str = co2Field.getText();

        // Validation Kg
        if (kgStr == null || kgStr.trim().isEmpty()) {
            errorKgLabel.setText("❌ Le total kg est obligatoire");
            errorKgLabel.setVisible(true);
            isValid = false;
        } else {
            try {
                double kg = Double.parseDouble(kgStr);
                if (kg < 0) {
                    errorKgLabel.setText("❌ Le total kg ne peut pas être négatif");
                    errorKgLabel.setVisible(true);
                    isValid = false;
                } else if (kg > 1000000) {
                    errorKgLabel.setText("❌ Le total kg ne peut pas dépasser 1 000 000 kg");
                    errorKgLabel.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errorKgLabel.setText("❌ Le total kg doit être un nombre valide");
                errorKgLabel.setVisible(true);
                isValid = false;
            }
        }

        // Validation CO₂
        if (co2Str == null || co2Str.trim().isEmpty()) {
            errorCo2Label.setText("❌ Le CO₂ évité est obligatoire");
            errorCo2Label.setVisible(true);
            isValid = false;
        } else {
            try {
                double co2 = Double.parseDouble(co2Str);
                if (co2 < 0) {
                    errorCo2Label.setText("❌ Le CO₂ ne peut pas être négatif");
                    errorCo2Label.setVisible(true);
                    isValid = false;
                } else if (co2 > 500000) {
                    errorCo2Label.setText("❌ Le CO₂ ne peut pas dépasser 500 000 kg");
                    errorCo2Label.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errorCo2Label.setText("❌ Le CO₂ doit être un nombre valide");
                errorCo2Label.setVisible(true);
                isValid = false;
            }
        }

        if (isValid) {
            try {
                double kg = Double.parseDouble(kgStr);
                double co2 = Double.parseDouble(co2Str);

                IndicateurImpact newIndicateur = new IndicateurImpact(kg, co2, LocalDateTime.now());
                if (indicateur != null) {
                    newIndicateur.setId(indicateur.getId());
                }

                if (newIndicateur.getId() == 0) {
                    dao.addIndicateur(newIndicateur);
                    showAlert("Succès", "✅ Indicateur ajouté avec succès !", Alert.AlertType.INFORMATION);
                } else {
                    dao.updateIndicateur(newIndicateur);
                    showAlert("Succès", "✅ Indicateur modifié avec succès !", Alert.AlertType.INFORMATION);
                }
                loadIndicateurs();
                return true;
            } catch (NumberFormatException e) {
                errorKgLabel.setText("❌ Valeur invalide");
                errorKgLabel.setVisible(true);
                return false;
            }
        }
        return false;
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
    private void goToZones() {
        Main.showZonePollueeListPage();
    }
}