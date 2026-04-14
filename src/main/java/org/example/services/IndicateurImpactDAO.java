package org.example.services;

import org.example.models.IndicateurImpact;
import org.example.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IndicateurImpactDAO {

    private DBConnection dbConnection = DBConnection.getInstance();

    // CREATE
    public void addIndicateur(IndicateurImpact indicateur) {
        String sql = "INSERT INTO indicateur_impact (total_kg_recoltes, co2_evite, date_calcul) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, indicateur.getTotalKgRecoltes());
            pstmt.setDouble(2, indicateur.getCo2Evite());
            pstmt.setTimestamp(3, Timestamp.valueOf(indicateur.getDateCalcul()));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                indicateur.setId(rs.getInt(1));
                System.out.println("✅ Indicateur ajouté avec succès (ID: " + indicateur.getId() + ") !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<IndicateurImpact> getAllIndicateurs() {
        List<IndicateurImpact> indicateurs = new ArrayList<>();
        String sql = "SELECT * FROM indicateur_impact ORDER BY id";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                indicateurs.add(extractIndicateurFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return indicateurs;
    }

    // READ BY ID
    public IndicateurImpact getIndicateurById(int id) {
        String sql = "SELECT * FROM indicateur_impact WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractIndicateurFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE
    public void updateIndicateur(IndicateurImpact indicateur) {
        String sql = "UPDATE indicateur_impact SET total_kg_recoltes = ?, co2_evite = ?, date_calcul = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, indicateur.getTotalKgRecoltes());
            pstmt.setDouble(2, indicateur.getCo2Evite());
            pstmt.setTimestamp(3, Timestamp.valueOf(indicateur.getDateCalcul()));
            pstmt.setInt(4, indicateur.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteIndicateur(int id) {
        String sql = "DELETE FROM indicateur_impact WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private IndicateurImpact extractIndicateurFromResultSet(ResultSet rs) throws SQLException {
        IndicateurImpact indicateur = new IndicateurImpact();
        indicateur.setId(rs.getInt("id"));
        indicateur.setTotalKgRecoltes(rs.getDouble("total_kg_recoltes"));
        indicateur.setCo2Evite(rs.getDouble("co2_evite"));
        indicateur.setDateCalcul(rs.getTimestamp("date_calcul").toLocalDateTime());
        return indicateur;
    }

    // Contrôle de saisie
    public boolean isValidTotalKg(double kg) {
        return kg >= 0;
    }

    public boolean isValidCo2(double co2) {
        return co2 >= 0;
    }
}