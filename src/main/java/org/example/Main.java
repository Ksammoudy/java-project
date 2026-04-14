package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        // =========================
        // LANCEMENT DE L'INTERFACE GRAPHIQUE
        // =========================
        primaryStage = stage;
        showZonePollueeListPage();  // ← Ouvre directement la page des zones polluées
        primaryStage.show();
    }

    // =========================
    // MENU INDICATEUR IMPACT (CONSOLE - conservé pour référence)
    // =========================
    public static void menuIndicateurImpact() {
        org.example.services.IndicateurImpactDAO dao = new org.example.services.IndicateurImpactDAO();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int choix;

        do {
            System.out.println("\n=== GESTION DES INDICATEURS D'IMPACT ===");
            System.out.println("1. Ajouter un indicateur");
            System.out.println("2. Modifier un indicateur");
            System.out.println("3. Supprimer un indicateur");
            System.out.println("4. Afficher tous les indicateurs");
            System.out.println("5. Retour");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> ajouterIndicateur(dao, scanner);
                case 2 -> modifierIndicateur(dao, scanner);
                case 3 -> supprimerIndicateur(dao, scanner);
                case 4 -> afficherTousIndicateurs(dao);
                case 5 -> System.out.println("Retour au menu principal.");
                default -> System.out.println("Choix invalide !");
            }
        } while (choix != 5);
    }

    private static void ajouterIndicateur(org.example.services.IndicateurImpactDAO dao, java.util.Scanner scanner) {
        System.out.print("Total kg récoltés : ");
        double kg = scanner.nextDouble();
        scanner.nextLine();
        if (!dao.isValidTotalKg(kg)) {
            System.out.println("❌ Erreur : Le kg doit être positif !");
            return;
        }

        System.out.print("CO₂ évité (kg) : ");
        double co2 = scanner.nextDouble();
        scanner.nextLine();
        if (!dao.isValidCo2(co2)) {
            System.out.println("❌ Erreur : Le CO₂ doit être positif !");
            return;
        }

        org.example.models.IndicateurImpact indicateur = new org.example.models.IndicateurImpact(kg, co2, java.time.LocalDateTime.now());
        dao.addIndicateur(indicateur);
        System.out.println("✅ Indicateur ajouté avec succès (ID: " + indicateur.getId() + ") !");
    }

    private static void modifierIndicateur(org.example.services.IndicateurImpactDAO dao, java.util.Scanner scanner) {
        System.out.print("ID de l'indicateur à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        org.example.models.IndicateurImpact indicateur = dao.getIndicateurById(id);
        if (indicateur == null) {
            System.out.println("❌ Indicateur non trouvé !");
            return;
        }

        System.out.print("Nouveau total kg (" + indicateur.getTotalKgRecoltes() + ") : ");
        String kgStr = scanner.nextLine();
        if (!kgStr.isEmpty()) {
            double kg = Double.parseDouble(kgStr);
            if (dao.isValidTotalKg(kg)) {
                indicateur.setTotalKgRecoltes(kg);
            } else {
                System.out.println("❌ Valeur invalide !");
                return;
            }
        }

        System.out.print("Nouveau CO₂ (" + indicateur.getCo2Evite() + ") : ");
        String co2Str = scanner.nextLine();
        if (!co2Str.isEmpty()) {
            double co2 = Double.parseDouble(co2Str);
            if (dao.isValidCo2(co2)) {
                indicateur.setCo2Evite(co2);
            } else {
                System.out.println("❌ Valeur invalide !");
                return;
            }
        }

        indicateur.setDateCalcul(java.time.LocalDateTime.now());
        dao.updateIndicateur(indicateur);
        System.out.println("✅ Indicateur modifié avec succès !");
    }

    private static void supprimerIndicateur(org.example.services.IndicateurImpactDAO dao, java.util.Scanner scanner) {
        System.out.print("ID de l'indicateur à supprimer : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        dao.deleteIndicateur(id);
        System.out.println("✅ Indicateur supprimé !");
    }

    private static void afficherTousIndicateurs(org.example.services.IndicateurImpactDAO dao) {
        java.util.List<org.example.models.IndicateurImpact> indicateurs = dao.getAllIndicateurs();
        if (indicateurs.isEmpty()) {
            System.out.println("Aucun indicateur trouvé.");
            return;
        }
        System.out.println("\n-------------------------------------------------------------------------------------");
        System.out.printf("%-5s %-20s %-15s %-20s%n", "ID", "Total Kg", "CO₂ évité", "Date calcul");
        System.out.println("-------------------------------------------------------------------------------------");
        for (org.example.models.IndicateurImpact i : indicateurs) {
            System.out.printf("%-5d %-20.2f %-15.2f %-20s%n",
                    i.getId(), i.getTotalKgRecoltes(), i.getCo2Evite(), i.getDateCalcul());
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }

    // =========================
    // TEST CONSOLE POUR ZONES POLLUÉES (conservé pour référence)
    // =========================
    public static void testZonePollueeConsole() {
        org.example.services.ZonePollueeDAO dao = new org.example.services.ZonePollueeDAO();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int choix;

        do {
            System.out.println("\n=== GESTION DES ZONES POLLUÉES (CONSOLE) ===");
            System.out.println("1. Ajouter une zone");
            System.out.println("2. Modifier une zone");
            System.out.println("3. Supprimer una zone");
            System.out.println("4. Afficher toutes les zones");
            System.out.println("5. Rechercher par nom");
            System.out.println("6. Trier par niveau (descendant)");
            System.out.println("7. Gérer les indicateurs d'impact");
            System.out.println("8. Quitter");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> ajouterZone(dao, scanner);
                case 2 -> modifierZone(dao, scanner);
                case 3 -> supprimerZone(dao, scanner);
                case 4 -> afficherToutesZones(dao);
                case 5 -> rechercherParNom(dao, scanner);
                case 6 -> trierParNiveau(dao);
                case 7 -> menuIndicateurImpact();
                case 8 -> System.out.println("Fin du test console.");
                default -> System.out.println("Choix invalide !");
            }
        } while (choix != 8);
    }

    private static void ajouterZone(org.example.services.ZonePollueeDAO dao, java.util.Scanner scanner) {
        System.out.print("Nom de la zone : ");
        String nom = scanner.nextLine();
        if (!dao.isValidNom(nom)) {
            System.out.println("❌ Erreur : Le nom est obligatoire !");
            return;
        }

        System.out.print("Coordonnées GPS (ex: 36.8065,10.1815) : ");
        String gps = scanner.nextLine();

        System.out.print("Niveau de pollution (1 à 10) : ");
        int niveau = scanner.nextInt();
        scanner.nextLine();
        if (!dao.isValidNiveau(niveau)) {
            System.out.println("❌ Erreur : Le niveau doit être entre 1 et 10 !");
            return;
        }

        org.example.services.IndicateurImpactDAO indicateurDAO = new org.example.services.IndicateurImpactDAO();
        java.util.List<org.example.models.IndicateurImpact> indicateurs = indicateurDAO.getAllIndicateurs();
        if (indicateurs.isEmpty()) {
            System.out.println("⚠️ Aucun indicateur disponible. Veuillez en créer un d'abord.");
            return;
        }

        System.out.println("\n📊 Indicateurs disponibles :");
        for (org.example.models.IndicateurImpact ind : indicateurs) {
            System.out.println("   ID " + ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg, CO₂: " + ind.getCo2Evite() + " kg");
        }

        System.out.print("\nID de l'indicateur associé : ");
        int indicateurId = scanner.nextInt();
        scanner.nextLine();

        org.example.models.IndicateurImpact indicateur = indicateurDAO.getIndicateurById(indicateurId);
        if (indicateur == null) {
            System.out.println("❌ Indicateur non trouvé !");
            return;
        }

        org.example.models.ZonePolluee zone = new org.example.models.ZonePolluee(nom, gps, niveau, java.time.LocalDateTime.now(), indicateur);
        dao.addZone(zone);
        System.out.println("✅ Zone ajoutée avec succès !");
    }

    private static void modifierZone(org.example.services.ZonePollueeDAO dao, java.util.Scanner scanner) {
        System.out.print("ID de la zone à modifier : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        org.example.models.ZonePolluee zone = dao.getZoneById(id);
        if (zone == null) {
            System.out.println("❌ Zone non trouvée !");
            return;
        }

        System.out.print("Nouveau nom (" + zone.getNomZone() + ") : ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) zone.setNomZone(nom);

        System.out.print("Nouveau niveau (" + zone.getNiveauPollution() + ") : ");
        String niveauStr = scanner.nextLine();
        if (!niveauStr.isEmpty()) {
            int niveau = Integer.parseInt(niveauStr);
            if (dao.isValidNiveau(niveau)) {
                zone.setNiveauPollution(niveau);
            } else {
                System.out.println("❌ Niveau invalide (1-10) !");
                return;
            }
        }

        System.out.print("Changer l'indicateur ? (o/N) : ");
        String changerIndicateur = scanner.nextLine();
        if (changerIndicateur.equalsIgnoreCase("o")) {
            org.example.services.IndicateurImpactDAO indicateurDAO = new org.example.services.IndicateurImpactDAO();
            java.util.List<org.example.models.IndicateurImpact> indicateurs = indicateurDAO.getAllIndicateurs();
            System.out.println("\n📊 Indicateurs disponibles :");
            for (org.example.models.IndicateurImpact ind : indicateurs) {
                System.out.println("   ID " + ind.getId() + " - " + ind.getTotalKgRecoltes() + " kg, CO₂: " + ind.getCo2Evite() + " kg");
            }
            System.out.print("Nouvel ID indicateur : ");
            int indicateurId = scanner.nextInt();
            scanner.nextLine();
            org.example.models.IndicateurImpact indicateur = indicateurDAO.getIndicateurById(indicateurId);
            if (indicateur != null) {
                zone.setIndicateur(indicateur);
            }
        }

        dao.updateZone(zone);
        System.out.println("✅ Zone modifiée avec succès !");
    }

    private static void supprimerZone(org.example.services.ZonePollueeDAO dao, java.util.Scanner scanner) {
        System.out.print("ID de la zone à supprimer : ");
        int id = scanner.nextInt();
        scanner.nextLine();

        dao.deleteZone(id);
        System.out.println("✅ Zone supprimée !");
    }

    private static void afficherToutesZones(org.example.services.ZonePollueeDAO dao) {
        java.util.List<org.example.models.ZonePolluee> zones = dao.getAllZones();
        afficherListe(zones);
    }

    private static void rechercherParNom(org.example.services.ZonePollueeDAO dao, java.util.Scanner scanner) {
        System.out.print("Mot-clé à rechercher : ");
        String keyword = scanner.nextLine();
        java.util.List<org.example.models.ZonePolluee> zones = dao.searchByNom(keyword);
        afficherListe(zones);
    }

    private static void trierParNiveau(org.example.services.ZonePollueeDAO dao) {
        java.util.List<org.example.models.ZonePolluee> zones = dao.sortByNiveauDesc();
        afficherListe(zones);
    }

    private static void afficherListe(java.util.List<org.example.models.ZonePolluee> zones) {
        if (zones.isEmpty()) {
            System.out.println("Aucune zone trouvée.");
            return;
        }
        System.out.println("\n-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s %-25s %-25s %-10s %-20s %-15s%n", "ID", "Nom", "GPS", "Niveau", "Date", "Indicateur");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        for (org.example.models.ZonePolluee z : zones) {
            String indicateurInfo = z.getIndicateur() != null ? "ID=" + z.getIndicateur().getId() : "Aucun";
            System.out.printf("%-5d %-25s %-25s %-10d %-20s %-15s%n",
                    z.getId(), z.getNomZone(), z.getCoordonneesGps(),
                    z.getNiveauPollution(), z.getDateIdentification(), indicateurInfo);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
    }

    // =========================
    // AUTH PAGES
    // =========================
    public static void showLoginPage() {
        loadPage("/org/example/views/login.fxml", "Connexion | WasteWise TN", 1100, 700);
    }

    public static void showRegisterPage() {
        loadPage("/org/example/views/register.fxml", "Créer un compte | WasteWise TN", 1100, 750);
    }

    public static void showForgotPasswordPage() {
        loadPage("/org/example/views/forgot_password.fxml", "Mot de passe oublié | WasteWise TN", 900, 600);
    }

    public static void showResetPasswordPage() {
        loadPage("/org/example/views/reset_password.fxml", "Nouveau mot de passe | WasteWise TN", 900, 600);
    }

    public static void showDashboardAdmin() {
        loadPage("/org/example/views/dashboard_admin.fxml", "Dashboard Admin | WasteWise TN", 1200, 750);
    }

    public static void showDashboardCitizen() {
        loadPage("/org/example/views/dashboard_citizen.fxml", "Dashboard Citoyen | WasteWise TN", 1200, 750);
    }

    public static void showDashboardValorizer() {
        loadPage("/org/example/views/dashboard_valorizer.fxml", "Dashboard Valorisateur | WasteWise TN", 1200, 750);
    }

    public static void showZonePollueeListPage() {
        loadPage("/org/example/views/zone_polluee_list.fxml", "Gestion des Zones Polluées | WasteWise TN", 1200, 700);
    }

    public static void showZonePollueeFormPage() {
        loadPage("/org/example/views/zone_polluee_form.fxml", "Zone Polluée | WasteWise TN", 600, 500);
    }
    public static void showIndicateurImpactListPage() {
        loadPage("/org/example/views/indicateur_impact_list.fxml", "Indicateurs d'Impact | WasteWise TN", 1200, 750);
    }
    public static void showZonePollueeDeletePage() {
        loadPage("/org/example/views/zone_polluee_delete.fxml", "Supprimer une zone | WasteWise TN", 600, 300);
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

    // =========================
    // GENERIC LOADER
    // =========================
    private static void loadPage(String fxmlPath, String title, int width, int height) {
        try {
            URL fxmlUrl = Main.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                throw new RuntimeException("FXML introuvable : " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), width, height);

            URL cssUrl = Main.class.getResource("/org/example/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

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

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) {
        launch(args);
    }
}