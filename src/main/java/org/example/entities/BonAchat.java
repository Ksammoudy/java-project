package org.example.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BonAchat {

    private Integer id;
    private Integer partenaireId;
    private String nomMagasin;
    private String logoMagasin;
    private String description;
    private Double valeurMonetaire;
    private Integer pointsRequis;
    private LocalDate dateDebut;
    private LocalDate dateExpiration;
    private Integer nombreMaximumUtilisations;
    private Integer nombreUtilisations;
    private String conditionsUtilisation;
    private String zoneGeographique;
    private String imagePromotionnelle;
    private String statut;
    private String historiqueModificationsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPartenaireId() {
        return partenaireId;
    }

    public void setPartenaireId(Integer partenaireId) {
        this.partenaireId = partenaireId;
    }

    public String getNomMagasin() {
        return nomMagasin;
    }

    public void setNomMagasin(String nomMagasin) {
        this.nomMagasin = nomMagasin;
    }

    public String getLogoMagasin() {
        return logoMagasin;
    }

    public void setLogoMagasin(String logoMagasin) {
        this.logoMagasin = logoMagasin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getValeurMonetaire() {
        return valeurMonetaire;
    }

    public void setValeurMonetaire(Double valeurMonetaire) {
        this.valeurMonetaire = valeurMonetaire;
    }

    public Integer getPointsRequis() {
        return pointsRequis;
    }

    public void setPointsRequis(Integer pointsRequis) {
        this.pointsRequis = pointsRequis;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Integer getNombreMaximumUtilisations() {
        return nombreMaximumUtilisations;
    }

    public void setNombreMaximumUtilisations(Integer nombreMaximumUtilisations) {
        this.nombreMaximumUtilisations = nombreMaximumUtilisations;
    }

    public Integer getNombreUtilisations() {
        return nombreUtilisations;
    }

    public void setNombreUtilisations(Integer nombreUtilisations) {
        this.nombreUtilisations = nombreUtilisations;
    }

    public String getConditionsUtilisation() {
        return conditionsUtilisation;
    }

    public void setConditionsUtilisation(String conditionsUtilisation) {
        this.conditionsUtilisation = conditionsUtilisation;
    }

    public String getZoneGeographique() {
        return zoneGeographique;
    }

    public void setZoneGeographique(String zoneGeographique) {
        this.zoneGeographique = zoneGeographique;
    }

    public String getImagePromotionnelle() {
        return imagePromotionnelle;
    }

    public void setImagePromotionnelle(String imagePromotionnelle) {
        this.imagePromotionnelle = imagePromotionnelle;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getHistoriqueModificationsJson() {
        return historiqueModificationsJson;
    }

    public void setHistoriqueModificationsJson(String historiqueModificationsJson) {
        this.historiqueModificationsJson = historiqueModificationsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
