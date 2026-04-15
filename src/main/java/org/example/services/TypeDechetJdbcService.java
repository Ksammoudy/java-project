package org.example.services;

import org.example.entities.TypeDechet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeDechetJdbcService extends AbstractJdbcService implements BaseCrudService<TypeDechet> {

    @Override
    public List<TypeDechet> findAll() throws SQLException {
        String sql = "SELECT id, libelle, valeur_points_kg, description_tri FROM type_dechet ORDER BY libelle";
        List<TypeDechet> types = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                types.add(mapRow(resultSet));
            }
        }

        return types;
    }

    @Override
    public Optional<TypeDechet> findById(int id) throws SQLException {
        String sql = "SELECT id, libelle, valeur_points_kg, description_tri FROM type_dechet WHERE id = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public TypeDechet create(TypeDechet entity) throws SQLException {
        String sql = "INSERT INTO type_dechet (libelle, valeur_points_kg, description_tri) VALUES (?, ?, ?)";

        try (PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getLibelle());
            statement.setObject(2, entity.getValeurPointsKg());
            statement.setString(3, entity.getDescriptionTri());
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
    public boolean update(TypeDechet entity) throws SQLException {
        String sql = "UPDATE type_dechet SET libelle = ?, valeur_points_kg = ?, description_tri = ? WHERE id = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, entity.getLibelle());
            statement.setObject(2, entity.getValeurPointsKg());
            statement.setString(3, entity.getDescriptionTri());
            statement.setInt(4, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM type_dechet WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public int calculatePoints(TypeDechet typeDechet, double quantite) {
        if (typeDechet == null || typeDechet.getValeurPointsKg() == null || quantite <= 0) {
            return 0;
        }

        return (int) Math.round(typeDechet.getValeurPointsKg() * quantite);
    }

    private TypeDechet mapRow(ResultSet resultSet) throws SQLException {
        TypeDechet typeDechet = new TypeDechet();
        typeDechet.setId(resultSet.getInt("id"));
        typeDechet.setLibelle(resultSet.getString("libelle"));
        typeDechet.setValeurPointsKg(resultSet.getObject("valeur_points_kg", Double.class));
        typeDechet.setDescriptionTri(resultSet.getString("description_tri"));
        return typeDechet;
    }
}
