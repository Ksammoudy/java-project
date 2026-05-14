package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.services.DeclarationDechetJdbcService;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenUiState;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;

public class DeclarationDechetDetailController {

    private final DeclarationDechetJdbcService service = new DeclarationDechetJdbcService();
    private DeclarationDechet currentDeclaration;

    @FXML
    private Label declarationIdLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label quantityLabel;

    @FXML
    private Label pointsLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label citizenLabel;

    @FXML
    private Label valorisateurLabel;

    @FXML
    private Label coordinatesLabel;

    @FXML
    private Label descriptionLabel;
    @FXML
    private Label qrCodeLabel;
    @FXML
    private ImageView qrImageView;
    @FXML
    private Button viewQrButton;
    @FXML
    private Button downloadQrButton;

    @FXML
    public void initialize() {
        currentDeclaration = AdminUiState.getSelectedDeclaration();
        if (currentDeclaration == null || currentDeclaration.getId() == null) {
            Main.showDeclarationListPage();
            return;
        }

        try {
            currentDeclaration = service.findById(currentDeclaration.getId()).orElse(currentDeclaration);
        } catch (SQLException | IllegalStateException ignored) {
            // Keep selected declaration as fallback.
        }

        declarationIdLabel.setText("#" + currentDeclaration.getId());
        typeLabel.setText(safe(currentDeclaration.getTypeDechetLibelle()));
        statusLabel.setText(formatStatus(currentDeclaration.getStatut()));
        quantityLabel.setText((currentDeclaration.getQuantite() == null ? 0 : currentDeclaration.getQuantite().intValue())
            + " " + safe(currentDeclaration.getUnite()));
        pointsLabel.setText(currentDeclaration.getPointsAttribues() == null ? "-" : String.valueOf(currentDeclaration.getPointsAttribues()));
        dateLabel.setText(currentDeclaration.getCreatedAt() == null ? "-" : currentDeclaration.getCreatedAt().toLocalDate().toString());
        scoreLabel.setText(currentDeclaration.getScoreIa() == null ? "-" : String.format("%.2f", currentDeclaration.getScoreIa()));
        citizenLabel.setText(safe(currentDeclaration.getCitoyenEmail()));
        valorisateurLabel.setText(currentDeclaration.getValorisateurConfirmateurId() == null ? "Non confirme" : "Valorisateur #" + currentDeclaration.getValorisateurConfirmateurId());
        coordinatesLabel.setText(formatCoordinates(currentDeclaration));
        descriptionLabel.setText(safe(currentDeclaration.getDescription()));
        qrCodeLabel.setText(safe(currentDeclaration.getQrCode()));

        boolean hasQr = currentDeclaration.getQrUrl() != null && !currentDeclaration.getQrUrl().isBlank();
        if (hasQr) {
            qrImageView.setImage(new Image(currentDeclaration.getQrUrl(), true));
            qrImageView.setVisible(true);
            qrImageView.setManaged(true);
        } else {
            qrImageView.setImage(null);
            qrImageView.setVisible(false);
            qrImageView.setManaged(false);
        }
        viewQrButton.setDisable(!hasQr);
        downloadQrButton.setDisable(!hasQr);
    }

    @FXML
    private void handleBack() {
        if (CitizenUiState.consumeReturnFromDetailToMyDeclarations()) {
            Main.showCitizenMyDeclarationsPage();
        } else {
            Main.showDeclarationListPage();
        }
    }

    private String formatCoordinates(DeclarationDechet declaration) {
        if (declaration.getLatitude() == null || declaration.getLongitude() == null) {
            return "-";
        }
        return String.format("%.4f / %.4f", declaration.getLatitude(), declaration.getLongitude());
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "En attente";
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "APPROUVEE" -> "Approuvee";
            case "EN_ATTENTE" -> "En attente";
            case "REFUSEE" -> "Refusee";
            case "VALIDATED" -> "Validee";
            default -> raw;
        };
    }

    @FXML
    private void handleViewQr() {
        if (currentDeclaration == null || currentDeclaration.getQrUrl() == null || currentDeclaration.getQrUrl().isBlank()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("QR Declaration");
        alert.setHeaderText("Declaration #" + currentDeclaration.getId());
        alert.setContentText(currentDeclaration.getQrCode());
        ImageView imageView = new ImageView(new Image(currentDeclaration.getQrUrl(), true));
        imageView.setFitHeight(220);
        imageView.setFitWidth(220);
        imageView.setPreserveRatio(true);
        alert.getDialogPane().setGraphic(imageView);
        alert.showAndWait();
    }

    @FXML
    private void handleDownloadQr() {
        if (currentDeclaration == null || currentDeclaration.getQrUrl() == null || currentDeclaration.getQrUrl().isBlank()) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Telecharger QR");
        chooser.setInitialFileName("qr_declaration_" + currentDeclaration.getId() + ".png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
        var file = chooser.showSaveDialog(descriptionLabel.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (InputStream inputStream = new URL(currentDeclaration.getQrUrl()).openStream()) {
            Files.copy(inputStream, Path.of(file.toURI()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Echec du telechargement QR.");
            alert.showAndWait();
        }
    }
}
