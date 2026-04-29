package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import org.example.Main;
import org.example.models.ZonePolluee;
import org.example.services.ZonePollueeDAO;
import netscape.javascript.JSObject;

import java.util.List;

public class MapController {

    @FXML private WebView mapWebView;

    private ZonePollueeDAO zoneDAO = new ZonePollueeDAO();
    private WebEngine webEngine;
    private boolean isRouteMode = false;
    private ZonePolluee startZone = null;
    private ZonePolluee endZone = null;
    private boolean measuringMode = false;

    @FXML
    public void initialize() {
        webEngine = mapWebView.getEngine();

        // Enable JavaScript
        webEngine.setJavaScriptEnabled(true);

        // Add Java bridge for callback
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", this);
            }
        });

        loadMap();
    }

    // Method called from JavaScript for geolocation
    public void setUserLocation(double lat, double lng) {
        javafx.application.Platform.runLater(() -> {
            String script = String.format(
                    "if(window.userMarker) map.removeLayer(window.userMarker);" +
                            "window.userMarker = L.marker([%f, %f], {" +
                            "   icon: L.divIcon({" +
                            "       html: '<div style=\"background-color: #4285f4; width: 20px; height: 20px; border-radius: 50%%; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);\"></div>'," +
                            "       iconSize: [20,20]" +
                            "   })" +
                            "}).addTo(map).bindPopup('<b>📍 Votre position</b>').openPopup();" +
                            "map.setView([%f, %f], 14);",
                    lat, lng, lat, lng
            );
            webEngine.executeScript(script);
        });
    }

    @FXML
    private void refreshMap() {
        loadMap();
    }

    @FXML
    private void centerMap() {
        webEngine.executeScript("map.setView([36.8065, 10.1815], 8);");
    }

    @FXML
    private void showMyLocation() {
        webEngine.executeScript(
                "if(navigator.geolocation) {" +
                        "   navigator.geolocation.getCurrentPosition(" +
                        "       function(pos) {" +
                        "           java.setUserLocation(pos.coords.latitude, pos.coords.longitude);" +
                        "       }," +
                        "       function(error) {" +
                        "           alert('Erreur: ' + error.message);" +
                        "       }" +
                        "   );" +
                        "} else {" +
                        "   alert('Géolocalisation non supportée');" +
                        "}"
        );
    }

    @FXML
    private void searchZone() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rechercher une zone");
        dialog.setHeaderText("Entrez le nom de la zone");
        dialog.setContentText("Nom :");

        dialog.showAndWait().ifPresent(searchText -> {
            if (!searchText.isEmpty()) {
                webEngine.executeScript(
                        "var found = false;" +
                                "window.markers.forEach(function(marker) {" +
                                "   if(marker.getPopup().getContent().toLowerCase().includes('" + searchText.toLowerCase() + "')) {" +
                                "       marker.openPopup();" +
                                "       map.setView(marker.getLatLng(), 13);" +
                                "       found = true;" +
                                "   }" +
                                "});" +
                                "if(!found) alert('Aucune zone trouvée');"
                );
            }
        });
    }

    @FXML
    private void startRoute() {
        if (!isRouteMode) {
            isRouteMode = true;
            startZone = null;
            endZone = null;
            showMessage("Mode itinéraire activé. Sélectionnez la zone de départ.");
            selectStartZone();
        } else {
            isRouteMode = false;
            startZone = null;
            endZone = null;
            showMessage("Mode itinéraire désactivé");
        }
    }

    private void selectStartZone() {
        List<ZonePolluee> zones = zoneDAO.getAllZones();
        if (zones.isEmpty()) {
            showMessage("Aucune zone disponible");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("🚗 Zone de départ");
        dialog.setHeaderText("Sélectionnez la zone de départ");

        StringBuilder zoneList = new StringBuilder();
        for (int i = 0; i < zones.size(); i++) {
            zoneList.append(i + 1).append(". ").append(zones.get(i).getNomZone()).append("\n");
        }
        dialog.setContentText("Entrez le numéro de la zone:\n\n" + zoneList.toString());

        dialog.showAndWait().ifPresent(input -> {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < zones.size()) {
                    startZone = zones.get(index);
                    showMessage("✅ Départ: " + startZone.getNomZone());
                    selectEndZone();
                } else {
                    showMessage("Numéro invalide");
                    isRouteMode = false;
                }
            } catch (NumberFormatException e) {
                showMessage("Veuillez entrer un numéro valide");
                isRouteMode = false;
            }
        });
    }

    private void selectEndZone() {
        List<ZonePolluee> zones = zoneDAO.getAllZones();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("🏁 Zone d'arrivée");
        dialog.setHeaderText("Sélectionnez la zone d'arrivée");

        StringBuilder zoneList = new StringBuilder();
        for (int i = 0; i < zones.size(); i++) {
            zoneList.append(i + 1).append(". ").append(zones.get(i).getNomZone()).append("\n");
        }
        dialog.setContentText("Entrez le numéro de la zone:\n\n" + zoneList.toString());

        dialog.showAndWait().ifPresent(input -> {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < zones.size()) {
                    endZone = zones.get(index);
                    calculateAndShowRealRoute();
                } else {
                    showMessage("Numéro invalide");
                    isRouteMode = false;
                }
            } catch (NumberFormatException e) {
                showMessage("Veuillez entrer un numéro valide");
                isRouteMode = false;
            }
        });
    }

    private void calculateAndShowRealRoute() {
        String[] startCoords = startZone.getCoordonneesGps().split(",");
        String[] endCoords = endZone.getCoordonneesGps().split(",");

        if (startCoords.length == 2 && endCoords.length == 2) {
            double startLat = Double.parseDouble(startCoords[0].trim());
            double startLng = Double.parseDouble(startCoords[1].trim());
            double endLat = Double.parseDouble(endCoords[0].trim());
            double endLng = Double.parseDouble(endCoords[1].trim());

            getRealRouteFromOSRM(startLat, startLng, endLat, endLng, startZone.getNomZone(), endZone.getNomZone());
        }
        isRouteMode = false;
    }

    private void getRealRouteFromOSRM(double startLat, double startLng, double endLat, double endLng, String startName, String endName) {
        // Create a JavaScript function to handle the routing
        String setupRouteFunction =
                "window.calculateRealRoute = function(startLat, startLng, endLat, endLng, startName, endName) {" +
                        "   var url = 'https://router.project-osrm.org/route/v1/driving/' + startLng + ',' + startLat + ';' + endLng + ',' + endLat + '?overview=full&geometries=geojson';" +
                        "   fetch(url)" +
                        "       .then(response => response.json())" +
                        "       .then(data => {" +
                        "           if(data.code === 'Ok') {" +
                        "               var route = data.routes[0];" +
                        "               var distance = (route.distance / 1000).toFixed(2);" +
                        "               var duration = (route.duration / 60).toFixed(0);" +
                        "               var coordinates = route.geometry.coordinates;" +
                        "               var latlngs = coordinates.map(coord => [coord[1], coord[0]]);" +
                        "               if(window.currentRoute) map.removeLayer(window.currentRoute);" +
                        "               if(window.startMarker) map.removeLayer(window.startMarker);" +
                        "               if(window.endMarker) map.removeLayer(window.endMarker);" +
                        "               if(window.routeInfo) map.removeControl(window.routeInfo);" +
                        "               window.startMarker = L.marker([startLat, startLng], {" +
                        "                   icon: L.divIcon({html: '<div style=\"background-color: #28a745; width: 30px; height: 30px; border-radius: 50%; border: 2px solid white; text-align: center; line-height: 30px; color: white; font-weight: bold;\">D</div>', iconSize: [30,30]})" +
                        "               }).addTo(map).bindPopup('<b>DÉPART</b><br>' + startName).openPopup();" +
                        "               window.endMarker = L.marker([endLat, endLng], {" +
                        "                   icon: L.divIcon({html: '<div style=\"background-color: #dc3545; width: 30px; height: 30px; border-radius: 50%; border: 2px solid white; text-align: center; line-height: 30px; color: white; font-weight: bold;\">A</div>', iconSize: [30,30]})" +
                        "               }).addTo(map).bindPopup('<b>ARRIVÉE</b><br>' + endName).openPopup();" +
                        "               window.currentRoute = L.polyline(latlngs, {color: '#0066cc', weight: 5, opacity: 0.8}).addTo(map);" +
                        "               var bounds = L.latLngBounds(latlngs);" +
                        "               map.fitBounds(bounds, {padding: [50, 50]});" +
                        "               var RouteInfoControl = L.Control.extend({onAdd: function(map) {" +
                        "                   var div = L.DomUtil.create('div');" +
                        "                   div.innerHTML = '<div style=\"background: white; padding: 10px 15px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.2); font-size: 14px; font-weight: bold;\">📏 Distance: ' + distance + ' km<br>⏱️ Durée: ' + duration + ' min</div>';" +
                        "                   return div;" +
                        "               }});" +
                        "               window.routeInfo = new RouteInfoControl({position: 'bottomleft'}).addTo(map);" +
                        "               java.showRouteInfo(distance, duration, startName, endName);" +
                        "           } else {" +
                        "               alert('Impossible de calculer l\\'itinéraire');" +
                        "           }" +
                        "       })" +
                        "       .catch(error => {" +
                        "           console.error('Error:', error);" +
                        "           alert('Erreur de connexion au service de routage');" +
                        "       });" +
                        "};";

        webEngine.executeScript(setupRouteFunction);

        String callScript = String.format(
                "window.calculateRealRoute(%f, %f, %f, %f, '%s', '%s');",
                startLat, startLng, endLat, endLng, startName.replace("'", "\\'"), endName.replace("'", "\\'")
        );

        webEngine.executeScript(callScript);
    }

    // Called from JavaScript to show route info
    public void showRouteInfo(double distance, double duration, String startName, String endName) {
        javafx.application.Platform.runLater(() -> {
            showMessage(String.format("✅ Itinéraire: %s → %s\n📏 %.2f km\n⏱️ %.0f minutes",
                    startName, endName, distance, duration));
        });
    }

    @FXML
    private void startMeasurement() {
        measuringMode = !measuringMode;

        if (measuringMode) {
            String setupMeasureFunction =
                    "if(!window.measureClickHandler) {" +
                            "   window.measureClickHandler = function(e) {" +
                            "       if(!window.measurePoints) window.measurePoints = [];" +
                            "       if(!window.measureMarkers) window.measureMarkers = [];" +
                            "       window.measurePoints.push(e.latlng);" +
                            "       var marker = L.marker(e.latlng, {" +
                            "           icon: L.divIcon({html: '<div style=\"background: #ff9800; width: 15px; height: 15px; border-radius: 50%; border: 2px solid white;\"></div>', iconSize: [15,15]})" +
                            "       }).addTo(map).bindPopup('Point ' + window.measurePoints.length);" +
                            "       window.measureMarkers.push(marker);" +
                            "       if(window.measurePoints.length === 2) {" +
                            "           var distance = window.measurePoints[0].distanceTo(window.measurePoints[1]);" +
                            "           var distanceKm = (distance / 1000).toFixed(2);" +
                            "           var distanceM = distance.toFixed(2);" +
                            "           var message = distanceKm >= 1 ? distanceKm + ' km' : distanceM + ' m';" +
                            "           if(window.measureLine) map.removeLayer(window.measureLine);" +
                            "           window.measureLine = L.polyline(window.measurePoints, {color: '#ff9800', weight: 3, dashArray: '5, 5'}).addTo(map);" +
                            "           var midPoint = L.latLng((window.measurePoints[0].lat + window.measurePoints[1].lat)/2, (window.measurePoints[0].lng + window.measurePoints[1].lng)/2);" +
                            "           L.marker(midPoint, {icon: L.divIcon({html: '<div style=\"background: #ff9800; color: white; padding: 5px 10px; border-radius: 20px; font-size: 12px; font-weight: bold;\">' + message + '</div>', iconSize: [80, 30]})}).addTo(map).bindPopup('Distance: ' + message).openPopup();" +
                            "           alert('📏 Distance mesurée: ' + message);" +
                            "           window.measurePoints = [];" +
                            "           window.measureMarkers = [];" +
                            "           window.measuring = false;" +
                            "           map.off('click', window.measureClickHandler);" +
                            "       }" +
                            "   };" +
                            "}" +
                            "window.measurePoints = [];" +
                            "window.measureMarkers = [];" +
                            "window.measuring = true;" +
                            "map.on('click', window.measureClickHandler);" +
                            "alert('📏 Mode mesure activé. Cliquez sur deux points sur la carte.');";

            webEngine.executeScript(setupMeasureFunction);
            showMessage("Mode mesure activé - Cliquez sur 2 points");
        } else {
            webEngine.executeScript(
                    "if(window.measureClickHandler) map.off('click', window.measureClickHandler);" +
                            "if(window.measureLine) map.removeLayer(window.measureLine);" +
                            "if(window.measureMarkers) window.measureMarkers.forEach(function(m) { map.removeLayer(m); });" +
                            "window.measurePoints = [];" +
                            "window.measureMarkers = [];" +
                            "alert('Mesure terminée');"
            );
            showMessage("Mode mesure désactivé");
        }
    }

    @FXML
    private void filterHighRisk() {
        webEngine.executeScript(
                "window.markers.forEach(function(marker) {" +
                        "   if(marker.options.risk === 'high') {" +
                        "       if(!map.hasLayer(marker)) marker.addTo(map);" +
                        "   } else {" +
                        "       if(map.hasLayer(marker)) map.removeLayer(marker);" +
                        "   }" +
                        "});"
        );
    }

    @FXML
    private void filterMediumRisk() {
        webEngine.executeScript(
                "window.markers.forEach(function(marker) {" +
                        "   if(marker.options.risk === 'medium') {" +
                        "       if(!map.hasLayer(marker)) marker.addTo(map);" +
                        "   } else {" +
                        "       if(map.hasLayer(marker)) map.removeLayer(marker);" +
                        "   }" +
                        "});"
        );
    }

    @FXML
    private void filterLowRisk() {
        webEngine.executeScript(
                "window.markers.forEach(function(marker) {" +
                        "   if(marker.options.risk === 'low') {" +
                        "       if(!map.hasLayer(marker)) marker.addTo(map);" +
                        "   } else {" +
                        "       if(map.hasLayer(marker)) map.removeLayer(marker);" +
                        "   }" +
                        "});"
        );
    }

    @FXML
    private void showAllZones() {
        webEngine.executeScript(
                "window.markers.forEach(function(marker) {" +
                        "   if(!map.hasLayer(marker)) marker.addTo(map);" +
                        "});"
        );
    }

    @FXML
    private void toggleHeatmap() {
        webEngine.executeScript(
                "if(window.heatLayer) {" +
                        "   if(map.hasLayer(window.heatLayer)) map.removeLayer(window.heatLayer);" +
                        "   else map.addLayer(window.heatLayer);" +
                        "} else {" +
                        "   var heatData = window.markers.map(function(m) {" +
                        "       return [m.getLatLng().lat, m.getLatLng().lng, m.options.intensity];" +
                        "   });" +
                        "   window.heatLayer = L.heatLayer(heatData, {radius: 25, blur: 15, maxZoom: 10}).addTo(map);" +
                        "}"
        );
    }

    private void loadMap() {
        List<ZonePolluee> zones = zoneDAO.getAllZones();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'/>");
        html.append("<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>");
        html.append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>");
        html.append("<script src='https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js'></script>");
        html.append("<style>");
        html.append("body, html { margin: 0; padding: 0; height: 100%; }");
        html.append("#map { height: 100%; width: 100%; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<div id='map'></div>");
        html.append("<script>");
        html.append("var map = L.map('map').setView([36.8065, 10.1815], 8);");
        html.append("L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {");
        html.append("attribution: '© OpenStreetMap contributors'");
        html.append("}).addTo(map);");
        html.append("window.markers = [];");
        html.append("window.heatLayer = null;");

        for (ZonePolluee zone : zones) {
            String[] coords = zone.getCoordonneesGps().split(",");
            if (coords.length == 2) {
                try {
                    double lat = Double.parseDouble(coords[0].trim());
                    double lng = Double.parseDouble(coords[1].trim());
                    int niveau = zone.getNiveauPollution();

                    String color;
                    String risk;
                    int intensity;
                    if (niveau >= 7) {
                        color = "#dc3545";
                        risk = "high";
                        intensity = 10;
                    } else if (niveau >= 4) {
                        color = "#ffc107";
                        risk = "medium";
                        intensity = 5;
                    } else {
                        color = "#28a745";
                        risk = "low";
                        intensity = 2;
                    }

                    html.append(String.format(
                            "var marker = L.marker([%f, %f], {" +
                                    "icon: L.divIcon({html: '<div style=\"background-color: %s; width: 20px; height: 20px; border-radius: 50%%; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);\"></div>', iconSize: [20,20]})," +
                                    "risk: '%s', intensity: %d" +
                                    "}).addTo(map).bindPopup('<b>🏭 %s</b><br>📊 Niveau: %d/10<br>⚠️ Risque: ' + getRiskLevel(%d) + '<br><button onclick=\"java.calculateRouteFromPoint(%f, %f, \\'%s\\')\" style=\"margin-top:5px;padding:5px;background:#0066cc;color:white;border:none;border-radius:3px;cursor:pointer;\">🚗 Itinéraire depuis ici</button>');" +
                                    "window.markers.push(marker);",
                            lat, lng, color, risk, intensity, zone.getNomZone(), niveau, niveau, lat, lng, zone.getNomZone().replace("'", "\\'")
                    ));

                } catch (NumberFormatException e) {
                    System.err.println("Coordonnées invalides: " + zone.getCoordonneesGps());
                }
            }
        }

        html.append("function getRiskLevel(level) {");
        html.append("   if(level >= 7) return '🔴 Élevé';");
        html.append("   else if(level >= 4) return '🟡 Moyen';");
        html.append("   else return '🟢 Faible';");
        html.append("}");

        html.append("</script>");
        html.append("</body></html>");

        webEngine.loadContent(html.toString());
        System.out.println("✅ Carte chargée avec " + zones.size() + " zones");
    }

    // Called from JavaScript when user clicks "Itinéraire depuis ici" on a marker
    public void calculateRouteFromPoint(double lat, double lng, String name) {
        javafx.application.Platform.runLater(() -> {
            List<ZonePolluee> zones = zoneDAO.getAllZones();
            for (ZonePolluee zone : zones) {
                String[] coords = zone.getCoordonneesGps().split(",");
                if (coords.length == 2) {
                    double zoneLat = Double.parseDouble(coords[0].trim());
                    double zoneLng = Double.parseDouble(coords[1].trim());
                    if (Math.abs(zoneLat - lat) < 0.0001 && Math.abs(zoneLng - lng) < 0.0001) {
                        startZone = zone;
                        break;
                    }
                }
            }

            if (startZone != null) {
                showMessage("✅ Départ sélectionné: " + startZone.getNomZone());
                selectEndZone();
            } else {
                showMessage("Erreur: Zone non trouvée");
            }
        });
    }

    private void showMessage(String message) {
        javafx.application.Platform.runLater(() -> {
            try {
                String escapedMessage = message.replace("'", "\\'").replace("\n", "\\n");
                webEngine.executeScript("alert('" + escapedMessage + "');");
            } catch (Exception e) {
                System.out.println("Message: " + message);
            }
        });
    }

    @FXML private void goToDashboard() { Main.showDashboardAdmin(); }
    @FXML private void goToZones() { Main.showZonePollueeListPage(); }
    @FXML private void goToIndicateurs() { Main.showIndicateurImpactListPage(); }
    @FXML private void goToQRDashboard() { Main.showQRDashboardPage(); }
}