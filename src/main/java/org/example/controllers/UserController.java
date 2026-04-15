package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.Main;
import org.example.models.User;
import org.example.services.UserService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserController {

    // =========================
    // TABLE LIST PAGE
    // =========================
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, String> createdAtColumn;

    @FXML
    private Label pageMessageLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    // =========================
    // SHOW PAGE
    // =========================
    @FXML
    private Label emailLabel;

    @FXML
    private Label nomCompletLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label rolesLabel;

    @FXML
    private Label createdAtLabel;

    // =========================
    // FORM PAGE
    // =========================
    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telephoneField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField rolesField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private CheckBox verifiedCheckBox;

    @FXML
    private Label formMessageLabel;

    // =========================
    // DELETE PAGE
    // =========================
    @FXML
    private Label deleteUserEmailLabel;

    @FXML
    private Label deleteUserNameLabel;

    @FXML
    private Label deleteMessageLabel;

    // =========================
    // STATE
    // =========================
    private final UserService userService = UserService.getInstance();

    private User selectedUser;
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeTableIfPresent();
        initializeFormIfPresent();
        initializeFiltersIfPresent();
        loadSelectedUserDataIfPresent();
    }

    // =========================
    // INITIALIZATION
    // =========================
    private void initializeTableIfPresent() {
        if (usersTable == null) {
            return;
        }

        if (emailColumn != null) {
            emailColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(valueOrDash(cellData.getValue().getEmail()))
            );
            emailColumn.setSortable(true);
        }

        if (fullNameColumn != null) {
            fullNameColumn.setCellValueFactory(cellData -> {
                User user = cellData.getValue();
                String fullName = (nullToEmpty(user.getNom()) + " " + nullToEmpty(user.getPrenom())).trim();
                return new SimpleStringProperty(fullName.isEmpty() ? "—" : fullName);
            });
            fullNameColumn.setSortable(true);
        }

        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(valueOrDash(cellData.getValue().getTelephone()))
            );
            phoneColumn.setSortable(true);
        }

        if (typeColumn != null) {
            typeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(formatType(cellData.getValue().getType()))
            );
            typeColumn.setSortable(true);
        }

        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isActive() ? "Actif" : "Inactif")
            );
            statusColumn.setSortable(true);
        }

        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(cellData -> {
                if (cellData.getValue().getCreatedAt() == null) {
                    return new SimpleStringProperty("—");
                }
                return new SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            });
            createdAtColumn.setSortable(true);
        }

        loadUsers();
    }

    private void initializeFormIfPresent() {
        if (typeComboBox != null) {
            typeComboBox.setItems(FXCollections.observableArrayList(
                    "CITIZEN",
                    "VALORIZER",
                    "ADMIN"
            ));
        }
    }

    private void initializeFiltersIfPresent() {
        if (typeFilterComboBox != null) {
            typeFilterComboBox.setItems(FXCollections.observableArrayList(
                    "Tous",
                    "Administrateur",
                    "Valorisateur",
                    "Citoyen"
            ));
            typeFilterComboBox.setValue("Tous");
        }

        if (statusFilterComboBox != null) {
            statusFilterComboBox.setItems(FXCollections.observableArrayList(
                    "Tous",
                    "Actif",
                    "Inactif"
            ));
            statusFilterComboBox.setValue("Tous");
        }
    }

    private void loadSelectedUserDataIfPresent() {
        if (selectedUser == null) {
            return;
        }

        if (emailLabel != null) {
            emailLabel.setText(valueOrDash(selectedUser.getEmail()));
        }

        if (nomCompletLabel != null) {
            String fullName = (nullToEmpty(selectedUser.getNom()) + " " + nullToEmpty(selectedUser.getPrenom())).trim();
            nomCompletLabel.setText(fullName.isEmpty() ? "—" : fullName);
        }

        if (telephoneLabel != null) {
            telephoneLabel.setText(valueOrDash(selectedUser.getTelephone()));
        }

        if (typeLabel != null) {
            typeLabel.setText(formatType(selectedUser.getType()));
        }

        if (rolesLabel != null) {
            rolesLabel.setText(extractRoles(selectedUser));
        }

        if (createdAtLabel != null) {
            createdAtLabel.setText(
                    selectedUser.getCreatedAt() == null
                            ? "—"
                            : selectedUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        }

        if (emailField != null) {
            emailField.setText(nullToEmpty(selectedUser.getEmail()));
        }

        if (nomField != null) {
            nomField.setText(nullToEmpty(selectedUser.getNom()));
        }

        if (prenomField != null) {
            prenomField.setText(nullToEmpty(selectedUser.getPrenom()));
        }

        if (telephoneField != null) {
            telephoneField.setText(nullToEmpty(selectedUser.getTelephone()));
        }

        if (typeComboBox != null) {
            typeComboBox.setValue(normalizeType(selectedUser.getType()));
        }

        if (rolesField != null) {
            rolesField.setText(extractRoles(selectedUser));
        }

        if (activeCheckBox != null) {
            activeCheckBox.setSelected(selectedUser.isActive());
        }

        if (verifiedCheckBox != null) {
            verifiedCheckBox.setSelected(false);
        }

        if (formTitleLabel != null) {
            formTitleLabel.setText("Modifier utilisateur");
        }

        if (deleteUserEmailLabel != null) {
            deleteUserEmailLabel.setText(valueOrDash(selectedUser.getEmail()));
        }

        if (deleteUserNameLabel != null) {
            String fullName = (nullToEmpty(selectedUser.getNom()) + " " + nullToEmpty(selectedUser.getPrenom())).trim();
            deleteUserNameLabel.setText(fullName.isEmpty() ? "—" : fullName);
        }
    }

    // =========================
    // PUBLIC STATE SETTER
    // =========================
    public void setSelectedUser(User user) {
        this.selectedUser = user;
        loadSelectedUserDataIfPresent();
    }

    // =========================
    // TABLE ACTIONS
    // =========================
    private void loadUsers() {
        if (usersTable == null) {
            return;
        }

        try {
            userList = FXCollections.observableArrayList(userService.read());
            usersTable.setItems(userList);

            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(userList.size()));
            }

            setPageMessage("Utilisateurs chargés avec succès.", true);

        } catch (SQLException e) {
            setPageMessage("Impossible de charger les utilisateurs : " + e.getMessage(), false);
            showErrorAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshUsers() {
        loadUsers();

        if (searchField != null) {
            searchField.clear();
        }
        if (typeFilterComboBox != null) {
            typeFilterComboBox.setValue("Tous");
        }
        if (statusFilterComboBox != null) {
            statusFilterComboBox.setValue("Tous");
        }
    }

    @FXML
    public void handleSearch() {
        applyFilters();
    }

    @FXML
    public void handleFilter() {
        applyFilters();
    }

    private void applyFilters() {
        if (usersTable == null) {
            return;
        }

        String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase()
                : "";

        String selectedType = typeFilterComboBox != null && typeFilterComboBox.getValue() != null
                ? typeFilterComboBox.getValue()
                : "Tous";

        String selectedStatus = statusFilterComboBox != null && statusFilterComboBox.getValue() != null
                ? statusFilterComboBox.getValue()
                : "Tous";

        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : userList) {
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
            String nom = user.getNom() != null ? user.getNom().toLowerCase() : "";
            String prenom = user.getPrenom() != null ? user.getPrenom().toLowerCase() : "";
            String type = formatType(user.getType());
            String status = user.isActive() ? "Actif" : "Inactif";

            boolean matchesKeyword =
                    keyword.isEmpty()
                            || email.contains(keyword)
                            || nom.contains(keyword)
                            || prenom.contains(keyword)
                            || type.toLowerCase().contains(keyword);

            boolean matchesType =
                    "Tous".equals(selectedType)
                            || type.equalsIgnoreCase(selectedType);

            boolean matchesStatus =
                    "Tous".equals(selectedStatus)
                            || status.equalsIgnoreCase(selectedStatus);

            if (matchesKeyword && matchesType && matchesStatus) {
                filteredList.add(user);
            }
        }

        usersTable.setItems(filteredList);

        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(filteredList.size()));
        }
    }

    @FXML
    public void handleToggleUser() {
        if (usersTable == null) {
            return;
        }

        User user = usersTable.getSelectionModel().getSelectedItem();

        if (user == null) {
            setPageMessage("Veuillez sélectionner un utilisateur.", false);
            return;
        }

        boolean success = userService.toggleActive(user.getId());

        if (success) {
            loadUsers();
            applyFilters();
            setPageMessage("Statut utilisateur mis à jour.", true);
        } else {
            setPageMessage("Erreur lors du changement de statut.", false);
        }
    }

    @FXML
    public void handleEditUser() {
        User user = getSelectedUserFromTableOrState();

        if (user == null) {
            setPageMessage("Veuillez sélectionner un utilisateur à modifier.", false);
            return;
        }

        Main.showAdminUserEditPage(user);
    }

    @FXML
    public void handleDeleteUser() {
        User user = getSelectedUserFromTableOrState();

        if (user == null) {
            setPageMessage("Veuillez sélectionner un utilisateur à supprimer.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer utilisateur");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userService.deleteUser(user.getId());

            if (success) {
                loadUsers();
                applyFilters();
                setPageMessage("Utilisateur supprimé avec succès.", true);
            } else {
                setPageMessage("Erreur lors de la suppression.", false);
            }
        }
    }

    @FXML
    public void handleBackToAdminDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    public void handleBack() {
        Main.showAdminUsersPage();
    }

    // =========================
    // FORM ACTIONS
    // =========================
    @FXML
    public void handleSaveUser() {
        if (emailField == null || nomField == null || prenomField == null || typeComboBox == null) {
            return;
        }

        if (selectedUser == null) {
            setFormMessage("Aucun utilisateur à modifier.", false);
            return;
        }

        String email = safeTrim(emailField.getText()).toLowerCase();
        String nom = safeTrim(nomField.getText());
        String prenom = safeTrim(prenomField.getText());
        String telephone = telephoneField != null ? safeTrim(telephoneField.getText()) : "";
        String type = typeComboBox.getValue() != null ? typeComboBox.getValue().trim() : "";
        String password = passwordField != null ? safeTrim(passwordField.getText()) : "";

        if (email.isEmpty()) {
            setFormMessage("L'email est obligatoire.", false);
            return;
        }

        if (!isValidEmail(email)) {
            setFormMessage("Email invalide.", false);
            return;
        }

        if (nom.isEmpty()) {
            setFormMessage("Le nom est obligatoire.", false);
            return;
        }

        if (prenom.isEmpty()) {
            setFormMessage("Le prénom est obligatoire.", false);
            return;
        }

        if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
            setFormMessage("Le téléphone doit contenir 8 chiffres.", false);
            return;
        }

        if (type.isEmpty()) {
            setFormMessage("Le type est obligatoire.", false);
            return;
        }

        selectedUser.setEmail(email);
        selectedUser.setNom(nom);
        selectedUser.setPrenom(prenom);
        selectedUser.setTelephone(telephone.isEmpty() ? null : telephone);
        selectedUser.setType(type);

        if (activeCheckBox != null) {
            selectedUser.setActive(activeCheckBox.isSelected());
        }

        boolean updated = userService.updateUserByAdmin(selectedUser);

        if (!updated) {
            setFormMessage("Erreur lors de la mise à jour de l'utilisateur.", false);
            return;
        }

        if (!password.isEmpty()) {
            boolean passwordUpdated = userService.updatePassword(selectedUser.getId(), password);

            if (!passwordUpdated) {
                setFormMessage("Utilisateur modifié, mais erreur lors du mot de passe.", false);
                return;
            }
        }

        setFormMessage("Utilisateur modifié avec succès.", true);
        Main.showAdminUsersPage();
    }

    @FXML
    public void handleCancelForm() {
        Main.showAdminUsersPage();
    }

    // =========================
    // DELETE PAGE ACTIONS
    // =========================
    @FXML
    public void handleConfirmDelete() {
        if (selectedUser == null) {
            setDeleteMessage("Aucun utilisateur sélectionné.", false);
            return;
        }

        boolean success = userService.deleteUser(selectedUser.getId());

        if (success) {
            setDeleteMessage("Utilisateur supprimé avec succès.", true);
            Main.showAdminUsersPage();
        } else {
            setDeleteMessage("Erreur lors de la suppression.", false);
        }
    }

    @FXML
    public void handleCancelDelete() {
        Main.showAdminUsersPage();
    }

    // =========================
    // MESSAGE HELPERS
    // =========================
    private void setPageMessage(String message, boolean success) {
        if (pageMessageLabel != null) {
            pageMessageLabel.getStyleClass().removeAll("message-success", "message-error");
            pageMessageLabel.getStyleClass().add(success ? "message-success" : "message-error");
            pageMessageLabel.setText(message);
        }
    }

    private void setFormMessage(String message, boolean success) {
        if (formMessageLabel != null) {
            formMessageLabel.getStyleClass().removeAll("message-success", "message-error");
            formMessageLabel.getStyleClass().add(success ? "message-success" : "message-error");
            formMessageLabel.setText(message);
        }
    }

    private void setDeleteMessage(String message, boolean success) {
        if (deleteMessageLabel != null) {
            deleteMessageLabel.getStyleClass().removeAll("message-success", "message-error");
            deleteMessageLabel.getStyleClass().add(success ? "message-success" : "message-error");
            deleteMessageLabel.setText(message);
        }
    }

    // =========================
    // HELPERS
    // =========================
    private User getSelectedUserFromTableOrState() {
        if (usersTable != null) {
            User fromTable = usersTable.getSelectionModel().getSelectedItem();
            if (fromTable != null) {
                return fromTable;
            }
        }
        return selectedUser;
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatType(String type) {
        if (type == null || type.isBlank()) {
            return "Citoyen";
        }

        type = type.toUpperCase();

        if (type.contains("ADMIN")) {
            return "Administrateur";
        }
        if (type.contains("VALORIZER") || type.contains("VALORISATEUR")) {
            return "Valorisateur";
        }
        return "Citoyen";
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "CITIZEN";
        }

        type = type.toUpperCase();

        if (type.contains("ADMIN")) {
            return "ADMIN";
        }
        if (type.contains("VALORIZER") || type.contains("VALORISATEUR")) {
            return "VALORIZER";
        }
        return "CITIZEN";
    }

    private String extractRoles(User user) {
        return formatType(user.getType());
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}