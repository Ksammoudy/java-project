package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.utils.DBConnection;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private TableView<EventStat> statsTable;
    @FXML private TableColumn<EventStat, String> colNomEvent;
    @FXML private TableColumn<EventStat, String> colOrganisateur;
    @FXML private TableColumn<EventStat, Integer> colNbParticipants;

    private Connection connection = DBConnection.getInstance().getConnection();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup table columns
        colNomEvent.setCellValueFactory(new PropertyValueFactory<>("nomEvent"));
        colOrganisateur.setCellValueFactory(new PropertyValueFactory<>("organisateur"));
        colNbParticipants.setCellValueFactory(new PropertyValueFactory<>("count"));

        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            Statement st = connection.createStatement();

            // 1. Totaux
            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM evenement");
            if (rs1.next()) totalEventsLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM participation");
            if (rs2.next()) totalParticipationsLabel.setText(String.valueOf(rs2.getInt(1)));

            // 2. Stats Table (Salla7na el requête hne)
            // Ken ism el column mouch 'organizer', badalha f-west el SELECT hne
            String query = "SELECT e.title, COUNT(p.id) as total " +
                    "FROM evenement e " +
                    "LEFT JOIN participation p ON e.id = p.evenement_id " +
                    "GROUP BY e.id, e.title";

            ObservableList<EventStat> statsList = FXCollections.observableArrayList();
            ResultSet rs3 = st.executeQuery(query);

            while (rs3.next()) {
                statsList.add(new EventStat(
                        rs3.getString("title"),
                        "Organisateur", // Placeholder lin t-thabbet fil ism fil base
                        rs3.getInt("total")
                ));
            }
            statsTable.setItems(statsList);

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper Class lel TableView
    public static class EventStat {
        private String nomEvent;
        private String organisateur;
        private int count;

        public EventStat(String nomEvent, String organisateur, int count) {
            this.nomEvent = nomEvent;
            this.organisateur = organisateur;
            this.count = count;
        }

        public String getNomEvent() { return nomEvent; }
        public String getOrganisateur() { return organisateur; }
        public int getCount() { return count; }
    }
}