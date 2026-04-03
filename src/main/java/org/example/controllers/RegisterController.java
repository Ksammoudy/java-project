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
        String type = citoyenRadio.isSelected() ? "CITIZEN" : "VALORIZER";

        String result = userService.registerUser(
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                telephoneField.getText(),
                type,
                passwordField.getText(),
                confirmPasswordField.getText(),
                agreeTermsCheckBox.isSelected(),
                faceEmbeddingArea.getText()
        );

        if ("SUCCESS".equals(result)) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Compte créé avec succès. Connecte-toi maintenant.");
            clearFields();
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(result);
        }
    }

    @FXML
    public void handleBackToLogin() {
        Main.showLoginPage();
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
    }
}