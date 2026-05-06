package org.example.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApiConfig {

    private static final Properties PROPERTIES = loadProperties();

    private ApiConfig() {
    }

    public static String newsApiKey() {
        return get("NEWS_API_KEY", "news.api.key");
    }

    public static String openAqApiKey() {
        return get("OPENAQ_API_KEY", "openaq.api.key");
    }

    public static String huggingFaceApiKey() {
        return get("HUGGINGFACE_API_KEY", "huggingface.api.key");
    }

    public static String stripeSecretKey() {
        return get("STRIPE_SECRET_KEY", "stripe.secret.key");
    }

    public static String stripeCurrency() {
        String value = get("STRIPE_PAYOUT_CURRENCY", "stripe.payout.currency");
        return value.isBlank() ? "usd" : value;
    }

    public static String appBaseUrl() {
        String value = firstNonBlank(
                System.getenv("STRIPE_APP_BASE_URL"),
                System.getenv("APP_BASE_URL"),
                System.getProperty("stripe.app.base.url"),
                System.getProperty("app.base.url"),
                PROPERTIES.getProperty("stripe.app.base.url"),
                PROPERTIES.getProperty("app.base.url")
        );
        return value.isBlank() ? "http://127.0.0.1:8000" : value;
    }

    public static String stripeConnectedAccountCountry() {
        String value = get("STRIPE_CONNECTED_ACCOUNT_COUNTRY", "stripe.connected.account.country");
        return value.isBlank() ? "TN" : value;
    }

    private static String get(String env, String propertyKey) {
        String envValue = System.getenv(env);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        String fileValue = PROPERTIES.getProperty(propertyKey);
        return fileValue == null ? "" : fileValue.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = ApiConfig.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }
}
