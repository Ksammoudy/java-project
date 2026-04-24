package org.example.models.gestionevent;

import java.util.Objects;
import java.sql.Date;

public class Evenement {
    private int id;
    private String titre;
    private String description;
    private String lieu;
    private Date Date; // Houni el type houa Date (majuscule)
    private int idOrganisateur;

    public Evenement() {
    }

    // Constructeur avec ID (bch naqraw mel base)
    public Evenement(int id, String titre, String description, String lieu, Date date, int idOrganisateur) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.Date = date;
        this.idOrganisateur = idOrganisateur;
    }

    // Constructeur sans ID (bch n-ajoutiw)
    public Evenement(String titre, String description, String lieu, Date date, int idOrganisateur) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.Date = date;
        this.idOrganisateur = idOrganisateur;
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

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        this.Date = date;
    }

    public int getIdOrganisateur() {
        return idOrganisateur;
    }

    public void setIdOrganisateur(int idOrganisateur) {
        this.idOrganisateur = idOrganisateur;
    }

    @Override
    public String toString() {
        return "Evenement{" + "titre='" + titre + '\'' + ", date=" + Date + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return id == evenement.id && idOrganisateur == evenement.idOrganisateur && Objects.equals(titre, evenement.titre) && Objects.equals(description, evenement.description) && Objects.equals(lieu, evenement.lieu) && Objects.equals(Date, evenement.Date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, description, lieu, Date, idOrganisateur);
    }
}

