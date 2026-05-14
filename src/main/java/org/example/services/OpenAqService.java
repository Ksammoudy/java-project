package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OpenAqService {

    private static final String ENDPOINT = "https://api.openaq.org/v3/locations";
    private static final String FALLBACK_KEY = "268c82faebaee91137e0d3706f8c6b0c87f2d3079c3d07da5fa566deb2fc04c3";
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static class Station {
        public final String name;
        public final String provider;
        public final double latitude;
        public final double longitude;
        public final List<String> pollutants;

        public Station(String name, String provider, double latitude, double longitude, List<String> pollutants) {
            this.name = name;
            this.provider = provider;
            this.latitude = latitude;
            this.longitude = longitude;
            this.pollutants = pollutants;
        }
    }

    public static class Result {
        private final boolean success;
        private final String message;
        private final List<Station> stations;

        public Result(boolean success, String message, List<Station> stations) {
            this.success = success;
            this.message = message;
            this.stations = stations;
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }

        public List<Station> stations() {
            return stations;
        }
    }

    public Result getLocations(double latitude, double longitude, int radius, int limit) {
        String apiKey = resolveApiKey();
        if (apiKey.isBlank()) {
            return new Result(false, "Cle OpenAQ manquante.", List.of());
        }

        int safeLimit = Math.max(1, Math.min(limit, 100));
        String url = ENDPOINT + "?limit=" + safeLimit;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-API-Key", apiKey)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            System.out.println("[OpenAQ] URL=" + url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String raw = response.body() == null ? "" : response.body().trim();
            System.out.println("[OpenAQ] HTTP status=" + status);
            System.out.println("[OpenAQ] Raw response body=" + raw);

            if (status != 200) {
                return new Result(false, "OpenAQ indisponible.", List.of());
            }

            JsonObject payload;
            try {
                payload = JsonParser.parseString(raw).getAsJsonObject();
            } catch (Exception parseEx) {
                return new Result(false, "Reponse OpenAQ invalide.", List.of());
            }

            if (!payload.has("results") || !payload.get("results").isJsonArray()) {
                System.err.println("[OpenAQ] Missing 'results' array in payload.");
                return new Result(false, "Reponse OpenAQ invalide (results absent).", List.of());
            }
            JsonArray results = payload.getAsJsonArray("results");
            System.out.println("[OpenAQ] results[] count=" + results.size());

            List<Station> stations = mapStations(results);
            System.out.println("Loaded OpenAQ stations: " + stations.size());
            if (stations.isEmpty()) {
                return new Result(false, "Aucune station OpenAQ trouvee.", List.of());
            }
            return new Result(true, null, stations);
        } catch (Exception ex) {
            System.err.println("[OpenAQ] Exception=" + ex.getMessage());
            return new Result(false, "Erreur OpenAQ.", List.of());
        }
    }

    private List<Station> mapStations(JsonArray results) {
        List<Station> stations = new ArrayList<>();
        for (JsonElement element : results) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject location = element.getAsJsonObject();
            Double lat = extractLatitude(location);
            Double lon = extractLongitude(location);
            if (lat == null || lon == null) {
                continue;
            }

            String name = location.has("name") && !location.get("name").isJsonNull()
                    ? location.get("name").getAsString()
                    : "Station";

            String provider = "OpenAQ";
            if (location.has("provider") && location.get("provider").isJsonObject()) {
                JsonObject providerObj = location.getAsJsonObject("provider");
                if (providerObj.has("name") && !providerObj.get("name").isJsonNull()) {
                    provider = providerObj.get("name").getAsString();
                }
            }

            Set<String> pollutants = new LinkedHashSet<>();
            if (location.has("sensors") && location.get("sensors").isJsonArray()) {
                for (JsonElement sensorElement : location.getAsJsonArray("sensors")) {
                    if (!sensorElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject sensor = sensorElement.getAsJsonObject();
                    if (sensor.has("parameter") && sensor.get("parameter").isJsonObject()) {
                        JsonObject parameter = sensor.getAsJsonObject("parameter");
                        if (parameter.has("displayName") && !parameter.get("displayName").isJsonNull()) {
                            pollutants.add(parameter.get("displayName").getAsString());
                        } else if (parameter.has("name") && !parameter.get("name").isJsonNull()) {
                            pollutants.add(parameter.get("name").getAsString());
                        }
                    }
                }
            }
            stations.add(new Station(name, provider, lat, lon, new ArrayList<>(pollutants)));
        }
        return stations;
    }

    private Double extractLatitude(JsonObject location) {
        if (location.has("coordinates") && location.get("coordinates").isJsonObject()) {
            JsonObject c = location.getAsJsonObject("coordinates");
            if (c.has("latitude") && !c.get("latitude").isJsonNull()) {
                return c.get("latitude").getAsDouble();
            }
            if (c.has("lat") && !c.get("lat").isJsonNull()) {
                return c.get("lat").getAsDouble();
            }
        }
        if (location.has("latitude") && !location.get("latitude").isJsonNull()) {
            return location.get("latitude").getAsDouble();
        }
        return null;
    }

    private Double extractLongitude(JsonObject location) {
        if (location.has("coordinates") && location.get("coordinates").isJsonObject()) {
            JsonObject c = location.getAsJsonObject("coordinates");
            if (c.has("longitude") && !c.get("longitude").isJsonNull()) {
                return c.get("longitude").getAsDouble();
            }
            if (c.has("lng") && !c.get("lng").isJsonNull()) {
                return c.get("lng").getAsDouble();
            }
            if (c.has("lon") && !c.get("lon").isJsonNull()) {
                return c.get("lon").getAsDouble();
            }
        }
        if (location.has("longitude") && !location.get("longitude").isJsonNull()) {
            return location.get("longitude").getAsDouble();
        }
        return null;
    }

    private String resolveApiKey() {
        String configured = ApiConfig.openAqApiKey();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return FALLBACK_KEY;
    }

}
