package org.example.services.gestionevent;

import org.example.models.gestionevent.Participation;
import org.example.utils.DBConnection;
import org.example.services.CRUD;
import org.example.utils.EmailService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationServices implements CRUD<Participation> {

    private Connection connection;

    public ParticipationServices() {
        connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public void create(Participation p) throws SQLException {
        // ✅ Salla7na el requête: zedna el colonne 'email' fil INSERT
        String req = "INSERT INTO participation (dateInscription, evenement_id, nomCitoyen, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDate(1, p.getDateInscription());
            ps.setInt(2, p.getIdEvenement());
            ps.setString(3, p.getNomCitoyen());
            ps.setString(4, p.getEmail()); // ✅ Tawa el mail yettsab fil base

            ps.executeUpdate();
            System.out.println("✅ Participation ajoutée fil Base avec succès !");

            try {
                if (p.getEmail() != null) {
                    EmailService.sendNotification(p.getEmail(), p.getNomCitoyen(), "Événement #" + p.getIdEvenement());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur mail: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Participation> read() throws SQLException {
        List<Participation> list = new ArrayList<>();
        // 🚀 JOIN m3a el table evenement bech njibou el 'title' + njibou el 'email' mel participation
        String req = "SELECT p.*, e.title AS nom_ev FROM participation p " +
                "JOIN evenement e ON p.evenement_id = e.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Participation p = new Participation();
                p.setId(rs.getInt("id"));
                p.setDateInscription(rs.getDate("dateInscription"));
                p.setIdEvenement(rs.getInt("evenement_id"));
                p.setNomCitoyen(rs.getString("nomCitoyen"));
                p.setNomEvenement(rs.getString("nom_ev"));

                // ✅ Zidna hathi bech el Java ya9ra el mail mel base w y-affichih fil table
                p.setEmail(rs.getString("email"));

                list.add(p);
            }
        }
        return list;
    }

    @Override
    public void update(Participation p) throws SQLException {
        // ✅ Zidna el email fil update zeda bech tnajjem tbadlou ken lzem
        String req = "UPDATE participation SET dateInscription=?, evenement_id=?, nomCitoyen=?, email=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDate(1, p.getDateInscription());
            ps.setInt(2, p.getIdEvenement());
            ps.setString(3, p.getNomCitoyen());
            ps.setString(4, p.getEmail());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Participation mise à jour");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String req = "DELETE FROM participation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Participation supprimée");
        }
    }

    public void delete(Participation p) throws SQLException {
        delete(p.getId());
    }

    public void createPrepared(Participation p) throws SQLException {
        create(p);
    }
}