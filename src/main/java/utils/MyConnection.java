package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MyConnection {
    private static MyConnection instance;
    private Connection connection;
    private String activeUrl;
    private final String user;
    private final String password;

    private MyConnection() {
        this.user = resolveConfig("WW_DB_USER", "root");
        this.password = resolveConfig("WW_DB_PASSWORD", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            reconnect();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC Driver not found.", e);
        }
    }

    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                reconnect();
            }
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Database connection is unavailable.", e);
        }
    }

    public synchronized String getActiveUrl() {
        return activeUrl;
    }

    private void reconnect() {
        List<String> candidateUrls = buildCandidateUrls();
        List<String> errors = new ArrayList<>();
        SQLException lastError = null;

        for (String candidateUrl : candidateUrls) {
            try {
                this.connection = DriverManager.getConnection(candidateUrl, user, password);
                this.activeUrl = candidateUrl;
                System.out.println("Connected to MySQL database: " + candidateUrl);
                return;
            } catch (SQLException e) {
                lastError = e;
                errors.add(candidateUrl + " -> " + e.getMessage());
            }
        }

        StringBuilder details = new StringBuilder("Unable to connect to MySQL database.");
        if (!errors.isEmpty()) {
            details.append(" Attempts: ").append(String.join(" | ", errors));
        }
        throw new IllegalStateException(details.toString(), lastError);
    }

    private List<String> buildCandidateUrls() {
        String explicitUrl = resolveConfig("WW_DB_URL", null);
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return List.of(explicitUrl.trim());
        }

        String host = resolveConfig("WW_DB_HOST", "127.0.0.1");
        String port = resolveConfig("WW_DB_PORT", "3306");
        String dbName = resolveConfig("WW_DB_NAME", "wwpi_bd");
        String fallbackCsv = resolveConfig("WW_DB_FALLBACKS", "wwwpi,wwpi");

        Set<String> dbNames = new LinkedHashSet<>();
        dbNames.add(dbName);
        for (String raw : fallbackCsv.split(",")) {
            String value = raw == null ? "" : raw.trim();
            if (!value.isEmpty()) {
                dbNames.add(value);
            }
        }

        List<String> urls = new ArrayList<>();
        for (String database : dbNames) {
            urls.add("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
        }
        return urls;
    }

    private String resolveConfig(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return defaultValue;
    }
}
