package org.example.services;

import org.example.entities.DeclarationDechet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeclarationDechetJdbcService extends AbstractJdbcService implements BaseCrudService<DeclarationDechet> {

    private static final String SELECT_WITH_JOINS = """
        SELECT d.id, d.description, d.statut, d.type_dechet_id, td.libelle AS type_dechet_libelle,
               d.photo, d.latitude, d.longitude, d.quantite, d.unite, d.created_at, d.score_ia,
               d.points_attribues, d.qr_code, d.citoyen_id, u.email AS citoyen_email,
               d.valorisateur_confirmateur_id, d.date_confirmation, d.statut_historique, d.deleted_at
        FROM declaration_dechet d
        LEFT JOIN type_dechet td ON td.id = d.type_dechet_id
        LEFT JOIN `user` u ON u.id = d.citoyen_id
        """;

    @Override
    public List<DeclarationDechet> findAll() throws SQLException {
        List<DeclarationDechet> declarations = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(SELECT_WITH_JOINS + " ORDER BY d.created_at DESC, d.id DESC");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                declarations.add(mapRow(resultSet));
            }
        }
        return declarations;
    }

    @Override
    public Optional<DeclarationDechet> findById(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(SELECT_WITH_JOINS + " WHERE d.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Declarations d'un citoyen (hors fiches archivees / soft-delete).
     */
    public List<DeclarationDechet> findByCitoyenId(int citoyenId) throws SQLException {
        List<DeclarationDechet> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " WHERE d.citoyen_id = ? AND d.deleted_at IS NULL ORDER BY d.created_at DESC, d.id DESC";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, citoyenId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapRow(resultSet));
                }
            }
        }
        return list;
    }

    @Override
    public DeclarationDechet create(DeclarationDechet entity) throws SQLException {
        String sql = """
            INSERT INTO declaration_dechet (
                description, statut, type_dechet_id, photo, latitude, longitude, quantite, unite,
                created_at, score_ia, points_attribues, qr_code, citoyen_id, valorisateur_confirmateur_id,
                date_confirmation, statut_historique, deleted_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindEntity(statement, entity);
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
    public boolean update(DeclarationDechet entity) throws SQLException {
        String sql = """
            UPDATE declaration_dechet
            SET description = ?, statut = ?, type_dechet_id = ?, photo = ?, latitude = ?, longitude = ?,
                quantite = ?, unite = ?, created_at = ?, score_ia = ?, points_attribues = ?, qr_code = ?,
                citoyen_id = ?, valorisateur_confirmateur_id = ?, date_confirmation = ?, statut_historique = ?,
                deleted_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            bindEntity(statement, entity);
            statement.setInt(18, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM declaration_dechet WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindEntity(PreparedStatement statement, DeclarationDechet entity) throws SQLException {
        statement.setString(1, entity.getDescription());
        statement.setString(2, entity.getStatut());
        statement.setObject(3, entity.getTypeDechetId());
        statement.setString(4, entity.getPhoto());
        statement.setObject(5, entity.getLatitude());
        statement.setObject(6, entity.getLongitude());
        statement.setObject(7, entity.getQuantite());
        statement.setString(8, entity.getUnite());
        statement.setTimestamp(9, toTimestamp(entity.getCreatedAt()));
        statement.setObject(10, entity.getScoreIa());
        statement.setObject(11, entity.getPointsAttribues());
        statement.setString(12, entity.getQrCode());
        statement.setObject(13, entity.getCitoyenId());
        statement.setObject(14, entity.getValorisateurConfirmateurId());
        statement.setTimestamp(15, toTimestamp(entity.getDateConfirmation()));
        statement.setString(16, entity.getStatutHistoriqueJson());
        statement.setTimestamp(17, toTimestamp(entity.getDeletedAt()));
    }

    private DeclarationDechet mapRow(ResultSet resultSet) throws SQLException {
        DeclarationDechet declaration = new DeclarationDechet();
        declaration.setId(resultSet.getInt("id"));
        declaration.setDescription(resultSet.getString("description"));
        declaration.setStatut(resultSet.getString("statut"));
        declaration.setTypeDechetId(resultSet.getObject("type_dechet_id", Integer.class));
        declaration.setTypeDechetLibelle(resultSet.getString("type_dechet_libelle"));
        declaration.setPhoto(resultSet.getString("photo"));
        declaration.setLatitude(resultSet.getObject("latitude", Double.class));
        declaration.setLongitude(resultSet.getObject("longitude", Double.class));
        declaration.setQuantite(resultSet.getObject("quantite", Double.class));
        declaration.setUnite(resultSet.getString("unite"));
        declaration.setCreatedAt(getLocalDateTime(resultSet, "created_at"));
        declaration.setScoreIa(resultSet.getObject("score_ia", Double.class));
        declaration.setPointsAttribues(resultSet.getObject("points_attribues", Integer.class));
        declaration.setQrCode(resultSet.getString("qr_code"));
        declaration.setCitoyenId(resultSet.getObject("citoyen_id", Integer.class));
        declaration.setCitoyenEmail(resultSet.getString("citoyen_email"));
        declaration.setValorisateurConfirmateurId(resultSet.getObject("valorisateur_confirmateur_id", Integer.class));
        declaration.setDateConfirmation(getLocalDateTime(resultSet, "date_confirmation"));
        declaration.setStatutHistoriqueJson(resultSet.getString("statut_historique"));
        declaration.setDeletedAt(getLocalDateTime(resultSet, "deleted_at"));
        return declaration;
    }
}
