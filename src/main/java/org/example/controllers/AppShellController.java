package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

import java.io.IOException;
import java.net.URL;

/**
 * Contrôleur de la coquille principale (AppShell).
 * La sidebar reste fixe ; seul le contenu central change.
 */
public class AppShellController {

    // ── Sidebar sections ──
    @FXML private VBox sectionAdmin;
    @FXML private VBox sectionCitoyen;
    @FXML private VBox sectionValorisateur;

    // ── Footer ──
    @FXML private Label footerName;
    @FXML private Label footerRole;
    @FXML private Label brandSubtitle;

    // ── Zone centrale ──
    @FXML private StackPane contentArea;

    // ── Boutons actifs (pour highlight) ──
    @FXML private Button btnAdminDashboard;
    @FXML private Button btnTypeDechet;
    @FXML private Button btnDeclarations;
    @FXML private Button btnUsers;
    @FXML private Button btnZones;
    @FXML private Button btnIndicateurs;
    @FXML private Button btnCarte;
    @FXML private Button btnQR;
    @FXML private Button btnAdvanced;
    @FXML private Button btnChatbot;

    @FXML private Button btnCitoyenDashboard;
    @FXML private Button btnDeclarer;
    @FXML private Button btnMesDeclarations;
    @FXML private Button btnStatistiques;
    @FXML private Button btnProfil;

    @FXML private Button btnValoDashboard;
    @FXML private Button btnDechetsRecus;
    @FXML private Button btnValorisation;
    @FXML private Button btnValoStats;
    @FXML private Button btnValoProfile;

    // ── Référence statique pour accès depuis Main ──
    private static AppShellController instance;

    @FXML
    public void initialize() {
        instance = this;
    }

    public static AppShellController getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════
    // CONFIGURATION DE LA SIDEBAR SELON LE RÔLE
    // ═══════════════════════════════════════════════════════

    public void configureForUser(User user) {
        if (user == null) return;

        String type = user.getType() != null ? user.getType().toUpperCase() : "";
        String name = ((user.getPrenom() != null ? user.getPrenom() : "") + " "
                + (user.getNom() != null ? user.getNom() : "")).trim();

        footerName.setText(name.isEmpty() ? user.getEmail() : name);

        // Masquer toutes les sections
        hide(sectionAdmin);
        hide(sectionCitoyen);
        hide(sectionValorisateur);

        switch (type) {
            case "ADMIN" -> {
                show(sectionAdmin);
                footerRole.setText("Administrateur");
                brandSubtitle.setText("Espace Admin");
            }
            case "CITIZEN", "CITOYEN" -> {
                show(sectionCitoyen);
                footerRole.setText("Citoyen");
                brandSubtitle.setText("Espace Citoyen");
            }
            case "VALORIZER", "VALORISATEUR" -> {
                show(sectionValorisateur);
                footerRole.setText("Valorisateur");
                brandSubtitle.setText("Espace Valorisateur");
            }
            default -> {
                show(sectionAdmin);
                footerRole.setText(type);
                brandSubtitle.setText("WasteWise TN");
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT DU CONTENU CENTRAL
    // ═══════════════════════════════════════════════════════

    public void loadContent(String fxmlPath) {
        loadContent(fxmlPath, null);
    }

    public <T> void loadContent(String fxmlPath, java.util.function.Consumer<T> controllerInit) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("Fragment FXML introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Node content = loader.load();

            if (controllerInit != null) {
                T controller = loader.getController();
                if (controller != null) controllerInit.accept(controller);
            }

            contentArea.getChildren().setAll(content);

        } catch (IOException e) {
            System.err.println("Erreur chargement fragment : " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — ADMIN
    // ═══════════════════════════════════════════════════════

    @FXML public void navAdminDashboard()  { setActive(btnAdminDashboard);  loadContent("/org/example/views/fragments/dashboard_admin_content.fxml"); }
    @FXML public void navTypeDechet()      { setActive(btnTypeDechet);       loadContent("/org/example/views/fragments/type_dechet_content.fxml"); }
    @FXML public void navDeclarations()    { setActive(btnDeclarations);     loadContent("/org/example/views/fragments/declarations_content.fxml"); }
    @FXML public void navUsers()           { setActive(btnUsers);            loadContent("/org/example/views/fragments/users_content.fxml"); }
    @FXML public void navZones()           { setActive(btnZones);            loadContent("/org/example/views/fragments/zones_content.fxml"); }
    @FXML public void navIndicateurs()     { setActive(btnIndicateurs);      loadContent("/org/example/views/fragments/indicateurs_content.fxml"); }
    @FXML public void navCarte()           { setActive(btnCarte);            loadContent("/org/example/views/fragments/map_content.fxml"); }
    @FXML public void navQR()              { setActive(btnQR);               loadContent("/org/example/views/fragments/qr_content.fxml"); }
    @FXML public void navAdvanced()        { setActive(btnAdvanced);         loadContent("/org/example/views/fragments/advanced_content.fxml"); }
    @FXML public void navChatbot()         { Main.showChatbot(); }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — CITOYEN
    // ═══════════════════════════════════════════════════════

    @FXML public void navCitoyenDashboard() { setActive(btnCitoyenDashboard); loadContent("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    @FXML public void navDeclarer()         { setActive(btnDeclarer);          loadContent("/org/example/views/fragments/declarations_content.fxml"); }
    @FXML public void navMesDeclarations()  { setActive(btnMesDeclarations);   loadContent("/org/example/views/fragments/declarations_content.fxml"); }
    @FXML public void navStatistiques()     { setActive(btnStatistiques);      loadContent("/org/example/views/fragments/citizen_dashboard_content.fxml"); }
    @FXML public void navProfil()           { setActive(btnProfil);            loadContent("/org/example/views/fragments/profile_content.fxml"); }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — VALORISATEUR
    // ═══════════════════════════════════════════════════════

    @FXML public void navValoDashboard()  { setActive(btnValoDashboard);  loadContent("/org/example/views/fragments/valorizer_dashboard_content.fxml"); }
    @FXML public void navDechetsRecus()   { setActive(btnDechetsRecus);   loadContent("/org/example/views/fragments/declarations_content.fxml"); }
    @FXML public void navValorisation()   { setActive(btnValorisation);   loadContent("/org/example/views/fragments/valorizer_dashboard_content.fxml"); }
    @FXML public void navValoStats()      { setActive(btnValoStats);      loadContent("/org/example/views/fragments/valorizer_dashboard_content.fxml"); }
    @FXML public void navValoProfile()    { setActive(btnValoProfile);    loadContent("/org/example/views/fragments/profile_content.fxml"); }

    // ═══════════════════════════════════════════════════════
    // DÉCONNEXION
    // ═══════════════════════════════════════════════════════

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        instance = null;
        Main.showLoginPage();
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════

    private void setActive(Button active) {
        // Retirer active de tous les boutons
        for (Button btn : allNavButtons()) {
            btn.getStyleClass().removeAll("active");
            if (!btn.getStyleClass().contains("sidebar-nav")) {
                btn.getStyleClass().add("sidebar-nav");
            }
        }
        if (active != null) {
            active.getStyleClass().removeAll("muted");
            active.getStyleClass().add("active");
        }
    }

    private Button[] allNavButtons() {
        return new Button[]{
            btnAdminDashboard, btnTypeDechet, btnDeclarations, btnUsers,
            btnZones, btnIndicateurs, btnCarte, btnQR, btnAdvanced, btnChatbot,
            btnCitoyenDashboard, btnDeclarer, btnMesDeclarations, btnStatistiques, btnProfil,
            btnValoDashboard, btnDechetsRecus, btnValorisation, btnValoStats, btnValoProfile
        };
    }

    private void show(VBox section) {
        section.setVisible(true);
        section.setManaged(true);
    }

    private void hide(VBox section) {
        section.setVisible(false);
        section.setManaged(false);
    }
}
