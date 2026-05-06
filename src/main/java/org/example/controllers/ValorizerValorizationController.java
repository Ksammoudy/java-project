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
    private TableColumn<DeclarationDechet, Void> actionColumn;

    @FXML
    public void initialize() {
        User user = resolveValorizerUser();
        valorizerNameLabel.setText(fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        updateNavigation("valorization");

        configureTable();
        loadRows();
    }

    private void configureTable() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt() != null
                        ? cellData.getValue().getCreatedAt().format(DATE_FORMAT)
                        : "—"));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeDechetLibelle() != null
                        ? cellData.getValue().getTypeDechetLibelle()
                        : "—"));

        quantiteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantite() + " kg"));

        pointsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getPointsAttribues() != null ? cellData.getValue().getPointsAttribues() : 0)));

        actionColumn.setCellFactory(param -> new TableCell<DeclarationDechet, Void>() {
            private final Button detailBtn = new Button("Details");
            
            {
                detailBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11;");
                detailBtn.setOnAction(e -> {
                    DeclarationDechet row = getTableView().getItems().get(getIndex());
                    Main.showDeclarationDetailPage();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailBtn);
            }
        });
    }

    private void loadRows() {
        try {
            masterList.clear();
            masterList.addAll(declarationService.findAll().stream()
                    .filter(d -> "APPROUVEE".equals(d.getStatut()))
                    .collect(Collectors.toList()));
            
            updateStatistics();
            
            boolean isEmpty = masterList.isEmpty();
            emptyHintLabel.setVisible(isEmpty);
            emptyHintLabel.setManaged(isEmpty);
            if (isEmpty) {
                emptyHintLabel.setText("Aucune declaration approuvee pour valorisation.");
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
        totalKgLabel.setText(String.format("%.1f", totalKg));
        totalPointsLabel.setText(String.valueOf(totalPoints));
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
}
