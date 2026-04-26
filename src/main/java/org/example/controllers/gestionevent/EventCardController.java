package org.example.controllers.gestionevent;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.io.IOException;
import javafx.scene.Node;

public class EventCardController {

    @FXML private WebView mapView;
    @FXML private Label eventTitle, eventLocation, organizerName, distanceLabel, weatherLabel;

    private int idEv; // 👈 Stockiw l-ID bech nasta3mlouh fil participation

    private final String HTML_CONTENT = """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script src="https://unpkg.com/leaflet-routing-machine/dist/leaflet-routing-machine.js"></script>
            <style>
                #map { height: 100vh; width: 100%; margin: 0; padding: 0; }
                .leaflet-routing-container { display: none; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([36.8065, 10.1815], 11);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

                function drawRouteFromAddress(userLat, userLon, address, controller) {
                    var url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodeURIComponent(address);
                    fetch(url).then(r => r.json()).then(data => {
                        if (data.length > 0) {
                            var eLat = parseFloat(data[0].lat), eLon = parseFloat(data[0].lon);

                            // Météo
                            fetch(`https://api.open-meteo.com/v1/forecast?latitude=${eLat}&longitude=${eLon}&current_weather=true`)
                                .then(res => res.json()).then(w => controller.updateWeather(Math.round(w.current_weather.temperature) + "°C"));

                            // Tracé
                            var routing = L.Routing.control({
                                waypoints: [L.latLng(userLat, userLon), L.latLng(eLat, eLon)],
                                lineOptions: { styles: [{ color: '#2ecc71', weight: 6 }] },
                                createMarker: () => null,
                                addWaypoints: false
                            }).addTo(map);

                            routing.on('routesfound', e => controller.updateDistance((e.routes[0].summary.totalDistance / 1000).toFixed(1) + " km"));

                            L.marker([eLat, eLon]).addTo(map).bindPopup(address).openPopup();
                            map.fitBounds(L.latLngBounds([userLat, userLon], [eLat, eLon]), {padding: [30, 30]});
                        }
                    });
                }
            </script>
        </body>
        </html>
        """;

    public void setEventData(int id, String title, String location, String organizer, double userLat, double userLon) {
        this.idEv = id; // 👈
        eventTitle.setText(title);
        eventLocation.setText("📍 " + location);
        organizerName.setText("👤 " + organizer);

        WebEngine engine = mapView.getEngine();
        engine.loadContent(HTML_CONTENT);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaController", this);
                engine.executeScript("drawRouteFromAddress(" + userLat + ", " + userLon + ", '" + location.replace("'", "\\'") + "', javaController)");
            }
        });
    }

    @FXML
    private void handleParticiper(ActionEvent event) {
        try {
            // 1. Charger el FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/event/AjouterParticipationFront.fxml"));
            Parent root = loader.load(); // 👈 Lezem na3mlou load() 9bal ma n-nadou lel Controller

            // 2. RECUPERER le controller du formulaire
            AjouterParticipationControllerFront controller = loader.getController();

            // 3. PASSER les données (HEDHI AHAM LIGNE)
            // Isked dhibet elli "this.idEv" mahouch 0 hne
            System.out.println("📤 Envoi de l'ID vers le formulaire: " + this.idEv);
            controller.setEventData(this.idEv, eventTitle.getText());

            // 4. Afficher la nouvelle scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateDistance(String distance) { Platform.runLater(() -> distanceLabel.setText("🚗 " + distance)); }
    public void updateWeather(String temp) { Platform.runLater(() -> { if (weatherLabel != null) weatherLabel.setText("☀️ " + temp); }); }
}