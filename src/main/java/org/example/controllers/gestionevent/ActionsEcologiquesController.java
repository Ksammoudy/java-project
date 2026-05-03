package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ActionsEcologiquesController {

    @FXML private FlowPane eventContainer;
    @FXML private HBox weatherContainer;
    @FXML private Label tempLabel;
    @FXML private Label cityLabel;

    private EvenementServices service = new EvenementServices();

    @FXML
    public void initialize() {
        // Initialisation static lil météo ken ma3andeksh service tawa
        if (tempLabel != null) tempLabel.setText("23°C");
        if (cityLabel != null) cityLabel.setText("Ariana, Tunisie");

        loadAllEvents();
    }

    @FXML
    private void handleShowWeatherDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/WeatherDetails.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Météo");
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation météo: " + e.getMessage());
        }
    }

    private void loadAllEvents() {
        try {
            List<Evenement> events = service.read();

            // 🔍 DEBUG: Thabbet fil Console ken list fergha wala la
            System.out.println("📊 Nombre d'événements trouvés: " + events.size());

            eventContainer.getChildren().clear();

            // Coordonnées par défaut (Ariana)
            double myLat = 36.8625;
            double myLon = 10.1956;

            for (Evenement ev : events) {
                try {
                    // 🔍 DEBUG: Thabbet chnowa jey mel Base
                    System.out.println("📌 Chargement de: " + ev.getTitre() + " | Lieu: " + ev.getLieu());

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/EventCard.fxml"));
                    Parent card = loader.load();

                    EventCardController cardCtrl = loader.getController();

                    // Salla7na el passation mta3 el data
                    cardCtrl.setEventData(
                            ev.getId(),
                            ev.getTitre(),
                            ev.getLieu() != null ? ev.getLieu() : "Lieu non défini",
                            ev.getNomOrganisateur() != null ? ev.getNomOrganisateur() : "Anonyme",
                            myLat,
                            myLon
                    );

                    eventContainer.getChildren().add(card);

                } catch (IOException e) {
                    System.err.println("❌ Erreur FXML EventCard: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
    }
}