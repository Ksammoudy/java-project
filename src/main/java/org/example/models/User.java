package org.example.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private int id;
    private String email;
    private String roles;
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private String type;
    private LocalDateTime createdAt;
    private boolean isActive;
    private String faceEmbedding;
    private LocalDateTime faceUpdatedAt;
    private LocalDateTime lastSeenAt;
    private String googleAuthenticatorSecret;
    private boolean isTwoFactorEnabled;
    private boolean isVerified;

    public User() {
    }

    // Constructeur complet avec id
    public User(int id, String email, String roles, String password, String nom, String prenom,
                String telephone, String type, LocalDateTime createdAt, boolean isActive,
                String faceEmbedding, LocalDateTime faceUpdatedAt, LocalDateTime lastSeenAt,
                String googleAuthenticatorSecret, boolean isTwoFactorEnabled, boolean isVerified) {
        this.id = id;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.roles = roles;
        this.password = password;
        this.nom = nom != null ? nom.trim() : null;
        this.prenom = prenom != null ? prenom.trim() : null;
        this.telephone = telephone;
        this.type = type;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.faceEmbedding = faceEmbedding;
        this.faceUpdatedAt = faceUpdatedAt;
        this.lastSeenAt = lastSeenAt;
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
        this.isTwoFactorEnabled = isTwoFactorEnabled;
        this.isVerified = isVerified;
    }

    // Constructeur sans id pour insertion
    public User(String email, String roles, String password, String nom, String prenom,
                String telephone, String type, LocalDateTime createdAt, boolean isActive,
                String faceEmbedding, LocalDateTime faceUpdatedAt, LocalDateTime lastSeenAt,
                String googleAuthenticatorSecret, boolean isTwoFactorEnabled, boolean isVerified) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.roles = roles;
        this.password = password;
        this.nom = nom != null ? nom.trim() : null;
        this.prenom = prenom != null ? prenom.trim() : null;
        this.telephone = telephone;
        this.type = type;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.faceEmbedding = faceEmbedding;
        this.faceUpdatedAt = faceUpdatedAt;
        this.lastSeenAt = lastSeenAt;
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
        this.isTwoFactorEnabled = isTwoFactorEnabled;
        this.isVerified = isVerified;
    }

    // Constructeur simple utile pour tests
    public User(String email, String password, String nom, String prenom, String telephone, String type) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.password = password;
        this.nom = nom != null ? nom.trim() : null;
        this.prenom = prenom != null ? prenom.trim() : null;
        this.telephone = telephone;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
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
        this.nom = nom != null ? nom.trim() : null;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom != null ? prenom.trim() : null;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFaceEmbedding() {
        return faceEmbedding;
    }

    public void setFaceEmbedding(String faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }

    public LocalDateTime getFaceUpdatedAt() {
        return faceUpdatedAt;
    }

    public void setFaceUpdatedAt(LocalDateTime faceUpdatedAt) {
        this.faceUpdatedAt = faceUpdatedAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getGoogleAuthenticatorSecret() {
        return googleAuthenticatorSecret;
    }

    public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) {
        this.googleAuthenticatorSecret = googleAuthenticatorSecret;
    }

    public boolean isTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        isTwoFactorEnabled = twoFactorEnabled;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", isVerified=" + isVerified +
                ", isTwoFactorEnabled=" + isTwoFactorEnabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                isActive == user.isActive &&
                isTwoFactorEnabled == user.isTwoFactorEnabled &&
                isVerified == user.isVerified &&
                Objects.equals(email, user.email) &&
                Objects.equals(roles, user.roles) &&
                Objects.equals(password, user.password) &&
                Objects.equals(nom, user.nom) &&
                Objects.equals(prenom, user.prenom) &&
                Objects.equals(telephone, user.telephone) &&
                Objects.equals(type, user.type) &&
                Objects.equals(createdAt, user.createdAt) &&
                Objects.equals(faceEmbedding, user.faceEmbedding) &&
                Objects.equals(faceUpdatedAt, user.faceUpdatedAt) &&
                Objects.equals(lastSeenAt, user.lastSeenAt) &&
                Objects.equals(googleAuthenticatorSecret, user.googleAuthenticatorSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, roles, password, nom, prenom, telephone, type,
                createdAt, isActive, faceEmbedding, faceUpdatedAt, lastSeenAt,
                googleAuthenticatorSecret, isTwoFactorEnabled, isVerified);
    }
}