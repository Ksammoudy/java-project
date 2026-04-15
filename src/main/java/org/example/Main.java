package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.controllers.UserController;
import org.example.models.User;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginPage();
        primaryStage.show();
    }

    // =========================
    // AUTH
    // =========================
    public static void showLoginPage() {
        loadPage("/org/example/views/login.fxml", "Connexion", 1100, 700);
    }

    public static void showRegisterPage() {
        loadPage("/org/example/views/register.fxml", "Inscription", 1100, 750);
    }

    public static void showForgotPasswordPage() {
        loadPage("/org/example/views/forgot_password.fxml", "Mot de passe oublié", 900, 600);
    }

    public static void showResetPasswordPage() {
        loadPage("/org/example/views/reset_password.fxml", "Réinitialisation", 900, 600);
    }

    // =========================
    // DASHBOARDS
    // =========================
    public static void showDashboardAdmin() {
        loadPage("/org/example/views/dashboard_admin.fxml", "Dashboard Admin", 1200, 800);
    }

    public static void showDashboardValorizer() {
        loadPage("/org/example/views/dashboard_valorizer.fxml", "Dashboard Valorizer", 1200, 800);
    }

    public static void showDashboardCitizen() {
        loadPage("/org/example/views/dashboard_citizen.fxml", "Dashboard Citizen", 1200, 800);
    }

    // =========================
    // PROFILE
    // =========================
    public static void showProfileViewPage() {
        loadPage("/org/example/views/profile_view.fxml", "Mon profil", 1100, 700);
    }

    public static void showProfileEditPage() {
        loadPage("/org/example/views/profile_edit.fxml", "Modifier profil", 1100, 700);
    }

    // =========================
    // USERS ADMIN
    // =========================
    public static void showAdminUsersPage() {
        loadPage("/org/example/views/admin_users.fxml", "Gestion des utilisateurs", 1200, 750);
    }

    public static void showAdminUserEditPage(User user) {
        try {
            URL fxmlUrl = Main.class.getResource("/org/example/views/admin_user_edit.fxml");

            if (fxmlUrl == null) {
                showError("FXML introuvable : /org/example/views/admin_user_edit.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            UserController controller = loader.getController();
            controller.setSelectedUser(user);

            Scene scene = new Scene(root, 950, 650);
            applyGlobalCss(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Modifier utilisateur");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur chargement page modification utilisateur : " + e.getMessage());
        }
    }

    // =========================
    // LOAD GENERIC PAGE
    // =========================
    private static void loadPage(String fxmlPath, String title, int width, int height) {
        try {
            URL fxmlUrl = Main.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                showError("FXML introuvable : " + fxmlPath);
                System.out.println("FXML introuvable : " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            applyGlobalCss(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur chargement page : " + e.getMessage());
        }
    }

    // =========================
    // CSS GLOBAL
    // =========================
    private static void applyGlobalCss(Scene scene) {
        URL cssUrl = Main.class.getResource("/org/example/styles/style.css");

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS introuvable : /org/example/styles/style.css");
        }
    }

    // =========================
    // ALERT ERROR
    // =========================
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}