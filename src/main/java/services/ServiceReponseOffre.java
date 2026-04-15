package services;

import entities.ReponseOffre;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponseOffre {
    private final Connection cnx;

    public ServiceReponseOffre() {
        this.cnx = MyConnection.getInstance().getConnection();
    }
    public void ajouter(ReponseOffre r) throws SQLException {
        valider(r);
        String sql = "INSERT INTO reponse_offre (quantite_proposee, date_soumis, statut, message, appel_offre_id, citoyen_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setDouble(1, r.getQuantiteProposee());
            pst.setTimestamp(2, r.getDateSoumis());
            pst.setString(3, normaliserStatut(r.getStatut()));
            pst.setString(4, normaliserMessage(r.getMessage()));
            pst.setInt(5, r.getAppelOffreId());
            pst.setInt(6, r.getCitoyenId());
            pst.executeUpdate();
        }
    }

    public void modifier(ReponseOffre r) throws SQLException {
        if (r.getId() <= 0) {
            throw new IllegalArgumentException("id invalide pour la modification.");
        }
        valider(r);
        String sql = "UPDATE reponse_offre SET quantite_proposee=?, date_soumis=?, statut=?, message=?, appel_offre_id=?, citoyen_id=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setDouble(1, r.getQuantiteProposee());
            pst.setTimestamp(2, r.getDateSoumis());
            pst.setString(3, normaliserStatut(r.getStatut()));
            pst.setString(4, normaliserMessage(r.getMessage()));
            pst.setInt(5, r.getAppelOffreId());
            pst.setInt(6, r.getCitoyenId());
            pst.setInt(7, r.getId());
            pst.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id invalide pour la suppression.");
        }
        String sql = "DELETE FROM reponse_offre WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
    public ReponseOffre recupererParId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id invalide.");
        }
        String sql = "SELECT id, quantite_proposee, date_soumis, statut, message, appel_offre_id, citoyen_id FROM reponse_offre WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<ReponseOffre> recupererTout() throws SQLException {
        List<ReponseOffre> list = new ArrayList<>();
        String sql = "SELECT id, quantite_proposee, date_soumis, statut, message, appel_offre_id, citoyen_id FROM reponse_offre ORDER BY id DESC";
        try (PreparedStatement pst = cnx.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void accepterReponse(int id) throws SQLException {
        mettreAJourStatut(id, ReponseOffre.STATUT_VALIDE);
    }

    public void refuserReponse(int id) throws SQLException {
        mettreAJourStatut(id, ReponseOffre.STATUT_REFUSE);
    }

    private void mettreAJourStatut(int id, String statut) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("id invalide pour statut.");
        }
        String sql = "UPDATE reponse_offre SET statut=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, normaliserStatut(statut));
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }
    private ReponseOffre mapRow(ResultSet rs) throws SQLException {
        return new ReponseOffre(
                rs.getInt("id"),
                rs.getDouble("quantite_proposee"),
                rs.getTimestamp("date_soumis"),
                rs.getString("statut"),
                rs.getString("message"),
                rs.getInt("appel_offre_id"),
                rs.getInt("citoyen_id")
        );
    }

    private void valider(ReponseOffre r) {
        if (r == null) {
            throw new IllegalArgumentException("ReponseOffre null.");
        }
        if (r.getQuantiteProposee() <= 0) {
            throw new IllegalArgumentException("quantite_proposee doit etre positive.");
        }
        if (r.getDateSoumis() == null) {
            throw new IllegalArgumentException("date_soumis obligatoire.");
        }
        normaliserStatut(r.getStatut());
        if (r.getAppelOffreId() <= 0) {
            throw new IllegalArgumentException("appel_offre_id invalide.");
        }
        if (r.getCitoyenId() <= 0) {
            throw new IllegalArgumentException("citoyen_id invalide.");
        }
    }

    private String normaliserStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return ReponseOffre.STATUT_EN_ATTENTE;
        }
        String s = statut.trim().toLowerCase().replace('_', ' ');
        if (s.equals("en attente") || s.equals("pending")) {
            return ReponseOffre.STATUT_EN_ATTENTE;
        }
        if (s.equals("valide") || s.equals("validee") || s.equals("acceptee")) {
            return ReponseOffre.STATUT_VALIDE;
        }
        if (s.equals("refuse") || s.equals("refusee") || s.equals("rejetee")) {
            return ReponseOffre.STATUT_REFUSE;
        }
        throw new IllegalArgumentException("statut invalide: " + statut);
    }

    private String normaliserMessage(String message) {
        if (message == null) {
            return null;
        }
        String m = message.trim();
        return m.isEmpty() ? null : m;
    }
}
