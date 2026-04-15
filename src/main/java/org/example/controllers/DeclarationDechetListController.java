package org.example.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.services.DeclarationDechetJdbcService;
import org.example.utils.AdminUiState;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DeclarationDechetListController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int DEMO_VALORISATEUR_ID = 2;

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final List<DeclarationDechet> masterData = new ArrayList<>();

    @FXML
    private Label syncLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label approvedCountLabel;

    @FXML
    private Label pendingCountLabel;

    @FXML
    private Label ecoPointsLabel;

    @FXML
    private Label topTypeLabel;

    @FXML
    private Label suspectCountLabel;

    @FXML
    private Label flashMessageLabel;

    @FXML
    private HBox flashMessageBox;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> sortFilter;

    @FXML
    private TextField quantityMinField;

    @FXML
    private TextField quantityMaxField;

    @FXML
    private BarChart<String, Number> typeChart;

    @FXML
    private TableView<DeclarationDechet> declarationTable;

    @FXML
    private TableColumn<DeclarationDechet, String> idColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> photoColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> citoyenColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> typeColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> quantiteColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> dateColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> statutColumn;

    @FXML
    private TableColumn<DeclarationDechet, String> riskColumn;

    @FXML
    private TableColumn<DeclarationDechet, DeclarationDechet> actionsColumn;

    @FXML
    public void initialize() {
        declarationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        configureFilters();
        configureColumns();
        loadDeclarations();
        attachFilterListeners();
        showFlashIfPresent();
    }

    @FXML
    private void refreshDeclarations() {
        loadDeclarations();
        showFlashIfPresent();
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        quantityMinField.clear();
        quantityMaxField.clear();
        statusFilter.setValue("Tous statuts");
        typeFilter.setValue("Tous types");
        sortFilter.setValue("Date desc");
        applyFilters();
    }

    @FXML
    private void openTypeDechetWorkshop() {
        Main.showTypeDechetWorkshopPage();
    }

    @FXML
    private void openAdminDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    private void openUsers() {
        Main.showAdminUsersPage();
    }

    @FXML
    private void closeFlash() {
        flashMessageBox.setManaged(false);
        flashMessageBox.setVisible(false);
    }

    private void configureFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("Tous statuts", "APPROUVEE", "EN_ATTENTE", "REFUSEE"));
        statusFilter.setValue("Tous statuts");
        typeFilter.setItems(FXCollections.observableArrayList("Tous types", "Plastique", "Papier / Carton", "Verre"));
        typeFilter.setValue("Tous types");
        sortFilter.setItems(FXCollections.observableArrayList("Date desc", "Date asc", "Quantite desc", "Quantite asc"));
        sortFilter.setValue("Date desc");
    }

    private void attachFilterListeners() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        quantityMinField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        quantityMaxField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        sortFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        photoColumn.setCellValueFactory(data -> new SimpleStringProperty(resolvePhotoCode(data.getValue().getTypeDechetLibelle())));
        citoyenColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveCitizenLabel(data.getValue())));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveTypeLabel(data.getValue())));
        quantiteColumn.setCellValueFactory(data -> new SimpleStringProperty(formatQuantite(data.getValue())));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getCreatedAt())));
        statutColumn.setCellValueFactory(data -> new SimpleStringProperty(normalizeStatus(data.getValue().getStatut())));
        statutColumn.setCellFactory(column -> new StatusCell());
        riskColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveRiskLabel(data.getValue())));
        riskColumn.setCellFactory(column -> new RiskCell());
        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(column -> new ActionsCell(this));
    }

    private void loadDeclarations() {
        masterData.clear();
        try {
            masterData.addAll(
                declarationService.findAll().stream()
                    .filter(item -> item.getDeletedAt() == null)
                    .collect(Collectors.toList())
            );
        } catch (SQLException | IllegalStateException exception) {
            masterData.addAll(buildOfflineSamples());
        }

        syncLabel.setText("Derniere sync: 28/02/2026 16:14:54");
        updateSummary(masterData);
        populateChart(masterData);
        applyFilters();
    }

    private void applyFilters() {
        List<DeclarationDechet> filtered = new ArrayList<>(masterData);
        String search = lower(searchField.getText());
        String selectedStatus = statusFilter.getValue();
        String selectedType = typeFilter.getValue();
        String selectedSort = sortFilter.getValue();
        Double minQuantity = parseDouble(quantityMinField.getText());
        Double maxQuantity = parseDouble(quantityMaxField.getText());

        if (!search.isBlank()) {
            filtered = filtered.stream()
                .filter(item -> resolveCitizenLabel(item).toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toList());
        }

        if (selectedStatus != null && !"Tous statuts".equals(selectedStatus)) {
            filtered = filtered.stream()
                .filter(item -> normalizeStatus(item.getStatut()).equals(selectedStatus))
                .collect(Collectors.toList());
        }

        if (selectedType != null && !"Tous types".equals(selectedType)) {
            filtered = filtered.stream()
                .filter(item -> safe(item.getTypeDechetLibelle()).equalsIgnoreCase(selectedType))
                .collect(Collectors.toList());
        }

        if (minQuantity != null) {
            filtered = filtered.stream()
                .filter(item -> item.getQuantite() != null && item.getQuantite() >= minQuantity)
                .collect(Collectors.toList());
        }

        if (maxQuantity != null) {
            filtered = filtered.stream()
                .filter(item -> item.getQuantite() != null && item.getQuantite() <= maxQuantity)
                .collect(Collectors.toList());
        }

        Comparator<DeclarationDechet> comparator = Comparator.comparing(
            (DeclarationDechet item) -> item.getCreatedAt() == null ? LocalDateTime.MIN : item.getCreatedAt()
        );
        if ("Date desc".equals(selectedSort)) {
            comparator = comparator.reversed();
        } else if ("Quantite desc".equals(selectedSort)) {
            comparator = Comparator.comparing(
                (DeclarationDechet item) -> item.getQuantite() == null ? 0.0 : item.getQuantite()
            ).reversed();
        } else if ("Quantite asc".equals(selectedSort)) {
            comparator = Comparator.comparing(
                (DeclarationDechet item) -> item.getQuantite() == null ? 0.0 : item.getQuantite()
            );
        }

        filtered.sort(comparator);
        declarationTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateSummary(List<DeclarationDechet> declarations) {
        long approved = declarations.stream().filter(d -> "APPROUVEE".equals(normalizeStatus(d.getStatut()))).count();
        long pending = declarations.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
        int ecoPoints = declarations.stream()
            .map(DeclarationDechet::getPointsAttribues)
            .filter(value -> value != null)
            .mapToInt(Integer::intValue)
            .sum();

        totalLabel.setText(String.valueOf(declarations.size()));
        approvedCountLabel.setText(String.valueOf(approved));
        pendingCountLabel.setText(String.valueOf(pending));
        ecoPointsLabel.setText(String.valueOf(ecoPoints));
        topTypeLabel.setText(resolveTopType(declarations));
        suspectCountLabel.setText(String.valueOf(
            declarations.stream().filter(d -> resolveRiskScore(d) >= 70).count()
        ));
    }

    private void populateChart(List<DeclarationDechet> declarations) {
        long plastique = declarations.stream().filter(d -> safe(d.getTypeDechetLibelle()).contains("Plastique")).count();
        long papier = declarations.stream().filter(d -> safe(d.getTypeDechetLibelle()).contains("Papier")).count();
        long verre = declarations.stream().filter(d -> safe(d.getTypeDechetLibelle()).contains("Verre")).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Plastique", plastique));
        series.getData().add(new XYChart.Data<>("Papier / Carton", papier));
        series.getData().add(new XYChart.Data<>("Verre", verre));
        typeChart.getData().setAll(series);
    }

    private void showFlashIfPresent() {
        String message = AdminUiState.consumeFlashMessage();
        boolean error = AdminUiState.consumeFlashError();
        if (message == null || message.isBlank()) {
            flashMessageBox.setManaged(false);
            flashMessageBox.setVisible(false);
            return;
        }

        flashMessageLabel.setText(message);
        flashMessageBox.setManaged(true);
        flashMessageBox.setVisible(true);
        flashMessageBox.getStyleClass().removeAll("success-banner", "error-banner");
        flashMessageLabel.getStyleClass().removeAll("success-banner-text", "error-banner-text");
        if (error) {
            flashMessageBox.getStyleClass().add("error-banner");
            flashMessageLabel.getStyleClass().add("error-banner-text");
        } else {
            flashMessageBox.getStyleClass().add("success-banner");
            flashMessageLabel.getStyleClass().add("success-banner-text");
        }
    }

    void openDetails(DeclarationDechet declaration) {
        if (declaration == null) {
            return;
        }
        AdminUiState.setSelectedDeclaration(declaration);
        Main.showDeclarationDetailPage();
    }

    void openUser(DeclarationDechet declaration) {
        if (declaration == null) {
            return;
        }
        AdminUiState.setFlash("Ouverture du module utilisateurs pour " + resolveCitizenName(declaration) + ".", false);
        Main.showAdminUsersPage();
    }

    void assignValorisateur(DeclarationDechet declaration) {
        if (declaration == null) {
            return;
        }
        declaration.setValorisateurConfirmateurId(DEMO_VALORISATEUR_ID);
        if ("EN_ATTENTE".equals(normalizeStatus(declaration.getStatut()))) {
            declaration.setStatut("APPROUVEE");
            declaration.setDateConfirmation(LocalDateTime.now());
        }

        try {
            if (declaration.getId() != null) {
                declarationService.update(declaration);
            }
            AdminUiState.setFlash("Declaration affectee et mise a jour.", false);
        } catch (SQLException | IllegalStateException exception) {
            AdminUiState.setFlash("Affectation en mode local uniquement.", true);
        }

        loadDeclarations();
        showFlashIfPresent();
    }

    void deleteDeclaration(DeclarationDechet declaration) {
        if (declaration == null) {
            return;
        }
        try {
            declaration.setDeletedAt(LocalDateTime.now());
            if (declaration.getId() != null && declarationService.update(declaration)) {
                AdminUiState.setFlash("Declaration archivee avec succes.", false);
            } else {
                AdminUiState.setFlash("Archivage impossible pour cette declaration.", true);
            }
        } catch (SQLException | IllegalStateException exception) {
            masterData.removeIf(item -> item.getId() != null && item.getId().equals(declaration.getId()));
            AdminUiState.setFlash("Archivage effectue en mode local.", true);
        }

        loadDeclarations();
        showFlashIfPresent();
    }

    private String resolveCitizenLabel(DeclarationDechet declaration) {
        String email = declaration.getCitoyenEmail();
        if (email == null || email.isBlank()) {
            return "Anonyme\n-";
        }
        return "Demo Utilisateur\n" + email;
    }

    private String resolveCitizenName(DeclarationDechet declaration) {
        return declaration.getCitoyenEmail() == null || declaration.getCitoyenEmail().isBlank()
            ? "Anonyme"
            : "Demo Utilisateur";
    }

    private String resolveTypeLabel(DeclarationDechet declaration) {
        String type = declaration.getTypeDechetLibelle() == null ? "Plastique" : declaration.getTypeDechetLibelle();
        return type + "\nValorisateur: " + (declaration.getValorisateurConfirmateurId() == null ? "Non confirme" : "Assigne");
    }

    private String formatQuantite(DeclarationDechet declaration) {
        return (declaration.getQuantite() == null ? 0 : declaration.getQuantite().intValue())
            + " " + (declaration.getUnite() == null ? "kg" : declaration.getUnite());
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "27/02/2026" : value.format(DATE_FORMATTER);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.toUpperCase(Locale.ROOT);
    }

    private String resolvePhotoCode(String type) {
        if (type == null) {
            return "IMG";
        }
        if (type.toLowerCase(Locale.ROOT).contains("papier")) {
            return "PAP";
        }
        if (type.toLowerCase(Locale.ROOT).contains("verre")) {
            return "VER";
        }
        return "PLA";
    }

    private String resolveRiskLabel(DeclarationDechet declaration) {
        int score = resolveRiskScore(declaration);
        if (score >= 70) {
            return "Eleve - " + score + "/100";
        }
        if (score >= 40) {
            return "Moyen - " + score + "/100";
        }
        return "Faible - " + score + "/100";
    }

    private int resolveRiskScore(DeclarationDechet declaration) {
        double quantite = declaration.getQuantite() == null ? 0 : declaration.getQuantite();
        int score = (int) Math.min(95, Math.round(quantite / 4.0));
        if ("REFUSEE".equals(normalizeStatus(declaration.getStatut()))) {
            score = Math.max(score, 80);
        } else if ("EN_ATTENTE".equals(normalizeStatus(declaration.getStatut()))) {
            score = Math.max(score, 55);
        }
        return score;
    }

    private String resolveTopType(List<DeclarationDechet> declarations) {
        return declarations.stream()
            .collect(Collectors.groupingBy(d -> safe(d.getTypeDechetLibelle()), Collectors.counting()))
            .entrySet()
            .stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
            .orElse("Aucune donnee");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String lower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private List<DeclarationDechet> buildOfflineSamples() {
        List<DeclarationDechet> samples = new ArrayList<>();
        samples.add(sample(19, "Papier / Carton", "APPROUVEE", 3, "kg", 200, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 10, 0)));
        samples.add(sample(18, "Plastique", "APPROUVEE", 25, "kg", 600, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 11, 0)));
        samples.add(sample(17, "Verre", "APPROUVEE", 10, "kg", 120, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 12, 0)));
        samples.add(sample(16, "Papier / Carton", "APPROUVEE", 256, "kg", 1800, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 13, 0)));
        samples.add(sample(15, "Plastique", "EN_ATTENTE", 124, "kg", 800, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 14, 0)));
        samples.add(sample(14, "Papier / Carton", "APPROUVEE", 123, "kg", 900, "demo@wastewise.tn", LocalDateTime.of(2026, 2, 27, 15, 0)));
        samples.add(sample(13, "Plastique", "EN_ATTENTE", 40, "kg", 400, null, LocalDateTime.of(2026, 2, 26, 10, 0)));
        samples.add(sample(12, "Plastique", "REFUSEE", 40, "kg", 400, null, LocalDateTime.of(2026, 2, 26, 9, 0)));
        return samples;
    }

    private DeclarationDechet sample(int id, String type, String status, int quantite, String unite, int points, String email, LocalDateTime createdAt) {
        DeclarationDechet declaration = new DeclarationDechet();
        declaration.setId(id);
        declaration.setTypeDechetLibelle(type);
        declaration.setStatut(status);
        declaration.setQuantite((double) quantite);
        declaration.setUnite(unite);
        declaration.setPointsAttribues(points);
        declaration.setCitoyenEmail(email);
        declaration.setCreatedAt(createdAt);
        declaration.setDescription("Declaration importee depuis l'historique admin.");
        declaration.setLatitude(36.8065);
        declaration.setLongitude(10.1815);
        return declaration;
    }

    private static final class StatusCell extends TableCell<DeclarationDechet, String> {
        private final Label label = new Label();

        private StatusCell() {
            label.setContentDisplay(ContentDisplay.CENTER);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            label.setText(item);
            label.getStyleClass().removeAll("status-pill-approved", "status-pill-pending", "status-pill-refused");
            if ("APPROUVEE".equals(item)) {
                label.getStyleClass().add("status-pill-approved");
            } else if ("REFUSEE".equals(item)) {
                label.getStyleClass().add("status-pill-refused");
            } else {
                label.getStyleClass().add("status-pill-pending");
            }
            setGraphic(label);
            setText(null);
        }
    }

    private static final class RiskCell extends TableCell<DeclarationDechet, String> {
        private final Label label = new Label();

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            label.setText(item);
            label.getStyleClass().removeAll("risk-chip-low", "risk-chip-medium", "risk-chip-high");
            if (item.startsWith("Eleve")) {
                label.getStyleClass().add("risk-chip-high");
            } else if (item.startsWith("Moyen")) {
                label.getStyleClass().add("risk-chip-medium");
            } else {
                label.getStyleClass().add("risk-chip-low");
            }
            setGraphic(label);
            setText(null);
        }
    }

    private static final class ActionsCell extends TableCell<DeclarationDechet, DeclarationDechet> {
        private final Button viewButton = new Button("Voir");
        private final Button userButton = new Button("User");
        private final Button assignButton = new Button("Affect");
        private final Button deleteButton = new Button("Suppr");
        private final HBox box = new HBox(6.0, viewButton, userButton, assignButton, deleteButton);

        private ActionsCell(DeclarationDechetListController controller) {
            viewButton.getStyleClass().addAll("action-chip", "action-chip-view");
            userButton.getStyleClass().addAll("action-chip", "action-chip-edit");
            assignButton.getStyleClass().addAll("action-chip", "action-chip-edit");
            deleteButton.getStyleClass().addAll("action-chip", "action-chip-delete");

            viewButton.setOnAction(event -> controller.openDetails(getCurrentItem()));
            userButton.setOnAction(event -> controller.openUser(getCurrentItem()));
            assignButton.setOnAction(event -> controller.assignValorisateur(getCurrentItem()));
            deleteButton.setOnAction(event -> controller.deleteDeclaration(getCurrentItem()));
        }

        @Override
        protected void updateItem(DeclarationDechet item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            setGraphic(box);
        }

        private DeclarationDechet getCurrentItem() {
            return getTableRow() == null ? null : (DeclarationDechet) getTableRow().getItem();
        }
    }
}
