package org.example.services;

import org.example.models.ZonePolluee;

public class QRCodeService {

    public String generateGoogleMapsUrl(ZonePolluee zone) {
        String[] coords = zone.getCoordonneesGps().split(",");
        if (coords.length == 2) {
            try {
                double lat = Double.parseDouble(coords[0].trim());
                double lng = Double.parseDouble(coords[1].trim());
                return "https://www.google.com/maps?q=" + lat + "," + lng;
            } catch (NumberFormatException e) {
                return "https://www.google.com/maps";
            }
        }
        return "https://www.google.com/maps";
    }

    public String generateQRCodeUrl(ZonePolluee zone) {
        String mapsUrl = generateGoogleMapsUrl(zone);
        return "https://quickchart.io/qr?text=" + java.net.URLEncoder.encode(mapsUrl) + "&size=300";
    }
}