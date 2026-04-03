package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.User;
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

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir l'email et le mot de passe.");
            return;
        }

        System.out.println("Tentative login avec : " + email);

        User user = userService.login(email, password);

        if (user == null) {
            showError("Email ou mot de passe incorrect.");
            System.out.println("Login échoué.");
            return;
        }

        System.out.println("Login réussi pour : " + user.getEmail());
        System.out.println("Type utilisateur : " + user.getType());

        SessionManager.setCurrentUser(user);

        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";

        if ("ADMIN".equals(type)) {
            System.out.println("Ouverture dashboard ADMIN");
            Main.showDashboardAdmin();
        } else if ("VALORIZER".equals(type) || "VALORISATEUR".equals(type)) {
            System.out.println("Ouverture dashboard VALORIZER");
            Main.showDashboardValorizer();
        } else {
            System.out.println("Ouverture dashboard CITIZEN");
            Main.showDashboardCitizen();
        }
    }

    @FXML
    public void handleGoogleLogin() {
        showInfo("Login Google non encore implémenté en JavaFX.");
    }

    @FXML
    public void handleFacebookLogin() {
        showInfo("Login Facebook non encore implémenté en JavaFX.");
    }

    @FXML
    public void handleGithubLogin() {
        showInfo("Login GitHub non encore implémenté en JavaFX.");
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

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: #dc3545;");
            messageLabel.setText(message);
        }
    }

    private void showInfo(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: #0d6efd;");
            messageLabel.setText(message);
        }
    }
}