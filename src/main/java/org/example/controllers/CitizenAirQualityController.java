package org.example.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.Main;
import org.example.models.User;
import org.example.services.OpenAqService;
import org.example.services.SessionManager;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

public class CitizenAirQualityController {

    private static final double TUNIS_LAT = 36.8065;
    private static final double TUNIS_LON = 10.1815;
    private final OpenAqService openAqService = new OpenAqService();
    private WebEngine mapEngine;
    private boolean mapReady;

    @FXML
    private Button navHome;
    @FXML
    private Button navDeclare;
    @FXML
    private Button navMyDeclarations;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navNews;
    @FXML
    private Button navAir;
    @FXML
    private Button navWithdraw;
    @FXML
    private Button navSettings;
    @FXML
    private Label citizenNameLabel;
    @FXML
    private Label headerEmailLabel;
    @FXML
    private Label pm25Label;
    @FXML
    private Label pm10Label;
    @FXML
    private Label updatedLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private WebView stationsMapView;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");
        CitizenSidebarHelper.applyActive(navAir, navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        pm25Label.setText("-");
        pm10Label.setText("-");
        updatedLabel.setText("-");
        statusLabel.setText("Cliquez sur Actualiser les donnees pour charger OpenAQ.");
        initializeMap();
        fetchAsync();
    }

    @FXML
    public void handleRefresh() {
        fetchAsync();
    }

    private void initializeMap() {
        if (stationsMapView == null) {
            return;
        }
        mapEngine = stationsMapView.getEngine();
        mapEngine.setJavaScriptEnabled(true);
        mapEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                mapReady = true;
                forceMapResize();
            }
        });
        stationsMapView.widthProperty().addListener((obs, oldV, newV) -> forceMapResize());
        stationsMapView.heightProperty().addListener((obs, oldV, newV) -> forceMapResize());
        mapEngine.loadContent(buildMapShellHtml());
    }

    private void forceMapResize() {
        if (mapEngine == null || !mapReady) {
            return;
        }
        try {
            mapEngine.executeScript("if(window.fixMapSize){window.fixMapSize();}");
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void fetchAsync() {
        refreshButton.setDisable(true);
        statusLabel.setText("Chargement...");

        Thread worker = new Thread(() -> {
            try {
                OpenAqService.Result result = openAqService.getLocations(TUNIS_LAT, TUNIS_LON, 25_000, 150);
                Platform.runLater(() -> {
                    refreshButton.setDisable(false);
                    if (!result.success()) {
                        pm25Label.setText("-");
                        pm10Label.setText("-");
                        updatedLabel.setText("-");
                        statusLabel.setText(result.message() == null ? "OpenAQ indisponible" : result.message());
                        updateMapStations(result.stations());
                        return;
                    }

                    int stationCount = result.stations().size();
                    long pm25Count = result.stations().stream().filter(s -> s.pollutants.stream().anyMatch(p -> {
                        String x = p == null ? "" : p.toLowerCase();
                        return x.contains("pm2.5") || x.contains("pm25");
                    })).count();
                    long pm10Count = result.stations().stream().filter(s -> s.pollutants.stream().anyMatch(p -> {
                        String x = p == null ? "" : p.toLowerCase();
                        return x.contains("pm10");
                    })).count();
                    pm25Label.setText(pm25Count > 0 ? pm25Count + " stations" : "Aucune");
                    pm10Label.setText(pm10Count > 0 ? pm10Count + " stations" : "Aucune");
                    updatedLabel.setText(String.valueOf(stationCount));
                    statusLabel.setText("OpenAQ charge (" + stationCount + " stations).");
                    if (stationCount == 0) {
                        System.err.println("[OpenAQ] 0 stations after parsing. Verify API payload and coordinates.");
                    }
                    updateMapStations(result.stations());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    refreshButton.setDisable(false);
                    pm25Label.setText("-");
                    pm10Label.setText("-");
                    updatedLabel.setText("-");
                    statusLabel.setText("OpenAQ indisponible");
                    updateMapStations(java.util.List.of());
                });
            }
        }, "openaq-air");
        worker.setDaemon(true);
        worker.start();
    }

    private void updateMapStations(java.util.List<OpenAqService.Station> stations) {
        if (mapEngine == null || !mapReady) {
            return;
        }
        JsonArray array = new JsonArray();
        for (OpenAqService.Station station : stations) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", station.name);
            obj.addProperty("provider", station.provider);
            obj.addProperty("latitude", station.latitude);
            obj.addProperty("longitude", station.longitude);
            JsonArray pollutants = new JsonArray();
            for (String pollutant : station.pollutants) {
                pollutants.add(pollutant);
            }
            obj.add("pollutants", pollutants);
            array.add(obj);
        }
        String json = array.toString().replace("\\", "\\\\").replace("'", "\\'");
        try {
            mapEngine.executeScript("window.updateStations('" + json + "');");
            mapEngine.executeScript("if(window.fixMapSize){window.fixMapSize();}");
        } catch (Exception ignored) {
            // ignore runtime JS failures
        }
    }

    private String buildMapShellHtml() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset='UTF-8'>
                    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
                    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
                    <style>
                        html,body{width:100%;height:100%;margin:0;padding:0;overflow:hidden;background:#f4f7fb;}
                        #map{width:100%;height:100%;}
                    </style>
                </head>
                <body>
                <div id='map'></div>
                <script>
                    const map = L.map('map', {zoomControl:true}).setView([36.8065,10.1815],8);
                    const layers = [
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom:19, attribution:'&copy; OpenStreetMap contributors'}),
                        L.tileLayer('https://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {maxZoom:19, attribution:'&copy; OpenStreetMap contributors, HOT'})
                    ];
                    let activeLayer = null;
                    let markersLayer = L.layerGroup().addTo(map);
                    function applyLayer(i){
                        if(activeLayer){ map.removeLayer(activeLayer); }
                        activeLayer = layers[i];
                        activeLayer.addTo(map);
                    }
                    applyLayer(0);
                    layers[0].on('tileerror', function(){ applyLayer(1); });
                    function esc(v){
                        return String(v ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
                    }
                    window.updateStations = function(stationsJson){
                        markersLayer.clearLayers();
                        let stations = [];
                        try { stations = JSON.parse(stationsJson || '[]'); } catch(e){ stations = []; }
                        const bounds = [];
                        stations.forEach(s => {
                            const lat = Number(s.latitude);
                            const lng = Number(s.longitude);
                            if(!isFinite(lat) || !isFinite(lng)){ return; }
                            const pollutants = Array.isArray(s.pollutants) && s.pollutants.length ? s.pollutants.join(', ') : 'Aucun';
                            const popup = '<b>' + esc(s.name || 'Station') + '</b><br/>' +
                                'Provider: ' + esc(s.provider || 'OpenAQ') + '<br/>' +
                                'Polluants suivis: ' + esc(pollutants);
                            L.marker([lat, lng]).bindPopup(popup).addTo(markersLayer);
                            bounds.push([lat, lng]);
                        });
                        if(bounds.length){
                            map.fitBounds(bounds, {padding:[24,24]});
                        } else {
                            map.setView([36.8065,10.1815],8);
                        }
                        if(window.fixMapSize){ window.fixMapSize(); }
                    };
                    window.fixMapSize = function(){
                        setTimeout(function(){ map.invalidateSize(true); }, 160);
                    };
                    window.addEventListener('resize', window.fixMapSize);
                    window.fixMapSize();
                </script>
                </body>
                </html>
                """;
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDeclareWaste() {
        Main.showDeclarationCitizenFormPage();
    }

    @FXML
    public void handleMyDeclarations() {
        Main.showCitizenMyDeclarationsPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showCitizenStatisticsPage();
    }

    @FXML
    public void handleNews() {
        Main.showCitizenNewsPage();
    }

    @FXML
    public void handleAirQuality() {
        Main.showCitizenAirQualityPage();
    }

    @FXML
    public void handleWithdraw() {
        Main.showCitizenWithdrawPage();
    }

    @FXML
    public void handleProfile() {
        Main.showCitizenSettingsPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}
