package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.gestionevent.Participation;
import org.example.services.gestionevent.ParticipationServices;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ParticipationsControllerFront implements Initializable {

    @FXML private TableView<Participation> participationTable;
    @FXML private TableColumn<Participation, String> colCitoyen;
    @FXML private TableColumn<Participation, String> colEvenement;
    @FXML private TableColumn<Participation, Date> colDate;
    @FXML private TextField searchField;
    @FXML private Label infoLabel;

    private final ParticipationServices ps = new ParticipationServices();
    private ObservableList<Participation> allParticipations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mapping el columns m3a el Model Participation
        colCitoyen.setCellValueFactory(new PropertyValueFactory<>("nomCitoyen"));
        colEvenement.setCellValueFactory(new PropertyValueFactory<>("nomEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        // Njibou el data mel loul ama n-khabbiwha
        loadAllData();
        participationTable.setVisible(false);
    }

    private void loadAllData() {
        try {
            List<Participation> data = ps.read();
            allParticipations.setAll(data);
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement SQL: " + e.getMessage());
        }
    }

    @FXML
    private void handleVerifier() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            participationTable.setVisible(false);
            infoLabel.setText("Veuillez saisir un nom pour vérifier.");
            infoLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Filter el list b-ism el citoyen barka
        List<Participation> filtered = allParticipations.stream()
                .filter(p -> p.getNomCitoyen().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            participationTable.setVisible(false);
            infoLabel.setText("Aucune participation trouvée pour ce nom.");
            infoLabel.setStyle("-fx-text-fill: orange;");
        } else {
            participationTable.setItems(FXCollections.observableArrayList(filtered));
            participationTable.setVisible(true);
            infoLabel.setText("Liste de vos engagements affichée.");
            infoLabel.setStyle("-fx-text-fill: #2e7d32;");
        }
    }
}