package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controllers.AppShellController;
import org.example.controllers.TwoFactorVerifyController;
import org.example.controllers.UserController;
import org.example.controllers.ZonePollueeController;
import org.example.models.User;
import org.example.services.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Point d'entrée principal — AppShell avec sidebar fixe.
 * Les pages auth (login, register...) s'affichent en plein écran sans sidebar.
 * Toutes les pages internes chargent leur contenu dans la zone centrale de l'AppShell.
 */
public class Main extends Application {

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        showLoginPage();
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT DE L'APP SHELL (sidebar fixe)
    // ═══════════════════════════════════════════════════════

    /**
     * Charge l'AppShell pour un utilisateur connecté.
     * Configure la sidebar selon le rôle et charge le contenu initial.
     */
    public static void showAppShell(User user, String initialFragmentPath) {
        try {
            URL url = Main.class.getResource("/org/example/views/app_shell.fxml");
            if (url == null) throw new IOException("app_shell.fxml introuvable");

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AppShellController shell = loader.getController();
            shell.configureForUser(user);
            shell.loadContent(initialFragmentPath);

            Scene scene = new Scene(root, 1280, 800);
            applyGlobalStylesheet(scene);

            primaryStage.setTitle("WasteWise TN");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erreur chargement AppShell : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════
    // REDIRECTION PAR RÔLE
    // ═══════════════════════════════════════════════════════

    public static void redirectByUserType(User user) {
        if (user == null) { showLoginPage(); return; }
        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";
        switch (type) {
            case "ADMIN"                         -> showDashboardAdmin();
            case "VALORIZER", "VALORISATEUR"     -> showDashboardValorizer();
            case "ORGANISATEUR", "ORGANIZER"     -> showAppShell(user, "/org/example/views/event/OrganisateurHome.fxml");
            case "PARTENAIRE", "PARTNER"         -> showAppShell(user, "/org/example/views/usser/profile_view.fxml");
            default                              -> showDashboardCitizen(); // CITIZEN par défaut
        }
    }

    // ═══════════════════════════════════════════════════════
    // DASHBOARDS — chargent l'AppShell avec le bon fragment
    // ═══════════════════════════════════════════════════════

    public static void showDashboardAdmin() {
        User user = SessionManager.getCurrentUser();
        if (user == null) { showLoginPage(); return; }
        showAppShell(user, "/org/example/views/usser/dashboard_admin.fxml");
    }

    public static void showDashboardCitizen() {
        User user = SessionManager.getCurrentUser();
        if (user == null) { showLoginPage(); return; }
        showAppShell(user, "/org/example/views/dashboard_citizen.fxml");
    }

    public static void showDashboardValorizer() {
        User user = SessionManager.getCurrentUser();
        if (user == null) { showLoginPage(); return; }
        showAppShell(user, "/org/example/views/usser/dashboard_valorizer.fxml");
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION INTERNE — charge un fragment dans l'AppShell
    // ═══════════════════════════════════════════════════════

    public static void showDeclarationListPage() {
        loadFragment("/org/example/views/fragments/declarations_content.fxml");
    }

    public static void showTypeDechetWorkshopPage() {
        loadFragment("/org/example/views/fragments/type_dechet_content.fxml");
    }

    public static void showAdminUsersPage() {
        loadFragment("/org/example/views/fragments/users_content.fxml");
    }

    public static void showZonePollueeListPage() {
        loadFragment("/org/example/views/fragments/zones_content.fxml");
    }

    public static void showIndicateurImpactListPage() {
        loadFragment("/org/example/views/fragments/indicateurs_content.fxml");
    }

    public static void showMapPage() {
        loadFragment("/org/example/views/fragments/map_content.fxml");
    }

    public static void showQRDashboardPage() {
        loadFragment("/org/example/views/fragments/qr_content.fxml");
    }

    public static void showAdvancedDashboard() {
        loadFragment("/org/example/views/fragments/advanced_content.fxml");
    }

    public static void showProfileViewPage() {
        loadFragment("/org/example/views/fragments/profile_content.fxml");
    }

    public static void showProfileEditPage() {
        loadFragment("/org/example/views/fragments/profile_content.fxml");
    }

    // Déclarations citoyen — redirige vers la liste
    public static void showDeclarationCitizenFormPage()  { loadFragment("/org/example/views/fragments/declarations_content.fxml"); }
    public static void showCitizenMyDeclarationsPage()   { loadFragment("/org/example/views/fragments/declarations_content.fxml"); }
    public static void showCitizenStatisticsPage()       { loadFragment("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    public static void showCitizenNewsPage()             { loadFragment("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    public static void showCitizenAirQualityPage()       { loadFragment("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    public static void showCitizenWithdrawPage()         { loadFragment("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    public static void showCitizenSettingsPage()         { loadFragment("/org/example/views/fragments/profile_content.fxml"); }

    // Admin pages
    public static void showDeclarationListPage(String s) { showDeclarationListPage(); }
    public static void showTypeDechetFormPage()          { loadFragment("/org/example/views/fragments/type_dechet_content.fxml"); }
    public static void showTypeDechetDetailPage()        { loadFragment("/org/example/views/fragments/type_dechet_content.fxml"); }
    public static void showDeclarationDetailPage()       { loadFragment("/org/example/views/fragments/declarations_content.fxml"); }

    public static void showAdminUserEditPage(User user) {
        AppShellController shell = AppShellController.getInstance();
        if (shell != null) {
            shell.loadContent("/org/example/views/fragments/users_content.fxml");
        }
    }

    // ═══════════════════════════════════════════════════════
    // PAGES AUTH — plein écran sans sidebar
    // ═══════════════════════════════════════════════════════

    public static void showLoginPage() {
        loadFullScreen("/org/example/views/usser/login.fxml", "Connexion | WasteWise TN", 1180, 760);
    }

    public static void showRegisterPage() {
        loadFullScreen("/org/example/views/usser/register.fxml", "Inscription | WasteWise TN", 1200, 750);
    }

    public static void showForgotPasswordPage() {
        loadFullScreen("/org/example/views/usser/forgot_password.fxml", "Mot de passe oublié | WasteWise TN", 900, 600);
    }

    public static void showResetPasswordPage() {
        loadFullScreen("/org/example/views/usser/reset_password.fxml", "Réinitialisation | WasteWise TN", 900, 600);
    }

    public static void showTwoFactorVerifyPage(User user) {
        loadFullScreen("/org/example/views/two_factor_verify.fxml", "Vérification 2FA | WasteWise TN", 450, 320);
    }

    public static void showTwoFactorSetupPage() {
        loadFullScreen("/org/example/views/two_factor_setup.fxml", "Activation 2FA | WasteWise TN", 650, 620);
    }

    public static void showFaceLoginPage() {
        loadFullScreen("/org/example/views/face_login.fxml", "Connexion par visage | WasteWise TN", 900, 600);
    }

    public static void showFaceEnrollPage() {
        loadFullScreen("/org/example/views/face_enroll.fxml", "Enregistrement visage | WasteWise TN", 900, 550);
    }

    // ═══════════════════════════════════════════════════════
    // CHATBOT — fenêtre séparée
    // ═══════════════════════════════════════════════════════

    public static void showChatbot() {
        try {
            URL url = Main.class.getResource("/org/example/views/chatbot.fxml");
            if (url == null) return;
            FXMLLoader loader = new FXMLLoader(url);
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 500, 700);
            applyGlobalStylesheet(scene);
            javafx.stage.Stage chatbotStage = new javafx.stage.Stage();
            chatbotStage.setTitle("WasteWise Assistant IA");
            chatbotStage.setScene(scene);
            chatbotStage.setMinWidth(450);
            chatbotStage.setMinHeight(600);
            chatbotStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS PRIVÉS
    // ═══════════════════════════════════════════════════════

    /**
     * Charge un fragment dans la zone centrale de l'AppShell existant.
     * Si l'AppShell n'est pas actif, redirige vers le dashboard approprié.
     */
    private static void loadFragment(String fragmentPath) {
        AppShellController shell = AppShellController.getInstance();
        if (shell != null) {
            shell.loadContent(fragmentPath);
        } else {
            // AppShell pas encore chargé — charger avec l'utilisateur courant
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                showAppShell(user, fragmentPath);
            } else {
                showLoginPage();
            }
        }
    }

    /**
     * Charge une page en plein écran (sans sidebar) — pour auth.
     */
    private static void loadFullScreen(String fxmlPath, String title, int width, int height) {
        try {
            URL url = Main.class.getResource(fxmlPath);
            if (url == null) {
                System.err.println("FXML introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            applyGlobalStylesheet(scene);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur chargement page : " + fxmlPath);
            e.printStackTrace();
        }
    }

    private static void applyGlobalStylesheet(Scene scene) {
        URL cssUrl = Main.class.getResource("/org/example/styles/style.css");
        if (cssUrl != null) {
            String url = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(url)) {
                scene.getStylesheets().add(url);
            }
        }
    }

    // Méthodes conservées pour compatibilité avec les controllers existants
    public static void navigateTo(String fxmlPath, String title, double width, double height) {
        loadFullScreen(fxmlPath, title, (int) width, (int) height);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

