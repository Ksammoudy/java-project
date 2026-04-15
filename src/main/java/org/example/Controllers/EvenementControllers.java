package org.example.Controllers;

import org.example.entities.Evenement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Optional;

public class EvenementControllers {

    @FXML private TableView<Evenement> tableEvenements;

    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, Void> colActions;

    private ObservableList<Evenement> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // 🔥 test data
        list.add(new Evenement(1, "Conference", "AI Talk", "Tunis",
                java.sql.Date.valueOf("2026-04-14"), 1));

        list.add(new Evenement(2, "Hackathon", "Coding event", "Sfax",
                java.sql.Date.valueOf("2026-05-01"), 2));

        // mapping
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colTitre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre()));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colLieu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLieu()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));

        tableEvenements.setItems(list);

        addActionsButtons();
    }

    // 🔥 ACTIONS
    private void addActionsButtons() {

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnView = new Button("View");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnView.setOnAction(e -> {
                    Evenement ev = getRowItem();
                    if (ev != null) showDetails(ev);
                });

                btnEdit.setOnAction(e -> {
                    Evenement ev = getRowItem();
                    if (ev != null) updateEvent(ev);
                });

                btnDelete.setOnAction(e -> {
                    Evenement ev = getRowItem();
                    if (ev != null) deleteEvent(ev);
                });
            }

            private Evenement getRowItem() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, btnView, btnEdit, btnDelete));
                }
            }
        });
    }

    // 👁 DETAILS
    private void showDetails(Evenement ev) {
        if (ev == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details");
        alert.setHeaderText(ev.getTitre());

        alert.setContentText(
                "Description: " + ev.getDescription() + "\n" +
                        "Lieu: " + ev.getLieu() + "\n" +
                        "Date: " + ev.getDate()
        );

        alert.showAndWait();
    }

    // ✏️ EDIT
    private void updateEvent(Evenement ev) {

        if (ev == null) {
            showError("Aucun événement sélectionné !");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditEvenement.fxml"));
            Parent root = loader.load();

            EditEvenementController controller = loader.getController();
            controller.setEvent(ev);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Event");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur ouverture fenêtre !");
        }
    }

    // 🗑 DELETE
    private void deleteEvent(Evenement ev) {

        if (ev == null) {
            showError("Aucun événement sélectionné !");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer événement ?");
        confirm.setContentText(ev.getTitre());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            list.remove(ev);
        }
    }

    // ➕ ADD EVENT (🔥 هذا كان ناقصك)
    @FXML
    private void openAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEvenement.fxml"));
            Parent root = loader.load();

            AddEvenementController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter Evenement");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur ouverture formulaire ajout !");
        }
    }

    // ➕ ADD (logic)
    public void addEvent(Evenement ev) {
        if (ev == null) {
            showError("Erreur ajout !");
            return;
        }

        list.add(ev);
    }

    // 🔄 REFRESH
    public void refreshTable() {
        tableEvenements.refresh();
    }

    // 🔴 ERROR
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}