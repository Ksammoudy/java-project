package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.SocialLoginResult;
import org.example.models.User;
import org.example.services.FacebookAuthService;
import org.example.services.GoogleAuthService;
import org.example.services.SessionManager;
import org.example.services.UserService;
import org.example.utils.UserTypeDialog;

import java.time.LocalDateTime;
import java.util.UUID;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label messageLabel;

    private final UserService userService = UserService.getInstance();
    private final FacebookAuthService facebookAuthService = new FacebookAuthService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField != null ? emailField.getText().trim().toLowerCase() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (email.isEmpty()) {
            showError("Le champ email est obligatoire.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Veuillez saisir une adresse email valide.");
            return;
        }

        if (password.isEmpty()) {
            showError("Le champ mot de passe est obligatoire.");
            return;
        }

        User user = userService.login(email, password);

        if (user == null) {
            showError("Email ou mot de passe incorrect.");
            return;
        }

        if (!user.isActive()) {
            showError("Compte désactivé.");
            return;
        }

        if (rememberMeCheckBox != null && rememberMeCheckBox.isSelected()) {
            System.out.println("Remember me activé.");
        }

        if (user.isTwoFactorEnabled()) {
            SessionManager.setPendingUser(user);
            showInfo("Veuillez saisir le code Google Authenticator.");
            Main.showTwoFactorVerifyPage(user);
            return;
        }

        SessionManager.setCurrentUser(user);
        showSuccess("Connexion réussie.");
        redirectByUserType(user);
    }

    @FXML
    public void handleGoogleLogin() {
        showInfo("Ouverture de Google...");

        googleAuthService.loginWithGoogle(new GoogleAuthService.AuthCallback() {
            @Override
            public void onSuccess(SocialLoginResult result) {
                Platform.runLater(() -> processSocialLogin(result));
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showError(message));
            }
        });
    }

    @FXML
    public void handleFacebookLogin() {
        showInfo("Ouverture de Facebook...");

        facebookAuthService.loginWithFacebook(new FacebookAuthService.AuthCallback() {
            @Override
            public void onSuccess(SocialLoginResult result) {
                Platform.runLater(() -> processSocialLogin(result));
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showError(message));
            }
        });
    }

    @FXML
    public void handleGithubLogin() {
        showInfo("Connexion GitHub non encore implémentée.");
    }

    @FXML
    public void handleForgotPassword() {
        Main.showForgotPasswordPage();
    }

    @FXML
    public void handleFaceLogin() {
        Main.showFaceLoginPage();
    }

    @FXML
    public void handleRegister() {
        Main.showRegisterPage();
    }

    private void processSocialLogin(SocialLoginResult result) {
        if (result == null) {
            showError("Connexion sociale échouée.");
            return;
        }

        if (result.isExistingUser()) {
            User user = result.getUser();

            if (user == null) {
                showError("Utilisateur introuvable.");
                return;
            }

            if (user.isTwoFactorEnabled()) {
                SessionManager.setPendingUser(user);
                Main.showTwoFactorVerifyPage(user);
                return;
            }

            SessionManager.setCurrentUser(user);
            showSuccess("Connexion " + result.getProvider() + " réussie.");
            redirectByUserType(user);
            return;
        }

        String selectedType = UserTypeDialog.showDialog();

        if (selectedType == null || selectedType.isBlank()) {
            showInfo("Choix annulé.");
            return;
        }

        User createdUser = createSocialUser(
                result.getEmail(),
                result.getFullName(),
                selectedType
        );

        if (createdUser == null) {
            showError("Impossible de créer le compte.");
            return;
        }

        SessionManager.setCurrentUser(createdUser);
        showSuccess("Compte créé avec succès.");
        redirectByUserType(createdUser);
    }

    private User createSocialUser(String email, String fullName, String type) {
        if (email == null || email.isBlank()) {
            return null;
        }

        User existing = userService.getUserByEmail(email.trim().toLowerCase());

        if (existing != null) {
            return existing;
        }

        User user = new User();

        user.setEmail(email.trim().toLowerCase());

        String prenom = "User";
        String nom = "Social";

        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+");

            if (parts.length > 0) {
                prenom = parts[0];
            }

            if (parts.length > 1) {
                nom = parts[parts.length - 1];
            }
        }

        user.setPrenom(prenom);
        user.setNom(nom);
        user.setPassword(UUID.randomUUID().toString());
        user.setRoles("[]");
        user.setType(type);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setVerified(true);
        user.setTwoFactorEnabled(false);
        user.setFaceEmbedding(null);
        user.setFaceUpdatedAt(null);
        user.setLastSeenAt(null);
        user.setGoogleAuthenticatorSecret(null);

        boolean added = userService.addUser(user);

        if (!added) {
            return null;
        }

        return userService.getUserByEmail(user.getEmail());
    }

    private void redirectByUserType(User user) {
        if (user == null) {
            showError("Utilisateur introuvable.");
            return;
        }

        String type = user.getType() != null
                ? user.getType().trim().toUpperCase()
                : "";

        switch (type) {
            case "ADMIN":
                Main.showDashboardAdmin();
                break;

            case "VALORIZER":
            case "VALORISATEUR":
                Main.showDashboardValorizer();
                break;

            case "CITIZEN":
            case "CITOYEN":
                Main.showDashboardCitizen();
                break;

            default:
                showError("Type utilisateur inconnu.");
                break;
        }
    }

    private boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText(message);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", message);
        }
    }

    private void showInfo(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill:blue;");
            messageLabel.setText(message);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill:green;");
            messageLabel.setText(message);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Succès", message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}