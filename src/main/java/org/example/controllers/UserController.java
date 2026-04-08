package org.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.UserService;
import org.example.utils.PasswordUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserController {

    private final UserService userService = UserService.getInstance();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    private static User selectedUser;

    // =========================
    // LIST PAGE
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
    private Label totalUsersLabel;
    @FXML
    private Label pageMessageLabel;

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

    private boolean editMode = false;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || currentUser.getType() == null || !currentUser.getType().equalsIgnoreCase("ADMIN")) {
            Main.showLoginPage();
            return;
        }

        if (usersTable != null) {
            configureTable();
            loadUsers();
        }

        if (typeComboBox != null) {
            typeComboBox.setItems(FXCollections.observableArrayList("ADMIN", "VALORIZER", "CITIZEN"));
        }

        if (emailField != null) {
            initFormPage();
        }

        if (deleteUserEmailLabel != null) {
            initDeletePage();
        }
    }

    // =========================================================
    // LIST PAGE
    // =========================================================
    private void configureTable() {
        emailColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(valueOrDash(data.getValue().getEmail()))
        );

        fullNameColumn.setCellValueFactory(data -> {
            User u = data.getValue();
            String fullName = (nullToEmpty(u.getPrenom()) + " " + nullToEmpty(u.getNom())).trim();
            return new ReadOnlyStringWrapper(fullName.isEmpty() ? "—" : fullName);
        });

        phoneColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(valueOrDash(data.getValue().getTelephone()))
        );

        typeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatType(data.getValue().getType()))
        );

        statusColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().isActive() ? "Actif" : "Désactivé")
        );

        createdAtColumn.setCellValueFactory(data -> {
            LocalDateTime createdAt = data.getValue().getCreatedAt();
            String value = createdAt != null
                    ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—";
            return new ReadOnlyStringWrapper(value);
        });
    }

    private void loadUsers() {
        usersList.setAll(userService.getAllUsers());
        usersTable.setItems(usersList);

        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(usersList.size()));
        }
    }

    @FXML
    private void handleRefreshUsers() {
        loadUsers();
        setPageMessage("Liste actualisée.", true);
    }

    @FXML
    private void handleNewUser() {
        selectedUser = null;
        openPage("/org/example/views/admin_user_form.fxml", "Créer utilisateur | WasteWise TN");
    }

    @FXML
    private void handleEditUser() {
        User user = getSelectedUserFromTable();
        if (user == null) return;

        selectedUser = user;
        openPage("/org/example/views/admin_user_form.fxml", "Modifier utilisateur | WasteWise TN");
    }

    @FXML
    private void handleDeleteUser() {
        User user = getSelectedUserFromTable();
        if (user == null) return;

        User currentAdmin = SessionManager.getCurrentUser();
        if (currentAdmin != null && currentAdmin.getId() == user.getId()) {
            setPageMessage("Impossible de supprimer votre propre compte.", false);
            return;
        }

        selectedUser = user;
        openPage("/org/example/views/admin_user_delete.fxml", "Supprimer utilisateur | WasteWise TN");
    }

    @FXML
    private void handleToggleUser() {
        User targetUser = getSelectedUserFromTable();
        if (targetUser == null) return;

        User currentAdmin = SessionManager.getCurrentUser();
        boolean newStatus = !targetUser.isActive();

        boolean changed = userService.setUserActiveStatus(targetUser.getId(), newStatus, currentAdmin);

        if (changed) {
            targetUser.setActive(newStatus);
            usersTable.refresh();

            if (newStatus) {
                setPageMessage("Utilisateur activé avec succès.", true);
            } else {
                setPageMessage("Utilisateur désactivé avec succès.", true);
            }
        } else {
            setPageMessage(
                    "Impossible de modifier le statut. Action autorisée seulement sur citoyen / valorisateur, et pas sur votre propre compte.",
                    false
            );
        }
    }

    @FXML
    private void handleBackToAdminDashboard() {
        Main.showDashboardAdmin();
    }

    private User getSelectedUserFromTable() {
        if (usersTable == null) {
            return null;
        }

        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
            return null;
        }
        return user;
    }

    // =========================================================
    // FORM PAGE
    // =========================================================
    private void initFormPage() {
        if (selectedUser != null) {
            editMode = true;

            if (formTitleLabel != null) {
                formTitleLabel.setText("✏ Modifier utilisateur");
            }

            emailField.setText(nullToEmpty(selectedUser.getEmail()));
            nomField.setText(nullToEmpty(selectedUser.getNom()));
            prenomField.setText(nullToEmpty(selectedUser.getPrenom()));
            telephoneField.setText(nullToEmpty(selectedUser.getTelephone()));
            typeComboBox.setValue(selectedUser.getType() != null ? selectedUser.getType().toUpperCase() : "CITIZEN");
            rolesField.setText(nullToEmpty(selectedUser.getRoles()));
            activeCheckBox.setSelected(selectedUser.isActive());
            verifiedCheckBox.setSelected(selectedUser.isVerified());

        } else {
            editMode = false;

            if (formTitleLabel != null) {
                formTitleLabel.setText("➕ Créer utilisateur");
            }

            if (typeComboBox != null) {
                typeComboBox.setValue("CITIZEN");
            }

            if (activeCheckBox != null) {
                activeCheckBox.setSelected(true);
            }

            if (verifiedCheckBox != null) {
                verifiedCheckBox.setSelected(false);
            }
        }
    }

    @FXML
    private void handleSaveUser() {
        String email = safeText(emailField).toLowerCase();
        String nom = safeText(nomField);
        String prenom = safeText(prenomField);
        String telephone = safeText(telephoneField);
        String type = (typeComboBox != null && typeComboBox.getValue() != null)
                ? typeComboBox.getValue().trim().toUpperCase()
                : "";
        String roles = safeText(rolesField);
        String password = passwordField != null && passwordField.getText() != null
                ? passwordField.getText().trim()
                : "";

        // =========================
        // CONTROLE DE SAISIE
        // =========================
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

        if (!isValidName(nom)) {
            setFormMessage("Nom invalide : lettres, espaces et tirets uniquement.", false);
            return;
        }

        if (prenom.isEmpty()) {
            setFormMessage("Le prénom est obligatoire.", false);
            return;
        }

        if (!isValidName(prenom)) {
            setFormMessage("Prénom invalide : lettres, espaces et tirets uniquement.", false);
            return;
        }

        if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
            setFormMessage("Le téléphone doit contenir exactement 8 chiffres.", false);
            return;
        }

        if (type.isEmpty()) {
            setFormMessage("Veuillez sélectionner un type d'utilisateur.", false);
            return;
        }

        if (!type.equals("ADMIN") && !type.equals("VALORIZER") && !type.equals("CITIZEN")) {
            setFormMessage("Type utilisateur invalide.", false);
            return;
        }

        if (!editMode) {
            if (password.isEmpty()) {
                setFormMessage("Le mot de passe est obligatoire pour la création.", false);
                return;
            }

            if (password.length() < 6) {
                setFormMessage("Le mot de passe doit contenir au moins 6 caractères.", false);
                return;
            }
        } else {
            if (!password.isEmpty() && password.length() < 6) {
                setFormMessage("Le nouveau mot de passe doit contenir au moins 6 caractères.", false);
                return;
            }
        }

        // =========================
        // MODE MODIFICATION
        // =========================
        if (editMode) {
            User currentAdmin = SessionManager.getCurrentUser();
            User user = userService.getUserById(selectedUser.getId());

            if (user == null) {
                setFormMessage("Utilisateur introuvable.", false);
                return;
            }

            if (userService.emailExistsForAnotherUser(email, user.getId())) {
                setFormMessage("Cet email est déjà utilisé.", false);
                return;
            }

            if (currentAdmin != null && currentAdmin.getId() == user.getId()) {
                if (activeCheckBox != null && !activeCheckBox.isSelected()) {
                    setFormMessage("Impossible de désactiver votre propre compte.", false);
                    return;
                }
            }

            user.setEmail(email);
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(telephone.isEmpty() ? null : telephone);
            user.setType(type);
            user.setRoles(roles.isEmpty() ? "[]" : roles);
            user.setActive(activeCheckBox != null && activeCheckBox.isSelected());
            user.setVerified(verifiedCheckBox != null && verifiedCheckBox.isSelected());

            if (!password.isEmpty()) {
                boolean passwordUpdated = userService.updatePassword(user.getId(), password);
                if (!passwordUpdated) {
                    setFormMessage("Erreur lors de la mise à jour du mot de passe.", false);
                    return;
                }

                user = userService.getUserById(user.getId());
                if (user == null) {
                    setFormMessage("Erreur après mise à jour du mot de passe.", false);
                    return;
                }

                user.setEmail(email);
                user.setNom(nom);
                user.setPrenom(prenom);
                user.setTelephone(telephone.isEmpty() ? null : telephone);
                user.setType(type);
                user.setRoles(roles.isEmpty() ? "[]" : roles);
                user.setActive(activeCheckBox != null && activeCheckBox.isSelected());
                user.setVerified(verifiedCheckBox != null && verifiedCheckBox.isSelected());
            }

            boolean updated = userService.updateUser(user);

            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur mis à jour avec succès.");
                selectedUser = null;
                openPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN");
            } else {
                setFormMessage("Erreur lors de la mise à jour.", false);
            }

        } else {
            // =========================
            // MODE CREATION
            // =========================
            if (userService.getUserByEmail(email) != null) {
                setFormMessage("Cet email existe déjà.", false);
                return;
            }

            User user = new User();
            user.setEmail(email);
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(telephone.isEmpty() ? null : telephone);
            user.setType(type);
            user.setRoles(roles.isEmpty() ? "[]" : roles);

            // Mot de passe hashé
            user.setPassword(PasswordUtil.hashPassword(password));

            user.setCreatedAt(LocalDateTime.now());
            user.setActive(activeCheckBox != null && activeCheckBox.isSelected());
            user.setVerified(verifiedCheckBox != null && verifiedCheckBox.isSelected());
            user.setFaceEmbedding(null);
            user.setFaceUpdatedAt(null);
            user.setLastSeenAt(null);
            user.setGoogleAuthenticatorSecret(null);
            user.setTwoFactorEnabled(false);

            boolean created = userService.addUser(user);

            if (created) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur créé avec succès.");
                openPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN");
            } else {
                setFormMessage("Erreur lors de la création de l'utilisateur.", false);
            }
        }
    }

    @FXML
    private void handleCancelForm() {
        selectedUser = null;
        openPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN");
    }

    // =========================================================
    // DELETE PAGE
    // =========================================================
    private void initDeletePage() {
        if (selectedUser == null) {
            if (deleteMessageLabel != null) {
                deleteMessageLabel.setText("Aucun utilisateur sélectionné.");
            }
            return;
        }

        if (deleteUserEmailLabel != null) {
            deleteUserEmailLabel.setText(valueOrDash(selectedUser.getEmail()));
        }

        if (deleteUserNameLabel != null) {
            String fullName = (nullToEmpty(selectedUser.getPrenom()) + " " + nullToEmpty(selectedUser.getNom())).trim();
            deleteUserNameLabel.setText(fullName.isEmpty() ? "—" : fullName);
        }
    }

    @FXML
    private void handleConfirmDelete() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        User currentAdmin = SessionManager.getCurrentUser();

        if (currentAdmin != null && currentAdmin.getId() == selectedUser.getId()) {
            setDeleteMessage("Impossible de supprimer votre propre compte.", false);
            return;
        }

        if ("ADMIN".equalsIgnoreCase(selectedUser.getType())) {
            setDeleteMessage("Suppression d'un administrateur interdite.", false);
            return;
        }

        boolean deleted = userService.deleteUser(selectedUser.getId());

        if (deleted) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
            selectedUser = null;
            openPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN");
        } else {
            setDeleteMessage("Erreur lors de la suppression.", false);
        }
    }

    @FXML
    private void handleCancelDelete() {
        selectedUser = null;
        openPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN");
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private void openPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            if (Main.getPrimaryStage() != null) {
                Main.getPrimaryStage().setTitle(title);
                Main.getPrimaryStage().setScene(scene);
                Main.getPrimaryStage().centerOnScreen();
                Main.getPrimaryStage().show();
            } else {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d’ouvrir la page : " + fxmlPath);
        }
    }

    private void setPageMessage(String message, boolean success) {
        if (pageMessageLabel != null) {
            pageMessageLabel.setText(message);
            pageMessageLabel.setStyle(success
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void setFormMessage(String message, boolean success) {
        if (formMessageLabel != null) {
            formMessageLabel.setText(message);
            formMessageLabel.setStyle(success
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void setDeleteMessage(String message, boolean success) {
        if (deleteMessageLabel != null) {
            deleteMessageLabel.setText(message);
            deleteMessageLabel.setStyle(success
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safeText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String valueOrDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatType(String type) {
        if (type == null || type.isBlank()) return "Citoyen";

        return switch (type.toUpperCase()) {
            case "ADMIN" -> "Administrateur";
            case "VALORIZER", "VALORISATEUR" -> "Valorisateur";
            default -> "Citoyen";
        };
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidName(String value) {
        return value.matches("[A-Za-zÀ-ÿ\\s-]{2,30}");
    }
}