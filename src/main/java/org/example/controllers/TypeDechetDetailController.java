package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.entities.TypeDechet;
import org.example.services.TypeDechetJdbcService;
import org.example.utils.AdminUiState;

import java.sql.SQLException;

public class TypeDechetDetailController {

    private final TypeDechetJdbcService service = new TypeDechetJdbcService();
    private TypeDechet currentType;

    @FXML
    private Label idLabel;

    @FXML
    private Label libelleLabel;

    @FXML
    private Label pointsLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label declarationUsageLabel;

    @FXML
    public void initialize() {
        currentType = AdminUiState.getSelectedTypeDechet();
        if (currentType == null || currentType.getId() == null) {
            Main.showTypeDechetWorkshopPage();
            return;
        }

        try {
            currentType = service.findById(currentType.getId()).orElse(currentType);
        } catch (SQLException | IllegalStateException ignored) {
            // Keep selected item fallback for offline mode.
        }

        idLabel.setText(currentType.getId() == null ? "-" : String.valueOf(currentType.getId()));
        libelleLabel.setText(safe(currentType.getLibelle()));
        pointsLabel.setText(currentType.getValeurPointsKg() == null ? "-" : String.valueOf(currentType.getValeurPointsKg()));
        descriptionLabel.setText(safe(currentType.getDescriptionTri()));
        declarationUsageLabel.setText("Disponible pour les declarations citoyennes.");
    }

    @FXML
    private void handleBack() {
        Main.showTypeDechetWorkshopPage();
    }

    @FXML
    private void handleEdit() {
        AdminUiState.setSelectedTypeDechet(currentType);
        Main.showTypeDechetFormPage();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
