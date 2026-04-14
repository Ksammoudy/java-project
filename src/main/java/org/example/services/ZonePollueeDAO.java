package org.example.services;

import org.example.models.ZonePolluee;
import org.example.models.IndicateurImpact;
import org.example.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ZonePollueeDAO {

    private DBConnection dbConnection = DBConnection.getInstance();

    // CREATE
    public void addZone(ZonePolluee zone) {
        String sql = "INSERT INTO zone_polluee (nom_zone, coordonnees_gps, niveau_pollution, date_identification, indicateur_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, zone.getNomZone());
            pstmt.setString(2, zone.getCoordonneesGps());
            pstmt.setInt(3, zone.getNiveauPollution());
            pstmt.setTimestamp(4, Timestamp.valueOf(zone.getDateIdentification()));

            // Gestion de l'indicateur
            if (zone.getIndicateur() != null) {
                pstmt.setInt(5, zone.getIndicateur().getId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                zone.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<ZonePolluee> getAllZones() {
        List<ZonePolluee> zones = new ArrayList<>();
        String sql = "SELECT z.*, i.id as indicateur_id, i.total_kg_recoltes, i.co2_evite, i.date_calcul " +
                "FROM zone_polluee z " +
                "LEFT JOIN indicateur_impact i ON z.indicateur_id = i.id " +
                "ORDER BY z.date_identification DESC";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                zones.add(extractZoneWithIndicateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zones;
    }

    // READ BY ID
    public ZonePolluee getZoneById(int id) {
        String sql = "SELECT z.*, i.id as indicateur_id, i.total_kg_recoltes, i.co2_evite, i.date_calcul " +
                "FROM zone_polluee z " +
                "LEFT JOIN indicateur_impact i ON z.indicateur_id = i.id " +
                "WHERE z.id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractZoneWithIndicateur(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE
    public void updateZone(ZonePolluee zone) {
        String sql = "UPDATE zone_polluee SET nom_zone = ?, coordonnees_gps = ?, niveau_pollution = ?, indicateur_id = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, zone.getNomZone());
            pstmt.setString(2, zone.getCoordonneesGps());
            pstmt.setInt(3, zone.getNiveauPollution());

            if (zone.getIndicateur() != null) {
                pstmt.setInt(4, zone.getIndicateur().getId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, zone.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteZone(int id) {
        String sql = "DELETE FROM zone_polluee WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // RECHERCHE par nom
    public List<ZonePolluee> searchByNom(String searchTerm) {
        List<ZonePolluee> zones = new ArrayList<>();
        String sql = "SELECT z.*, i.id as indicateur_id, i.total_kg_recoltes, i.co2_evite, i.date_calcul " +
                "FROM zone_polluee z " +
                "LEFT JOIN indicateur_impact i ON z.indicateur_id = i.id " +
                "WHERE z.nom_zone LIKE ? " +
                "ORDER BY z.date_identification DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                zones.add(extractZoneWithIndicateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zones;
    }

    // TRI par niveau (décroissant)
    public List<ZonePolluee> sortByNiveauDesc() {
        List<ZonePolluee> zones = new ArrayList<>();
        String sql = "SELECT z.*, i.id as indicateur_id, i.total_kg_recoltes, i.co2_evite, i.date_calcul " +
                "FROM zone_polluee z " +
                "LEFT JOIN indicateur_impact i ON z.indicateur_id = i.id " +
                "ORDER BY z.niveau_pollution DESC";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                zones.add(extractZoneWithIndicateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zones;
    }

    // Contrôle de saisie
    public boolean isValidNiveau(int niveau) {
        return niveau >= 1 && niveau <= 10;
    }

    public boolean isValidNom(String nom) {
        return nom != null && !nom.trim().isEmpty();
    }

    // Extraction avec jointure
    private ZonePolluee extractZoneWithIndicateur(ResultSet rs) throws SQLException {
        ZonePolluee zone = new ZonePolluee();
        zone.setId(rs.getInt("id"));
        zone.setNomZone(rs.getString("nom_zone"));
        zone.setCoordonneesGps(rs.getString("coordonnees_gps"));
        zone.setNiveauPollution(rs.getInt("niveau_pollution"));
        zone.setDateIdentification(rs.getTimestamp("date_identification").toLocalDateTime());

        // Récupérer l'indicateur associé
        int indicateurId = rs.getInt("indicateur_id");
        if (indicateurId > 0 && rs.getObject("total_kg_recoltes") != null) {
            IndicateurImpact indicateur = new IndicateurImpact();
            indicateur.setId(indicateurId);
            indicateur.setTotalKgRecoltes(rs.getDouble("total_kg_recoltes"));
            indicateur.setCo2Evite(rs.getDouble("co2_evite"));
            indicateur.setDateCalcul(rs.getTimestamp("date_calcul").toLocalDateTime());
            zone.setIndicateur(indicateur);
        }

        return zone;
    }
}