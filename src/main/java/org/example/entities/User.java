package org.example.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {

    private Integer id;
    private String email;
    private String rolesJson;
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String photoProfil;
    private boolean notifyValidation;
    private boolean notifyPoints;
    private boolean notifyRefus;
    private boolean notifyNouvellesDeclarations;
    private String langue;
    private String theme;
    private String unitePreferee;
    private LocalDateTime dateInscription;
    private LocalDateTime derniereConnexion;
    private String statutCentre;
    private BigDecimal capaciteMaxJournaliere;
    private String organisationCentre;
    private String zoneCouverture;
    private String typesDechetsAcceptes;
    private String stripeConnectAccountId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRolesJson() {
        return rolesJson;
    }

    public void setRolesJson(String rolesJson) {
        this.rolesJson = rolesJson;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }

    public boolean isNotifyValidation() {
        return notifyValidation;
    }

    public void setNotifyValidation(boolean notifyValidation) {
        this.notifyValidation = notifyValidation;
    }

    public boolean isNotifyPoints() {
        return notifyPoints;
    }

    public void setNotifyPoints(boolean notifyPoints) {
        this.notifyPoints = notifyPoints;
    }

    public boolean isNotifyRefus() {
        return notifyRefus;
    }

    public void setNotifyRefus(boolean notifyRefus) {
        this.notifyRefus = notifyRefus;
    }

    public boolean isNotifyNouvellesDeclarations() {
        return notifyNouvellesDeclarations;
    }

    public void setNotifyNouvellesDeclarations(boolean notifyNouvellesDeclarations) {
        this.notifyNouvellesDeclarations = notifyNouvellesDeclarations;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUnitePreferee() {
        return unitePreferee;
    }

    public void setUnitePreferee(String unitePreferee) {
        this.unitePreferee = unitePreferee;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public String getStatutCentre() {
        return statutCentre;
    }

    public void setStatutCentre(String statutCentre) {
        this.statutCentre = statutCentre;
    }

    public BigDecimal getCapaciteMaxJournaliere() {
        return capaciteMaxJournaliere;
    }

    public void setCapaciteMaxJournaliere(BigDecimal capaciteMaxJournaliere) {
        this.capaciteMaxJournaliere = capaciteMaxJournaliere;
    }

    public String getOrganisationCentre() {
        return organisationCentre;
    }

    public void setOrganisationCentre(String organisationCentre) {
        this.organisationCentre = organisationCentre;
    }

    public String getZoneCouverture() {
        return zoneCouverture;
    }

    public void setZoneCouverture(String zoneCouverture) {
        this.zoneCouverture = zoneCouverture;
    }

    public String getTypesDechetsAcceptes() {
        return typesDechetsAcceptes;
    }

    public void setTypesDechetsAcceptes(String typesDechetsAcceptes) {
        this.typesDechetsAcceptes = typesDechetsAcceptes;
    }

    public String getStripeConnectAccountId() {
        return stripeConnectAccountId;
    }

    public void setStripeConnectAccountId(String stripeConnectAccountId) {
        this.stripeConnectAccountId = stripeConnectAccountId;
    }

    public String getNomComplet() {
        String safePrenom = prenom == null ? "" : prenom.trim();
        String safeNom = nom == null ? "" : nom.trim();
        return (safePrenom + " " + safeNom).trim();
    }
}
