package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;
import org.example.utils.AdminUiState;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;
import org.example.utils.CitizenUiState;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CitizenMyDeclarationsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final List<DeclarationDechet> masterList = new ArrayList<>();

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
    private Label emptyHintLabel;

    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<DeclarationDechet> declarationsTable;
    @FXML
    private TableColumn<DeclarationDechet, String> dateColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> typeColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> statutColumn;
    @FXML
    private TableColumn<DeclarationDechet, String> quantiteColumn;
    @FXML
    private TableColumn<DeclarationDechet, Void> actionColumn;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        CitizenSidebarHelper.applyActive(navMyDeclarations,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Approuvee", "Refusee"));
        statusFilter.setValue("Tous");
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());

        configureTable();
        loadRows();
    }

    private void configureTable() {
        declarationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "—" : data.getValue().getCreatedAt().format(DATE_FORMAT)
        ));
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTypeDechetLibelle() != null ? data.getValue().getTypeDechetLibelle() : "—"
        ));
        statutColumn.setCellValueFactory(data -> new SimpleStringProperty(formatStatut(data.getValue().getStatut())));
        quantiteColumn.setCellValueFactory(data -> new SimpleStringProperty(formatQuantite(data.getValue())));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir");
            {
                btn.getStyleClass().add("outline-btn");
                btn.setOnAction(e -> {
                    DeclarationDechet row = getTableRow().getItem();
                    if (row != null) {
                        openDetail(row);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadRows() {
        masterList.clear();
        Integer cid = CitizenSession.resolveCitizenDatabaseId();
        if (cid == null) {
            emptyHintLabel.setManaged(true);
            emptyHintLabel.setVisible(true);
            emptyHintLabel.setText("Aucun compte citoyen trouve en base. Ajoutez un utilisateur CITIZEN ou connectez-vous.");
            declarationsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            List<DeclarationDechet> list = declarationService.findByCitoyenId(cid);
            masterList.addAll(list);
            applyFilters();
        } catch (SQLException | RuntimeException e) {
            emptyHintLabel.setManaged(true);
            emptyHintLabel.setVisible(true);
            emptyHintLabel.setText("Erreur de chargement : DB indisponible.");
            declarationsTable.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void applyFilters() {
        String st = statusFilter != null ? statusFilter.getValue() : "Tous";
        String q = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase(Locale.ROOT)
                : "";

        List<DeclarationDechet> filtered = masterList.stream()
                .filter(d -> matchesStatus(d, st))
                .filter(d -> matchesSearch(d, q))
                .collect(Collectors.toList());

        ObservableList<DeclarationDechet> obs = FXCollections.observableArrayList(filtered);
        declarationsTable.setItems(obs);

        boolean empty = filtered.isEmpty();
        emptyHintLabel.setManaged(empty);
        emptyHintLabel.setVisible(empty);
        if (masterList.isEmpty()) {
            emptyHintLabel.setText("Vous n'avez pas encore de declaration.");
        } else if (empty) {
            emptyHintLabel.setText("Aucune declaration ne correspond aux filtres.");
        } else {
            emptyHintLabel.setText("");
        }
    }

    @FXML
    public void resetFilters() {
        if (statusFilter != null) {
            statusFilter.setValue("Tous");
        }
        if (searchField != null) {
            searchField.clear();
        }
        applyFilters();
    }

    private boolean matchesStatus(DeclarationDechet d, String uiStatus) {
        if (uiStatus == null || "Tous".equals(uiStatus)) {
            return true;
        }
        String norm = normalizeStatus(d.getStatut());
        return switch (uiStatus) {
            case "En attente" -> "EN_ATTENTE".equals(norm);
            case "Approuvee" -> "APPROUVEE".equals(norm);
            case "Refusee" -> "REFUSEE".equals(norm);
            default -> true;
        };
    }

    private boolean matchesSearch(DeclarationDechet d, String q) {
        if (q.isEmpty()) {
            return true;
        }
        String desc = d.getDescription() != null ? d.getDescription().toLowerCase(Locale.ROOT) : "";
        String type = d.getTypeDechetLibelle() != null ? d.getTypeDechetLibelle().toLowerCase(Locale.ROOT) : "";
        return desc.contains(q) || type.contains(q);
    }

    private static String normalizeStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return "EN_ATTENTE";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private void openDetail(DeclarationDechet declaration) {
        AdminUiState.setSelectedDeclaration(declaration);
        CitizenUiState.setReturnFromDetailToMyDeclarations(true);
        Main.showDeclarationDetailPage();
    }

    private static String formatStatut(String raw) {
        if (raw == null || raw.isBlank()) {
            return "En attente";
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "APPROUVEE" -> "Approuvee";
            case "EN_ATTENTE" -> "En attente";
            case "REFUSEE" -> "Refusee";
            default -> raw;
        };
    }

    private static String formatQuantite(DeclarationDechet d) {
        if (d.getQuantite() == null) {
            return "—";
        }
        String u = d.getUnite() == null ? "" : " " + d.getUnite();
        return d.getQuantite() + u;
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
}
