package org.example.entities;

import java.sql.Date;
import java.util.Objects;

public class Participation {
    private int id;
    private Date dateInscription;
    private int idEvenement;
    private int idCitoyen;

    // 1. Constructeur Vide
    public Participation() {
    }

    // 2. Constructeur avec Paramètres (Tout) - Lel Affichage
    public Participation(int id, Date dateInscription, int idEvenement, int idCitoyen) {
        this.id = id;
        this.dateInscription = dateInscription;
        this.idEvenement = idEvenement;
        this.idCitoyen = idCitoyen;
    }

    // 3. Constructeur sans ID - Lel Ajout (khater id auto-increment)
    public Participation(Date dateInscription, int idEvenement, int idCitoyen) {
        this.dateInscription = dateInscription;
        this.idEvenement = idEvenement;
        this.idCitoyen = idCitoyen;
    }

    // 4. Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public int getIdCitoyen() { return idCitoyen; }
    public void setIdCitoyen(int idCitoyen) { this.idCitoyen = idCitoyen; }

    // 5. ToString
    @Override
    public String toString() {
        return "Participation{" +
                "id=" + id +
                ", dateInscription=" + dateInscription +
                ", idEvenement=" + idEvenement +
                ", idCitoyen=" + idCitoyen +
                '}';
    }

    // 6. Equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participation that = (Participation) o;
        return id == that.id &&
                idEvenement == that.idEvenement &&
                idCitoyen == that.idCitoyen &&
                Objects.equals(dateInscription, that.dateInscription);
    }

    // 7. HashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, dateInscription, idEvenement, idCitoyen);
    }
}