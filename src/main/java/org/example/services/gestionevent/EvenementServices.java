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

    // Validation
    private void validateEvenement(Evenement e) {

        if (e.getTitre() == null || e.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Titre obligatoire");
        }

        if (e.getDescription() == null || e.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description obligatoire");
        }

        if (e.getLieu() == null || e.getLieu().trim().isEmpty()) {
            throw new IllegalArgumentException("Lieu obligatoire");
        }

        if (e.getDate() == null) {
            throw new IllegalArgumentException("Date obligatoire");
        }

        if (e.getIdOrganisateur() <= 0) {
            throw new IllegalArgumentException("ID Organisateur invalide");
        }
    }

    // CREATE
    @Override
    public void create(Evenement e) throws SQLException {

        validateEvenement(e);

        String req = "INSERT INTO evenement (title, description, lieu, dateHeure, idOrganisateur) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, e.getTitre());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getLieu());
        ps.setTimestamp(4, new Timestamp(e.getDate().getTime()));
        ps.setInt(5, e.getIdOrganisateur());

        ps.executeUpdate();
        System.out.println("Evenement ajouté");
    }

    // READ
    @Override
    public List<Evenement> read() throws SQLException {

        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Evenement(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("lieu"),
                    rs.getDate("dateHeure"),
                    rs.getInt("idOrganisateur")
            ));
        }

        return list;
    }

    // UPDATE
    @Override
    public void update(Evenement e) throws SQLException {

        validateEvenement(e);

        String req = "UPDATE evenement SET title=?, description=?, lieu=?, dateHeure=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, e.getTitre());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getLieu());
        ps.setTimestamp(4, new Timestamp(e.getDate().getTime()));
        ps.setInt(5, e.getId());

        ps.executeUpdate();
        System.out.println("Evenement modifié");
    }

    // DELETE (CORRIGÉ selon interface CRUD)
    @Override
    public void delete(Evenement e) throws SQLException {

        String req = "DELETE FROM evenement WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setInt(1, e.getId());

        ps.executeUpdate();
        System.out.println("Evenement supprimé");
    }

    // CREATE PREPARED
    @Override
    public void createPrepared(Evenement e) throws SQLException {

        validateEvenement(e);

        String req = "INSERT INTO evenement (title, description, lieu, dateHeure, idOrganisateur) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, e.getTitre());
        ps.setString(2, e.getDescription());
        ps.setString(3, e.getLieu());
        ps.setTimestamp(4, new Timestamp(e.getDate().getTime()));
        ps.setInt(5, e.getIdOrganisateur());

        ps.executeUpdate();
        System.out.println("Evenement ajouté (PreparedStatement)");
    }
}
