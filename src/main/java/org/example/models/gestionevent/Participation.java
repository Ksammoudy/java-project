package org.example.models.gestionevent;

import java.sql.Date;
import java.util.Objects;

public class Participation {
    private int id;
    private Date dateInscription;
    private int idEvenement;
    private int idCitoyen;
    private String nomCitoyen;
    private String nomEvenement;
    private String email; // 👈 1. Zidna l-email hne

    // 1. Constructeur Vide
    public Participation() {
    }

    // 2. Constructeur pour l'Affichage (Read)
    public Participation(int id, String nomCitoyen, String email, String nomEvenement, Date dateInscription) {
        this.id = id;
        this.nomCitoyen = nomCitoyen;
        this.email = email; // 👈
        this.nomEvenement = nomEvenement;
        this.dateInscription = dateInscription;
    }

    // 3. Constructeur pour l'Ajout (Dhibet mel Formulaire)
    public Participation(int idEvenement, String nomCitoyen, String email, Date dateInscription) {
        this.idEvenement = idEvenement;
        this.nomCitoyen = nomCitoyen;
        this.email = email; // 👈
        this.dateInscription = dateInscription;
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

    public String getNomCitoyen() { return nomCitoyen; }
    public void setNomCitoyen(String nomCitoyen) { this.nomCitoyen = nomCitoyen; }

    public String getNomEvenement() { return nomEvenement; }
    public void setNomEvenement(String nomEvenement) { this.nomEvenement = nomEvenement; }

    public String getEmail() { return email; } // 👈 Getter email
    public void setEmail(String email) { this.email = email; } // 👈 Setter email

    // 5. ToString (Zid fih el email bech t-thabbet fih)
    @Override
    public String toString() {
        return "Participation{" +
                "id=" + id +
                ", Citoyen='" + nomCitoyen + '\'' +
                ", Email='" + email + '\'' +
                ", Evenement='" + nomEvenement + '\'' +
                ", date=" + dateInscription +
                '}';
    }

    // 6. Equals & HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participation that = (Participation) o;
        return id == that.id &&
                Objects.equals(nomCitoyen, that.nomCitoyen) &&
                Objects.equals(email, that.email) && // 👈
                Objects.equals(nomEvenement, that.nomEvenement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nomCitoyen, email, nomEvenement);
    }
}