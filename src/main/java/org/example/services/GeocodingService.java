package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GeocodingService {

    private static final String API_KEY = "eef41da01abe4e6798e450ff01625166";
    private static final String BASE_URL = "https://api.opencagedata.com/geocode/v1/json";
    private static final OkHttpClient client = new OkHttpClient();

    public static double[] geocodeAddress(String address) {
        try {
            String url = BASE_URL + "?q=" + java.net.URLEncoder.encode(address, "UTF-8") + "&key=" + API_KEY + "&language=fr";

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string();
                JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

                JsonArray results = json.getAsJsonArray("results");
                if (results.size() > 0) {
                    JsonObject geometry = results.get(0).getAsJsonObject().getAsJsonObject("geometry");
                    double lat = geometry.get("lat").getAsDouble();
                    double lng = geometry.get("lng").getAsDouble();
                    return new double[]{lat, lng};
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String reverseGeocode(double latitude, double longitude) {
        try {
            String url = BASE_URL + "?q=" + latitude + "," + longitude + "&key=" + API_KEY + "&language=fr";

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string();
                JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

                JsonArray results = json.getAsJsonArray("results");
                if (results.size() > 0) {
                    return results.get(0).getAsJsonObject().get("formatted").getAsString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}