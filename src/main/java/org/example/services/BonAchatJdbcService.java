package org.example.services;

import org.example.entities.BonAchat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BonAchatJdbcService extends AbstractJdbcService implements BaseCrudService<BonAchat> {

    private static final String BASE_SELECT = """
        SELECT id, partenaire_id, nom_magasin, logo_magasin, description, valeur_monetaire,
               points_requis, date_debut, date_expiration, nombre_maximum_utilisations,
               nombre_utilisations, conditions_utilisation, zone_geographique, image_promotionnelle,
               statut, historique_modifications, created_at, updated_at
        FROM bon_achat
        """;

    @Override
    public List<BonAchat> findAll() throws SQLException {
        List<BonAchat> bons = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " ORDER BY created_at DESC");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                bons.add(mapRow(resultSet));
            }
        }
        return bons;
    }

    @Override
    public Optional<BonAchat> findById(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public BonAchat create(BonAchat entity) throws SQLException {
        String sql = """
            INSERT INTO bon_achat (
                partenaire_id, nom_magasin, logo_magasin, description, valeur_monetaire, points_requis,
                date_debut, date_expiration, nombre_maximum_utilisations, nombre_utilisations,
                conditions_utilisation, zone_geographique, image_promotionnelle, statut,
                historique_modifications, created_at, updated_at
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
    public boolean update(BonAchat entity) throws SQLException {
        String sql = """
            UPDATE bon_achat
            SET partenaire_id = ?, nom_magasin = ?, logo_magasin = ?, description = ?, valeur_monetaire = ?,
                points_requis = ?, date_debut = ?, date_expiration = ?, nombre_maximum_utilisations = ?,
                nombre_utilisations = ?, conditions_utilisation = ?, zone_geographique = ?,
                image_promotionnelle = ?, statut = ?, historique_modifications = ?, created_at = ?, updated_at = ?
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
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM bon_achat WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindEntity(PreparedStatement statement, BonAchat entity) throws SQLException {
        statement.setObject(1, entity.getPartenaireId());
        statement.setString(2, entity.getNomMagasin());
        statement.setString(3, entity.getLogoMagasin());
        statement.setString(4, entity.getDescription());
        statement.setObject(5, entity.getValeurMonetaire());
        statement.setObject(6, entity.getPointsRequis());
        statement.setDate(7, toSqlDate(entity.getDateDebut()));
        statement.setDate(8, toSqlDate(entity.getDateExpiration()));
        statement.setObject(9, entity.getNombreMaximumUtilisations());
        statement.setObject(10, entity.getNombreUtilisations());
        statement.setString(11, entity.getConditionsUtilisation());
        statement.setString(12, entity.getZoneGeographique());
        statement.setString(13, entity.getImagePromotionnelle());
        statement.setString(14, entity.getStatut());
        statement.setString(15, entity.getHistoriqueModificationsJson());
        statement.setTimestamp(16, toTimestamp(entity.getCreatedAt()));
        statement.setTimestamp(17, toTimestamp(entity.getUpdatedAt()));
    }

    private BonAchat mapRow(ResultSet resultSet) throws SQLException {
        BonAchat bonAchat = new BonAchat();
        bonAchat.setId(resultSet.getInt("id"));
        bonAchat.setPartenaireId(resultSet.getObject("partenaire_id", Integer.class));
        bonAchat.setNomMagasin(resultSet.getString("nom_magasin"));
        bonAchat.setLogoMagasin(resultSet.getString("logo_magasin"));
        bonAchat.setDescription(resultSet.getString("description"));
        bonAchat.setValeurMonetaire(resultSet.getObject("valeur_monetaire", Double.class));
        bonAchat.setPointsRequis(resultSet.getObject("points_requis", Integer.class));
        bonAchat.setDateDebut(getLocalDate(resultSet, "date_debut"));
        bonAchat.setDateExpiration(getLocalDate(resultSet, "date_expiration"));
        bonAchat.setNombreMaximumUtilisations(resultSet.getObject("nombre_maximum_utilisations", Integer.class));
        bonAchat.setNombreUtilisations(resultSet.getObject("nombre_utilisations", Integer.class));
        bonAchat.setConditionsUtilisation(resultSet.getString("conditions_utilisation"));
        bonAchat.setZoneGeographique(resultSet.getString("zone_geographique"));
        bonAchat.setImagePromotionnelle(resultSet.getString("image_promotionnelle"));
        bonAchat.setStatut(resultSet.getString("statut"));
        bonAchat.setHistoriqueModificationsJson(resultSet.getString("historique_modifications"));
        bonAchat.setCreatedAt(getLocalDateTime(resultSet, "created_at"));
        bonAchat.setUpdatedAt(getLocalDateTime(resultSet, "updated_at"));
        return bonAchat;
    }
}
