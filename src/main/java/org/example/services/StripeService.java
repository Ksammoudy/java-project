package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.models.User;
import org.example.utils.DBConnection;

import java.awt.Desktop;
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
import java.util.ArrayList;
import java.util.List;

public class StripeService {
    private static final String STRIPE_BASE_URL = "https://api.stripe.com/v1";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    public record Result(boolean success, String message, String payoutId, String accountId) {
    }

    public boolean isEnabled() {
        return !ApiConfig.stripeSecretKey().isBlank();
    }

    public String payoutCurrency() {
        return ApiConfig.stripeCurrency().toUpperCase();
    }

    public Result createPayout(User user, int amountMinor, String description) {
        if (!isEnabled()) {
            return new Result(false, "Stripe n est pas configure (STRIPE_SECRET_KEY manquante).", null, null);
        }
        if (amountMinor <= 0) {
            return new Result(false, "Montant de retrait invalide.", null, null);
        }
        try {
            String accountId = ensureConnectedAccount(user);
            if (accountId == null || accountId.isBlank()) {
                return new Result(false, "Compte Stripe invalide.", null, null);
            }
            JsonObject response = stripeRequest("POST", "/payouts",
                    List.of(
                            pair("amount", String.valueOf(amountMinor)),
                            pair("currency", ApiConfig.stripeCurrency().toLowerCase()),
                            pair("description", description)
                    ), accountId);
            if (response.has("error")) {
                return new Result(false, extractStripeError(response), null, accountId);
            }
            String payoutId = response.has("id") ? response.get("id").getAsString() : null;
            if (payoutId == null || payoutId.isBlank()) {
                return new Result(false, "Stripe a retourne une reponse payout invalide.", null, accountId);
            }
            return new Result(true, "Retrait Stripe effectue.", payoutId, accountId);
        } catch (Exception e) {
            return new Result(false, "Erreur reseau Stripe: " + e.getMessage(), null, null);
        }
    }

    public Result openOnboarding(User user) {
        if (!isEnabled()) {
            return new Result(false, "Stripe n est pas configure (STRIPE_SECRET_KEY manquante).", null, null);
        }
        try {
            String accountId = ensureConnectedAccount(user);
            if (accountId == null || accountId.isBlank()) {
                return new Result(false, "Compte Stripe invalide.", null, null);
            }

            String baseUrl = ApiConfig.appBaseUrl().replaceAll("/+$", "");
            JsonObject response = stripeRequest("POST", "/account_links",
                    List.of(
                            pair("account", accountId),
                            pair("refresh_url", baseUrl + "/citoyen/withdraw?stripe=retry"),
                            pair("return_url", baseUrl + "/citoyen/withdraw?stripe=done"),
                            pair("type", "account_onboarding")
                    ), null);
            if (response.has("error")) {
                return new Result(false, extractStripeError(response), null, accountId);
            }
            String url = response.has("url") ? response.get("url").getAsString() : null;
            if (url == null || url.isBlank()) {
                return new Result(false, "Stripe a retourne un lien onboarding invalide.", null, accountId);
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            }
            return new Result(true, "Onboarding Stripe ouvert dans le navigateur.", null, accountId);
        } catch (Exception e) {
            return new Result(false, "Erreur reseau Stripe: " + e.getMessage(), null, null);
        }
    }

    private String ensureConnectedAccount(User user) throws Exception {
        String existing = loadUserStripeAccountId(user.getId());
        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        List<NameValue> body = new ArrayList<>();
        body.add(pair("type", "express"));
        body.add(pair("country", ApiConfig.stripeConnectedAccountCountry().toUpperCase()));
        body.add(pair("email", user.getEmail()));
        body.add(pair("capabilities[transfers][requested]", "true"));
        body.add(pair("business_type", "individual"));
        body.add(pair("metadata[wastewise_user_id]", String.valueOf(user.getId())));
        body.add(pair("metadata[wastewise_email]", user.getEmail() == null ? "" : user.getEmail()));

        JsonObject response = stripeRequest("POST", "/accounts", body, null);
        if (response.has("error")) {
            return null;
        }
        String accountId = response.has("id") ? response.get("id").getAsString() : null;
        if (accountId != null && !accountId.isBlank()) {
            saveUserStripeAccountId(user.getId(), accountId);
        }
        return accountId;
    }

    private JsonObject stripeRequest(String method, String path, List<NameValue> form, String stripeAccount) throws Exception {
        String secretKey = ApiConfig.stripeSecretKey();
        String body = encodeForm(form);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(STRIPE_BASE_URL + path))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30));
        if (stripeAccount != null && !stripeAccount.isBlank()) {
            builder.header("Stripe-Account", stripeAccount);
        }
        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else {
            builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() >= 400) {
            JsonObject errorEnvelope = new JsonObject();
            errorEnvelope.add("error", payload.has("error") ? payload.get("error") : payload);
            return errorEnvelope;
        }
        return payload;
    }

    private String extractStripeError(JsonObject payload) {
        if (payload.has("error") && payload.get("error").isJsonObject()) {
            JsonObject e = payload.getAsJsonObject("error");
            if (e.has("message") && !e.get("message").isJsonNull()) {
                String message = e.get("message").getAsString();
                if (message.toLowerCase().contains("signed up for connect")) {
                    return "Stripe Connect n est pas active sur ce compte.";
                }
                return message;
            }
        }
        return "Erreur Stripe.";
    }

    private String loadUserStripeAccountId(int userId) {
        if (userId <= 0) {
            return null;
        }
        String sql = "SELECT stripe_connect_account_id FROM user WHERE id = ?";
        Connection cnx = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private void saveUserStripeAccountId(int userId, String accountId) {
        if (userId <= 0 || accountId == null || accountId.isBlank()) {
            return;
        }
        String sql = "UPDATE user SET stripe_connect_account_id = ? WHERE id = ?";
        Connection cnx = DBConnection.getInstance().getConnection();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private static NameValue pair(String key, String value) {
        return new NameValue(key, value == null ? "" : value);
    }

    private static String encodeForm(List<NameValue> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(values.get(i).key(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(values.get(i).value(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private record NameValue(String key, String value) {
    }
}
