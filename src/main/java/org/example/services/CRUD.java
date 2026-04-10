package org.example.services;

import java.sql.SQLException;
import java.util.List;

public interface CRUD<T> {
    void create(T t) throws SQLException;
    List<T> read() throws SQLException;
    void update(T t) throws SQLException;
    void delete(T t) throws SQLException;
    void createPrepared(T t) throws SQLException;
}