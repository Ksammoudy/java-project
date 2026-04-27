package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    @FXML private TableColumn<Participation, String> subjectColumn; // Ism l-event
    @FXML private TableColumn<Participation, String> dateColumn;    // Date d'envoi
    @FXML private TableColumn<Participation, String> emailColumn;   // Destination

    private ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes : On transforme l'affichage
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 2. Charger les données
        loadData();
    }

    private void loadData() {
        try {
            List<Participation> list = ps.read();
            masterData.setAll(list);
            notificationTable.setItems(masterData);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des notifications: " + e.getMessage());
        }
    }

    @FXML
    void handleSearchNotification(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            notificationTable.setItems(masterData);
            return;
        }

        // 3. Filtrage par nom de citoyen (bech kol wahed ychouf ken el mailat mte3o)
        FilteredList<Participation> filteredData = new FilteredList<>(masterData, p -> {
            return p.getNomCitoyen().toLowerCase().contains(searchText);
        });

        notificationTable.setItems(filteredData);
    }
}