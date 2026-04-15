package org.example.services;

import org.example.entities.Transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionJdbcService extends AbstractJdbcService implements BaseCrudService<Transaction> {

    @Override
    public List<Transaction> findAll() throws SQLException {
        String sql = """
            SELECT id_transaction, wallet_id, montant, type, motif, date_transaction
            FROM wallet_transaction
            ORDER BY date_transaction DESC
            """;
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(mapRow(resultSet));
            }
        }
        return transactions;
    }

    public List<Transaction> findByWalletId(int walletId, int limit) throws SQLException {
        String sql = """
            SELECT id_transaction, wallet_id, montant, type, motif, date_transaction
            FROM wallet_transaction
            WHERE wallet_id = ?
            ORDER BY date_transaction DESC, id_transaction DESC
            LIMIT ?
            """;
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, walletId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapRow(resultSet));
                }
            }
        }
        return transactions;
    }

    @Override
    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = """
            SELECT id_transaction, wallet_id, montant, type, motif, date_transaction
            FROM wallet_transaction
            WHERE id_transaction = ?
            """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Transaction create(Transaction entity) throws SQLException {
        String sql = """
            INSERT INTO wallet_transaction (wallet_id, montant, type, motif, date_transaction)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, entity.getWalletId());
            statement.setObject(2, entity.getMontant());
            statement.setString(3, entity.getType());
            statement.setString(4, entity.getMotif());
            statement.setTimestamp(5, toTimestamp(entity.getDateTransaction()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
        }
        return entity;
    }

    @Override
    public boolean update(Transaction entity) throws SQLException {
        String sql = """
            UPDATE wallet_transaction
            SET wallet_id = ?, montant = ?, type = ?, motif = ?, date_transaction = ?
            WHERE id_transaction = ?
            """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setObject(1, entity.getWalletId());
            statement.setObject(2, entity.getMontant());
            statement.setString(3, entity.getType());
            statement.setString(4, entity.getMotif());
            statement.setTimestamp(5, toTimestamp(entity.getDateTransaction()));
            statement.setInt(6, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wallet_transaction WHERE id_transaction = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Transaction mapRow(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getInt("id_transaction"));
        transaction.setWalletId(resultSet.getObject("wallet_id", Integer.class));
        transaction.setMontant(resultSet.getObject("montant", Integer.class));
        transaction.setType(resultSet.getString("type"));
        transaction.setMotif(resultSet.getString("motif"));
        transaction.setDateTransaction(getLocalDateTime(resultSet, "date_transaction"));
        return transaction;
    }
}
