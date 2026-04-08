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
    public void handleRegister() {
        // Récupération et nettoyage des champs
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String telephone = telephoneField.getText() != null ? telephoneField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";
        String faceEmbedding = faceEmbeddingArea.getText() != null ? faceEmbeddingArea.getText().trim() : "";

        String type = null;
        if (citoyenRadio.isSelected()) {
            type = "CITIZEN";
        } else if (valorizerRadio.isSelected()) {
            type = "VALORIZER";
        }

        // Contrôle de saisie
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

        if (!agreeTermsCheckBox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation.");
            return;
        }

        // Appel du service
        String result = userService.registerUser(
                nom,
                prenom,
                email,
                telephone,
                type,
                password,
                confirmPassword,
                agreeTermsCheckBox.isSelected(),
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
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        faceEmbeddingArea.clear();
        agreeTermsCheckBox.setSelected(false);

        citoyenRadio.setSelected(true);
        valorizerRadio.setSelected(false);
        messageLabel.setText("");
    }
}