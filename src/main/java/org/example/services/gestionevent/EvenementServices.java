package org.example.services.gestionevent;

import org.example.models.gestionevent.Evenement;
import org.example.services.CRUD;
import org.example.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementServices implements CRUD<Evenement> {

    private Connection connection;

    public EvenementServices() {
        connection = DBConnection.getInstance().getConnection();
    }

    private void validateEvenement(Evenement e) {
        if (e.getTitre() == null || e.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Titre obligatoire");
        }
        if (e.getDescription() == null || e.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description obligatoire");
        }
        if (e.getDate() == null) {
            throw new IllegalArgumentException("Date obligatoire");
        }
    }

    @Override
    public void create(Evenement e) throws SQLException {
        validateEvenement(e);
        // Na7ina idOrganisateur khater mahouch mawjoud fil DB mte3ek
        String req = "INSERT INTO evenement (title, description, dateHeure, nomOrganisateur, lieu) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, new Timestamp(e.getDate().getTime()));
            ps.setString(4, e.getNomOrganisateur());
            ps.setString(5, e.getLieu());
            ps.executeUpdate();
            System.out.println("✅ Événement ajouté avec succès!");
        }
    }

    @Override
    public List<Evenement> read() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("dateHeure");
                java.sql.Date sqlDate = (ts != null) ? new java.sql.Date(ts.getTime()) : null;

                // FIX: idOrganisateur n7ottouh 0 khater column 'idOrganisateur' not found
                list.add(new Evenement(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        sqlDate,
                        0,
                        rs.getString("lieu"),
                        rs.getString("nomOrganisateur")
                ));
            }
        }
        return list;
    }

    @Override
    public void update(Evenement e) throws SQLException {
        validateEvenement(e);
        // Mise à jour sans idOrganisateur
        String req = "UPDATE evenement SET title=?, description=?, dateHeure=?, lieu=?, nomOrganisateur=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, e.getTitre());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, new Timestamp(e.getDate().getTime()));
            ps.setString(4, e.getLieu());
            ps.setString(5, e.getNomOrganisateur());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
            System.out.println("✅ Événement modifié!");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Événement supprimé!");
        }
    }

    public void delete(Evenement e) throws SQLException {
        delete(e.getId());
    }

    public void createPrepared(Evenement e) throws SQLException {
        create(e);
    }

    public List<Evenement> getByOrganisateur(String nomOrg) {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement WHERE nomOrganisateur = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomOrg);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("dateHeure");
                    java.sql.Date sqlDate = (ts != null) ? new java.sql.Date(ts.getTime()) : null;

                    list.add(new Evenement(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            sqlDate,
                            0,
                            rs.getString("lieu"),
                            rs.getString("nomOrganisateur")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
        }
        return list;
    }
}