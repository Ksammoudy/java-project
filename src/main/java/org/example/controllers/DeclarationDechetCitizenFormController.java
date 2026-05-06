package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.entities.TypeDechet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.HuggingFaceService;
import org.example.services.SessionManager;
import org.example.services.TypeDechetJdbcService;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DeclarationDechetCitizenFormController {

    private static final int DESCRIPTION_MIN = 10;
    private static final int DESCRIPTION_MAX = 2000;
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private final TypeDechetJdbcService typeDechetService = new TypeDechetJdbcService();
    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final HuggingFaceService huggingFaceService = new HuggingFaceService();

    private Path selectedPhotoPath;

    @FXML
    private Button navHome;
    @FXML
    private Button navDeclare;
    @FXML
    private Button navMyDeclarations;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navNews;
    @FXML
    private Button navAir;
    @FXML
    private Button navWithdraw;
    @FXML
    private Button navSettings;

    @FXML
    private Label citizenNameLabel;
    @FXML
    private Label headerEmailLabel;

    @FXML
    private HBox summaryErrorBox;
    @FXML
    private Label validationLabel;

    @FXML
    private ComboBox<TypeDechet> typeDechetCombo;
    @FXML
    private Label typeErrorLabel;

    @FXML
    private TextField quantiteField;
    @FXML
    private Label quantiteErrorLabel;

    @FXML
    private ComboBox<String> uniteCombo;
    @FXML
    private Label uniteErrorLabel;

    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label descriptionErrorLabel;

    @FXML
    private Label photoHintLabel;

    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private Label geoErrorLabel;

    @FXML
    private TextField scoreIaField;
    @FXML
    private Label scoreErrorLabel;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "--");

        CitizenSidebarHelper.applyActive(navDeclare,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        uniteCombo.setItems(FXCollections.observableArrayList("kg", "g", "L", "m3", "unite"));
        uniteCombo.getSelectionModel().selectFirst();

        configureTypeCombo();
        loadTypes();
    }

    private void configureTypeCombo() {
        typeDechetCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TypeDechet object) {
                return object == null ? "" : object.getLibelle();
            }

            @Override
            public TypeDechet fromString(String string) {
                return null;
            }
        });
        typeDechetCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TypeDechet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLibelle());
            }
        });
        typeDechetCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TypeDechet item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getLibelle());
            }
        });
    }

    private void loadTypes() {
        try {
            List<TypeDechet> types = typeDechetService.findAll();
            typeDechetCombo.setItems(FXCollections.observableArrayList(types));
            if (types.isEmpty()) {
                setSummaryError("Aucun type de dechet en base : ajoutez-en depuis l'admin.");
            }
        } catch (SQLException | RuntimeException e) {
            setSummaryError("Impossible de charger les types de dechets. DB indisponible.");
        }
    }

    @FXML
    public void handleBrowsePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );
        var file = chooser.showOpenDialog(validationLabel.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (file.length() > MAX_PHOTO_BYTES) {
            setSummaryError("La photo ne doit pas depasser 5 Mo.");
            return;
        }
        selectedPhotoPath = file.toPath();
        photoHintLabel.setText(file.getName());
        clearSummaryError();
    }

    @FXML
    public void handleClearPhoto() {
        selectedPhotoPath = null;
        photoHintLabel.setText("Aucun fichier selectionne");
    }

    @FXML
    public void handleSubmit() {
        System.out.println("Methode ajouterDeclaration appelee");
        clearAllErrors();

        boolean ok = true;

        TypeDechet type = typeDechetCombo.getSelectionModel().getSelectedItem();
        System.out.println("[DeclarationDechet][DEBUG] type_dechet selectionne depuis ComboBox id="
                + (type == null ? null : type.getId()));
        if (type == null || type.getId() == null) {
            setFieldError(typeErrorLabel, "Choisissez un type de dechet.");
            ok = false;
        } else if (type.getId() <= 0) {
            setFieldError(typeErrorLabel, "Type de dechet invalide.");
            ok = false;
        }

        String qRaw = quantiteField != null ? quantiteField.getText() : "";
        String qTrim = qRaw.trim().replace(',', '.');
        Double quantite = null;
        if (qTrim.isEmpty()) {
            setFieldError(quantiteErrorLabel, "La quantite est obligatoire.");
            ok = false;
        } else {
            try {
                quantite = Double.parseDouble(qTrim);
                if (quantite <= 0) {
                    setFieldError(quantiteErrorLabel, "La quantite doit etre strictement positive.");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                setFieldError(quantiteErrorLabel, "Nombre invalide.");
                ok = false;
            }
        }

        String unite = uniteCombo.getSelectionModel().getSelectedItem();
        if (unite == null || unite.isBlank()) {
            setFieldError(uniteErrorLabel, "Choisissez une unite.");
            ok = false;
        }

        String description = descriptionArea != null ? descriptionArea.getText() : "";
        String descTrim = description.trim();
        if (descTrim.length() < DESCRIPTION_MIN) {
            setFieldError(descriptionErrorLabel, "Minimum " + DESCRIPTION_MIN + " caracteres.");
            ok = false;
        } else if (descTrim.length() > DESCRIPTION_MAX) {
            setFieldError(descriptionErrorLabel, "Maximum " + DESCRIPTION_MAX + " caracteres.");
            ok = false;
        }

        String latText = latitudeField != null ? latitudeField.getText().trim() : "";
        String lonText = longitudeField != null ? longitudeField.getText().trim() : "";
        Double lat = null;
        Double lon = null;
        if (latText.isEmpty() || lonText.isEmpty()) {
            setFieldError(geoErrorLabel, "Latitude et longitude sont obligatoires.");
            ok = false;
        } else {
            lat = parseCoordinate(latText, true);
            lon = parseCoordinate(lonText, false);
            if (lat == null || lon == null) {
                setFieldError(geoErrorLabel, "Coordonnees invalides (lat -90..90, lon -180..180).");
                ok = false;
            } else if (Math.abs(lat) < 0.000001 && Math.abs(lon) < 0.000001) {
                setFieldError(geoErrorLabel, "Coordonnees 0,0 detectees: veuillez verifier la position.");
                ok = false;
            }
        }

        Double scoreIa = null;
        if (selectedPhotoPath != null) {
            HuggingFaceService.Result ia = huggingFaceService.classifyImage(selectedPhotoPath);
            if (!ia.success()) {
                setFieldError(scoreErrorLabel, ia.error() == null ? "Analyse image impossible." : ia.error());
                ok = false;
            } else {
                scoreIa = ia.score();
                if (scoreIaField != null && scoreIa != null) {
                    scoreIaField.setText(String.format(Locale.ROOT, "%.4f", scoreIa));
                }
                String selectedTypeLabel = type != null && type.getLibelle() != null ? type.getLibelle() : "";
                boolean typeMatches = isTypeMatchingLabel(selectedTypeLabel, ia.label());
                if (scoreIa != null && scoreIa > 0.6d && !typeMatches) {
                    setFieldError(scoreErrorLabel, "IA: image '" + ia.label() + "' incoherente avec le type selectionne.");
                    ok = false;
                }
            }
        }

        if (!ok || type == null || quantite == null || unite == null) {
            setSummaryError("Veuillez corriger les champs en erreur.");
            return;
        }

        String photoRelative = null;
        if (selectedPhotoPath != null) {
            try {
                photoRelative = copyPhotoToUploads(selectedPhotoPath);
            } catch (IOException e) {
                setSummaryError("Photo : " + e.getMessage());
                return;
            }
        }

        DeclarationDechet entity = new DeclarationDechet();
        entity.setDescription(descTrim);
        entity.setStatut("EN_ATTENTE");
        entity.setTypeDechetId(type != null ? type.getId() : null);
        entity.setPhoto(photoRelative);
        entity.setLatitude(lat);
        entity.setLongitude(lon);
        entity.setQuantite(quantite);
        entity.setUnite(unite != null ? unite.trim() : null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setScoreIa(null);
        entity.setPointsAttribues(0);
        entity.setCitoyenId(CitizenSession.resolveCitizenDatabaseId());
        entity.setQrCode(null);
        entity.setValorisateurConfirmateurId(null);
        entity.setDateConfirmation(null);
        entity.setStatutHistoriqueJson(null);
        entity.setDeletedAt(null);

        logFormPayload(entity);

        try {
            declarationService.create(entity);
            AdminUiState.setFlash("Declaration enregistree avec succes.", false);
            Main.showDashboardCitizen();
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            System.err.println("SQLState : " + e.getSQLState() + ", ErrorCode : " + e.getErrorCode());
            e.printStackTrace();
            setSummaryError(mapSqlErrorMessage(e));
        } catch (RuntimeException e) {
            System.err.println("Runtime ERROR: " + e.getMessage());
            e.printStackTrace();
            setSummaryError("Erreur lors de l'enregistrement en base. Verifiez la connexion JDBC.");
        }
    }

    private boolean isTypeMatchingLabel(String selectedType, String label) {
        String type = normalizeText(selectedType);
        String predicted = normalizeText(label);
        if (type.isBlank() || predicted.isBlank()) {
            return false;
        }
        if (predicted.contains(type) || type.contains(predicted)) {
            return true;
        }

        String[][] aliases = new String[][]{
                {"plastique", "plastic", "bottle", "pet", "container"},
                {"carton", "cardboard", "box"},
                {"papier", "paper", "newspaper", "notebook"},
                {"verre", "glass", "bottle"},
                {"metal", "metal", "can", "aluminum", "steel"},
                {"canette", "can", "aluminum"}
        };
        for (String[] family : aliases) {
            if (!type.contains(family[0])) {
                continue;
            }
            for (int i = 1; i < family.length; i++) {
                if (predicted.contains(family[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeText(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.replaceAll("\\s+", " ");
    }

    private String mapSqlErrorMessage(SQLException e) {
        String raw = e.getMessage();
        if (raw == null || raw.isBlank()) {
            return "Erreur SQL lors de l'enregistrement de la declaration.";
        }

        String msg = raw.toLowerCase(Locale.ROOT);
        String column = extractColumnName(raw);
        String suffix = column == null ? "" : " (colonne: " + column + ").";
        if (msg.contains("citoyen_id") && (msg.contains("null") || msg.contains("constraint"))) {
            return "Enregistrement refuse par la base: citoyen_id est obligatoire dans votre schema.";
        }
        if (msg.contains("type_dechet_id")) {
            return "Enregistrement refuse: type_dechet_id invalide ou absent.";
        }
        if (msg.contains("unknown column")) {
            return "Schema DB incompatible avec le service declaration_dechet (colonnes SQL).";
        }
        if (msg.contains("foreign key")) {
            return "Contrainte de relation invalide (type de dechet ou citoyen).";
        }
        if (msg.contains("cannot be null") || msg.contains("null value")) {
            return "Enregistrement refuse: un champ obligatoire est NULL en base" + suffix;
        }
        if (msg.contains("data truncation") || msg.contains("incorrect")) {
            return "Enregistrement refuse: type/format d'une valeur invalide" + suffix;
        }
        return "Erreur SQL lors de l'enregistrement de la declaration.";
    }

    private void logFormPayload(DeclarationDechet entity) {
        System.out.println("DEBUG DECLARATION : " + entity);
        System.out.println("[DeclarationDechet][DEBUG] Soumission formulaire citoyen");
        System.out.println("[DeclarationDechet][DEBUG] type=" + entity.getTypeDechetId()
                + ", quantite=" + entity.getQuantite()
                + ", unite=" + entity.getUnite()
                + ", description=" + entity.getDescription()
                + ", latitude=" + entity.getLatitude()
                + ", longitude=" + entity.getLongitude()
                + ", photo=" + entity.getPhoto()
                + ", statut=" + entity.getStatut()
                + ", createdAt=" + entity.getCreatedAt()
                + ", citoyenId=" + entity.getCitoyenId());
    }

    private String extractColumnName(String sqlMessage) {
        String raw = sqlMessage == null ? "" : sqlMessage;
        String lower = raw.toLowerCase(Locale.ROOT);

        int p = lower.indexOf("column '");
        if (p >= 0) {
            int start = p + "column '".length();
            int end = raw.indexOf('\'', start);
            if (end > start) {
                return raw.substring(start, end);
            }
        }

        p = lower.indexOf("for key '");
        if (p >= 0) {
            int start = p + "for key '".length();
            int end = raw.indexOf('\'', start);
            if (end > start) {
                return raw.substring(start, end);
            }
        }

        return null;
    }

    private Double parseCoordinate(String raw, boolean latitude) {
        try {
            double v = Double.parseDouble(raw.replace(',', '.'));
            if (latitude) {
                if (v < -90 || v > 90) {
                    return null;
                }
            } else {
                if (v < -180 || v > 180) {
                    return null;
                }
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    public void handleCancel() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDeclareWaste() {
        Main.showDeclarationCitizenFormPage();
    }

    @FXML
    public void handleMyDeclarations() {
        Main.showCitizenMyDeclarationsPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showCitizenStatisticsPage();
    }

    @FXML
    public void handleNews() {
        Main.showCitizenNewsPage();
    }

    @FXML
    public void handleAirQuality() {
        Main.showCitizenAirQualityPage();
    }

    @FXML
    public void handleWithdraw() {
        Main.showCitizenWithdrawPage();
    }

    @FXML
    public void handleProfile() {
        Main.showCitizenSettingsPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }

    private String copyPhotoToUploads(Path source) throws IOException {
        Path base = Path.of(System.getProperty("user.dir"), "uploads", "dechets");
        Files.createDirectories(base);
        String ext = extractExtension(source.getFileName().toString());
        String name = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path target = base.resolve(name);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return "uploads/dechets/" + name;
    }

    private static String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void setSummaryError(String message) {
        validationLabel.setText(message);
        summaryErrorBox.setManaged(true);
        summaryErrorBox.setVisible(true);
    }

    private void clearSummaryError() {
        validationLabel.setText("");
        summaryErrorBox.setManaged(false);
        summaryErrorBox.setVisible(false);
    }

    private void setFieldError(Label label, String message) {
        if (label == null) {
            return;
        }
        label.setText(message);
        label.setManaged(true);
        label.setVisible(true);
    }

    private void clearFieldError(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setManaged(false);
        label.setVisible(false);
    }

    private void clearAllErrors() {
        clearSummaryError();
        clearFieldError(typeErrorLabel);
        clearFieldError(quantiteErrorLabel);
        clearFieldError(uniteErrorLabel);
        clearFieldError(descriptionErrorLabel);
        clearFieldError(geoErrorLabel);
        clearFieldError(scoreErrorLabel);
    }
}


