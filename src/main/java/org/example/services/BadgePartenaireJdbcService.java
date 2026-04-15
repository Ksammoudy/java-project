package org.example.services;

import org.example.entities.BadgePartenaire;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BadgePartenaireJdbcService extends AbstractJdbcService implements BaseCrudService<BadgePartenaire> {

    private static final String BASE_SELECT = """
        SELECT id, partenaire_id, code, nom, description, couleur, icone,
               score_impact, created_at, updated_at, is_current
        FROM badge_partenaire
        """;

    @Override
    public List<BadgePartenaire> findAll() throws SQLException {
        List<BadgePartenaire> badges = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " ORDER BY updated_at DESC");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                badges.add(mapRow(resultSet));
            }
        }
        return badges;
    }

    @Override
    public Optional<BadgePartenaire> findById(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public BadgePartenaire create(BadgePartenaire entity) throws SQLException {
        String sql = """
            INSERT INTO badge_partenaire (
                partenaire_id, code, nom, description, couleur, icone,
                score_impact, created_at, updated_at, is_current
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
    public boolean update(BadgePartenaire entity) throws SQLException {
        String sql = """
            UPDATE badge_partenaire
            SET partenaire_id = ?, code = ?, nom = ?, description = ?, couleur = ?, icone = ?,
                score_impact = ?, created_at = ?, updated_at = ?, is_current = ?
            WHERE id = ?
            """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            bindEntity(statement, entity);
            statement.setInt(11, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM badge_partenaire WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindEntity(PreparedStatement statement, BadgePartenaire entity) throws SQLException {
        statement.setObject(1, entity.getPartenaireId());
        statement.setString(2, entity.getCode());
        statement.setString(3, entity.getNom());
        statement.setString(4, entity.getDescription());
        statement.setString(5, entity.getCouleur());
        statement.setString(6, entity.getIcone());
        statement.setObject(7, entity.getScoreImpact());
        statement.setTimestamp(8, toTimestamp(entity.getCreatedAt()));
        statement.setTimestamp(9, toTimestamp(entity.getUpdatedAt()));
        statement.setBoolean(10, entity.isCurrent());
    }

    private BadgePartenaire mapRow(ResultSet resultSet) throws SQLException {
        BadgePartenaire badge = new BadgePartenaire();
        badge.setId(resultSet.getInt("id"));
        badge.setPartenaireId(resultSet.getObject("partenaire_id", Integer.class));
        badge.setCode(resultSet.getString("code"));
        badge.setNom(resultSet.getString("nom"));
        badge.setDescription(resultSet.getString("description"));
        badge.setCouleur(resultSet.getString("couleur"));
        badge.setIcone(resultSet.getString("icone"));
        badge.setScoreImpact(resultSet.getObject("score_impact", Integer.class));
        badge.setCreatedAt(getLocalDateTime(resultSet, "created_at"));
        badge.setUpdatedAt(getLocalDateTime(resultSet, "updated_at"));
        badge.setCurrent(resultSet.getBoolean("is_current"));
        return badge;
    }
}
