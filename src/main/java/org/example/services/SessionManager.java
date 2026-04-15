package org.example.services;

import org.example.models.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    // ✅ AJOUT ICI
    public static void logout() {
        clearSession();
        System.out.println("Utilisateur déconnecté.");
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}