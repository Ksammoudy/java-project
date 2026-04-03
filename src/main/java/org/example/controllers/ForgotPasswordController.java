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
    public void handleSendLink() {
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";

        if (email.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Veuillez saisir votre email.");
            return;
        }

        User user = userService.findByEmail(email);

        // Message générique comme Symfony
        if (user != null) {
            ResetPasswordToken token = resetPasswordService.createToken(user, 30);
            boolean sent = resetPasswordService.sendResetEmail(user, token);

            if (sent) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText(
                        "Si un compte existe avec cet email, un code de réinitialisation a été envoyé.\n"
                                + "Consultez votre boîte mail puis ouvrez la page de réinitialisation."
                );
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(
                        "Le code a été généré, mais l'email n'a pas pu être envoyé.\n"
                                + "Vérifiez la configuration SMTP dans MailUtil.java."
                );
            }
        } else {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Si un compte existe avec cet email, un code de réinitialisation a été envoyé.");
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
}