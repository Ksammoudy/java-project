package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private final ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> participationList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Mapping el Columns - LEZEM l-asémi bin l-guillemets ykounou nafs Getters mta3 el Model
        colCitoyen.setCellValueFactory(new PropertyValueFactory<>("nomCitoyen"));
        // Salla7tlek hedhi mel "titreEvenement" l- "titreEv" kima fil Service
        colEvenement.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        // 2. Configuration des Buttons d'action (CRUD)
        setupActions();

        // 3. Chargement des données
        loadData();
    }

    private void loadData() {
        try {
            participationList.clear();
            List<Participation> data = ps.read();

            // Debug bech na3rfou ken el lista fiha data mel base wala lé
            System.out.println("📊 Debug: Nombre de participations l9inehom = " + data.size());

            if (data.isEmpty()) {
                System.out.println("⚠️ Warning: El base de données raj3et lista fergha!");
            }

            participationList.addAll(data);
            participationTable.setItems(participationList);
            participationTable.refresh();

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("🗑️");
            private final Button btnDetails = new Button("📄");
            private final HBox pane = new HBox(btnDetails, btnDelete);

            {
                pane.setSpacing(10);
                pane.setAlignment(Pos.CENTER);

                btnDetails.getStyleClass().add("button-view");
                btnDelete.getStyleClass().add("button-delete");

                btnDelete.setOnAction(event -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });

                // Zid action lel details ken t7eb t-warri description
                btnDetails.setOnAction(event -> {
                    System.out.println("Viewing details for participation ID: " + getTableView().getItems().get(getIndex()).getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleDelete(Participation p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette participation ?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ps.delete(p);
                    loadData(); // Refresh automatique ba3d el delete
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAjouterParticipant(ActionEvent event) {
        System.out.println("🚀 Clikit 3al bouton ajouter!");
        try {
            URL fxmlUrl = getClass().getResource("/org/example/views/event/AjouterParticipation.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ Mal9itech el fichier FXML! Thabbet fil Path: /org/example/views/event/AjouterParticipation.fxml");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            // Nel9aw el contentArea mel scene mta3 el bouton elli nzelna 3lih (ID CSS #contentArea)
            AnchorPane contentArea = (AnchorPane) ((Node) event.getSource()).getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);

                // Beich el page j'dida t-ji mrigla 3al kbor
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
            } else {
                System.err.println("❌ Erreur: contentArea mal9inehouch! Thabbet fil Admin.fxml (lezem id='contentArea')");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }
}