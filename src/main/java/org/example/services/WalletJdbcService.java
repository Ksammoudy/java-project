package org.example.services;

import org.example.entities.Wallet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletJdbcService extends AbstractJdbcService implements BaseCrudService<Wallet> {

    @Override
    public List<Wallet> findAll() throws SQLException {
        String sql = "SELECT id_wallet, utilisateur_id, solde_actuel, date_mj FROM wallet ORDER BY id_wallet";
        List<Wallet> wallets = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                wallets.add(mapRow(resultSet));
            }
        }
        return wallets;
    }

    @Override
    public Optional<Wallet> findById(int id) throws SQLException {
        String sql = "SELECT id_wallet, utilisateur_id, solde_actuel, date_mj FROM wallet WHERE id_wallet = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Wallet> findByUtilisateurId(int utilisateurId) throws SQLException {
        String sql = "SELECT id_wallet, utilisateur_id, solde_actuel, date_mj FROM wallet WHERE utilisateur_id = ? LIMIT 1";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Wallet create(Wallet entity) throws SQLException {
        String sql = "INSERT INTO wallet (utilisateur_id, solde_actuel, date_mj) VALUES (?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, entity.getUtilisateurId());
            statement.setObject(2, entity.getSoldeActuel());
            statement.setTimestamp(3, toTimestamp(entity.getDateMj()));
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
    public boolean update(Wallet entity) throws SQLException {
        String sql = "UPDATE wallet SET utilisateur_id = ?, solde_actuel = ?, date_mj = ? WHERE id_wallet = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setObject(1, entity.getUtilisateurId());
            statement.setObject(2, entity.getSoldeActuel());
            statement.setTimestamp(3, toTimestamp(entity.getDateMj()));
            statement.setInt(4, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM wallet WHERE id_wallet = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Wallet mapRow(ResultSet resultSet) throws SQLException {
        Wallet wallet = new Wallet();
        wallet.setId(resultSet.getInt("id_wallet"));
        wallet.setUtilisateurId(resultSet.getObject("utilisateur_id", Integer.class));
        wallet.setSoldeActuel(resultSet.getObject("solde_actuel", Integer.class));
        wallet.setDateMj(getLocalDateTime(resultSet, "date_mj"));
        return wallet;
    }
}
