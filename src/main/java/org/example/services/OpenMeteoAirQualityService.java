package org.example.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lecture indicative qualite de l'air (PM2.5, PM10) via l'API publique Open-Meteo (sans cle).
 */
public final class OpenMeteoAirQualityService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private OpenMeteoAirQualityService() {
    }

    public static AirQualityReading fetch(double latitude, double longitude) throws Exception {
        String uri = String.format(
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=%.5f&longitude=%.5f&current=pm10,pm2_5",
                latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return parse(response.body());
    }

    private static AirQualityReading parse(String json) {
        int idx = json.indexOf("\"current\"");
        String slice = idx >= 0 ? json.substring(idx) : json;
        Double pm25 = findDouble(slice, "pm2_5");
        Double pm10 = findDouble(slice, "pm10");
        String time = findTime(slice);
        return new AirQualityReading(pm25, pm10, time);
    }

    private static Double findDouble(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\":([0-9.\\-]+)").matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String findTime(String json) {
        Matcher m = Pattern.compile("\"time\":\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    public static final class AirQualityReading {
        private final Double pm25;
        private final Double pm10;
        private final String timeUtc;

        public AirQualityReading(Double pm25, Double pm10, String timeUtc) {
            this.pm25 = pm25;
            this.pm10 = pm10;
            this.timeUtc = timeUtc;
        }

        public Double getPm25() {
            return pm25;
        }

        public Double getPm10() {
            return pm10;
        }

        public String getTimeUtc() {
            return timeUtc;
        }
    }
}
