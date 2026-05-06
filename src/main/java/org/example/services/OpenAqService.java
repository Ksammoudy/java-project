package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OpenAqService {
    private static final String ENDPOINT = "https://api.openaq.org/v3/locations";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public record Station(String name, String provider, double latitude, double longitude, List<String> parameters) {
    }

    public record Result(boolean success, String message, List<Station> stations) {
    }

    public Result getLocations(double latitude, double longitude, int radius, int limit) {
        String apiKey = ApiConfig.openAqApiKey();
        if (apiKey.isBlank()) {
            return new Result(false, "OPENAQ_API_KEY manquante.", List.of());
        }

        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeRadius = Math.max(1, Math.min(radius, 1_000_000));
        List<String> tries = List.of(
                "coordinates=" + enc(String.format("%.4f,%.4f", latitude, longitude)) + "&radius=" + safeRadius + "&limit=" + safeLimit,
                "country=TN&limit=" + safeLimit,
                "limit=" + safeLimit
        );

        String lastMessage = "Aucune station disponible.";
        for (String query : tries) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ENDPOINT + "?" + query))
                        .header("X-API-Key", apiKey)
                        .header("Accept", "application/json")
                        .timeout(Duration.ofSeconds(20))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    lastMessage = "OpenAQ indisponible (" + response.statusCode() + ").";
                    continue;
                }
                JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray results = payload.has("results") && payload.get("results").isJsonArray()
                        ? payload.getAsJsonArray("results")
                        : new JsonArray();
                List<Station> stations = mapStations(results);
                if (!stations.isEmpty()) {
                    return new Result(true, null, stations);
                }
            } catch (Exception e) {
                lastMessage = "Erreur OpenAQ: " + e.getMessage();
            }
        }
        return new Result(false, lastMessage, List.of());
    }

    private List<Station> mapStations(JsonArray results) {
        List<Station> stations = new ArrayList<>();
        results.forEach(item -> {
            if (!item.isJsonObject()) {
                return;
            }
            JsonObject location = item.getAsJsonObject();
            Double lat = null;
            Double lon = null;
            if (location.has("coordinates") && location.get("coordinates").isJsonObject()) {
                JsonObject c = location.getAsJsonObject("coordinates");
                if (c.has("latitude") && !c.get("latitude").isJsonNull()) {
                    lat = c.get("latitude").getAsDouble();
                } else if (c.has("lat") && !c.get("lat").isJsonNull()) {
                    lat = c.get("lat").getAsDouble();
                }
                if (c.has("longitude") && !c.get("longitude").isJsonNull()) {
                    lon = c.get("longitude").getAsDouble();
                } else if (c.has("lng") && !c.get("lng").isJsonNull()) {
                    lon = c.get("lng").getAsDouble();
                } else if (c.has("lon") && !c.get("lon").isJsonNull()) {
                    lon = c.get("lon").getAsDouble();
                }
            }
            if (lat == null && location.has("latitude") && !location.get("latitude").isJsonNull()) {
                lat = location.get("latitude").getAsDouble();
            }
            if (lon == null && location.has("longitude") && !location.get("longitude").isJsonNull()) {
                lon = location.get("longitude").getAsDouble();
            }
            if (lat == null || lon == null) {
                return;
            }
            Set<String> parameters = new LinkedHashSet<>();
            if (location.has("sensors") && location.get("sensors").isJsonArray()) {
                location.getAsJsonArray("sensors").forEach(sensorItem -> {
                    if (!sensorItem.isJsonObject()) {
                        return;
                    }
                    JsonObject sensor = sensorItem.getAsJsonObject();
                    if (sensor.has("parameter") && sensor.get("parameter").isJsonObject()) {
                        JsonObject parameter = sensor.getAsJsonObject("parameter");
                        if (parameter.has("name") && !parameter.get("name").isJsonNull()) {
                            parameters.add(parameter.get("name").getAsString());
                        }
                    }
                });
            }
            String provider = "OpenAQ";
            if (location.has("providers") && location.get("providers").isJsonArray() && !location.getAsJsonArray("providers").isEmpty()) {
                JsonObject p = location.getAsJsonArray("providers").get(0).getAsJsonObject();
                if (p.has("name") && !p.get("name").isJsonNull()) {
                    provider = p.get("name").getAsString();
                }
            }
            String name = location.has("name") && !location.get("name").isJsonNull() ? location.get("name").getAsString() : "Station";
            stations.add(new Station(name, provider, lat, lon, new ArrayList<>(parameters)));
        });
        return stations;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

