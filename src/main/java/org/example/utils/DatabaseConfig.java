package org.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public record DatabaseConfig(String url, String username, String password) {

    private static final String DEFAULT_URL = "jdbc:mysql://127.0.0.1:3306/pidev?serverTimezone=UTC&useSSL=false";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

    public static DatabaseConfig load() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger database.properties.", e);
        }

        String url = firstNonBlank(
            System.getProperty("db.url"),
            System.getenv("DB_URL"),
            properties.getProperty("db.url"),
            DEFAULT_URL
        );
        String username = firstNonBlank(
            System.getProperty("db.username"),
            System.getenv("DB_USERNAME"),
            properties.getProperty("db.username"),
            DEFAULT_USERNAME
        );
        String password = firstNonBlank(
            System.getProperty("db.password"),
            System.getenv("DB_PASSWORD"),
            properties.getProperty("db.password"),
            DEFAULT_PASSWORD
        );

        return new DatabaseConfig(url, username, password);
    }

    public String databaseName() {
        String sanitizedUrl = url;
        int querySeparator = sanitizedUrl.indexOf('?');
        if (querySeparator >= 0) {
            sanitizedUrl = sanitizedUrl.substring(0, querySeparator);
        }

        int lastSlash = sanitizedUrl.lastIndexOf('/');
        return lastSlash >= 0 ? sanitizedUrl.substring(lastSlash + 1) : sanitizedUrl;
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }

        return Objects.requireNonNull(candidates[candidates.length - 1]);
    }
}
