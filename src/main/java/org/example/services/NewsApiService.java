package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.DBConnection;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NewsApiService {
    private static final String ENDPOINT = "https://newsapi.org/v2/everything";
    private static final List<String> DEFAULT_KEYWORDS = List.of(
            "environment", "recycling", "waste", "pollution", "climate"
    );

    public record Article(
            String title,
            String description,
            String sourceName,
            String author,
            String publishedAt,
            String url,
            String urlToImage
    ) {
    }

    public record Result(boolean available, String message, List<Article> articles) {
    }

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public Result getPersonalizedWasteWiseNews(int pageSize, Integer citizenId) {
        String apiKey = ApiConfig.newsApiKey();
        if (apiKey.isBlank()) {
            return new Result(false, "Impossible de charger les nouveautés.", List.of());
        }

        int safePageSize = Math.max(1, Math.min(pageSize, 20));
        List<String> userPreferences = loadWastePreferences(citizenId);
        String query = String.join(" OR ", DEFAULT_KEYWORDS);
        String url = ENDPOINT
                + "?q=" + enc(query)
                + "&language=fr"
                + "&sortBy=publishedAt"
                + "&pageSize=" + safePageSize
                + "&apiKey=" + enc(apiKey);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            System.out.println("[NewsAPI] URL=" + url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String raw = response.body() == null ? "" : response.body().trim();
            System.out.println("[NewsAPI] HTTP status=" + status);

            if (status != 200) {
                System.err.println("[NewsAPI] Raw response (error): " + raw);
                return new Result(false, "Impossible de charger les nouveautés.", List.of());
            }

            JsonObject payload = JsonParser.parseString(raw).getAsJsonObject();
            JsonArray array = payload.has("articles") && payload.get("articles").isJsonArray()
                    ? payload.getAsJsonArray("articles")
                    : new JsonArray();
            List<Article> mapped = mapArticles(array);
            List<Article> sorted = rankByPreferences(mapped, userPreferences);
            return new Result(true, null, sorted);
        } catch (Exception e) {
            return new Result(false, "Impossible de charger les nouveautés.", List.of());
        }
    }

    private List<Article> mapArticles(JsonArray array) {
        List<Article> mapped = new ArrayList<>();
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                continue;
            }
            JsonObject article = item.getAsJsonObject();
            JsonObject sourceObj = article.has("source") && article.get("source").isJsonObject()
                    ? article.getAsJsonObject("source")
                    : new JsonObject();

            String title = asString(article, "title");
            if (title.isBlank()) {
                continue;
            }
            mapped.add(new Article(
                    title,
                    asString(article, "description"),
                    asString(sourceObj, "name"),
                    asString(article, "author"),
                    asString(article, "publishedAt"),
                    asString(article, "url"),
                    asString(article, "urlToImage")
            ));
        }
        return mapped;
    }

    private List<Article> rankByPreferences(List<Article> articles, List<String> preferences) {
        if (preferences.isEmpty()) {
            return articles;
        }
        return articles.stream()
                .sorted(Comparator
                        .comparingInt((Article a) -> scoreArticle(a, preferences)).reversed()
                        .thenComparing((Article a) -> parsePublishedAt(a.publishedAt()), Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private int scoreArticle(Article article, List<String> preferences) {
        String haystack = (safe(article.title()) + " " + safe(article.description())).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String pref : preferences) {
            if (pref.isBlank()) {
                continue;
            }
            if (haystack.contains(pref.toLowerCase(Locale.ROOT))) {
                score += 3;
            }
        }
        for (String keyword : DEFAULT_KEYWORDS) {
            if (haystack.contains(keyword)) {
                score += 1;
            }
        }
        return score;
    }

    private Instant parsePublishedAt(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Instant.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> loadWastePreferences(Integer citizenId) {
        if (citizenId == null || citizenId <= 0) {
            return List.of();
        }
        String sql = "SELECT types_dechets_acceptes FROM `user` WHERE id = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, citizenId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return List.of();
                }
                String raw = rs.getString(1);
                return normalizePreferences(raw);
            }
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<String> normalizePreferences(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replace("[", " ")
                .replace("]", " ")
                .replace("\"", " ")
                .replace("'", " ")
                .replace("/", " ")
                .replace("_", " ");
        String[] tokens = cleaned.split("[,;|\\s]+");
        Set<String> result = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            if (token.length() < 3) {
                continue;
            }
            result.add(token.trim());
        }
        return new ArrayList<>(result);
    }

    private static String asString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
