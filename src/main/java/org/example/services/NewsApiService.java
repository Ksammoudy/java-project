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
import java.util.List;
import java.util.Locale;

public class NewsApiService {
    private static final String ENDPOINT = "https://newsapi.org/v2/everything";
    private static final List<String> POLLUTION_KEYWORDS = List.of(
            "pollution", "polluant", "polluted", "polluting", "contamination", "smog", "air quality", "water quality"
    );

    public record Article(String title, String description, String url, String image, String source, String publishedAt) {
    }

    public record Result(boolean available, String message, List<Article> articles) {
    }

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public Result getWasteAndEnergyNews(int limit) {
        String apiKey = ApiConfig.newsApiKey();
        if (apiKey.isBlank()) {
            return new Result(false, "NEWS_API_KEY manquante.", List.of());
        }

        int pageSize = Math.max(1, Math.min(limit, 20));
        try {
            Result first = fetch(apiKey, pageSize, true);
            if (first.available() && first.articles().isEmpty()) {
                first = fetch(apiKey, pageSize, false);
            }
            if (!first.available()) {
                return first;
            }
            return new Result(true, first.articles().isEmpty() ? "Aucun article pollution recent trouve." : null, first.articles());
        } catch (Exception e) {
            return new Result(false, "Impossible de charger les nouveautes pour le moment.", List.of());
        }
    }

    private Result fetch(String apiKey, int pageSize, boolean frenchOnly) throws Exception {
        StringBuilder query = new StringBuilder()
                .append("q=").append(enc("(\"pollution\" OR \"air pollution\" OR \"water pollution\")"))
                .append("&searchIn=title,description,content")
                .append("&sortBy=publishedAt")
                .append("&pageSize=").append(pageSize);
        if (frenchOnly) {
            query.append("&language=fr");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "?" + query))
                .header("X-Api-Key", apiKey)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return new Result(false, "NewsAPI indisponible (" + response.statusCode() + ").", List.of());
        }

        JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray array = payload.has("articles") && payload.get("articles").isJsonArray()
                ? payload.getAsJsonArray("articles")
                : new JsonArray();
        List<Article> mapped = new ArrayList<>();
        array.forEach(item -> {
            if (!item.isJsonObject()) {
                return;
            }
            JsonObject a = item.getAsJsonObject();
            String title = asString(a, "title");
            String description = asString(a, "description");
            String content = asString(a, "content");
            String normalized = (title + " " + description + " " + content).toLowerCase(Locale.ROOT);
            boolean isPollution = POLLUTION_KEYWORDS.stream().anyMatch(normalized::contains);
            if (!isPollution || mapped.size() >= pageSize) {
                return;
            }
            JsonObject sourceObj = a.has("source") && a.get("source").isJsonObject() ? a.getAsJsonObject("source") : new JsonObject();
            mapped.add(new Article(
                    title.isBlank() ? "Sans titre" : title,
                    description,
                    asString(a, "url"),
                    asString(a, "urlToImage"),
                    asString(sourceObj, "name").isBlank() ? "Source inconnue" : asString(sourceObj, "name"),
                    asString(a, "publishedAt")
            ));
        });
        return new Result(true, null, mapped);
    }

    private static String asString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

