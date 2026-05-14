package org.example.controllers;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ValorizerValidateQRController {

    private static final int DEMO_VALORISATEUR_ID = 1;

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();

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
    private TextField manualQrCodeField;
    @FXML
    private TextField qrLinkField;
    @FXML
    private Label uploadedImageLabel;

    @FXML
    private Label validationResultLabel;
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
    }

    @FXML
    public void handleValidateManualQr() {
        validateQrValue(manualQrCodeField.getText());
    }

    @FXML
    public void handleValidateQrLink() {
        String extracted = DeclarationDechetJdbcService.extractQrCodeFromQrLink(qrLinkField.getText());
        if (extracted == null) {
            setValidationMessage("QR code invalide", "#e53e3e");
            clearDeclarationDetails();
            return;
        }
        validateQrValue(extracted);
    }

    @FXML
    public void handleUploadQrImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importer image QR");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images QR", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(validationResultLabel.getScene().getWindow());
        if (file == null) {
            return;
        }

        uploadedImageLabel.setText(file.getName());
        try {
            String decoded = decodeQrFromImage(file);
            if (decoded == null || decoded.isBlank()) {
                setValidationMessage("QR code invalide", "#e53e3e");
                clearDeclarationDetails();
                return;
            }
            manualQrCodeField.setText(decoded);
            validateQrValue(decoded);
        } catch (IOException | NotFoundException e) {
            setValidationMessage("QR code invalide", "#e53e3e");
            clearDeclarationDetails();
        }
    }

    @FXML
    public void handleClear() {
        manualQrCodeField.clear();
        qrLinkField.clear();
        uploadedImageLabel.setText("Aucune image");
        validationResultLabel.setText("");
        clearDeclarationDetails();
    }

    private void validateQrValue(String rawQrValue) {
        String qrValue = rawQrValue == null ? "" : rawQrValue.trim();
        String normalizedQrValue = DeclarationDechetJdbcService.extractQrCodeFromQrLink(qrValue);
        if (normalizedQrValue == null || normalizedQrValue.isBlank()) {
            setValidationMessage("QR code invalide", "#e53e3e");
            clearDeclarationDetails();
            return;
        }

        try {
            DeclarationDechetJdbcService.QrValidationResult result =
                    declarationService.validateDeclarationByQrCode(normalizedQrValue, DEMO_VALORISATEUR_ID);

            if (result.status() == DeclarationDechetJdbcService.QrValidationStatus.INVALID) {
                setValidationMessage("QR code invalide", "#e53e3e");
                clearDeclarationDetails();
                return;
            }

            if (result.status() == DeclarationDechetJdbcService.QrValidationStatus.ALREADY_VALIDATED) {
                setValidationMessage("Déclaration déjà validée", "#d97706");
                populateDeclarationDetails(result.declaration());
                return;
            }

            setValidationMessage("Déclaration validée avec succès", "#22863a");
            populateDeclarationDetails(result.declaration());
        } catch (SQLException e) {
            setValidationMessage("QR code invalide", "#e53e3e");
            clearDeclarationDetails();
        }
    }

    private void populateDeclarationDetails(DeclarationDechet declaration) {
        if (declaration == null) {
            clearDeclarationDetails();
            return;
        }

        declarationIdLabel.setText("Declaration #" + declaration.getId());
        typeLabel.setText(declaration.getTypeDechetLibelle() != null ? declaration.getTypeDechetLibelle() : "—");
        quantityLabel.setText((declaration.getQuantite() == null ? "-" : declaration.getQuantite()) + " "
                + (declaration.getUnite() == null ? "" : declaration.getUnite()));
        citizenLabel.setText(declaration.getCitoyenEmail() != null ? declaration.getCitoyenEmail() : "—");
        descriptionArea.setText(declaration.getDescription() != null ? declaration.getDescription() : "");
    }

    private void clearDeclarationDetails() {
        declarationIdLabel.setText("—");
        typeLabel.setText("—");
        quantityLabel.setText("—");
        citizenLabel.setText("—");
        descriptionArea.clear();
    }

    private void setValidationMessage(String message, String colorHex) {
        validationResultLabel.setText(message);
        validationResultLabel.setStyle("-fx-text-fill: " + colorHex + ";");
    }

    private String decodeQrFromImage(File file) throws IOException, NotFoundException {
        var bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            throw new IOException("Image invalide");
        }
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
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

    private void updateNavigation(String page) {
        if (navHome != null) navHome.getStyleClass().remove("active");
        if (navWasteReceived != null) navWasteReceived.getStyleClass().remove("active");
        if (navValorization != null) navValorization.getStyleClass().remove("active");
        if (navStatistics != null) navStatistics.getStyleClass().remove("active");
        if (navSettings != null) navSettings.getStyleClass().remove("active");

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
            default:
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
}
