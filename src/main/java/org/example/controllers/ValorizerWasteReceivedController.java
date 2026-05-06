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
import javafx.scene.layout.HBox;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValorizerWasteReceivedController {

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
    private TableColumn<DeclarationDechet, Void> actionColumn;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        updateNavigation("waste");

        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Approuvee", "Refusee"));
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
                        : "—"));

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
                        : "—"));

        statutColumn.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new SimpleStringProperty(formatStatut(statut));
        });

        quantiteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantite() + " kg"));

        actionColumn.setCellFactory(param -> new TableCell<DeclarationDechet, Void>() {
            private final Button detailBtn = new Button("Voir");
            private final Button validateBtn = new Button("Valider");
            
            {
                detailBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                detailBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    Main.showDeclarationDetailPage();
                });

                validateBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                validateBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    if ("EN_ATTENTE".equals(row.getStatut())) {
                        showConfirmationMessage("Déclaration validée avec succès!");
                        loadRows();
                    }
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
                    
                    if ("EN_ATTENTE".equals(row.getStatut())) {
                        box.getChildren().add(validateBtn);
                    }
                    
                    setGraphic(box);
                }
            }
        });
    }

    private void loadRows() {
        try {
            masterList.clear();
            masterList.addAll(declarationService.findAll());
            applyFilters();
            updateStatistics();
        } catch (SQLException e) {
            System.err.println("Erreur au chargement des déclarations: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur au chargement des déclarations");
        }
    }

    private void applyFilters() {
        String statusValue = statusFilter.getValue();
        String searchValue = searchField.getText().toLowerCase();

        List<DeclarationDechet> filtered = masterList.stream()
                .filter(d -> {
                    String statut = formatStatut(d.getStatut());
                    return "Tous".equals(statusValue) || statut.equals(statusValue);
                })
                .filter(d -> searchValue.isEmpty() ||
                        (d.getTypeDechetLibelle() != null && d.getTypeDechetLibelle().toLowerCase().contains(searchValue)) ||
                        (d.getDescription() != null && d.getDescription().toLowerCase().contains(searchValue)))
                .collect(Collectors.toList());

        boolean isEmpty = filtered.isEmpty();
        emptyHintLabel.setVisible(isEmpty);
        emptyHintLabel.setManaged(isEmpty);
        if (isEmpty) {
            emptyHintLabel.setText("Aucune déclaration ne correspond aux critères.");
        }

        ObservableList<DeclarationDechet> data = FXCollections.observableArrayList(filtered);
        declarationsTable.setItems(data);
    }

    private void updateStatistics() {
        int total = masterList.size();
        long pending = masterList.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
        long approved = masterList.stream().filter(d -> "APPROUVEE".equals(d.getStatut())).count();

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

    /**
     * Met à jour la navigation pour marquer le bouton courant comme actif.
     */
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
        if (statut == null) return "—";
        return switch (statut.toUpperCase()) {
            case "EN_ATTENTE" -> "En attente";
            case "APPROUVEE" -> "Approuvee";
            case "REFUSEE" -> "Refusee";
            default -> statut;
        };
    }

    private void showConfirmationMessage(String message) {
        System.out.println("Info: " + message);
    }

    private void showErrorMessage(String message) {
        System.err.println("Error: " + message);
    }
}
