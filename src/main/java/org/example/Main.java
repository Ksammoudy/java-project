package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.controllers.TwoFactorVerifyController;
import org.example.controllers.UserController;
import org.example.models.User;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class Main extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        showLoginPage();
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
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
        loadPage("/org/example/views/reset_password.fxml", "Réinitialisation du mot de passe", 900, 600);
    }

    public static void showTwoFactorVerifyPage(User user) {
        loadPageWithController(
                "/org/example/views/two_factor_verify.fxml",
                "Vérification 2FA",
                450,
                320,
                (TwoFactorVerifyController controller) -> controller.setUser(user)
        );
    }

    public static void showTwoFactorSetupPage() {
        loadPage("/org/example/views/two_factor_setup.fxml", "Activation Google Authenticator", 650, 620);
    }

    public static void showFaceLoginPage() {
        loadPage("/org/example/views/face_login.fxml", "Connexion par visage", 900, 600);
    }

    public static void showFaceEnrollPage() {
        loadPage("/org/example/views/face_enroll.fxml", "Enregistrement du visage", 900, 550);
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

    public static void redirectByUserType(User user) {
        if (user == null) {
            showError("Utilisateur introuvable.");
            showLoginPage();
            return;
        }

        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";

        switch (type) {
            case "ADMIN":
                showDashboardAdmin();
                break;

            case "VALORIZER":
            case "VALORISATEUR":
                showDashboardValorizer();
                break;

            case "CITIZEN":
            case "CITOYEN":
                showDashboardCitizen();
                break;

            default:
                showError("Type utilisateur inconnu : " + type);
                showLoginPage();
                break;
        }
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
        loadPageWithController(
                "/org/example/views/admin_user_edit.fxml",
                "Modifier utilisateur",
                950,
                650,
                (UserController controller) -> controller.setSelectedUser(user)
        );
    }

    // =========================
    // LOAD GENERIC PAGE
    // =========================
    private static void loadPage(String fxmlPath, String title, int width, int height) {
        try {
            checkPrimaryStage();

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
            showError("Erreur lors du chargement de la page : " + e.getMessage());
        }
    }

    private static <T> void loadPageWithController(
            String fxmlPath,
            String title,
            int width,
            int height,
            Consumer<T> controllerInitializer
    ) {
        try {
            checkPrimaryStage();

            URL fxmlUrl = Main.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                showError("FXML introuvable : " + fxmlPath);
                System.out.println("FXML introuvable : " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            T controller = loader.getController();
            if (controller != null && controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            Scene scene = new Scene(root, width, height);
            applyGlobalCss(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page : " + e.getMessage());
        }
    }

    // =========================
    // CSS GLOBAL
    // =========================
    private static void applyGlobalCss(Scene scene) {
        if (scene == null) {
            return;
        }

        URL cssUrl = Main.class.getResource("/org/example/styles/style.css");
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } else {
            System.out.println("CSS introuvable : /org/example/styles/style.css");
        }
    }

    // =========================
    // TOOLS
    // =========================
    private static void checkPrimaryStage() {
        if (primaryStage == null) {
            throw new IllegalStateException("Le Stage principal n'est pas encore initialisé.");
        }
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}