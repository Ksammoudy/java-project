package org.example.services;

import org.example.utils.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class AbstractJdbcService {

    protected Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    protected LocalDateTime getLocalDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    protected LocalDate getLocalDate(ResultSet resultSet, String column) throws SQLException {
        java.sql.Date date = resultSet.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    protected Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    protected java.sql.Date toSqlDate(LocalDate value) {
        return value == null ? null : java.sql.Date.valueOf(value);
    }
}
