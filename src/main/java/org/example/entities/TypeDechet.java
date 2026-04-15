package org.example.entities;

public class TypeDechet {

    private Integer id;
    private String libelle;
    private Double valeurPointsKg;
    private String descriptionTri;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Double getValeurPointsKg() {
        return valeurPointsKg;
    }

    public void setValeurPointsKg(Double valeurPointsKg) {
        this.valeurPointsKg = valeurPointsKg;
    }

    public String getDescriptionTri() {
        return descriptionTri;
    }

    public void setDescriptionTri(String descriptionTri) {
        this.descriptionTri = descriptionTri;
    }
}
