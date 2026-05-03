package entities;

import java.sql.Timestamp;

public class ReponseOffre {
    public static final String STATUT_EN_ATTENTE = "en attente";
    public static final String STATUT_VALIDE = "valide";
    public static final String STATUT_REFUSE = "refuse";

    private int id;
    private double quantiteProposee;
    private Timestamp dateSoumis;
    private String statut;
    private String message;
    private int appelOffreId;
    private int citoyenId;

    public ReponseOffre() {
        this.statut = STATUT_EN_ATTENTE;
        this.dateSoumis = new Timestamp(System.currentTimeMillis());
    }

    public ReponseOffre(double quantiteProposee, Timestamp dateSoumis, String statut, String message, int appelOffreId, int citoyenId) {
        this.quantiteProposee = quantiteProposee;
        this.dateSoumis = dateSoumis;
        this.statut = statut;
        this.message = message;
        this.appelOffreId = appelOffreId;
        this.citoyenId = citoyenId;
    }

    public ReponseOffre(int id, double quantiteProposee, Timestamp dateSoumis, String statut, String message, int appelOffreId, int citoyenId) {
        this.id = id;
        this.quantiteProposee = quantiteProposee;
        this.dateSoumis = dateSoumis;
        this.statut = statut;
        this.message = message;
        this.appelOffreId = appelOffreId;
        this.citoyenId = citoyenId;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getQuantiteProposee() {
        return quantiteProposee;
    }

    public void setQuantiteProposee(double quantiteProposee) {
        this.quantiteProposee = quantiteProposee;
    }

    public Timestamp getDateSoumis() {
        return dateSoumis;
    }

    public void setDateSoumis(Timestamp dateSoumis) {
        this.dateSoumis = dateSoumis;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public int getAppelOffreId() {
        return appelOffreId;
    }

    public void setAppelOffreId(int appelOffreId) {
        this.appelOffreId = appelOffreId;
    }

    public int getCitoyenId() {
        return citoyenId;
    }

    public void setCitoyenId(int citoyenId) {
        this.citoyenId = citoyenId;
    }

    @Override
    public String toString() {
        return "ReponseOffre{id=" + id + ", quantiteProposee=" + quantiteProposee + ", dateSoumis=" + dateSoumis + ", statut=" + statut + ", appelOffreId=" + appelOffreId + ", citoyenId=" + citoyenId + "}";
    }
}
