package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/pidev";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static MyConnection instance;
    private Connection connection;

    private MyConnection() {
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
        return URL;
    }

    private void reconnect() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL database: " + URL);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to MySQL database: " + URL, e);
        }
    }
}
