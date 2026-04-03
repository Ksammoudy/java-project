package org.example.services;

import org.example.models.ResetPasswordToken;
import org.example.models.User;
import org.example.utils.MailUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResetPasswordService {

    private static ResetPasswordService instance;

    // stockage temporaire en mémoire
    private final Map<String, ResetPasswordToken> tokenStore = new HashMap<>();

    private final UserService userService = UserService.getInstance();

    private ResetPasswordService() {
    }

    public static synchronized ResetPasswordService getInstance() {
        if (instance == null) {
            instance = new ResetPasswordService();
        }
        return instance;
    }

    public ResetPasswordToken createToken(User user, int ttlMinutes) {
        invalidateUserTokens(user);

        ResetPasswordToken token = new ResetPasswordToken();
        token.setUser(user);
        token.setToken(generateToken());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));

        tokenStore.put(token.getToken(), token);
        return token;
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private void invalidateUserTokens(User user) {
        for (ResetPasswordToken token : tokenStore.values()) {
            if (token.getUser() != null
                    && token.getUser().getEmail() != null
                    && user.getEmail() != null
                    && token.getUser().getEmail().equalsIgnoreCase(user.getEmail())
                    && !token.isUsed()) {
                token.setUsedAt(LocalDateTime.now());
            }
        }
    }

    public ResetPasswordToken findValidToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return null;
        }

        ResetPasswordToken token = tokenStore.get(tokenValue.trim());

        if (token == null) {
            return null;
        }

        if (token.isUsed() || token.isExpired()) {
            return null;
        }

        return token;
    }

    public boolean sendResetEmail(User user, ResetPasswordToken token) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || token == null) {
            return false;
        }

        String subject = "Réinitialisation de votre mot de passe - WasteWise TN";
        String content = "Bonjour " + user.getPrenom() + ",\n\n"
                + "Vous avez demandé la réinitialisation de votre mot de passe.\n\n"
                + "Votre code de réinitialisation est :\n"
                + token.getToken() + "\n\n"
                + "Ce code expire le : " + token.getExpiresAt() + "\n\n"
                + "Ouvrez l'application WasteWise TN, allez dans la page "
                + "\"Réinitialiser le mot de passe\", puis collez ce code.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n"
                + "Cordialement,\n"
                + "WasteWise TN";

        return MailUtil.sendEmail(user.getEmail(), subject, content);
    }

    public String resetPassword(String tokenValue, String newPassword, String confirmPassword) {
        ResetPasswordToken token = findValidToken(tokenValue);

        if (token == null) {
            return "Code invalide ou expiré.";
        }

        if (newPassword == null || newPassword.isBlank()) {
            return "Veuillez saisir un mot de passe.";
        }

        if (newPassword.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères.";
        }

        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return "Les mots de passe ne correspondent pas.";
        }

        User user = token.getUser();
        if (user == null) {
            return "Utilisateur introuvable.";
        }

        boolean updated = userService.updatePassword(user.getId(), newPassword);
        if (!updated) {
            return "Erreur lors de la mise à jour du mot de passe.";
        }

        token.setUsedAt(LocalDateTime.now());
        return "SUCCESS";
    }
}