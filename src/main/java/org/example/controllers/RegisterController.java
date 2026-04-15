package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.services.UserService;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private RadioButton citoyenRadio;

    @FXML
    private RadioButton valorizerRadio;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox agreeTermsCheckBox;

    @FXML
    private TextArea faceEmbeddingArea;

    @FXML
    private Label messageLabel;

    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }

        if (citoyenRadio != null) {
            citoyenRadio.setSelected(true);
        }
    }

    @FXML
    public void handleRegister() {
        String nom = nomField != null && nomField.getText() != null
                ? nomField.getText().trim()
                : "";

        String prenom = prenomField != null && prenomField.getText() != null
                ? prenomField.getText().trim()
                : "";

        String email = emailField != null && emailField.getText() != null
                ? emailField.getText().trim().toLowerCase()
                : "";

        String telephone = telephoneField != null && telephoneField.getText() != null
                ? telephoneField.getText().trim()
                : "";

        String password = passwordField != null && passwordField.getText() != null
                ? passwordField.getText()
                : "";

        String confirmPassword = confirmPasswordField != null && confirmPasswordField.getText() != null
                ? confirmPasswordField.getText()
                : "";

        String faceEmbedding = faceEmbeddingArea != null && faceEmbeddingArea.getText() != null
                ? faceEmbeddingArea.getText().trim()
                : "";

        String type = null;
        if (citoyenRadio != null && citoyenRadio.isSelected()) {
            type = "CITIZEN";
        } else if (valorizerRadio != null && valorizerRadio.isSelected()) {
            type = "VALORIZER";
        }

        if (nom.isEmpty()) {
            showError("Le champ nom est obligatoire.");
            return;
        }

        if (prenom.isEmpty()) {
            showError("Le champ prénom est obligatoire.");
            return;
        }

        if (email.isEmpty()) {
            showError("Le champ email est obligatoire.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Veuillez saisir une adresse email valide.");
            return;
        }

        if (telephone.isEmpty()) {
            showError("Le champ téléphone est obligatoire.");
            return;
        }

        if (!telephone.matches("\\d{8}")) {
            showError("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        if (type == null) {
            showError("Veuillez sélectionner un type d'utilisateur.");
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

        if (confirmPassword.isEmpty()) {
            showError("Veuillez confirmer le mot de passe.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (agreeTermsCheckBox == null || !agreeTermsCheckBox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation.");
            return;
        }

        String result = userService.registerUser(
                nom,
                prenom,
                email,
                telephone,
                type,
                password,
                confirmPassword,
                true,
                faceEmbedding
        );

        if ("SUCCESS".equals(result)) {
            showSuccess("Compte créé avec succès. Connecte-toi maintenant.");
            clearFields();
        } else {
            showError(result);
        }
    }

    @FXML
    public void handleBackToLogin() {
        Main.showLoginPage();
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

    private void clearFields() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (emailField != null) emailField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (faceEmbeddingArea != null) faceEmbeddingArea.clear();
        if (agreeTermsCheckBox != null) agreeTermsCheckBox.setSelected(false);
        if (citoyenRadio != null) citoyenRadio.setSelected(true);
        if (valorizerRadio != null) valorizerRadio.setSelected(false);
    }
}