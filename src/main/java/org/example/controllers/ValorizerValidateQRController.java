package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class ValorizerValidateQRController {

    @FXML
    private Button navHome;
    @FXML
    private Button navWasteReceived;
    @FXML
    private Button navValorization;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navSettings;

    @FXML
    private Label valorizerNameLabel;
    @FXML
    private Label headerEmailLabel;

    @FXML
    private TextField qrCodeField;
    @FXML
    private Label scanResultLabel;
    @FXML
    private Label declarationIdLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label quantityLabel;
    @FXML
    private Label citizenLabel;
    @FXML
    private TextArea descriptionArea;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        updateNavigation("qr");

        qrCodeField.setOnAction(e -> scanQRCode());
    }

    @FXML
    public void scanQRCode() {
        String qrValue = qrCodeField.getText().trim();
        
        if (qrValue.isEmpty()) {
            scanResultLabel.setText("Veuillez entrer un QR code ou ID.");
            scanResultLabel.setStyle("-fx-text-fill: #e53e3e;");
            return;
        }

        // Simulate QR scan
        if (qrValue.matches("\\d+")) {
            // Simulate loading declaration data
            declarationIdLabel.setText("Declaration #" + qrValue);
            typeLabel.setText("Plastique");
            quantityLabel.setText("15.5 kg");
            citizenLabel.setText("Citoyen #123");
            descriptionArea.setText("Plastique PET collecté lors de la campagne de sensibilisation.");
            
            scanResultLabel.setText("✓ Declaration valide et en attente de confirmation");
            scanResultLabel.setStyle("-fx-text-fill: #22863a;");
        } else {
            scanResultLabel.setText("✗ QR code invalide ou non reconnu.");
            scanResultLabel.setStyle("-fx-text-fill: #e53e3e;");
            clearDeclarationDetails();
        }
    }

    @FXML
    public void handleValidate() {
        String declId = declarationIdLabel.getText();
        if (declId.isEmpty() || declId.contains("—")) {
            showError("Veuillez scanner une declaration valide.");
            return;
        }

        showMessage("Declaration validee et approuvee avec succes!");
        qrCodeField.clear();
        clearDeclarationDetails();
        scanResultLabel.setText("");
    }

    @FXML
    public void handleReject() {
        String declId = declarationIdLabel.getText();
        if (declId.isEmpty() || declId.contains("—")) {
            showError("Veuillez scanner une declaration valide.");
            return;
        }

        showMessage("Declaration rejetee.");
        qrCodeField.clear();
        clearDeclarationDetails();
        scanResultLabel.setText("");
    }

    @FXML
    public void handleClear() {
        qrCodeField.clear();
        clearDeclarationDetails();
        scanResultLabel.setText("");
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleWasteReceived() {
        Main.showValorizerWasteReceivedPage();
    }

    @FXML
    public void handleValorization() {
        Main.showValorizerValorizationPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showValorizerStatisticsPage();
    }

    @FXML
    public void handleSettings() {
        Main.showValorizerSettingsPage();
    }

    private void clearDeclarationDetails() {
        declarationIdLabel.setText("—");
        typeLabel.setText("—");
        quantityLabel.setText("—");
        citizenLabel.setText("—");
        descriptionArea.clear();
    }

    private void updateNavigation(String page) {
        if (navHome != null) navHome.getStyleClass().remove("active");
        if (navWasteReceived != null) navWasteReceived.getStyleClass().remove("active");
        if (navValorization != null) navValorization.getStyleClass().remove("active");
        if (navStatistics != null) navStatistics.getStyleClass().remove("active");
        if (navSettings != null) navSettings.getStyleClass().remove("active");

        // QR validation is not in the main nav menu
        switch (page) {
            case "home":
                if (navHome != null) navHome.getStyleClass().add("active");
                break;
            case "waste":
                if (navWasteReceived != null) navWasteReceived.getStyleClass().add("active");
                break;
            case "valorization":
                if (navValorization != null) navValorization.getStyleClass().add("active");
                break;
            case "statistics":
                if (navStatistics != null) navStatistics.getStyleClass().add("active");
                break;
            case "settings":
                if (navSettings != null) navSettings.getStyleClass().add("active");
                break;
        }
    }

    private User resolveValorizerUser() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            return user;
        }

        User demo = new User();
        demo.setNom("Utilisateur");
        demo.setPrenom("Demo");
        demo.setEmail("demo@wastewise.tn");
        demo.setType("VALORISATEUR");
        SessionManager.setCurrentUser(demo);
        return demo;
    }

    private String fullName(User user) {
        String prenom = user.getPrenom() == null ? "" : user.getPrenom();
        String nom = user.getNom() == null ? "" : user.getNom();
        String combined = (prenom + " " + nom).trim();
        return combined.isEmpty() ? "Utilisateur Demo" : combined;
    }

    private void showMessage(String message) {
        System.out.println("Info: " + message);
    }

    private void showError(String message) {
        System.err.println("Error: " + message);
    }
}
