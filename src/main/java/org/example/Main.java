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

import java.io.IOException;
import java.net.URL;

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
        Text title = new Text("Mode Developpement");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-fill: #2d3748;");

        Text subtitle = new Text("Choisissez un espace a ouvrir directement");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: #718096;");

        Button btnAdmin = new Button("Dashboard Admin");
        btnAdmin.setPrefWidth(200);
        btnAdmin.setPrefHeight(48);
        btnAdmin.setStyle(
                "-fx-background-color: #6c63ff; -fx-text-fill: white; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
        );
        btnAdmin.setOnAction(e -> showDashboardAdmin());

        Button btnCitoyen = new Button("Dashboard Citoyen");
        btnCitoyen.setPrefWidth(200);
        btnCitoyen.setPrefHeight(48);
        btnCitoyen.setStyle(
                "-fx-background-color: #48bb78; -fx-text-fill: white; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
        );
        btnCitoyen.setOnAction(e -> showDashboardCitizen());

        Button btnValorizer = new Button("Dashboard Valorisateur");
        btnValorizer.setPrefWidth(200);
        btnValorizer.setPrefHeight(48);
        btnValorizer.setStyle(
                "-fx-background-color: #ed8936; -fx-text-fill: white; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
        );
        btnValorizer.setOnAction(e -> showDashboardValorizer());

        Button btnLogin = new Button("Aller au Login (auth normale)");
        btnLogin.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0aec0; "
                        + "-fx-font-size: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;"
        );
        btnLogin.setOnAction(e -> showLoginPage());

        HBox buttons = new HBox(16, btnAdmin, btnCitoyen, btnValorizer);
        buttons.setStyle("-fx-alignment: center;");

        VBox root = new VBox(20, title, subtitle, buttons, btnLogin);
        root.setStyle("-fx-alignment: center; -fx-padding: 60; -fx-background-color: #f7fafc;");

        Scene scene = new Scene(root, 760, 320);
        applyGlobalStylesheet(scene);
        primaryStage.setTitle("PiDev JavaFX — Dev Launcher");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        navigateTo("/org/example/views/login.fxml", "Connexion", 900, 600);
    }

    public static void showRegisterPage() {
        navigateTo("/org/example/views/register.fxml", "Inscription", 900, 600);
    }

    public static void showForgotPasswordPage() {
        navigateTo("/org/example/views/forgot_password.fxml", "Mot de passe oublie", 900, 600);
    }

    public static void showResetPasswordPage() {
        navigateTo("/org/example/views/reset_password.fxml", "Reinitialisation", 900, 600);
    }

    public static void showDashboardAdmin() {
        navigateTo("/org/example/views/dashboard_admin.fxml", "Dashboard Admin", 1200, 750);
    }

    public static void showDashboardCitizen() {
        navigateTo("/org/example/views/dashboard_citizen.fxml", "Dashboard Citoyen", 1200, 750);
    }

    public static void showDashboardValorizer() {
        navigateTo("/org/example/views/dashboard_valorizer.fxml", "Dashboard Valorisateur", 1200, 750);
    }

    public static void showAdminUsersPage() {
        navigateTo("/org/example/views/admin_users.fxml", "Utilisateurs", 1200, 750);
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
        navigateTo("/org/example/views/profile_view.fxml", "Profil", 1000, 700);
    }

    public static void showProfileEditPage() {
        navigateTo("/org/example/views/profile_edit.fxml", "Modifier le profil", 1000, 700);
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

    public static void main(String[] args) {
        launch(args);
    }
}
