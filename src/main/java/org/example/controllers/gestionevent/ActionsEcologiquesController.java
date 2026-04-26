package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ActionsEcologiquesController {

    @FXML private FlowPane eventContainer;
    private EvenementServices service = new EvenementServices();

    @FXML
    public void initialize() {
        loadAllEvents();
    }

    private void loadAllEvents() {
        try {
            List<Evenement> events = service.read();
            double myLat = 36.8065;
            double myLon = 10.1815;

            eventContainer.getChildren().clear();

            for (Evenement ev : events) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/EventCard.fxml"));
                    Parent card = loader.load();

                    EventCardController cardCtrl = loader.getController();

                    // SALLA7NA HNE: Zidna ev.getId() l-loula dhibet
                    cardCtrl.setEventData(
                            ev.getId(),
                            ev.getTitre(),
                            ev.getLieu() != null ? ev.getLieu() : "Tunisie",
                            ev.getNomOrganisateur() != null ? ev.getNomOrganisateur() : "Inconnu",
                            myLat,
                            myLon
                    );

                    eventContainer.getChildren().add(card);

                } catch (IOException e) {
                    System.err.println("❌ Erreur FXML Card: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
    }
}