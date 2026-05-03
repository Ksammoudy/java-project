package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.gestionevent.Participation;
import org.example.services.gestionevent.ParticipationServices;

import java.sql.SQLException;
import java.util.List;

public class NotificationControllerFront {

    @FXML private TextField searchField;
    @FXML private TableView<Participation> notificationTable;
    @FXML private TableColumn<Participation, String> subjectColumn;
    @FXML private TableColumn<Participation, String> dateColumn;
    @FXML private TableColumn<Participation, String> emailColumn;
    @FXML private Label statusLabel; // Optionnel: bech t-9olou "Aucun résultat"

    private ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // On charge les données en arrière-plan
        loadData();

        // On s'assure que le tableau est caché au début
        notificationTable.setVisible(false);
    }

    private void loadData() {
        try {
            List<Participation> list = ps.read();
            masterData.setAll(list);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des notifications: " + e.getMessage());
        }
    }

    @FXML
    void handleSearchNotification(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            notificationTable.setVisible(false);
            if(statusLabel != null) statusLabel.setText("Veuillez saisir un nom.");
            return;
        }

        // Filtrage par nom de citoyen
        FilteredList<Participation> filteredData = new FilteredList<>(masterData, p -> {
            return p.getNomCitoyen().toLowerCase().contains(searchText);
        });

        if (filteredData.isEmpty()) {
            notificationTable.setVisible(false);
            if(statusLabel != null) statusLabel.setText("Aucune notification trouvée pour ce nom.");
        } else {
            notificationTable.setItems(filteredData);
            notificationTable.setVisible(true);
            if(statusLabel != null) statusLabel.setText("Affichage des notifications pour : " + searchText);
        }
    }
}