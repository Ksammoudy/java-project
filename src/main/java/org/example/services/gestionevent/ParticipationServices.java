package org.example.services.gestionevent;

import org.example.models.gestionevent.Participation;
import org.example.utils.DBConnection;
import org.example.services.CRUD;

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
        // 🛠️ Thabbet dhibet fil asémi mta3 el colonnes fil Base (pidev_db)
        String req = "INSERT INTO participation (dateInscription, evenement_id, idCitoyen, nomCitoyen, nomEvenement, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDate(1, p.getDateInscription());
            ps.setInt(2, p.getIdEvenement());
            ps.setInt(3, p.getIdCitoyen());
            ps.setString(4, p.getNomCitoyen());
            ps.setString(5, p.getNomEvenement());
            ps.setString(6, p.getEmail());
            ps.executeUpdate();
            System.out.println("✅ Participation ajoutée fil Base !");
        }
    }

    @Override
    public List<Participation> read() throws SQLException {
        List<Participation> list = new ArrayList<>();
        // 🛠️ SELECT l-kol bech ma wellich famma column ne9sa
        String req = "SELECT * FROM participation";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Participation p = new Participation();
                p.setId(rs.getInt("id"));
                p.setNomCitoyen(rs.getString("nomCitoyen"));
                p.setEmail(rs.getString("email"));
                p.setNomEvenement(rs.getString("nomEvenement"));
                p.setDateInscription(rs.getDate("dateInscription"));
                // Zid hedhom bech el ID mta3 l-evenement yo9od mrigel
                p.setIdEvenement(rs.getInt("evenement_id"));

                list.add(p);
            }
        }
        return list;
    }

    @Override
    public void update(Participation p) throws SQLException {
        String req = "UPDATE participation SET dateInscription=?, evenement_id=?, idCitoyen=?, nomCitoyen=?, nomEvenement=?, email=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setDate(1, p.getDateInscription());
            ps.setInt(2, p.getIdEvenement());
            ps.setInt(3, p.getIdCitoyen());
            ps.setString(4, p.getNomCitoyen());
            ps.setString(5, p.getNomEvenement());
            ps.setString(6, p.getEmail());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Participation mise à jour");
        }
    }

    @Override
    public void delete(Participation p) throws SQLException {
        String req = "DELETE FROM participation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Participation supprimée");
        }
    }

    @Override
    public void createPrepared(Participation p) throws SQLException {
        create(p);
    }
}