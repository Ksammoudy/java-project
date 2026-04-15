package org.example.entities;

import java.time.LocalDateTime;

public class Wallet {

    private Integer id;
    private Integer utilisateurId;
    private Integer soldeActuel;
    private LocalDateTime dateMj;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Integer getSoldeActuel() {
        return soldeActuel;
    }

    public void setSoldeActuel(Integer soldeActuel) {
        this.soldeActuel = soldeActuel;
    }

    public LocalDateTime getDateMj() {
        return dateMj;
    }

    public void setDateMj(LocalDateTime dateMj) {
        this.dateMj = dateMj;
    }
}
