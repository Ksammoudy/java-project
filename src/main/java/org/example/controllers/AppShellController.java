package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Contrôleur de la coquille principale (AppShell).
 * La sidebar reste fixe ; seul le contenu central change.
 *
 * Accès par rôle (selon spécification) :
 *  CITOYEN      : profil, offres(accueil+réponses), participations+badges, zones+carte+IA
 *  VALORISATEUR : profil, offres(accueil+appels), zones+dashboard avancé
 *  ADMIN        : profil, modération offres, events+participations, zones+indicateurs+QR+advanced, utilisateurs
 *  ORGANISATEUR : profil, mes events, créer event, participations
 *  PARTENAIRE   : profil uniquement
 */
public class AppShellController {

    // ── Sections sidebar ──
    @FXML private VBox sectionCitoyen;
    @FXML private VBox sectionValorisateur;
    @FXML private VBox sectionAdmin;
    @FXML private VBox sectionOrganisateur;
    @FXML private VBox sectionPartenaire;

    // ── Footer ──
    @FXML private Label footerName;
    @FXML private Label footerRole;
    @FXML private Label brandSubtitle;

    // ── Zone centrale ──
    @FXML private StackPane contentArea;

    // ── Boutons citoyen ──
    @FXML private Button cit_home;
    @FXML private Button cit_declare;
    @FXML private Button cit_myDeclarations;
    @FXML private Button cit_statistics;
    @FXML private Button cit_news;
    @FXML private Button cit_airQuality;
    @FXML private Button cit_withdraw;
    @FXML private Button cit_settings;
    @FXML private Button cit_profil;
    @FXML private Button cit_offres;
    @FXML private Button cit_reponses;
    @FXML private Button cit_events;
    @FXML private Button cit_participations;
    @FXML private Button cit_badges;
    @FXML private Button cit_zones;
    @FXML private Button cit_carte;
    @FXML private Button cit_ia;

    // ── Boutons valorisateur ──
    @FXML private Button val_profil;
    @FXML private Button val_offres;
    @FXML private Button val_appels;
    @FXML private Button val_zones;
    @FXML private Button val_advanced;
    // ── Boutons admin ──

    // ── Boutons admin ──
    @FXML private Button adm_profil;
    @FXML private Button adm_offres_mod;
    @FXML private Button adm_event_dashboard;
    @FXML private Button adm_event_list;
    @FXML private Button adm_participations;
    @FXML private Button adm_event_calendar;
    @FXML private Button adm_zones;
    @FXML private Button adm_indicateurs;
    @FXML private Button adm_qr;
    @FXML private Button adm_advanced;
    @FXML private Button adm_users;

    // ── Boutons organisateur ──
    @FXML private Button org_profil;
    @FXML private Button org_events;
    @FXML private Button org_ajouter;
    @FXML private Button org_participations;

    // ── Boutons partenaire ──
    @FXML private Button par_profil;

    // ── Référence statique ──
    private static AppShellController instance;
    private Button currentActive;

    @FXML
    public void initialize() {
        instance = this;
    }

    public static AppShellController getInstance() {
        return instance;
    }

    // ═══════════════════════════════════════════════════════
    // CONFIGURATION SIDEBAR SELON RÔLE
    // ═══════════════════════════════════════════════════════

    public void configureForUser(User user) {
        if (user == null) return;

        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        String nom    = user.getNom()    != null ? user.getNom()    : "";
        String name   = (prenom + " " + nom).trim();

        footerName.setText(name.isEmpty() ? (user.getEmail() != null ? user.getEmail() : "Utilisateur") : name);

        // Masquer toutes les sections
        hide(sectionCitoyen);
        hide(sectionValorisateur);
        hide(sectionAdmin);
        hide(sectionOrganisateur);
        hide(sectionPartenaire);

        switch (type) {
            case "CITIZEN", "CITOYEN" -> {
                show(sectionCitoyen);
                footerRole.setText("Citoyen");
                brandSubtitle.setText("Espace Citoyen");
                nav_cit_home();
            }
            case "VALORIZER", "VALORISATEUR" -> {
                show(sectionValorisateur);
                footerRole.setText("Valorisateur");
                brandSubtitle.setText("Espace Valorisateur");
                nav_val_profil();
            }
            case "ADMIN" -> {
                show(sectionAdmin);
                footerRole.setText("Administrateur");
                brandSubtitle.setText("Espace Admin");
                nav_adm_profil();
            }
            case "ORGANISATEUR", "ORGANIZER" -> {
                show(sectionOrganisateur);
                footerRole.setText("Organisateur");
                brandSubtitle.setText("Espace Organisateur");
                nav_org_profil();
            }
            case "PARTENAIRE", "PARTNER" -> {
                show(sectionPartenaire);
                footerRole.setText("Partenaire");
                brandSubtitle.setText("Espace Partenaire");
                nav_par_profil();
            }
            default -> {
                // Rôle inconnu → afficher section citoyen par défaut
                show(sectionCitoyen);
                footerRole.setText(type.isEmpty() ? "Utilisateur" : type);
                brandSubtitle.setText("WasteWise TN");
                nav_cit_home();
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT DU CONTENU CENTRAL
    // ═══════════════════════════════════════════════════════

    public void loadContent(String fxmlPath) {
        loadContent(fxmlPath, null);
    }

    public <T> void loadContent(String fxmlPath, Consumer<T> controllerInit) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("Fragment introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Node content = extractContentNode(root);

            if (controllerInit != null) {
                T ctrl = loader.getController();
                if (ctrl != null) controllerInit.accept(ctrl);
            }

            contentArea.getChildren().setAll(content);

        } catch (IOException e) {
            System.err.println("Erreur chargement fragment : " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — CITOYEN
    // Profil, Offres(accueil+réponses), Participations+Badges, Zones+Carte+IA
    // ═══════════════════════════════════════════════════════

    @FXML public void nav_cit_home() {
        setActive(cit_home);
        loadContent("/org/example/views/dashboard_citizen.fxml");
    }

    /**
     * Evite la double sidebar quand on charge une vue complete (BorderPane)
     * dans l'AppShell: on ne garde que la zone center.
     */
    private Node extractContentNode(Parent root) {
        if (root instanceof BorderPane borderPane) {
            Node center = borderPane.getCenter();
            if (center != null) {
                borderPane.setCenter(null);
                return center;
            }
        }
        return root;
    }

    @FXML public void nav_cit_declare() {
        setActive(cit_declare);
        loadContent("/org/example/views/declaration_dechet_citizen_form.fxml");
    }

    @FXML public void nav_cit_myDeclarations() {
        setActive(cit_myDeclarations);
        loadContent("/org/example/views/citizen_my_declarations.fxml");
    }

    @FXML public void nav_cit_statistics() {
        setActive(cit_statistics);
        loadContent("/org/example/views/citizen_statistics.fxml");
    }

    @FXML public void nav_cit_news() {
        setActive(cit_news);
        loadContent("/org/example/views/citizen_news.fxml");
    }

    @FXML public void nav_cit_airQuality() {
        setActive(cit_airQuality);
        loadContent("/org/example/views/citizen_air_quality.fxml");
    }

    @FXML public void nav_cit_withdraw() {
        setActive(cit_withdraw);
        loadContent("/org/example/views/citizen_withdraw.fxml");
    }

    @FXML public void nav_cit_settings() {
        setActive(cit_settings);
        loadContent("/org/example/views/citizen_settings.fxml");
    }

    @FXML public void nav_cit_profil() {
        setActive(cit_profil);
        loadContent("/org/example/views/usser/profile_view.fxml");
    }

    @FXML public void nav_cit_offres() {
        setActive(cit_offres);
        loadContent("/fxml/Dashboard.fxml");
    }

    @FXML public void nav_cit_reponses() {
        setActive(cit_reponses);
        loadContent("/fxml/reponseoffre/ReponseOffreList.fxml");
    }

    @FXML public void nav_cit_events() {
        setActive(cit_events);
        loadContent("/org/example/views/event/AfficherEvenement.fxml");
    }

    @FXML public void nav_cit_participations() {
        setActive(cit_participations);
        loadContent("/org/example/views/event/AfficherParticipations.fxml");
    }

    @FXML public void nav_cit_badges() {
        setActive(cit_badges);
        loadContent("/org/example/views/event/BadgesFront.fxml");
    }

    @FXML public void nav_cit_zones() {
        setActive(cit_zones);
        loadContent("/org/example/views/zone_polluee_list.fxml");
    }

    @FXML public void nav_cit_carte() {
        setActive(cit_carte);
        loadContent("/org/example/views/map.fxml");
    }

    @FXML public void nav_cit_ia() {
        setActive(cit_ia);
        Main.showChatbot();
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — VALORISATEUR
    // Profil, Offres(accueil+appels), Zones+Dashboard avancé
    // ═══════════════════════════════════════════════════════

    @FXML public void nav_val_profil() {
        setActive(val_profil);
        loadContent("/org/example/views/usser/profile_view.fxml");
    }

    @FXML public void nav_val_offres() {
        setActive(val_offres);
        loadContent("/fxml/Dashboard.fxml");
    }

    @FXML public void nav_val_appels() {
        setActive(val_appels);
        loadContent("/fxml/appeloffre/AppelOffreList.fxml");
    }

    @FXML public void nav_val_zones() {
        // Zones polluées (CRUD valorisateur)
        setActive(val_zones);
        loadContent("/org/example/views/zone_polluee_list.fxml");
    }

    @FXML public void nav_val_advanced() {
        // Dashboard avancé
        setActive(val_advanced);
        loadContent("/org/example/views/advanced_dashboard.fxml");
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — ADMIN
    // Profil, Modération offres, Events+Participations,
    // Zones+Indicateurs+QR+Advanced, Utilisateurs
    // ═══════════════════════════════════════════════════════

    @FXML public void nav_adm_profil() {
        setActive(adm_profil);
        loadContent("/org/example/views/usser/profile_view.fxml");
    }

    @FXML public void nav_adm_offres_mod() {
        setActive(adm_offres_mod);
        loadContent("/fxml/admin/AdminDashboard.fxml");
    }

    @FXML public void nav_adm_event_dashboard() {
        setActive(adm_event_dashboard);
        loadContent("/org/example/views/event/Dashboard.fxml");
    }

    @FXML public void nav_adm_event_list() {
        setActive(adm_event_list);
        loadContent("/org/example/views/event/AfficherEvenement.fxml");
    }

    @FXML public void nav_adm_event_calendar() {
        setActive(adm_event_calendar);
        loadContent("/org/example/views/event/Calendrier.fxml");
    }

    @FXML public void nav_adm_participations() {
        // Supervision participations
        setActive(adm_participations);
        loadContent("/org/example/views/event/AfficherParticipations.fxml");
    }

    @FXML public void nav_adm_zones() {
        // Zones polluées
        setActive(adm_zones);
        loadContent("/org/example/views/zone_polluee_list.fxml");
    }

    @FXML public void nav_adm_indicateurs() {
        // Indicateurs d'impact
        setActive(adm_indicateurs);
        loadContent("/org/example/views/indicateur_impact_list.fxml");
    }

    @FXML public void nav_adm_qr() {
        // Dashboard scans QR
        setActive(adm_qr);
        loadContent("/org/example/views/qr_dashboard.fxml");
    }

    @FXML public void nav_adm_advanced() {
        // Dashboard avancé
        setActive(adm_advanced);
        loadContent("/org/example/views/advanced_dashboard.fxml");
    }

    @FXML public void nav_adm_users() {
        // Gestion utilisateurs (désactiver/activer)
        setActive(adm_users);
        loadContent("/org/example/views/usser/admin_users.fxml");
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — ORGANISATEUR
    // Profil, Mes events, Créer event, Participations
    // ═══════════════════════════════════════════════════════

    @FXML public void nav_org_profil() {
        setActive(org_profil);
        loadContent("/org/example/views/usser/profile_view.fxml");
    }

    @FXML public void nav_org_events() {
        // Ses événements (OrganisateurHome)
        setActive(org_events);
        loadContent("/org/example/views/event/OrganisateurHome.fxml");
    }

    @FXML public void nav_org_ajouter() {
        // Créer un événement
        setActive(org_ajouter);
        loadContent("/org/example/views/event/AjouterEvenement.fxml");
    }

    @FXML public void nav_org_participations() {
        // Superviser les participations de son event
        setActive(org_participations);
        loadContent("/org/example/views/event/AfficherParticipations.fxml");
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION — PARTENAIRE
    // Profil uniquement
    // ═══════════════════════════════════════════════════════

    @FXML public void nav_par_profil() {
        setActive(par_profil);
        loadContent("/org/example/views/usser/profile_view.fxml");
    }

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

    private void setActive(Button btn) {
        // Retirer active du bouton précédent
        if (currentActive != null) {
            currentActive.getStyleClass().removeAll("active");
            if (!currentActive.getStyleClass().contains("sidebar-nav")) {
                currentActive.getStyleClass().add("sidebar-nav");
            }
        }
        if (btn != null) {
            btn.getStyleClass().removeAll("muted");
            btn.getStyleClass().add("active");
            currentActive = btn;
        }
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
