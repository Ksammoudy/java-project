package org.example.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.example.Main;
import org.example.entities.TypeDechet;
import org.example.services.TypeDechetJdbcService;
import org.example.utils.AdminUiState;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TypeDechetWorkshopController {

    private final TypeDechetJdbcService service = new TypeDechetJdbcService();

    @FXML
    private Label totalTypesLabel;

    @FXML
    private Label bannerLabel;

    @FXML
    private HBox bannerBox;

    @FXML
    private TableView<TypeDechet> typeTable;

    @FXML
    private TableColumn<TypeDechet, String> idColumn;

    @FXML
    private TableColumn<TypeDechet, String> libelleColumn;

    @FXML
    private TableColumn<TypeDechet, String> pointsColumn;

    @FXML
    private TableColumn<TypeDechet, String> descriptionColumn;

    @FXML
    private TableColumn<TypeDechet, TypeDechet> actionsColumn;

    @FXML
    public void initialize() {
        typeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        libelleColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getLibelle())));
        pointsColumn.setCellValueFactory(data -> new SimpleStringProperty(formatPoints(data.getValue())));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getDescriptionTri())));
        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        actionsColumn.setCellFactory(column -> new ActionsCell(this));
        loadTypes();
        showFlashIfPresent();
    }

    @FXML
    private void handleInsert() {
        AdminUiState.setSelectedTypeDechet(null);
        Main.showTypeDechetFormPage();
    }

    @FXML
    private void handleOpenDeclarations() {
        Main.showDeclarationListPage();
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
    private void closeBanner() {
        bannerBox.setManaged(false);
        bannerBox.setVisible(false);
    }

    void openDetails(TypeDechet typeDechet) {
        if (typeDechet == null) {
            return;
        }
        AdminUiState.setSelectedTypeDechet(typeDechet);
        Main.showTypeDechetDetailPage();
    }

    void openEdit(TypeDechet typeDechet) {
        if (typeDechet == null) {
            return;
        }
        AdminUiState.setSelectedTypeDechet(typeDechet);
        Main.showTypeDechetFormPage();
    }

    void deleteType(TypeDechet typeDechet) {
        if (typeDechet == null) {
            return;
        }
        try {
            if (typeDechet.getId() != null && service.delete(typeDechet.getId())) {
                AdminUiState.setFlash("Type de dechet supprime avec succes.", false);
            } else {
                AdminUiState.setFlash("Suppression impossible pour ce type de dechet.", true);
            }
        } catch (SQLException | IllegalStateException exception) {
            AdminUiState.setFlash("Suppression impossible: " + exception.getMessage(), true);
        }
        loadTypes();
        showFlashIfPresent();
    }

    private void loadTypes() {
        List<TypeDechet> types;
        try {
            types = service.findAll();
            if (types.isEmpty()) {
                types = buildOfflineTypes();
            }
        } catch (SQLException | IllegalStateException exception) {
            types = buildOfflineTypes();
        }

        typeTable.setItems(FXCollections.observableArrayList(types));
        totalTypesLabel.setText(String.valueOf(types.size()));
    }

    private void showFlashIfPresent() {
        String message = AdminUiState.consumeFlashMessage();
        boolean error = AdminUiState.consumeFlashError();
        if (message == null || message.isBlank()) {
            bannerBox.setManaged(false);
            bannerBox.setVisible(false);
            return;
        }

        bannerLabel.setText(message);
        bannerBox.setManaged(true);
        bannerBox.setVisible(true);
        bannerBox.getStyleClass().removeAll("success-banner", "error-banner");
        bannerLabel.getStyleClass().removeAll("success-banner-text", "error-banner-text");
        if (error) {
            bannerBox.getStyleClass().add("error-banner");
            bannerLabel.getStyleClass().add("error-banner-text");
        } else {
            bannerBox.getStyleClass().add("success-banner");
            bannerLabel.getStyleClass().add("success-banner-text");
        }
    }

    private List<TypeDechet> buildOfflineTypes() {
        List<TypeDechet> types = new ArrayList<>();
        types.add(buildType(1, "Plastique", 15.0, "Nettoyer et separer avant depot."));
        types.add(buildType(2, "Verre", 3.0, "Deposer sans bouchon ni residus."));
        types.add(buildType(3, "Papier / Carton", 9.0, "Plier et garder au sec avant tri."));
        return types;
    }

    private TypeDechet buildType(int id, String libelle, Double points, String description) {
        TypeDechet type = new TypeDechet();
        type.setId(id);
        type.setLibelle(libelle);
        type.setValeurPointsKg(points);
        type.setDescriptionTri(description);
        return type;
    }

    private String formatPoints(TypeDechet type) {
        if (type.getValeurPointsKg() == null) {
            return "";
        }
        return type.getValeurPointsKg() % 1 == 0
            ? String.valueOf(type.getValeurPointsKg().intValue())
            : String.format("%.2f", type.getValeurPointsKg());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class ActionsCell extends TableCell<TypeDechet, TypeDechet> {
        private final Button detailButton = new Button("Detail");
        private final Button editButton = new Button("Modifier");
        private final Button deleteButton = new Button("Supprimer");
        private final HBox box = new HBox(8.0, detailButton, editButton, deleteButton);

        private ActionsCell(TypeDechetWorkshopController controller) {
            detailButton.getStyleClass().addAll("action-chip", "action-chip-view");
            editButton.getStyleClass().addAll("action-chip", "action-chip-edit");
            deleteButton.getStyleClass().addAll("action-chip", "action-chip-delete");

            detailButton.setOnAction(event -> controller.openDetails(getCurrentItem()));
            editButton.setOnAction(event -> controller.openEdit(getCurrentItem()));
            deleteButton.setOnAction(event -> controller.deleteType(getCurrentItem()));
        }

        @Override
        protected void updateItem(TypeDechet item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            setGraphic(box);
        }

        private TypeDechet getCurrentItem() {
            return getTableRow() == null ? null : (TypeDechet) getTableRow().getItem();
        }
    }
}
