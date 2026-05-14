package org.example.utils;

public final class WeatherLocationState {

    private static volatile Double latitude;
    private static volatile Double longitude;

    private WeatherLocationState() {
    }

    public static void updateSelectedLocation(Double lat, Double lon) {
        if (lat == null || lon == null) {
            return;
        }
        latitude = lat;
        longitude = lon;
    }

    public static Coordinates getSelectedLocationOrNull() {
        if (latitude == null || longitude == null) {
            return null;
        }
        return new Coordinates(latitude, longitude);
    }

    public record Coordinates(double latitude, double longitude) {
    }
}
