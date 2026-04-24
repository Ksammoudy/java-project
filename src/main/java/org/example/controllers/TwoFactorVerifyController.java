package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.TwoFactorService;

public class TwoFactorVerifyController {

    @FXML
    private TextField codeField;

    @FXML
    private Label messageLabel;

    private User user;

    @FXML
    public void initialize() {
        // Sécurité : si setUser() n'est pas appelé, on récupère le pending user
        if (this.user == null) {
            this.user = SessionManager.getPendingUser();
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    public void handleVerify() {
        // Sécurité supplémentaire
        if (user == null) {
            user = SessionManager.getPendingUser();
        }

        if (user == null) {
            showMessage("Utilisateur introuvable.", true);
            return;
        }

        String code = codeField != null && codeField.getText() != null
                ? codeField.getText().trim()
                : "";

        if (code.isEmpty()) {
            showMessage("Veuillez saisir le code.", true);
            return;
        }

        if (!code.matches("\\d{6}")) {
            showMessage("Le code doit contenir 6 chiffres.", true);
            return;
        }

        String secret = user.getGoogleAuthenticatorSecret();

        if (secret == null || secret.isBlank()) {
            showMessage("Aucun secret 2FA associé à cet utilisateur.", true);
            return;
        }

        boolean valid = TwoFactorService.verifyCode(secret, code);

        if (!valid) {
            showMessage("Code invalide !", true);
            return;
        }

        SessionManager.setCurrentUser(user);
        SessionManager.clearPendingUser();

        showMessage("Vérification réussie.", false);

        Main.redirectByUserType(user);
    }

    @FXML
    public void handleCancel() {
        SessionManager.clearPendingUser();
        Main.showLoginPage();
    }

    private void showMessage(String message, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            if (isError) {
                messageLabel.setStyle("-fx-text-fill: red;");
            } else {
                messageLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }
}