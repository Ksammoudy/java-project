package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class ValorizerValorizationController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
    private Label totalValorizedLabel;
    @FXML
    private Label totalKgLabel;
    @FXML
    private Label totalPointsLabel;

    @FXML
    private TableView<DeclarationDechet> valorisationTable;
    @FXML
    private TableColumn<DeclarationDechet, String> dateColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> typeColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> quantiteColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> pointsColumn;
    @FXML
    private TableColumn<DeclarationDechet, Void> qrPreviewColumn;
    @FXML
    private TableColumn<DeclarationDechet, Void> actionColumn;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "-");

        updateNavigation("valorization");

        configureTable();
        loadRows();
    }

    private void configureTable() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt() != null
                        ? cellData.getValue().getCreatedAt().format(DATE_FORMAT)
                        : "-"));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeDechetLibelle() != null
                        ? cellData.getValue().getTypeDechetLibelle()
                        : "-"));

        quantiteColumn.setCellValueFactory(cellData -> {
            DeclarationDechet declaration = cellData.getValue();
            String unit = declaration.getUnite() == null || declaration.getUnite().isBlank() ? "kg" : declaration.getUnite();
            return new SimpleStringProperty((declaration.getQuantite() == null ? 0 : declaration.getQuantite()) + " " + unit);
        });

        pointsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getPointsAttribues() != null ? cellData.getValue().getPointsAttribues() : 0)));

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
            private final Button detailBtn = new Button("Details");
            private final Button viewQrBtn = new Button("Voir QR");
            private final Button downloadQrBtn = new Button("Telecharger QR");

            {
                detailBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                detailBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    AdminUiState.setSelectedDeclaration(row);
                    Main.showDeclarationDetailPage();
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
                    return;
                }
                HBox actions = new HBox(6, detailBtn, viewQrBtn, downloadQrBtn);
                setGraphic(actions);
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

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
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
        var file = chooser.showSaveDialog(valorisationTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (InputStream inputStream = new URL(declaration.getQrUrl()).openStream()) {
            Files.copy(inputStream, Path.of(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            showErrorMessage("Echec du telechargement QR.");
        }
    }

    private void loadRows() {
        try {
            masterList.clear();
            masterList.addAll(declarationService.findAll().stream()
                    .filter(d -> isValorizedStatus(d.getStatut()))
                    .collect(Collectors.toList()));

            updateStatistics();

            boolean isEmpty = masterList.isEmpty();
            emptyHintLabel.setVisible(isEmpty);
            emptyHintLabel.setManaged(isEmpty);
            if (isEmpty) {
                emptyHintLabel.setText("Aucune declaration approuvee ou validee pour valorisation.");
            }

            ObservableList<DeclarationDechet> data = FXCollections.observableArrayList(masterList);
            valorisationTable.setItems(data);
        } catch (SQLException e) {
            System.err.println("Erreur au chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = masterList.size();
        double totalKg = masterList.stream().mapToDouble(d -> d.getQuantite() != null ? d.getQuantite() : 0).sum();
        int totalPoints = masterList.stream().mapToInt(d -> d.getPointsAttribues() != null ? d.getPointsAttribues() : 0).sum();

        totalValorizedLabel.setText(String.valueOf(total));
        totalKgLabel.setText(String.format(Locale.ROOT, "%.1f", totalKg));
        totalPointsLabel.setText(String.valueOf(totalPoints));
    }

    private boolean isValorizedStatus(String rawStatus) {
        String status = rawStatus == null ? "" : rawStatus.trim().toUpperCase(Locale.ROOT);
        return "APPROUVEE".equals(status) || "VALIDATED".equals(status);
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

    private void showErrorMessage(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
