package org.example.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class ResetPasswordToken {

    private int id;
    private User user;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;

    public ResetPasswordToken() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur complet
    public ResetPasswordToken(int id, User user, String token,
                              LocalDateTime expiresAt, LocalDateTime createdAt, LocalDateTime usedAt) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.usedAt = usedAt;
    }

    // Constructeur pour insertion
    public ResetPasswordToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    // 🔥 Méthodes utiles (bonne pratique)
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    @Override
    public String toString() {
        return "ResetPasswordToken{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", usedAt=" + usedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetPasswordToken that = (ResetPasswordToken) o;
        return id == that.id &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token);
    }
}