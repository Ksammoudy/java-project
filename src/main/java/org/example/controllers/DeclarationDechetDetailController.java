package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.services.DeclarationDechetJdbcService;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenUiState;

import java.sql.SQLException;

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
        statusLabel.setText(safe(currentDeclaration.getStatut()));
        quantityLabel.setText((currentDeclaration.getQuantite() == null ? 0 : currentDeclaration.getQuantite().intValue())
            + " " + safe(currentDeclaration.getUnite()));
        pointsLabel.setText(currentDeclaration.getPointsAttribues() == null ? "-" : String.valueOf(currentDeclaration.getPointsAttribues()));
        dateLabel.setText(currentDeclaration.getCreatedAt() == null ? "-" : currentDeclaration.getCreatedAt().toLocalDate().toString());
        scoreLabel.setText(currentDeclaration.getScoreIa() == null ? "-" : String.format("%.2f", currentDeclaration.getScoreIa()));
        citizenLabel.setText(safe(currentDeclaration.getCitoyenEmail()));
        valorisateurLabel.setText(currentDeclaration.getValorisateurConfirmateurId() == null ? "Non confirme" : "Valorisateur #" + currentDeclaration.getValorisateurConfirmateurId());
        coordinatesLabel.setText(formatCoordinates(currentDeclaration));
        descriptionLabel.setText(safe(currentDeclaration.getDescription()));
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
}
