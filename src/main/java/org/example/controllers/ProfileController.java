package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.UserService;
import org.example.utils.PasswordUtil;

public class ProfileController {

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté.");
            return;
        }

        loadUserData();
    }

    private void loadUserData() {
        if (nomLabel != null) nomLabel.setText(valueOrDash(currentUser.getNom()));
        if (prenomLabel != null) prenomLabel.setText(valueOrDash(currentUser.getPrenom()));
        if (emailLabel != null) emailLabel.setText(valueOrDash(currentUser.getEmail()));
        if (telephoneLabel != null) telephoneLabel.setText(valueOrDash(currentUser.getTelephone()));
        if (roleLabel != null) roleLabel.setText(formatType(currentUser.getType()));

        if (nomField != null) nomField.setText(nullToEmpty(currentUser.getNom()));
        if (prenomField != null) prenomField.setText(nullToEmpty(currentUser.getPrenom()));
        if (emailField != null) emailField.setText(nullToEmpty(currentUser.getEmail()));
        if (telephoneField != null) telephoneField.setText(nullToEmpty(currentUser.getTelephone()));
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) return;

        String nom = nomField != null ? nomField.getText().trim() : "";
        String prenom = prenomField != null ? prenomField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim().toLowerCase() : "";
        String telephone = telephoneField != null ? telephoneField.getText().trim() : "";
        String currentPassword = currentPasswordField != null ? currentPasswordField.getText().trim() : "";
        String newPassword = newPasswordField != null ? newPasswordField.getText().trim() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText().trim() : "";

        // 🔴 CONTROLE DE SAISIE

        if (nom.isEmpty()) {
            setMessage("Le nom est obligatoire.", false);
            return;
        }

        if (prenom.isEmpty()) {
            setMessage("Le prénom est obligatoire.", false);
            return;
        }

        if (email.isEmpty()) {
            setMessage("L'email est obligatoire.", false);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            setMessage("Email invalide.", false);
            return;
        }

        if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
            setMessage("Le téléphone doit contenir 8 chiffres.", false);
            return;
        }

        if (userService.emailExistsForAnotherUser(email, currentUser.getId())) {
            setMessage("Cet email est déjà utilisé.", false);
            return;
        }

        // 🔐 Gestion mot de passe
        if (!newPassword.isEmpty()) {

            if (currentPassword.isEmpty()) {
                setMessage("Veuillez saisir votre mot de passe actuel.", false);
                return;
            }

            if (!PasswordUtil.checkPassword(currentPassword, currentUser.getPassword())) {
                setMessage("Mot de passe actuel incorrect.", false);
                return;
            }

            if (newPassword.length() < 6) {
                setMessage("Le nouveau mot de passe doit contenir au moins 6 caractères.", false);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                setMessage("Les mots de passe ne correspondent pas.", false);
                return;
            }

            currentUser.setPassword(PasswordUtil.hashPassword(newPassword));
        }

        // 🔹 Mise à jour
        currentUser.setNom(nom);
        currentUser.setPrenom(prenom);
        currentUser.setEmail(email);
        currentUser.setTelephone(telephone.isEmpty() ? null : telephone);

        boolean updated = userService.updateProfile(currentUser);

        if (updated) {
            SessionManager.setCurrentUser(currentUser);
            setMessage("Profil mis à jour avec succès.", true);
        } else {
            setMessage("Erreur lors de la mise à jour du profil.", false);
        }
    }
    @FXML
    private void handleEditProfilePage() {
        Main.showProfileEditPage();
    }

    @FXML
    private void handleViewProfilePage() {
        Main.showProfileViewPage();
    }

    @FXML
    private void handleBackToDashboard() {
        if (currentUser == null) return;

        String type = currentUser.getType() == null ? "" : currentUser.getType().toUpperCase();

        if (type.contains("VALORIZER") || type.contains("VALORISATEUR")) {
            Main.showDashboardValorizer();
        } else if (type.contains("ADMIN")) {
            Main.showDashboardAdmin();
        } else {
            Main.showDashboardCitizen();
        }
    }

    private void setMessage(String text, boolean success) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            messageLabel.setStyle(success
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatType(String type) {
        if (type == null || type.isBlank()) return "Citoyen";

        type = type.toUpperCase();

        if (type.contains("ADMIN")) return "Administrateur";
        if (type.contains("VALORIZER") || type.contains("VALORISATEUR")) return "Valorisateur";
        return "Citoyen";
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}