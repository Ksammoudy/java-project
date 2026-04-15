package org.example;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.utils.SceneManager;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showDashboardAdmin();
        primaryStage.show();
    }

    public static void showLoginPage() {
        loadPage("/org/example/views/login.fxml", "Connexion | WasteWise TN", 1100, 700);
    }

    public static void showRegisterPage() {
        loadPage("/org/example/views/register.fxml", "Creer un compte | WasteWise TN", 1100, 750);
    }

    public static void showForgotPasswordPage() {
        loadPage("/org/example/views/forgot_password.fxml", "Mot de passe oublie | WasteWise TN", 900, 600);
    }

    public static void showResetPasswordPage() {
        loadPage("/org/example/views/reset_password.fxml", "Nouveau mot de passe | WasteWise TN", 900, 600);
    }

    public static void showDashboardAdmin() {
        loadPage("/org/example/views/dashboard_admin.fxml", "Dashboard Admin | WasteWise TN", 1280, 760);
    }

    public static void showDashboardCitizen() {
        loadPage("/org/example/views/dashboard_citizen.fxml", "Dashboard Citoyen | WasteWise TN", 1280, 760);
    }

    public static void showDashboardValorizer() {
        loadPage("/org/example/views/dashboard_valorizer.fxml", "Dashboard Valorisateur | WasteWise TN", 1280, 760);
    }

    public static void showDeclarationListPage() {
        loadPage("/org/example/views/declaration_dechet_list.fxml", "Declarations de dechets | WasteWise TN", 1280, 760);
    }

    public static void showDeclarationDetailPage() {
        loadPage("/org/example/views/declaration_dechet_detail.fxml", "Detail declaration | WasteWise TN", 1180, 760);
    }

    public static void showTypeDechetWorkshopPage() {
        loadPage("/org/example/views/type_dechet_workshop.fxml", "Gestion des types de dechet | WasteWise TN", 1280, 760);
    }

    public static void showTypeDechetFormPage() {
        loadPage("/org/example/views/type_dechet_form.fxml", "Formulaire type de dechet | WasteWise TN", 1180, 760);
    }

    public static void showTypeDechetDetailPage() {
        loadPage("/org/example/views/type_dechet_detail.fxml", "Detail type de dechet | WasteWise TN", 1180, 760);
    }

    public static void showProfileViewPage() {
        loadPage("/org/example/views/profile_view.fxml", "Mon Profil | WasteWise TN", 1200, 750);
    }

    public static void showProfileEditPage() {
        loadPage("/org/example/views/profile_edit.fxml", "Modifier Profil | WasteWise TN", 1200, 750);
    }

    public static void showAdminUsersPage() {
        loadPage("/org/example/views/admin_users.fxml", "Utilisateurs | WasteWise TN", 1200, 750);
    }

    public static void showAdminUserFormPage() {
        loadPage("/org/example/views/admin_user_form.fxml", "Formulaire utilisateur | WasteWise TN", 900, 650);
    }

    public static void showAdminUserDeletePage() {
        loadPage("/org/example/views/admin_user_delete.fxml", "Supprimer utilisateur | WasteWise TN", 700, 400);
    }

    private static void loadPage(String fxmlPath, String title, int width, int height) {
        try {
            SceneManager.loadInto(primaryStage, fxmlPath, title, width, height);
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
