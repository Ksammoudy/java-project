package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.User;
import org.example.services.FacebookAuthService;
import org.example.services.SessionManager;
import org.example.services.UserService;

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

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField != null && emailField.getText() != null
                ? emailField.getText().trim().toLowerCase()
                : "";

        String password = passwordField != null && passwordField.getText() != null
                ? passwordField.getText()
                : "";

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

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        User user = userService.login(email, password);

        if (user == null) {
            showError("Email ou mot de passe incorrect.");
            return;
        }

        SessionManager.setCurrentUser(user);

        if (rememberMeCheckBox != null && rememberMeCheckBox.isSelected()) {
            System.out.println("Option 'Se souvenir de moi' cochée.");
        }

        showSuccess("Connexion réussie.");
        redirectByUserType(user);
    }

    @FXML
    public void handleGoogleLogin() {
        showInfo("Connexion Google non encore implémentée en JavaFX.");
    }

    @FXML
    public void handleFacebookLogin() {
        showInfo("Ouverture de Facebook...");

        facebookAuthService.loginWithFacebook(new FacebookAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Platform.runLater(() -> {
                    if (user == null) {
                        showError("Connexion Facebook échouée.");
                        return;
                    }

                    SessionManager.setCurrentUser(user);
                    showSuccess("Connexion Facebook réussie.");
                    redirectByUserType(user);
                });
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> showError(message));
            }
        });
    }

    @FXML
    public void handleGithubLogin() {
        showInfo("Connexion GitHub non encore implémentée en JavaFX.");
    }

    @FXML
    public void handleForgotPassword() {
        Main.showForgotPasswordPage();
    }

    @FXML
    public void handleFaceLogin() {
        showInfo("Connexion avec visage à implémenter.");
    }

    @FXML
    public void handleRegister() {
        Main.showRegisterPage();
    }

    private void redirectByUserType(User user) {
        if (user == null) {
            showError("Utilisateur introuvable.");
            return;
        }

        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";

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
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(message);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", message);
        }
    }

    private void showInfo(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: blue;");
            messageLabel.setText(message);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information", message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
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