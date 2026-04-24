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

    // VALIDATION
    private void validateParticipation(Participation p) {

        if (p.getDateInscription() == null) {
            throw new IllegalArgumentException("Date inscription obligatoire");
        }

        if (p.getIdEvenement() <= 0) {
            throw new IllegalArgumentException("ID Evenement invalide");
        }

        if (p.getIdCitoyen() <= 0) {
            throw new IllegalArgumentException("ID Citoyen invalide");
        }
    }

    // CREATE
    @Override
    public void create(Participation p) throws SQLException {

        validateParticipation(p);

        String req = "INSERT INTO participation (dateInscription, evenement_id, idCitoyen) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setDate(1, p.getDateInscription());
        ps.setInt(2, p.getIdEvenement());
        ps.setInt(3, p.getIdCitoyen());

        ps.executeUpdate();
        System.out.println("Participation ajoutée");
    }

    // READ
    @Override
    public List<Participation> read() throws SQLException {

        List<Participation> list = new ArrayList<>();
        String req = "SELECT * FROM participation";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Participation(
                    rs.getInt("id"),
                    rs.getDate("dateInscription"),
                    rs.getInt("evenement_id"),
                    rs.getInt("idCitoyen")
            ));
        }

        return list;
    }

    // UPDATE
    @Override
    public void update(Participation p) throws SQLException {

        validateParticipation(p);

        String req = "UPDATE participation SET dateInscription=?, evenement_id=?, idCitoyen=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setDate(1, p.getDateInscription());
        ps.setInt(2, p.getIdEvenement());
        ps.setInt(3, p.getIdCitoyen());
        ps.setInt(4, p.getId());

        ps.executeUpdate();
        System.out.println("Participation modifiée");
    }

    // DELETE (CORRIGÉ selon CRUD<T>)
    @Override
    public void delete(Participation p) throws SQLException {

        String req = "DELETE FROM participation WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setInt(1, p.getId());

        ps.executeUpdate();
        System.out.println("Participation supprimée");
    }

    // CREATE PREPARED
    @Override
    public void createPrepared(Participation p) throws SQLException {

        validateParticipation(p);

        String req = "INSERT INTO participation (dateInscription, evenement_id, idCitoyen) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setDate(1, p.getDateInscription());
        ps.setInt(2, p.getIdEvenement());
        ps.setInt(3, p.getIdCitoyen());

        ps.executeUpdate();
        System.out.println("Participation ajoutée (PreparedStatement)");
    }
}
