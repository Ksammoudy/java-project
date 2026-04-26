package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices; // Zid el import hedha

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherEvenementsController implements Initializable {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colDescription;
    @FXML private TableColumn<Evenement, java.sql.Date> colDate;
    @FXML private TableColumn<Evenement, String> colOrganisateur;
    @FXML private TableColumn<Evenement, Void> colActions;

    private final EvenementServices service = new EvenementServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Mapping el columns
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrganisateur.setCellValueFactory(new PropertyValueFactory<>("nomOrganisateur"));

        // 2. Setup des Actions
        setupActions();

        // 3. Load Data mel Base de données
        refreshTable();
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("👁️");
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(btnView, btnEdit, btnDelete);

            {
                pane.setSpacing(10);
                pane.setStyle("-fx-alignment: CENTER;");
                btnView.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");

                btnView.setOnAction(event -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    loadPage("/org/example/views/event/DetailsEvenement.fxml", ev);
                });

                btnEdit.setOnAction(event -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    loadPage("/org/example/views/event/AjouterEvenement.fxml", ev);
                });

                btnDelete.setOnAction(event -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet événement ?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            try {
                                service.delete(ev); // Fassa5 mel Base
                                refreshTable();    // Update el TableView
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void refreshTable() {
        try {
            List<Evenement> list = service.read(); // Jib el data mel base
            eventTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du refresh: " + e.getMessage());
        }
    }

    private void loadPage(String fxmlPath, Evenement ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Injection des données selon le controller cible
            if (fxmlPath.contains("DetailsEvenement")) {
                DetailsEvenementController controller = loader.getController();
                if (ev != null) {
                    controller.setEvenement(ev);
                }
            } else if (fxmlPath.contains("AjouterEvenement")) {
                AjouterEvenementController controller = loader.getController();
                if (ev != null) {
                    controller.initData(ev); // Mode Modification
                }
            }

            AnchorPane contentArea = (AnchorPane) eventTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        loadPage("/org/example/views/event/AjouterEvenement.fxml", null);
    }
}