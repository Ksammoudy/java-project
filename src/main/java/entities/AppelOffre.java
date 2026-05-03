package entities;

import java.sql.Timestamp;

public class AppelOffre {
    private int id;
    private String titre;
    private String description;
    private double quantiteDemandee;
    private Timestamp dateLimite;
    private int valorisateurId;

    public AppelOffre() {
    }

    public AppelOffre(String titre, String description, double quantiteDemandee, Timestamp dateLimite, int valorisateurId) {
        this.titre = titre;
        this.description = description;
        this.quantiteDemandee = quantiteDemandee;
        this.dateLimite = dateLimite;
        this.valorisateurId = valorisateurId;
    }

    public AppelOffre(int id, String titre, String description, double quantiteDemandee, Timestamp dateLimite, int valorisateurId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.quantiteDemandee = quantiteDemandee;
        this.dateLimite = dateLimite;
        this.valorisateurId = valorisateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public void setQuantiteDemandee(double quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }

    public Timestamp getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(Timestamp dateLimite) {
        this.dateLimite = dateLimite;
    }

    public int getValorisateurId() {
        return valorisateurId;
    }

    public void setValorisateurId(int valorisateurId) {
        this.valorisateurId = valorisateurId;
    }

    @Override
    public String toString() {
        return "AppelOffre{id=" + id + ", titre=" + titre + ", quantiteDemandee=" + quantiteDemandee + ", dateLimite=" + dateLimite + ", valorisateurId=" + valorisateurId + "}";
    }
}
