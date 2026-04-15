package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.entities.TypeDechet;
import org.example.services.TypeDechetJdbcService;
import org.example.utils.AdminUiState;

import java.sql.SQLException;

public class TypeDechetFormController {

    private final TypeDechetJdbcService service = new TypeDechetJdbcService();
    private TypeDechet currentType;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label pageSubtitleLabel;

    @FXML
    private TextField libelleField;

    @FXML
    private TextField pointsField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label formMessageLabel;

    @FXML
    public void initialize() {
        currentType = AdminUiState.getSelectedTypeDechet();
        if (currentType == null) {
            pageTitleLabel.setText("Ajouter un type de dechet");
            pageSubtitleLabel.setText("Creez une nouvelle categorie pour les declarations citoyennes.");
            currentType = new TypeDechet();
        } else {
            pageTitleLabel.setText("Modifier un type de dechet");
            pageSubtitleLabel.setText("Mettez a jour le score points/kg et les consignes de tri.");
            libelleField.setText(currentType.getLibelle());
            if (currentType.getValeurPointsKg() != null) {
                pointsField.setText(String.valueOf(currentType.getValeurPointsKg()));
            }
            descriptionField.setText(currentType.getDescriptionTri());
        }
    }

    @FXML
    private void handleSave() {
        String validationMessage = validateForm();
        if (validationMessage != null) {
            formMessageLabel.setText(validationMessage);
            formMessageLabel.getStyleClass().removeAll("success-text");
            formMessageLabel.getStyleClass().add("error-text");
            return;
        }

        currentType.setLibelle(libelleField.getText().trim());
        currentType.setValeurPointsKg(Double.parseDouble(pointsField.getText().trim()));
        currentType.setDescriptionTri(descriptionField.getText() == null ? null : descriptionField.getText().trim());

        try {
            if (currentType.getId() == null) {
                service.create(currentType);
                AdminUiState.setFlash("Type de dechet ajoute avec succes.", false);
            } else {
                service.update(currentType);
                AdminUiState.setFlash("Type de dechet modifie avec succes.", false);
            }
            AdminUiState.setSelectedTypeDechet(null);
            Main.showTypeDechetWorkshopPage();
        } catch (SQLException | IllegalStateException exception) {
            formMessageLabel.setText("Enregistrement impossible: " + exception.getMessage());
            formMessageLabel.getStyleClass().removeAll("success-text");
            formMessageLabel.getStyleClass().add("error-text");
        }
    }

    @FXML
    private void handleCancel() {
        AdminUiState.setSelectedTypeDechet(null);
        Main.showTypeDechetWorkshopPage();
    }

    private String validateForm() {
        if (libelleField.getText() == null || libelleField.getText().trim().isBlank()) {
            return "Le libelle est obligatoire.";
        }

        String pointsValue = pointsField.getText() == null ? "" : pointsField.getText().trim();
        if (pointsValue.isBlank()) {
            return "La valeur points/kg est obligatoire.";
        }

        try {
            double points = Double.parseDouble(pointsValue);
            if (points <= 0) {
                return "La valeur points/kg doit etre strictement positive.";
            }
        } catch (NumberFormatException exception) {
            return "La valeur points/kg doit etre un nombre valide.";
        }

        if (descriptionField.getText() != null && !descriptionField.getText().isBlank()
            && descriptionField.getText().trim().length() < 5) {
            return "La description tri doit contenir au moins 5 caracteres.";
        }

        return null;
    }
}
