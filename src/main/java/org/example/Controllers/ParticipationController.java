package org.example.Controllers;

import org.example.entities.Participation;
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

public class ParticipationController {

    @FXML private TableView<Participation> tableParticipation;

    @FXML private TableColumn<Participation, Integer> colId;
    @FXML private TableColumn<Participation, String> colDate;
    @FXML private TableColumn<Participation, Integer> colEvent;
    @FXML private TableColumn<Participation, Integer> colCitoyen;
    @FXML private TableColumn<Participation, Void> colActions;

    private ObservableList<Participation> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        list.add(new Participation(1,
                java.sql.Date.valueOf("2026-04-10"),
                101, 1));

        list.add(new Participation(2,
                java.sql.Date.valueOf("2026-04-12"),
                102, 2));

        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getId()).asObject());

        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getDateInscription())
                )
        );

        colEvent.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getIdEvenement()).asObject());

        colCitoyen.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getIdCitoyen()).asObject());

        tableParticipation.setItems(list);

        addActions();
    }

    // 🔥 ACTIONS BUTTONS
    private void addActions() {

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnView = new Button("View");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            {
                btnView.setOnAction(e -> {
                    Participation p = getRow();
                    if (p != null) show(p);
                });

                btnEdit.setOnAction(e -> {
                    Participation p = getRow();
                    if (p != null) editParticipation(p);
                });

                btnDelete.setOnAction(e -> {
                    Participation p = getRow();
                    if (p != null) list.remove(p);
                });
            }

            private Participation getRow() {
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

    // 👁 SHOW
    private void show(Participation p) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Participation Details");
        alert.setHeaderText("ID: " + p.getId());

        alert.setContentText(
                "Date: " + p.getDateInscription() + "\n" +
                        "Event ID: " + p.getIdEvenement() + "\n" +
                        "Citoyen ID: " + p.getIdCitoyen()
        );

        alert.showAndWait();
    }

    // ✏️ EDIT
    private void editParticipation(Participation p) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/EditParticipation.fxml")
            );

            Parent root = loader.load();

            EditParticipationController controller = loader.getController();

            controller.setParticipation(p);
            controller.setMainController(this); // 🔥 IMPORTANT FOR REFRESH

            Stage stage = new Stage();
            stage.setTitle("Edit Participation");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ➕ ADD
    @FXML
    private void openAddParticipation() {
        System.out.println("Open Add Participation Form");
    }

    // 🔄 REFRESH
    public void refreshTable() {
        tableParticipation.refresh();
    }
}