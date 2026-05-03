package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AfficherEvenementsController implements Initializable {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colAdresse;
    @FXML private TableColumn<Evenement, Integer> colImpactIA;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, String> colOrganisateur;
    @FXML private TableColumn<Evenement, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;

    private final EvenementServices es = new EvenementServices();
    private ObservableList<Evenement> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrganisateur.setCellValueFactory(new PropertyValueFactory<>("nomOrganisateur"));

        // Impact IA avec Design dynamic (kima el tsawer)
        colImpactIA.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    int predicted = predictParticipations(ev.getLieu());

                    Label lbl = new Label("~" + predicted + " pers. attendues");
                    lbl.setStyle("-fx-background-color: #00d2ff; -fx-text-fill: black; -fx-padding: 2 10; -fx-background-radius: 15; -fx-font-weight: bold;");

                    ProgressBar pb = new ProgressBar(predicted / 100.0);
                    pb.setPrefWidth(100);
                    pb.setStyle("-fx-accent: #2d5a27;");

                    VBox vbox = new VBox(lbl, pb);
                    vbox.setAlignment(Pos.CENTER);
                    vbox.setSpacing(4);
                    setGraphic(vbox);
                }
            }
        });

        setupActions();
        loadData();

        // Search Logic
        FilteredList<Evenement> filteredData = new FilteredList<>(masterData, e -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(event -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return event.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                        event.getNomOrganisateur().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Evenement> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(eventTable.comparatorProperty());
        eventTable.setItems(sortedData);
    }

    public int predictParticipations(String lieu) {
        if (lieu == null) return 20;
        String l = lieu.toLowerCase();
        if (l.contains("lac")) return 45 + (int)(Math.random() * 10);
        if (l.contains("centre ville")) return 60 + (int)(Math.random() * 15);
        return 30 + (int)(Math.random() * 10);
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

    private void loadData() {
        try { masterData.setAll(es.read()); } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDetails(Evenement ev) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'événement");
        alert.setHeaderText(ev.getTitre());
        alert.setContentText("Lieu: " + ev.getLieu() + "\nOrganisateur: " + ev.getNomOrganisateur());
        alert.showAndWait();
    }

    private void handleEdit(Evenement ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/AjouterEvenement.fxml"));
            Parent root = loader.load();
            AjouterEvenementController controller = loader.getController();
            controller.initData(ev); // Thabbet el méthode hedhi mawjouda f-AjouterEvenementController
            AnchorPane contentArea = (AnchorPane) eventTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleDelete(Evenement ev) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet événement ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    es.delete(ev);
                    loadData();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    @FXML
    private void handleGoToAjouter() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/event/AjouterEvenement.fxml"));
            AnchorPane contentArea = (AnchorPane) eventTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}