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

public class ParticipationsControllerFront {

    @FXML private TextField searchField; // El barre de recherche
    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, String> eventColumn;
    @FXML private TableColumn<Participation, String> nameColumn;
    @FXML private TableColumn<Participation, String> statusColumn; // Inscrit, etc.

    private ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes avec le modèle Participation
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomCitoyen"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // wala ay champ ekher t7eb t-affichih

        // 2. Charger les données mel base
        loadData();
    }

    private void loadData() {
        try {
            List<Participation> list = ps.read();
            masterData.setAll(list);
            participationTable.setItems(masterData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleVerifier(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            participationTable.setItems(masterData);
            return;
        }

        // 3. Filtrage par nom de citoyen
        FilteredList<Participation> filteredData = new FilteredList<>(masterData, p -> {
            return p.getNomCitoyen().toLowerCase().contains(searchText);
        });

        participationTable.setItems(filteredData);
    }
}