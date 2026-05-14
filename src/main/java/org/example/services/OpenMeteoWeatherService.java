package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

public final class OpenMeteoWeatherService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public WeatherReading fetch(double latitude, double longitude) throws Exception {
        String url = String.format(
                Locale.ROOT,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&timezone=auto",
                latitude, longitude
        );
        System.out.println("[METEO] URL=" + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        System.out.println("[METEO] HTTP status=" + status);

        String body = response.body() == null ? "" : response.body();
        if (status != 200) {
            System.err.println("[METEO] Raw response (error): " + body);
            throw new IllegalStateException("HTTP " + status);
        }

        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("current") || !root.get("current").isJsonObject()) {
            throw new IllegalStateException("Champ current absent.");
        }

        JsonObject current = root.getAsJsonObject("current");
        Double temperature = getDouble(current, "temperature_2m");
        Double humidity = getDouble(current, "relative_humidity_2m");
        Double feelsLike = getDouble(current, "apparent_temperature");
        Double precipitation = getDouble(current, "precipitation");
        Double windSpeed = getDouble(current, "wind_speed_10m");
        Integer weatherCode = getInt(current, "weather_code");
        String updatedAt = current.has("time") && !current.get("time").isJsonNull()
                ? current.get("time").getAsString()
                : "--";

        if (temperature == null || humidity == null || feelsLike == null || precipitation == null || windSpeed == null || weatherCode == null) {
            throw new IllegalStateException("Donnees meteo incompletes.");
        }

        return new WeatherReading(
                temperature,
                feelsLike,
                humidity,
                windSpeed,
                precipitation,
                weatherCode,
                weatherCodeToText(weatherCode),
                updatedAt
        );
    }

    private static Double getDouble(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception ex) {
            return null;
        }
    }

    private static Integer getInt(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String weatherCodeToText(int code) {
        if (code == 0) {
            return "Clear sky \u2600\ufe0f";
        }
        if (code >= 1 && code <= 3) {
            return "Partly cloudy \u26c5";
        }
        if (code >= 45 && code <= 48) {
            return "Fog \ud83c\udf2b\ufe0f";
        }
        if (code >= 51 && code <= 67) {
            return "Rain \ud83c\udf27\ufe0f";
        }
        if (code >= 71 && code <= 77) {
            return "Snow \u2744\ufe0f";
        }
        if (code == 95) {
            return "Thunderstorm \u26c8\ufe0f";
        }
        return "Weather update";
    }

    public record WeatherReading(
            double temperatureC,
            double feelsLikeC,
            double humidityPercent,
            double windKmh,
            double rainMm,
            int weatherCode,
            String weatherText,
            String updatedAt
    ) {
    }
}
