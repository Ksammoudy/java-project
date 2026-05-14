package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
import org.example.utils.WeatherLocationState;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DeclarationDechetCitizenFormController {

    private static final int DESCRIPTION_MIN = 10;
    private static final int DESCRIPTION_MAX = 2000;
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;
    private static final String LOCATION_ALERT_PREFIX = "LOCATION:";

    private final TypeDechetJdbcService typeDechetService = new TypeDechetJdbcService();
    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final HuggingFaceService huggingFaceService = new HuggingFaceService();

    private Path selectedPhotoPath;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private WebEngine webEngine;

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
    private ImageView photoPreview;
    @FXML
    private Label photoErrorLabel;

    @FXML
    private WebView mapWebView;
    @FXML
    private Label geoSelectionLabel;
    @FXML
    private Label geoErrorLabel;

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

        setGeoSelection(null, null);
        if (photoPreview != null) {
            photoPreview.setManaged(false);
            photoPreview.setVisible(false);
        }
        initializeMapWebView();
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

    private void initializeMapWebView() {
        if (mapWebView == null) {
            setSummaryError("Composant carte introuvable dans le formulaire.");
            return;
        }

        webEngine = mapWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnAlert(event -> handleMapAlert(event.getData()));
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                forceMapResize();
            }
        });

        mapWebView.widthProperty().addListener((obs, oldV, newV) -> forceMapResize());
        mapWebView.heightProperty().addListener((obs, oldV, newV) -> forceMapResize());

        URL mapUrl = getClass().getResource("/org/example/views/maps/declaration_map.html");
        if (mapUrl == null) {
            setSummaryError("Fichier de carte introuvable.");
            return;
        }
        webEngine.load(mapUrl.toExternalForm());
    }

    private void forceMapResize() {
        if (webEngine == null) {
            return;
        }
        try {
            webEngine.executeScript("if (window.fixMapSize) { window.fixMapSize(); }");
        } catch (Exception ignored) {
            // Ignore if script is not ready yet.
        }
    }

    private void handleMapAlert(String data) {
        if (data == null || !data.startsWith(LOCATION_ALERT_PREFIX)) {
            return;
        }
        String payload = data.substring(LOCATION_ALERT_PREFIX.length()).trim();
        String[] parts = payload.split(",");
        if (parts.length != 2) {
            return;
        }
        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());
            selectedLatitude = lat;
            selectedLongitude = lng;
            WeatherLocationState.updateSelectedLocation(lat, lng);
            setGeoSelection(lat, lng);
            clearFieldError(geoErrorLabel);
        } catch (NumberFormatException ignored) {
            // Ignore malformed payload.
        }
    }

    private void setGeoSelection(Double lat, Double lng) {
        if (geoSelectionLabel == null) {
            return;
        }
        if (lat == null || lng == null) {
            geoSelectionLabel.setText("Position selectionnee : --");
            return;
        }
        geoSelectionLabel.setText(String.format(Locale.ROOT, "Position selectionnee : %.6f, %.6f", lat, lng));
    }

    @FXML
    public void handleBrowsePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(validationLabel.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (file.length() > MAX_PHOTO_BYTES) {
            setSummaryError("La photo ne doit pas depasser 5 Mo.");
            return;
        }
        selectedPhotoPath = file.toPath();
        photoHintLabel.setText(file.getName());
        clearFieldError(photoErrorLabel);
        clearSummaryError();
        showPhotoPreview(file);
    }

    @FXML
    public void handleClearPhoto() {
        selectedPhotoPath = null;
        photoHintLabel.setText("Aucun fichier selectionne");
        if (photoPreview != null) {
            photoPreview.setImage(null);
            photoPreview.setManaged(false);
            photoPreview.setVisible(false);
        }
    }

    private void showPhotoPreview(File file) {
        if (photoPreview == null) {
            return;
        }
        Image image = new Image(file.toURI().toString(), true);
        photoPreview.setImage(image);
        photoPreview.setManaged(true);
        photoPreview.setVisible(true);
    }

    @FXML
    public void handleSubmit() {
        clearAllErrors();

        boolean ok = true;

        TypeDechet type = typeDechetCombo.getSelectionModel().getSelectedItem();
        if (type == null || type.getId() == null || type.getId() <= 0) {
            setFieldError(typeErrorLabel, "Choisissez un type de dechet.");
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

        if (selectedPhotoPath == null) {
            setFieldError(photoErrorLabel, "La photo est obligatoire.");
            ok = false;
        }

        if (selectedLatitude == null || selectedLongitude == null) {
            setFieldError(geoErrorLabel, "Cliquez sur la carte pour fixer la position.");
            ok = false;
        }

        if (!ok || type == null || quantite == null || unite == null) {
            setSummaryError("Veuillez corriger les champs en erreur.");
            return;
        }

        HuggingFaceService.Result ia = huggingFaceService.classifyImage(selectedPhotoPath);
        if (!ia.success()) {
            showApiFailurePopup();
            return;
        }

        String detectedLabel = ia.label();
        Double detectedScore = ia.score();
        if (!isTypeCompatible(type.getLibelle(), detectedLabel)) {
            showIncompatibleTypePopup(type.getLibelle(), detectedLabel, detectedScore);
            return;
        }

        String photoRelative;
        try {
            photoRelative = copyPhotoToUploads(selectedPhotoPath);
        } catch (IOException e) {
            setSummaryError("Photo : " + e.getMessage());
            return;
        }

        DeclarationDechet entity = new DeclarationDechet();
        entity.setDescription(descTrim);
        entity.setStatut("EN_ATTENTE");
        entity.setTypeDechetId(type.getId());
        entity.setPhoto(photoRelative);
        entity.setLatitude(selectedLatitude);
        entity.setLongitude(selectedLongitude);
        entity.setQuantite(quantite);
        entity.setUnite(unite.trim());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setAiDetectedLabel(detectedLabel);
        entity.setScoreIa(detectedScore);
        entity.setPointsAttribues(0);
        entity.setCitoyenId(resolveCitizenId());
        entity.setQrCode(null);
        entity.setQrUrl(null);
        entity.setValidatedByQr(false);
        entity.setValidatedAt(null);
        entity.setValorisateurId(null);
        entity.setValorisateurConfirmateurId(null);
        entity.setDateConfirmation(null);
        entity.setStatutHistoriqueJson(null);
        entity.setDeletedAt(null);

        try {
            declarationService.create(entity);
            showSuccessPopup(type.getLibelle(), detectedLabel, detectedScore);
            AdminUiState.setFlash("Declaration enregistree avec succes.", false);
            Main.showDashboardCitizen();
        } catch (SQLException e) {
            showSimpleErrorPopup("Erreur lors de l'enregistrement de la declaration.");
        } catch (RuntimeException e) {
            showSimpleErrorPopup("Erreur lors de l'enregistrement de la declaration.");
        }
    }

    private Integer resolveCitizenId() {
        Integer resolved = CitizenSession.resolveCitizenDatabaseId();
        return resolved != null ? resolved : 1;
    }

    private boolean isTypeCompatible(String selectedType, String label) {
        String type = normalizeText(selectedType);
        String detected = normalizeText(label);
        if (type.isBlank() || detected.isBlank()) {
            return false;
        }

        Map<String, List<String>> aliases = Map.of(
                "plastique", List.of("plastic", "bottle", "bag", "container", "packaging"),
                "papier/carton", List.of("paper", "cardboard", "carton", "box"),
                "verre", List.of("glass", "bottle", "jar"),
                "metal", List.of("metal", "can", "tin", "aluminum"),
                "organique", List.of("food", "fruit", "vegetable", "organic", "garbage"),
                "electronique", List.of("electronic", "computer", "phone", "battery")
        );

        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            String family = entry.getKey();
            if (!containsTypeFamily(type, family)) {
                continue;
            }
            for (String keyword : entry.getValue()) {
                if (detected.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

        return detected.contains(type) || type.contains(detected);
    }

    private boolean containsTypeFamily(String normalizedType, String familyKey) {
        if ("papier/carton".equals(familyKey)) {
            return normalizedType.contains("papier") || normalizedType.contains("carton") || normalizedType.contains("paper");
        }
        if ("metal".equals(familyKey)) {
            return normalizedType.contains("metal") || normalizedType.contains("metall") || normalizedType.contains("metallique");
        }
        if ("electronique".equals(familyKey)) {
            return normalizedType.contains("electronique") || normalizedType.contains("electronic");
        }
        return normalizedType.contains(familyKey);
    }

    private String normalizeText(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.replaceAll("\\s+", " ");
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
        clearFieldError(photoErrorLabel);
        clearFieldError(geoErrorLabel);
    }

    private void showSuccessPopup(String selectedType, String detectedLabel, Double score) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Déclaration ajoutée avec succès");
        alert.setContentText(
                "Type choisi : " + safeValue(selectedType) + "\n" +
                        "Détecté par IA : " + safeValue(detectedLabel) + "\n" +
                        "Score : " + formatScore(score)
        );
        alert.showAndWait();
    }

    private void showIncompatibleTypePopup(String selectedType, String detectedLabel, Double score) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Le type choisi ne correspond pas à l’image");
        alert.setContentText(
                "Type choisi : " + safeValue(selectedType) + "\n" +
                        "Détecté par IA : " + safeValue(detectedLabel) + "\n" +
                        "Score : " + formatScore(score)
        );
        alert.showAndWait();
    }

    private void showApiFailurePopup() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("Erreur lors de l’analyse de l’image.");
        alert.showAndWait();
    }

    private void showSimpleErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safeValue(String value) {
        return (value == null || value.isBlank()) ? "--" : value;
    }

    private String formatScore(Double score) {
        return score == null ? "--" : String.format(Locale.ROOT, "%.4f", score);
    }
}
