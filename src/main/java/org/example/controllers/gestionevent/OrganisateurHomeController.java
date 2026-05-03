package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

// --- IMPORT EL CONTROLLERS EL OKHRIN (HNE EL GHAlta) ---
// Thabbet bark ennou el controllers hedhom mawjoudin f-nafs el package
import org.example.controllers.gestionevent.ModifierOrgController;
import org.example.controllers.gestionevent.DetailsOrgController;

import java.io.IOException;
import java.sql.SQLException;

public class OrganisateurHomeController {

    @FXML private StackPane contentArea;
    @FXML private VBox tableViewContainer;
    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> colTitre, colOrg;
    @FXML private TableColumn<Evenement, java.sql.Date> colDate;
    @FXML private TableColumn<Evenement, Void> colGestion;
    @FXML private TextField searchField;

    private final EvenementServices service = new EvenementServices();
    private ObservableList<Evenement> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrg.setCellValueFactory(new PropertyValueFactory<>("nomOrganisateur"));

        eventTable.setPlaceholder(new Label("Veuillez entrer votre nom d'organisateur pour voir vos actions."));

        addButtonsToTable();
        showMyEvents();
    }

    @FXML
    private void handleFilter() {
        String name = searchField.getText();
        if (name == null || name.isEmpty()) {
            masterData.clear();
            eventTable.setItems(masterData);
            return;
        }
        masterData.setAll(service.getByOrganisateur(name));
        eventTable.setItems(masterData);
    }

    private void addButtonsToTable() {
        Callback<TableColumn<Evenement, Void>, TableCell<Evenement, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnModif = new Button("📝");
            private final Button btnDetails = new Button("👁");
            private final Button btnSupp = new Button("🗑");
            private final HBox pane = new HBox(btnDetails, btnModif, btnSupp);

            {
                pane.setSpacing(5);
                btnDetails.setStyle("-fx-background-color: #00B4D8; -fx-text-fill: white; -fx-background-radius: 5;");
                btnModif.setStyle("-fx-background-color: #97D73F; -fx-text-fill: white; -fx-background-radius: 5;");
                btnSupp.setStyle("-fx-background-color: #EF233C; -fx-text-fill: white; -fx-background-radius: 5;");

                btnSupp.setOnAction(e -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    handleDelete(ev);
                });

                btnDetails.setOnAction(e -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    loadViewWithData("DetailsOrgFront", ev);
                });

                btnModif.setOnAction(e -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    loadViewWithData("ModifierOrgFront", ev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(pane);
            }
        };
        colGestion.setCellFactory(cellFactory);
    }

    private void handleDelete(Evenement ev) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'événement : " + ev.getTitre() + " ?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.delete(ev);
                masterData.remove(ev);
                System.out.println("✅ Événement supprimé.");
            } catch (SQLException ex) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur fatale");
                errorAlert.setHeaderText("Impossible de supprimer");
                errorAlert.setContentText("Cet événement a déjà des participants inscrits.");
                errorAlert.show();
            }
        }
    }

    private void loadViewWithData(String fxml, Evenement ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/" + fxml + ".fxml"));
            Parent root = loader.load();

            // Rbat s7i7 m3a el controllers
            if (fxml.equals("ModifierOrgFront")) {
                ModifierOrgController controller = loader.getController();
                controller.initData(ev);
            } else if (fxml.equals("DetailsOrgFront")) {
                DetailsOrgController controller = loader.getController();
                controller.setEvenement(ev);
            }

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
        }
    }

    @FXML
    private void showMyEvents() {
        if (tableViewContainer != null) {
            contentArea.getChildren().setAll(tableViewContainer);
        }
    }

    @FXML
    private void showAjouter() {
        loadPage("AjouterEvenementFront");
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/" + fxml + ".fxml"));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        loadPage("RoleSelection");
    }
}