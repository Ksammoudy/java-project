package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;
import org.example.utils.AdminUiState;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ValorizerWasteReceivedController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int DEMO_VALORISATEUR_ID = 1;

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final List<DeclarationDechet> masterList = new ArrayList<>();

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
    private Label emptyHintLabel;

    @FXML
    private Label totalCountLabel;
    @FXML
    private Label pendingCountLabel;
    @FXML
    private Label approvedCountLabel;

    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<DeclarationDechet> declarationsTable;
    @FXML
    private TableColumn<DeclarationDechet, String> dateColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> citizenColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> typeColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> statutColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> quantiteColumn;
    @FXML
    private TableColumn<DeclarationDechet, Void> qrPreviewColumn;
    @FXML
    private TableColumn<DeclarationDechet, Void> actionColumn;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");

        updateNavigation("waste");

        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Approuvee", "Validee", "Refusee"));
        statusFilter.setValue("Tous");
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());

        configureTable();
        loadRows();
    }

    private void configureTable() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt() != null
                        ? cellData.getValue().getCreatedAt().format(DATE_FORMAT)
                        : "-"));

        citizenColumn.setCellValueFactory(cellData -> {
            DeclarationDechet decl = cellData.getValue();
            if (decl.getCitoyenId() != null) {
                return new SimpleStringProperty(decl.getCitoyenId().toString());
            }
            return new SimpleStringProperty("Citoyen #" + decl.getId());
        });

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeDechetLibelle() != null
                        ? cellData.getValue().getTypeDechetLibelle()
                        : "-"));

        statutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatStatut(cellData.getValue().getStatut())));

        quantiteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantite() + " kg"));

        qrPreviewColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(52);
                imageView.setFitHeight(52);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                DeclarationDechet declaration = getTableRow().getItem();
                if (declaration.getQrUrl() == null || declaration.getQrUrl().isBlank()) {
                    setGraphic(null);
                    return;
                }
                imageView.setImage(new Image(declaration.getQrUrl(), true));
                setGraphic(imageView);
            }
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = new Button("Voir");
            private final Button validateBtn = new Button("Valider");
            private final Button viewQrBtn = new Button("Voir QR");
            private final Button downloadQrBtn = new Button("Telecharger QR");

            {
                detailBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                detailBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    AdminUiState.setSelectedDeclaration(row);
                    Main.showDeclarationDetailPage();
                });

                validateBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                validateBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    validateDeclaration(row);
                });

                viewQrBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                viewQrBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    showQrPreview(row);
                });

                downloadQrBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                downloadQrBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    downloadQrImage(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                } else {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(6);
                    box.getChildren().add(detailBtn);
                    box.getChildren().add(viewQrBtn);
                    box.getChildren().add(downloadQrBtn);

                    if (canBeValidated(row)) {
                        box.getChildren().add(validateBtn);
                    }

                    setGraphic(box);
                }
            }
        });
    }

    private void showQrPreview(DeclarationDechet declaration) {
        if (declaration == null || declaration.getQrUrl() == null || declaration.getQrUrl().isBlank()) {
            showErrorMessage("QR indisponible pour cette declaration.");
            return;
        }

        ImageView imageView = new ImageView(new Image(declaration.getQrUrl(), true));
        imageView.setFitWidth(220);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("QR Declaration");
        alert.setHeaderText("Declaration #" + declaration.getId());
        alert.setContentText(declaration.getQrCode() == null ? "" : declaration.getQrCode());
        alert.getDialogPane().setGraphic(imageView);
        alert.showAndWait();
    }

    private void downloadQrImage(DeclarationDechet declaration) {
        if (declaration == null || declaration.getQrUrl() == null || declaration.getQrUrl().isBlank()) {
            showErrorMessage("QR indisponible pour cette declaration.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Telecharger QR");
        chooser.setInitialFileName("qr_declaration_" + declaration.getId() + ".png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
        var file = chooser.showSaveDialog(declarationsTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (InputStream inputStream = new URL(declaration.getQrUrl()).openStream()) {
            Files.copy(inputStream, Path.of(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            showErrorMessage("Echec du telechargement QR.");
        }
    }

    private void validateDeclaration(DeclarationDechet declaration) {
        if (declaration == null || declaration.getQrCode() == null || declaration.getQrCode().isBlank()) {
            showErrorMessage("QR code invalide");
            return;
        }

        try {
            DeclarationDechetJdbcService.QrValidationResult result =
                    declarationService.validateDeclarationByQrCode(declaration.getQrCode(), DEMO_VALORISATEUR_ID);

            if (result.status() == DeclarationDechetJdbcService.QrValidationStatus.INVALID) {
                showErrorMessage("QR code invalide");
                return;
            }

            if (result.status() == DeclarationDechetJdbcService.QrValidationStatus.ALREADY_VALIDATED) {
                showWarningMessage("Declaration deja validee");
                loadRows();
                return;
            }

            showConfirmationMessage("Declaration validee avec succes");
            loadRows();
        } catch (SQLException e) {
            showErrorMessage("Erreur de validation de la declaration");
        }
    }

    private boolean canBeValidated(DeclarationDechet declaration) {
        if (declaration == null) {
            return false;
        }

        String status = normalizeStatus(declaration.getStatut());
        boolean alreadyValidated = Boolean.TRUE.equals(declaration.getValidatedByQr())
                || "VALIDATED".equals(status)
                || declaration.getValidatedAt() != null;
        return !alreadyValidated && !"REFUSEE".equals(status);
    }

    private void loadRows() {
        try {
            masterList.clear();
            masterList.addAll(declarationService.findAll());
            applyFilters();
            updateStatistics();
        } catch (SQLException e) {
            showErrorMessage("Erreur au chargement des declarations");
        }
    }

    private void applyFilters() {
        String statusValue = statusFilter.getValue();
        String searchValue = searchField.getText().toLowerCase(Locale.ROOT);

        List<DeclarationDechet> filtered = masterList.stream()
                .filter(d -> {
                    String statut = formatStatut(d.getStatut());
                    return "Tous".equals(statusValue) || statut.equals(statusValue);
                })
                .filter(d -> searchValue.isEmpty() ||
                        (d.getTypeDechetLibelle() != null && d.getTypeDechetLibelle().toLowerCase(Locale.ROOT).contains(searchValue)) ||
                        (d.getDescription() != null && d.getDescription().toLowerCase(Locale.ROOT).contains(searchValue)))
                .collect(Collectors.toList());

        boolean isEmpty = filtered.isEmpty();
        emptyHintLabel.setVisible(isEmpty);
        emptyHintLabel.setManaged(isEmpty);
        if (isEmpty) {
            emptyHintLabel.setText("Aucune declaration ne correspond aux criteres.");
        }

        ObservableList<DeclarationDechet> data = FXCollections.observableArrayList(filtered);
        declarationsTable.setItems(data);
    }

    private void updateStatistics() {
        int total = masterList.size();
        long pending = masterList.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
        long approved = masterList.stream().filter(d -> {
            String status = normalizeStatus(d.getStatut());
            return "APPROUVEE".equals(status) || "VALIDATED".equals(status);
        }).count();

        totalCountLabel.setText(String.valueOf(total));
        pendingCountLabel.setText(String.valueOf(pending));
        approvedCountLabel.setText(String.valueOf(approved));
    }

    @FXML
    public void resetFilters() {
        statusFilter.setValue("Tous");
        searchField.setText("");
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

    private String formatStatut(String statut) {
        if (statut == null) return "-";
        return switch (normalizeStatus(statut)) {
            case "EN_ATTENTE" -> "En attente";
            case "APPROUVEE" -> "Approuvee";
            case "VALIDATED" -> "Validee";
            case "REFUSEE" -> "Refusee";
            default -> statut;
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void showConfirmationMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
