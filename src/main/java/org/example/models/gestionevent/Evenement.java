package org.example.models.gestionevent;

import java.util.Objects;
import java.sql.Date;

public class Evenement {
    private int id;
    private String titre;
    private String description;
    private Date date;
    private int idOrganisateur;
    private String nomOrganisateur;
    private String lieu;

    public Evenement() {
    }

    // 1. Constructeur avec ID (Utilisé par le Service pour lire la base)
    public Evenement(int id, String titre, String description, Date date, int idOrganisateur, String lieu, String nomOrganisateur) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.idOrganisateur = idOrganisateur;
        this.lieu = lieu;             // Salla7na el tartib hne
        this.nomOrganisateur = nomOrganisateur;
    }

    // 2. Constructeur sans ID (Utilisé pour l'Ajout)
    // TARTIB: Titre, Description, Lieu, Date, NomOrganisateur
    public Evenement(String titre, String description, String lieu, Date date, String nomOrganisateur) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;             // Salla7na el tartib hne
        this.date = date;
        this.nomOrganisateur = nomOrganisateur;
    }

    // --- GETTERS & SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getNomOrganisateur() { return nomOrganisateur; }
    public void setNomOrganisateur(String nomOrganisateur) { this.nomOrganisateur = nomOrganisateur; }

    public int getIdOrganisateur() { return idOrganisateur; }
    public void setIdOrganisateur(int idOrganisateur) { this.idOrganisateur = idOrganisateur; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    @Override
    public String toString() {
        return "Evenement{" + "titre='" + titre + '\'' + ", lieu='" + lieu + '\'' + ", organisateur='" + nomOrganisateur + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return id == evenement.id && idOrganisateur == evenement.idOrganisateur &&
                Objects.equals(titre, evenement.titre) &&
                Objects.equals(description, evenement.description) &&
                Objects.equals(date, evenement.date) &&
                Objects.equals(lieu, evenement.lieu) &&
                Objects.equals(nomOrganisateur, evenement.nomOrganisateur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, description, date, idOrganisateur, lieu, nomOrganisateur);
    }
}