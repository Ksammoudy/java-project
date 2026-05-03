package org.example.controllers.gestionevent;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Entry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class CalendrierController implements Initializable {

    @FXML
    private AnchorPane calendarPane;

    // Initialisation mte3 el service
    private final EvenementServices es = new EvenementServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CalendarView calendarView = new CalendarView();
        Calendar wasteEvents = new Calendar("WasteWise Events");
        wasteEvents.setStyle(Calendar.Style.STYLE1);

        try {
            System.out.println("🔍 Tentative de lecture des événements...");
            List<Evenement> list = es.read();

            if (list == null) {
                System.err.println("❌ Erreur: La liste retournée par es.read() est NULL");
            } else {
                System.out.println("📊 Nombre d'événements trouvés: " + list.size());

                for (Evenement ev : list) {
                    // Check ken el date mouch null bech ma na3mlouch NullPointerException
                    if (ev != null && ev.getDate() != null) {

                        // Nesta3mlou el Nom mta3 l'événement (ken 3andek getTitre() badalha)
                        String titre = (ev.getNomOrganisateur() != null) ? ev.getNomOrganisateur() : "Événement sans nom";

                        Entry<String> entry = new Entry<>(titre);

                        // Conversion java.sql.Date -> java.time.LocalDate
                        entry.setInterval(ev.getDate().toLocalDate(),
                                LocalTime.of(9, 0),
                                ev.getDate().toLocalDate(),
                                LocalTime.of(11, 0));

                        wasteEvents.addEntry(entry);
                    } else {
                        System.out.println("⚠️ Événement ignoré (date null ou objet null)");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur critique dans le chargement: ");
            e.printStackTrace(); // Beich ya3tik el ligne dhibet mnin el ghalta
        }

        CalendarSource mySource = new CalendarSource("Ma Base");
        mySource.getCalendars().add(wasteEvents);
        calendarView.getCalendarSources().setAll(mySource); // Nesta3mlou setAll khir

        // Affichage
        calendarView.setRequestedTime(LocalTime.now());
        calendarPane.getChildren().setAll(calendarView);

        AnchorPane.setTopAnchor(calendarView, 0.0);
        AnchorPane.setBottomAnchor(calendarView, 0.0);
        AnchorPane.setLeftAnchor(calendarView, 0.0);
        AnchorPane.setRightAnchor(calendarView, 0.0);
    }
}