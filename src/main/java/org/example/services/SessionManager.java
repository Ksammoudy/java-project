package org.example.services;

import org.example.models.User;

public class SessionManager {

    // ✅ Utilisateur complètement connecté (après 2FA)
    private static User currentUser;

    // 🔐 Utilisateur en attente de validation 2FA
    private static User pendingUser;

    private SessionManager() {
    }

    // =========================
    // SESSION PRINCIPALE
    // =========================
    public static void setCurrentUser(User user) {
        currentUser = user;
        pendingUser = null; // on nettoie le pending
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // =========================
    // 2FA (TEMP USER)
    // =========================
    public static void setPendingUser(User user) {
        pendingUser = user;
    }

    public static User getPendingUser() {
        return pendingUser;
    }

    public static boolean isTwoFactorPending() {
        return pendingUser != null;
    }

    public static void clearPendingUser() {
        pendingUser = null;
    }

    // =========================
    // LOGOUT
    // =========================
    public static void logout() {
        currentUser = null;
        pendingUser = null;
        System.out.println("Utilisateur déconnecté.");
    }

    public static void clearSession() {
        logout();
    }
}