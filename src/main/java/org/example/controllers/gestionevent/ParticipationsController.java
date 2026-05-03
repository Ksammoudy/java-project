package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.example.models.gestionevent.Participation;
import org.example.services.gestionevent.ParticipationServices;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ParticipationsController implements Initializable {

    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, String> colCitoyen;
    @FXML private TableColumn<Participation, String> colEvenement;
    @FXML private TableColumn<Participation, Date> colDate;
    @FXML private TableColumn<Participation, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox; // 👈 Zid hathi fx:id fil FXML

    private final ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Setup Columns
        colCitoyen.setCellValueFactory(new PropertyValueFactory<>("nomCitoyen"));
        colEvenement.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        setupActions();
        loadData();

        // 2. Recherche Dynamique (FilteredList)
        FilteredList<Participation> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(participation -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return participation.getNomCitoyen().toLowerCase().contains(lowerCaseFilter) ||
                        participation.getNomEvenement().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // 3. Tri Dynamique (SortedList)
        SortedList<Participation> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(participationTable.comparatorProperty());
        participationTable.setItems(sortedData);

        // 4. Action de Tri b-el ComboBox (kima image_37.png)
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList("Nom (A-Z)", "Nom (Z-A)"));
            sortComboBox.setOnAction(event -> {
                String choice = sortComboBox.getValue();
                if ("Nom (A-Z)".equals(choice)) {
                    masterData.sort((p1, p2) -> p1.getNomCitoyen().compareToIgnoreCase(p2.getNomCitoyen()));
                } else if ("Nom (Z-A)".equals(choice)) {
                    masterData.sort((p1, p2) -> p2.getNomCitoyen().compareToIgnoreCase(p1.getNomCitoyen()));
                }
            });
        }
    }

    private void loadData() {
        try {
            List<Participation> data = ps.read();
            masterData.setAll(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetails = new Button("📄");
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(btnDetails, btnEdit, btnDelete);

            {
                pane.setSpacing(8);
                pane.setAlignment(Pos.CENTER);
                btnDetails.getStyleClass().add("button-view");
                btnEdit.getStyleClass().add("button-edit");
                btnDelete.getStyleClass().add("button-delete");

                btnDetails.setOnAction(event -> handleDetails(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void handleDetails(Participation p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails");
        alert.setHeaderText(null);
        alert.setContentText("Citoyen: " + p.getNomCitoyen() + "\nÉvénement: " + p.getNomEvenement());
        alert.showAndWait();
    }

    private void handleEdit(Participation p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/ModifierParticipation.fxml"));
            Parent root = loader.load();
            ModifierParticipationController controller = loader.getController();
            controller.initData(p);
            AnchorPane contentArea = (AnchorPane) participationTable.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Participation p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ps.delete(p);
                    loadData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleAjouterParticipant(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/event/AjouterParticipation.fxml"));
            AnchorPane contentArea = (AnchorPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}