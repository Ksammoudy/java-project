package org.example.utils;

import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.UserService;

import java.sql.SQLException;
import java.util.Locale;


public final class CitizenSession {

    private CitizenSession() {
    }

    /**
     * Initialisation lazy du service utilisateur (évite de forcer la connexion DB au chargement de la classe).
     */
    private static UserService getUserService() {
        return UserService.getInstance();
    }

    public static User ensureCitizenUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return user;
        }
        User demo = new User();
        demo.setNom("Utilisateur");
        demo.setPrenom("Demo");
        demo.setEmail("demo@wastewise.tn");
        demo.setType("CITOYEN");
        SessionManager.setCurrentUser(demo);
        return demo;
    }

    /**
     * Identifiant {@code user.id} pour les requetes JDBC, ou {@code null} si aucun citoyen n'existe en base.
     */
    public static Integer resolveCitizenDatabaseId() {
        User u = SessionManager.getCurrentUser();
        if (u != null && u.getId() > 0) {
            return u.getId();
        }
        try {
            for (User user : getUserService().read()) {
                if (user.getType() == null) {
                    continue;
                }
                String t = user.getType().trim().toUpperCase(Locale.ROOT);
                if ("CITIZEN".equals(t) || "CITOYEN".equals(t)) {
                    SessionManager.setCurrentUser(user);
                    return user.getId();
                }
            }
        } catch (SQLException | RuntimeException e) {
            // Ignore: DB indisponible, retourner null
            return null;
        }
        return null;
    }

    public static String fullName(User user) {
        if (user == null) {
            return "Utilisateur Demo";
        }
        String prenom = user.getPrenom() == null ? "" : user.getPrenom();
        String nom = user.getNom() == null ? "" : user.getNom();
        String combined = (prenom + " " + nom).trim();
        return combined.isEmpty() ? "Utilisateur Demo" : combined;
    }
}
