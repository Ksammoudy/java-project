package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.example.Main;
import org.example.models.IndicateurImpact;
import org.example.services.IndicateurImpactDAO;

import java.time.LocalDateTime;
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

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotalKg.setCellValueFactory(new PropertyValueFactory<>("totalKgRecoltes"));
        colCo2.setCellValueFactory(new PropertyValueFactory<>("co2Evite"));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateCalcul().toString()));

        // Colonne Actions avec boutons
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setOnAction(e -> editIndicateur(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteIndicateur(getTableView().getItems().get(getIndex())));
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #ffc107;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #dc3545;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, editBtn, deleteBtn));
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'indicateur ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet indicateur ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.deleteIndicateur(indicateur.getId());
            loadIndicateurs();
            showAlert("Succès", "Indicateur supprimé avec succès.", Alert.AlertType.INFORMATION);
        }
    }

    private void showFormDialog(IndicateurImpact indicateur) {
        Dialog<IndicateurImpact> dialog = new Dialog<>();
        dialog.setTitle(indicateur == null ? "Ajouter un indicateur" : "Modifier un indicateur");
        dialog.setHeaderText(indicateur == null ? "Nouvel indicateur d'impact" : "Modifier l'indicateur");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField kgField = new TextField();
        kgField.setPromptText("Total kg récoltés");
        TextField co2Field = new TextField();
        co2Field.setPromptText("CO₂ évité (kg)");

        if (indicateur != null) {
            kgField.setText(String.valueOf(indicateur.getTotalKgRecoltes()));
            co2Field.setText(String.valueOf(indicateur.getCo2Evite()));
        }

        grid.add(new Label("Total kg récoltés :"), 0, 0);
        grid.add(kgField, 1, 0);
        grid.add(new Label("CO₂ évité (kg) :"), 0, 1);
        grid.add(co2Field, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    double kg = Double.parseDouble(kgField.getText());
                    double co2 = Double.parseDouble(co2Field.getText());

                    if (kg < 0 || co2 < 0) {
                        showAlert("Erreur", "Les valeurs doivent être positives.", Alert.AlertType.ERROR);
                        return null;
                    }

                    IndicateurImpact newIndicateur = new IndicateurImpact(kg, co2, LocalDateTime.now());
                    if (indicateur != null) {
                        newIndicateur.setId(indicateur.getId());
                    }
                    return newIndicateur;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez saisir des nombres valides.", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<IndicateurImpact> result = dialog.showAndWait();
        result.ifPresent(ind -> {
            if (ind.getId() == 0) {
                dao.addIndicateur(ind);
                showAlert("Succès", "Indicateur ajouté avec succès.", Alert.AlertType.INFORMATION);
            } else {
                dao.updateIndicateur(ind);
                showAlert("Succès", "Indicateur modifié avec succès.", Alert.AlertType.INFORMATION);
            }
            loadIndicateurs();
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
    private void goToZones() {
        Main.showZonePollueeListPage();
    }
}