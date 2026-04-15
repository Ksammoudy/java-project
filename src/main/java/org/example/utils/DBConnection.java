package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        connect();
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion JDBC indisponible.", e);
        }

        if (connection == null) {
            throw new IllegalStateException("Connexion JDBC indisponible.");
        }

        return connection;
    }

    public boolean testConnection() {
        try {
            return getConnection() != null && getConnection().isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void connect() {
        DatabaseConfig config = DatabaseConfig.load();

        try {
            connection = DriverManager.getConnection(config.url(), config.username(), config.password());
            System.out.println("Connexion MySQL reussie vers " + config.databaseName());
        } catch (SQLException e) {
            connection = null;
            System.out.println("Erreur connexion DB: " + e.getMessage());
        }
    }
}
