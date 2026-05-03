package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.controllers.TwoFactorVerifyController;
import org.example.controllers.UserController;
import org.example.controllers.ZonePollueeController;
import org.example.models.User;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Point d'entrée : lanceur dev (accès direct aux dashboards) + navigation centralisée
 * pour toutes les vues FXML (auth conservée, non bloquante au démarrage).
 */
public class Main extends Application {

    private static Stage primaryStage;

    /**
     * Stage principal (lanceur + navigation). Expose pour les ecrans qui chargent du FXML hors {@link #navigateTo}.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showDevLauncher();
    }

    private void showDevLauncher() {
        // ── Titre ──
        Text title = new Text("WasteWise TN — Dev Launcher");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #2d3748;");

        Text subtitle = new Text("Choisissez un module a ouvrir directement");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: #718096;");

        // ── Ligne 1 : Dashboards utilisateurs ──
        Button btnAdmin = createBtn("Dashboard Admin", "#6c63ff");
        btnAdmin.setOnAction(e -> showDashboardAdmin());

        Button btnCitoyen = createBtn("Dashboard Citoyen", "#48bb78");
        btnCitoyen.setOnAction(e -> showDashboardCitizen());

        Button btnValorizer = createBtn("Dashboard Valorisateur", "#ed8936");
        btnValorizer.setOnAction(e -> showDashboardValorizer());

        // ── Ligne 2 : Modules métier ──
        Button btnEvenements = createBtn("Gestion Événements", "#e53e3e");
        btnEvenements.setOnAction(e -> navigateTo("/org/example/views/event/RoleSelection.fxml", "Événements", 1200, 750));

        Button btnZones = createBtn("Zones Polluées", "#2b6cb0");
        btnZones.setOnAction(e -> showZonePollueeListPage());

        Button btnOffres = createBtn("Appels d'Offres", "#744210");
        btnOffres.setOnAction(e -> navigateTo("/fxml/appeloffre/AppelOffreList.fxml", "Offres", 1200, 750));

        // ── Ligne 3 : Autres ──
        Button btnDeclarations = createBtn("Déclarations Déchets", "#276749");
        btnDeclarations.setOnAction(e -> showDeclarationListPage());

        Button btnMap = createBtn("Carte Interactive", "#553c9a");
        btnMap.setOnAction(e -> showMapPage());

        Button btnChatbot = createBtn("Chatbot IA", "#2c7a7b");
        btnChatbot.setOnAction(e -> showChatbot());

        // ── Login ──
        Button btnLogin = new Button("→ Aller au Login (auth normale)");
        btnLogin.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0aec0; "
                        + "-fx-font-size: 12px; -fx-border-color: #e2e8f0; "
                        + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"
        );
        btnLogin.setOnAction(e -> showLoginPage());

        // ── Layout ──
        HBox row1 = new HBox(12, btnAdmin, btnCitoyen, btnValorizer);
        row1.setStyle("-fx-alignment: center;");

        HBox row2 = new HBox(12, btnEvenements, btnZones, btnOffres);
        row2.setStyle("-fx-alignment: center;");

        HBox row3 = new HBox(12, btnDeclarations, btnMap, btnChatbot);
        row3.setStyle("-fx-alignment: center;");

        VBox root = new VBox(16, title, subtitle, row1, row2, row3, btnLogin);
        root.setStyle("-fx-alignment: center; -fx-padding: 40; -fx-background-color: #f7fafc;");

        Scene scene = new Scene(root, 900, 420);
        applyGlobalStylesheet(scene);
        primaryStage.setTitle("PiDev JavaFX — Dev Launcher");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createBtn(String label, String color) {
        Button btn = new Button(label);
        btn.setPrefWidth(200);
        btn.setPrefHeight(44);
        btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; "
                        + "-fx-font-size: 13px; -fx-font-weight: bold; "
                        + "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        return btn;
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

    /**
     * Remplace la scene du stage principal (une seule fenetre, navigation coherente).
     */
    public static void navigateTo(String fxmlPath, String title, double width, double height) {
        if (primaryStage == null) {
            System.err.println("Main.navigateTo: primaryStage non initialise.");
            return;
        }
        try {
            URL resource = Main.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML introuvable: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            applyGlobalStylesheet(scene);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Navigation impossible vers : " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void showLoginPage() {
        navigateTo("/org/example/views/usser/login.fxml", "Connexion", 1100, 700);
    }

    public static void showRegisterPage() {
        navigateTo("/org/example/views/usser/register.fxml", "Inscription", 1100, 750);
    }

    public static void showForgotPasswordPage() {
        navigateTo("/org/example/views/usser/forgot_password.fxml", "Mot de passe oublie", 900, 600);
    }

    public static void showResetPasswordPage() {
        navigateTo("/org/example/views/usser/reset_password.fxml", "Reinitialisation", 900, 600);
    }

    public static void showDashboardAdmin() {
        navigateTo("/org/example/views/usser/dashboard_admin.fxml", "Dashboard Admin", 1200, 750);
    }

    public static void showDashboardCitizen() {
        navigateTo("/org/example/views/dashboard_citizen.fxml", "Dashboard Citoyen", 1200, 750);
    }

    public static void showDashboardValorizer() {
        navigateTo("/org/example/views/usser/dashboard_valorizer.fxml", "Dashboard Valorisateur", 1200, 750);
    }

    public static void showAdminUsersPage() {
        navigateTo("/org/example/views/usser/admin_users.fxml", "Utilisateurs", 1200, 750);
    }

    public static void showAdminUserEditPage(User user) {
        navigateTo("/org/example/views/usser/admin_user_form.fxml", "Modifier utilisateur", 950, 650);
    }

    // =========================
    // AUTH AVANCÉE (2FA, Face)
    // =========================
    public static void showTwoFactorVerifyPage(User user) {
        navigateTo("/org/example/views/two_factor_verify.fxml", "Vérification 2FA", 450, 320);
    }

    public static void showTwoFactorSetupPage() {
        navigateTo("/org/example/views/two_factor_setup.fxml", "Activation Google Authenticator", 650, 620);
    }

    public static void showFaceLoginPage() {
        navigateTo("/org/example/views/face_login.fxml", "Connexion par visage", 900, 600);
    }

    public static void showFaceEnrollPage() {
        navigateTo("/org/example/views/face_enroll.fxml", "Enregistrement du visage", 900, 550);
    }

    public static void redirectByUserType(User user) {
        if (user == null) { showLoginPage(); return; }
        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";
        switch (type) {
            case "ADMIN" -> showDashboardAdmin();
            case "VALORIZER", "VALORISATEUR" -> showDashboardValorizer();
            default -> showDashboardCitizen();
        }
    }

    public static void showDeclarationListPage() {
        navigateTo("/org/example/views/declaration_dechet_list.fxml", "Declarations", 1200, 750);
    }

    public static void showTypeDechetWorkshopPage() {
        navigateTo("/org/example/views/type_dechet_workshop.fxml", "Types de dechets", 1200, 750);
    }

    public static void showTypeDechetFormPage() {
        navigateTo("/org/example/views/type_dechet_form.fxml", "Type de dechet", 1000, 700);
    }

    public static void showTypeDechetDetailPage() {
        navigateTo("/org/example/views/type_dechet_detail.fxml", "Detail type de dechet", 1000, 700);
    }

    public static void showDeclarationDetailPage() {
        navigateTo("/org/example/views/declaration_dechet_detail.fxml", "Detail declaration", 1000, 700);
    }

    public static void showProfileViewPage() {
        navigateTo("/org/example/views/usser/profile_view.fxml", "Profil", 1000, 700);
    }

    public static void showProfileEditPage() {
        navigateTo("/org/example/views/usser/profile_edit.fxml", "Modifier le profil", 1000, 700);
    }

    public static void showDeclarationCitizenFormPage() {
        navigateTo("/org/example/views/declaration_dechet_citizen_form.fxml", "Declarer un dechet", 1200, 750);
    }

    public static void showCitizenMyDeclarationsPage() {
        navigateTo("/org/example/views/citizen_my_declarations.fxml", "Mes declarations", 1200, 750);
    }

    public static void showCitizenStatisticsPage() {
        navigateTo("/org/example/views/citizen_statistics.fxml", "Statistiques", 1200, 750);
    }

    public static void showCitizenNewsPage() {
        navigateTo("/org/example/views/citizen_news.fxml", "Nouveautes", 1200, 750);
    }

    public static void showCitizenAirQualityPage() {
        navigateTo("/org/example/views/citizen_air_quality.fxml", "Air Quality", 1200, 750);
    }

    public static void showCitizenWithdrawPage() {
        navigateTo("/org/example/views/citizen_withdraw.fxml", "Withdraw", 1200, 750);
    }

    public static void showCitizenSettingsPage() {
        navigateTo("/org/example/views/citizen_settings.fxml", "Parametres", 1200, 750);
    }

    // =========================
    // ZONES POLLUÉES
    // =========================
    private static org.example.controllers.ZonePollueeController zonePollueeController;

    public static void showZonePollueeListPage() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/org/example/views/zone_polluee_list.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1200, 700);
            zonePollueeController = loader.getController();
            applyGlobalStylesheet(scene);
            primaryStage.setTitle("Gestion des Zones Polluées | WasteWise TN");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showIndicateurImpactListPage() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/org/example/views/indicateur_impact_list.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1200, 750);
            org.example.controllers.IndicateurImpactController controller = loader.getController();
            controller.setZoneRefreshCallback(() -> { if (zonePollueeController != null) zonePollueeController.loadZones(); });
            applyGlobalStylesheet(scene);
            primaryStage.setTitle("Indicateurs d'Impact | WasteWise TN");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showQRDashboardPage() {
        navigateTo("/org/example/views/qr_dashboard.fxml", "Dashboard Scans QR | WasteWise TN", 1200, 750);
    }

    public static void showMapPage() {
        navigateTo("/org/example/views/map.fxml", "Carte interactive | WasteWise TN", 1200, 750);
    }

    public static void showAdvancedDashboard() {
        navigateTo("/org/example/views/advanced_dashboard.fxml", "Dashboard Avancé | WasteWise TN", 1400, 850);
    }

    public static void showChatbot() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("/org/example/views/chatbot.fxml"));
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

    public static void main(String[] args) {
        launch(args);
    }
}
