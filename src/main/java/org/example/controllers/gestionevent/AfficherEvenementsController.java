package org.example.controllers.gestionevent;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class AfficherEvenementsController implements Initializable {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colAdresse;
    @FXML private TableColumn<Evenement, String> colImpactIA;
    @FXML private TableColumn<Evenement, String> colDate;
    @FXML private TableColumn<Evenement, String> colOrganisateur;
    @FXML private TableColumn<Evenement, Void> colActions;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnCreer;

    private final EvenementServices es = new EvenementServices();
    private ObservableList<Evenement> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Use lambda-based cell value factories to ensure correct type binding
        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colAdresse.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        colOrganisateur.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomOrganisateur()));
        colDate.setCellValueFactory(data -> {
            java.sql.Date d = data.getValue().getDate();
            return new SimpleStringProperty(d != null ? DATE_FORMAT.format(d) : "");
        });

        // Impact IA avec Design dynamic
        colImpactIA.setCellValueFactory(data -> new SimpleStringProperty(""));
        colImpactIA.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    int predicted = predictParticipations(ev.getLieu());

                    Label lbl = new Label("~" + predicted + " pers. attendues");
                    lbl.setStyle("-fx-background-color: #00d2ff; -fx-text-fill: black; -fx-padding: 2 10; -fx-background-radius: 15; -fx-font-weight: bold;");

                    ProgressBar pb = new ProgressBar(Math.min(predicted / 100.0, 1.0));
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

        // 🛡️ Gérer la visibilité du bouton Créer et de la colonne Actions selon le rôle
        org.example.models.User user = org.example.services.SessionManager.getCurrentUser();
        if (user != null) {
            String role = user.getType() != null ? user.getType().trim().toUpperCase() : "";
            if (role.equals("CITIZEN") || role.equals("CITOYEN") || role.equals("VALORIZER") || role.equals("VALORISATEUR")) {
                if (btnCreer != null) {
                    btnCreer.setVisible(false);
                    btnCreer.setManaged(false);
                }
                // Si l'utilisateur est citoyen, on cache la colonne des actions Edit/Delete
                colActions.setVisible(false);
            }
        }

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
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) eventTable.getScene().lookup("#contentArea");
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
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) eventTable.getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}