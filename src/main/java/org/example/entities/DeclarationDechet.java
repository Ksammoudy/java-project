package org.example.entities;

import java.time.LocalDateTime;

public class DeclarationDechet {

    private Integer id;
    private String description;
    private String statut;
    private Integer typeDechetId;
    private String typeDechetLibelle;
    private String photo;
    private Double latitude;
    private Double longitude;
    private Double quantite;
    private String unite;
    private LocalDateTime createdAt;
    private Double scoreIa;
    private Integer pointsAttribues;
    private String qrCode;
    private Integer citoyenId;
    private String citoyenEmail;
    private Integer valorisateurConfirmateurId;
    private LocalDateTime dateConfirmation;
    private String statutHistoriqueJson;
    private LocalDateTime deletedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getTypeDechetId() {
        return typeDechetId;
    }

    public void setTypeDechetId(Integer typeDechetId) {
        this.typeDechetId = typeDechetId;
    }

    public String getTypeDechetLibelle() {
        return typeDechetLibelle;
    }

    public void setTypeDechetLibelle(String typeDechetLibelle) {
        this.typeDechetLibelle = typeDechetLibelle;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getScoreIa() {
        return scoreIa;
    }

    public void setScoreIa(Double scoreIa) {
        this.scoreIa = scoreIa;
    }

    public Integer getPointsAttribues() {
        return pointsAttribues;
    }

    public void setPointsAttribues(Integer pointsAttribues) {
        this.pointsAttribues = pointsAttribues;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Integer getCitoyenId() {
        return citoyenId;
    }

    public void setCitoyenId(Integer citoyenId) {
        this.citoyenId = citoyenId;
    }

    public String getCitoyenEmail() {
        return citoyenEmail;
    }

    public void setCitoyenEmail(String citoyenEmail) {
        this.citoyenEmail = citoyenEmail;
    }

    public Integer getValorisateurConfirmateurId() {
        return valorisateurConfirmateurId;
    }

    public void setValorisateurConfirmateurId(Integer valorisateurConfirmateurId) {
        this.valorisateurConfirmateurId = valorisateurConfirmateurId;
    }

    public LocalDateTime getDateConfirmation() {
        return dateConfirmation;
    }

    public void setDateConfirmation(LocalDateTime dateConfirmation) {
        this.dateConfirmation = dateConfirmation;
    }

    public String getStatutHistoriqueJson() {
        return statutHistoriqueJson;
    }

    public void setStatutHistoriqueJson(String statutHistoriqueJson) {
        this.statutHistoriqueJson = statutHistoriqueJson;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
