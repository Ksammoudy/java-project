package org.example.utils;

import org.example.models.User;
import org.example.services.SessionManager;

/**
 * Utilitaire pour la session citoyen.
 */
public class CitizenSession {

    public static User ensureCitizenUser() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            user = new User();
            user.setNom("Demo");
            user.setPrenom("Citoyen");
            user.setEmail("citoyen@wastewise.tn");
            user.setType("CITIZEN");
            SessionManager.setCurrentUser(user);
        }
        return user;
    }

    public static String fullName(User user) {
        if (user == null) return "Utilisateur";
        String prenom = user.getPrenom() == null ? "" : user.getPrenom();
        String nom = user.getNom() == null ? "" : user.getNom();
        String combined = (prenom + " " + nom).trim();
        return combined.isEmpty() ? "Citoyen Demo" : combined;
    }

    public static Integer resolveCitizenDatabaseId() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return null;
        return user.getId() > 0 ? user.getId() : null;
    }
}
