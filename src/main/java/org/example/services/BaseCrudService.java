package org.example.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BaseCrudService<T> {

    List<T> findAll() throws SQLException;

    Optional<T> findById(int id) throws SQLException;

    T create(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(int id) throws SQLException;
}
