package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.ResetPasswordToken;
import org.example.models.User;
import org.example.services.ResetPasswordService;
import org.example.services.UserService;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private final UserService userService = UserService.getInstance();
    private final ResetPasswordService resetPasswordService = ResetPasswordService.getInstance();

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    public void handleSendLink() {
        String email = emailField != null && emailField.getText() != null
                ? emailField.getText().trim().toLowerCase()
                : "";

        // Contrôle de saisie
        if (email.isEmpty()) {
            showError("Veuillez saisir votre email.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Veuillez saisir une adresse email valide.");
            return;
        }

        User user = userService.getUserByEmail(email);

        // Message générique pour éviter de révéler si l'email existe ou non
        if (user != null) {
            ResetPasswordToken token = resetPasswordService.createToken(user, 30);
            boolean sent = resetPasswordService.sendResetEmail(user, token);

            if (sent) {
                showSuccess(
                        "Si un compte existe avec cet email, un code de réinitialisation a été envoyé.\n" +
                                "Consultez votre boîte mail puis ouvrez la page de réinitialisation."
                );
            } else {
                showError(
                        "Le code a été généré, mais l'email n'a pas pu être envoyé.\n" +
                                "Vérifiez la configuration SMTP dans MailUtil.java."
                );
            }
        } else {
            showSuccess("Si un compte existe avec cet email, un code de réinitialisation a été envoyé.");
        }
    }

    @FXML
    public void handleBackToLogin() {
        Main.showLoginPage();
    }

    @FXML
    public void handleOpenResetPage() {
        Main.showResetPasswordPage();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText(message);
        }
    }
}