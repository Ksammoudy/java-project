package org.example.models;

import java.time.LocalDateTime;

public class IndicateurImpact {
    private int id;
    private double totalKgRecoltes;
    private double co2Evite;
    private LocalDateTime dateCalcul;

    public IndicateurImpact() {}

    public IndicateurImpact(double totalKgRecoltes, double co2Evite, LocalDateTime dateCalcul) {
        this.totalKgRecoltes = totalKgRecoltes;
        this.co2Evite = co2Evite;
        this.dateCalcul = dateCalcul;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotalKgRecoltes() { return totalKgRecoltes; }
    public void setTotalKgRecoltes(double totalKgRecoltes) { this.totalKgRecoltes = totalKgRecoltes; }

    public double getCo2Evite() { return co2Evite; }
    public void setCo2Evite(double co2Evite) { this.co2Evite = co2Evite; }

    public LocalDateTime getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(LocalDateTime dateCalcul) { this.dateCalcul = dateCalcul; }

    @Override
    public String toString() {
        return "IndicateurImpact{id=" + id + ", totalKg=" + totalKgRecoltes + ", co2=" + co2Evite + "}";
    }
}