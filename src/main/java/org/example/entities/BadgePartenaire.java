package org.example.entities;

import java.time.LocalDateTime;

public class BadgePartenaire {

    private Integer id;
    private Integer partenaireId;
    private String code;
    private String nom;
    private String description;
    private String couleur;
    private String icone;
    private Integer scoreImpact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean current;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Integer getScoreImpact() {
        return scoreImpact;
    }

    public void setScoreImpact(Integer scoreImpact) {
        this.scoreImpact = scoreImpact;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
