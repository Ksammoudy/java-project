package org.example.services;

import org.example.entities.Wallet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WalletJdbcService extends AbstractJdbcService implements BaseCrudService<Wallet> {
    private static final String GAIN_TYPE = "Gain";
    private static final String GAIN_MOTIF_PREFIX = "Gain declaration #";

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

    /**
     * Synchronise les points gagnés (déclarations APPROUVEE/VALIDATED) vers le wallet citoyen.
     * Idempotent: chaque déclaration est créditée une seule fois via wallet_transaction.
     */
    public Wallet syncCitizenWalletPoints(int citoyenId) throws SQLException {
        Wallet wallet = findByUtilisateurId(citoyenId).orElse(null);
        if (wallet == null) {
            Wallet created = new Wallet();
            created.setUtilisateurId(citoyenId);
            created.setSoldeActuel(0);
            created.setDateMj(LocalDateTime.now());
            wallet = create(created);
        }

        int walletId = wallet.getId();
        int currentBalance = wallet.getSoldeActuel() == null ? 0 : wallet.getSoldeActuel();
        int addedPoints = 0;

        String declarationsSql = """
                SELECT d.id,
                       d.points_attribues,
                       d.quantite,
                       td.valeur_points_kg
                FROM declaration_dechet d
                LEFT JOIN type_dechet td ON td.id = d.type_dechet_id
                WHERE d.citoyen_id = ?
                  AND d.deleted_at IS NULL
                  AND UPPER(COALESCE(d.statut, '')) IN ('APPROUVEE', 'VALIDATED')
                ORDER BY d.id ASC
                """;

        try (PreparedStatement declarationStatement = getConnection().prepareStatement(declarationsSql)) {
            declarationStatement.setInt(1, citoyenId);
            try (ResultSet declarationRows = declarationStatement.executeQuery()) {
                while (declarationRows.next()) {
                    int declarationId = declarationRows.getInt("id");
                    Integer pointsDb = declarationRows.getObject("points_attribues", Integer.class);
                    Double quantite = declarationRows.getObject("quantite", Double.class);
                    Double pointsKg = declarationRows.getObject("valeur_points_kg", Double.class);

                    int points = pointsDb == null ? 0 : pointsDb;
                    if (points <= 0 && quantite != null && quantite > 0 && pointsKg != null && pointsKg > 0) {
                        points = (int) Math.round(quantite * pointsKg);
                        if (points > 0) {
                            updateDeclarationPoints(declarationId, points);
                        }
                    }
                    if (points <= 0) {
                        continue;
                    }

                    String motif = GAIN_MOTIF_PREFIX + declarationId;
                    if (gainTransactionExists(walletId, motif)) {
                        continue;
                    }

                    insertGainTransaction(walletId, points, motif);
                    addedPoints += points;
                }
            }
        }

        if (addedPoints > 0) {
            wallet.setSoldeActuel(currentBalance + addedPoints);
            wallet.setDateMj(LocalDateTime.now());
            update(wallet);
        } else if (wallet.getDateMj() == null) {
            wallet.setDateMj(LocalDateTime.now());
            update(wallet);
        }

        return findById(walletId).orElse(wallet);
    }

    private void updateDeclarationPoints(int declarationId, int points) throws SQLException {
        String sql = "UPDATE declaration_dechet SET points_attribues = ? WHERE id = ? AND (points_attribues IS NULL OR points_attribues <= 0)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, points);
            statement.setInt(2, declarationId);
            statement.executeUpdate();
        }
    }

    private boolean gainTransactionExists(int walletId, String motif) throws SQLException {
        String sql = """
                SELECT 1
                FROM wallet_transaction
                WHERE wallet_id = ?
                  AND type = ?
                  AND motif = ?
                LIMIT 1
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, walletId);
            statement.setString(2, GAIN_TYPE);
            statement.setString(3, motif);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertGainTransaction(int walletId, int points, String motif) throws SQLException {
        String sql = """
                INSERT INTO wallet_transaction (wallet_id, montant, type, motif, date_transaction)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, walletId);
            statement.setInt(2, points);
            statement.setString(3, GAIN_TYPE);
            statement.setString(4, motif);
            statement.setTimestamp(5, toTimestamp(LocalDateTime.now()));
            statement.executeUpdate();
        }
    }
}
