package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.services.ResetPasswordService;

public class ResetPasswordController {

    @FXML
    private TextField tokenField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final ResetPasswordService resetPasswordService = ResetPasswordService.getInstance();

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleResetPassword() {
        String token = tokenField != null && tokenField.getText() != null
                ? tokenField.getText().trim()
                : "";

        String newPassword = passwordField != null && passwordField.getText() != null
                ? passwordField.getText().trim()
                : "";

        String confirmPassword = confirmPasswordField != null && confirmPasswordField.getText() != null
                ? confirmPasswordField.getText().trim()
                : "";

        if (token.isEmpty()) {
            showError("Veuillez saisir le code de réinitialisation.");
            return;
        }

        if (newPassword.isEmpty()) {
            showError("Veuillez saisir un nouveau mot de passe.");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        String result = resetPasswordService.resetPassword(token, newPassword, confirmPassword);

        if ("SUCCESS".equals(result)) {
            showSuccess("Mot de passe réinitialisé avec succès !");
        } else {
            showError(result);
        }
    }

    @FXML
    public void handleBackToLogin() {
        Main.showLoginPage();
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.getStyleClass().setAll("message-label", "message-error");
            messageLabel.setText(message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.getStyleClass().setAll("message-label", "message-success");
            messageLabel.setText(message);
        }
    }
}