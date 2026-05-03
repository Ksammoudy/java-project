package org.example.models;

import java.time.LocalDateTime;

public class ZonePolluee {
    private int id;
    private String nomZone;
    private String coordonneesGps;
    private int niveauPollution;
    private LocalDateTime dateIdentification;
    private IndicateurImpact indicateur;  // ← Changé de int à IndicateurImpact

    public ZonePolluee() {}

    public ZonePolluee(String nomZone, String coordonneesGps, int niveauPollution, LocalDateTime dateIdentification, IndicateurImpact indicateur) {
        this.nomZone = nomZone;
        this.coordonneesGps = coordonneesGps;
        this.niveauPollution = niveauPollution;
        this.dateIdentification = dateIdentification;
        this.indicateur = indicateur;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomZone() { return nomZone; }
    public void setNomZone(String nomZone) { this.nomZone = nomZone; }

    public String getCoordonneesGps() { return coordonneesGps; }
    public void setCoordonneesGps(String coordonneesGps) { this.coordonneesGps = coordonneesGps; }

    public int getNiveauPollution() { return niveauPollution; }
    public void setNiveauPollution(int niveauPollution) { this.niveauPollution = niveauPollution; }

    public LocalDateTime getDateIdentification() { return dateIdentification; }
    public void setDateIdentification(LocalDateTime dateIdentification) { this.dateIdentification = dateIdentification; }

    public IndicateurImpact getIndicateur() { return indicateur; }
    public void setIndicateur(IndicateurImpact indicateur) { this.indicateur = indicateur; }

    @Override
    public String toString() {
        return nomZone + " (niveau " + niveauPollution + "/10)";
    }
}