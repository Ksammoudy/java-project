package services;

import entities.AppelOffre;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ServiceAppelOffre {
    private Connection cnx;

    public ServiceAppelOffre() {
        this.cnx = null;
    }

    private Connection getConnection() throws SQLException {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyConnection.getInstance().getConnection();
            }
            if (cnx == null || cnx.isClosed()) {
                throw new SQLException("Connexion JDBC fermee ou indisponible.");
            }
            return cnx;
        } catch (IllegalStateException e) {
            throw new SQLException("Connexion JDBC indisponible.", e);
        }
    }

    public void ajouter(AppelOffre a) throws SQLException {
        valider(a);

        String sql = "INSERT INTO appel_offre (titre, description, quantite_demandee, date_limite, valorisateur_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, a.getTitre().trim());
            pst.setString(2, a.getDescription().trim());
            pst.setDouble(3, a.getQuantiteDemandee());
            pst.setTimestamp(4, a.getDateLimite());
            pst.setInt(5, a.getValorisateurId());

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Insertion echouee: aucune ligne ajoutee dans appel_offre.");
            }
        }
    }

    public void modifier(AppelOffre a) throws SQLException {
        if (a == null || a.getId() <= 0) {
            throw new IllegalArgumentException("id invalide pour la modification.");
        }
        valider(a);

        String sql = "UPDATE appel_offre SET titre=?, description=?, quantite_demandee=?, date_limite=?, valorisateur_id=? WHERE id=?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, a.getTitre().trim());
            pst.setString(2, a.getDescription().trim());
            pst.setDouble(3, a.getQuantiteDemandee());
            pst.setTimestamp(4, a.getDateLimite());
            pst.setInt(5, a.getValorisateurId());
            pst.setInt(6, a.getId());

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Modification echouee: aucun appel_offre trouve avec id=" + a.getId());
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id invalide pour la suppression.");
        }

        String sql = "DELETE FROM appel_offre WHERE id=?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);

            int affected = pst.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Suppression echouee: aucun appel_offre trouve avec id=" + id);
            }
        }
    }

    public AppelOffre recupererParId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id invalide.");
        }

        String sql = "SELECT id, titre, description, quantite_demandee, date_limite, valorisateur_id FROM appel_offre WHERE id=?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public List<AppelOffre> recupererTout() throws SQLException {
        List<AppelOffre> list = new ArrayList<>();
        String sql = "SELECT id, titre, description, quantite_demandee, date_limite, valorisateur_id FROM appel_offre ORDER BY id DESC";

        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    public List<AppelOffre> recupererAppelsNonExpires() throws SQLException {
        List<AppelOffre> list = new ArrayList<>();
        String sql = "SELECT id, titre, description, quantite_demandee, date_limite, valorisateur_id " +
                "FROM appel_offre WHERE date_limite > ? ORDER BY date_limite ASC";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public List<AppelOffre> recupererParValorisateur(int valorisateurId) throws SQLException {
        if (valorisateurId <= 0) {
            throw new IllegalArgumentException("valorisateurId invalide.");
        }

        List<AppelOffre> list = new ArrayList<>();
        String sql = "SELECT id, titre, description, quantite_demandee, date_limite, valorisateur_id " +
                "FROM appel_offre WHERE valorisateur_id = ? ORDER BY date_limite DESC";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, valorisateurId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public List<AppelOffre> rechercherParTitre(String motCle) throws SQLException {
        String keyword = motCle == null ? "" : motCle.trim();
        if (keyword.isEmpty()) {
            throw new IllegalArgumentException("Le mot-cle de recherche est obligatoire.");
        }

        List<AppelOffre> list = new ArrayList<>();
        String sql = "SELECT id, titre, description, quantite_demandee, date_limite, valorisateur_id " +
                "FROM appel_offre WHERE LOWER(titre) LIKE LOWER(?) ORDER BY id DESC";

        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    private AppelOffre mapRow(ResultSet rs) throws SQLException {
        return new AppelOffre(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getDouble("quantite_demandee"),
                rs.getTimestamp("date_limite"),
                rs.getInt("valorisateur_id")
        );
    }

    private void valider(AppelOffre a) {
        if (a == null) {
            throw new IllegalArgumentException("AppelOffre null.");
        }

        String titre = a.getTitre() == null ? "" : a.getTitre().trim();
        String description = a.getDescription() == null ? "" : a.getDescription().trim();

        if (titre.isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }

        if (description.isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire.");
        }

        if (a.getQuantiteDemandee() <= 0) {
            throw new IllegalArgumentException("quantite_demandee doit etre strictement positive.");
        }

        if (a.getDateLimite() == null) {
            throw new IllegalArgumentException("date_limite obligatoire.");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (!a.getDateLimite().after(now)) {
            throw new IllegalArgumentException("date_limite doit etre strictement dans le futur.");
        }

        if (a.getValorisateurId() <= 0) {
            throw new IllegalArgumentException("valorisateur_id invalide.");
        }

        a.setTitre(titre);
        a.setDescription(description);
    }
}
