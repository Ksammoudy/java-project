package main;

import entities.AppelOffre;
import entities.ReponseOffre;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

public class MainTest {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Connection cnx = MyConnection.getInstance().getConnection();
            System.out.println("=== TEST JDBC WasteWiseJava ===");
            System.out.println("Connected: " + (cnx != null && !cnx.isClosed()));
            System.out.println("Database: " + cnx.getCatalog());
            System.out.println("Message: JDBC connection to shared Symfony DB is OK.");

            ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
            ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();

            lancerMenu(serviceAppelOffre, serviceReponseOffre);
        } catch (Exception e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }

    private static void lancerMenu(ServiceAppelOffre sa, ServiceReponseOffre sr) {
        int choix;
        do {
            afficherMenu();
            choix = lireInt("Choix: ");

            try {
                switch (choix) {
                    case 1:
                        afficherAppels(sa.recupererTout());
                        break;
                    case 2:
                        ajouterAppel(sa);
                        break;
                    case 3:
                        modifierAppel(sa);
                        break;
                    case 4:
                        supprimerAppel(sa);
                        break;
                    case 5:
                        afficherReponses(sr.recupererTout());
                        break;
                    case 6:
                        ajouterReponse(sr);
                        break;
                    case 7:
                        modifierReponse(sr);
                        break;
                    case 8:
                        supprimerReponse(sr);
                        break;
                    case 9:
                        accepterReponse(sr);
                        break;
                    case 10:
                        refuserReponse(sr);
                        break;
                    case 0:
                        System.out.println("Fin du test console.");
                        break;
                    default:
                        System.out.println("Choix invalide.");
                }
            } catch (Exception e) {
                System.out.println("Erreur: " + e.getMessage());
            }

        } while (choix != 0);
    }

    private static void afficherMenu() {
        System.out.println("\n===== MENU CRUD JDBC =====");
        System.out.println("1. Lister AppelOffre");
        System.out.println("2. Ajouter AppelOffre");
        System.out.println("3. Modifier AppelOffre");
        System.out.println("4. Supprimer AppelOffre");
        System.out.println("5. Lister ReponseOffre");
        System.out.println("6. Ajouter ReponseOffre");
        System.out.println("7. Modifier ReponseOffre");
        System.out.println("8. Supprimer ReponseOffre");
        System.out.println("9. Accepter ReponseOffre");
        System.out.println("10. Refuser ReponseOffre");
        System.out.println("0. Quitter");
    }

    private static void ajouterAppel(ServiceAppelOffre sa) throws Exception {
        String titre = lireTexteObligatoire("Titre: ");
        String description = lireTexteObligatoire("Description: ");
        double quantite = lireDouble("Quantite demandee: ");
        Timestamp dateLimite = lireTimestamp("Date limite (yyyy-mm-dd hh:mm:ss): ");
        int valorisateurId = lireInt("valorisateur_id: ");

        AppelOffre a = new AppelOffre(titre, description, quantite, dateLimite, valorisateurId);
        sa.ajouter(a);
        System.out.println("AppelOffre ajoute.");
    }

    private static void modifierAppel(ServiceAppelOffre sa) throws Exception {
        int id = lireInt("id appel_offre a modifier: ");
        String titre = lireTexteObligatoire("Nouveau titre: ");
        String description = lireTexteObligatoire("Nouvelle description: ");
        double quantite = lireDouble("Nouvelle quantite demandee: ");
        Timestamp dateLimite = lireTimestamp("Nouvelle date limite (yyyy-mm-dd hh:mm:ss): ");
        int valorisateurId = lireInt("Nouveau valorisateur_id: ");

        AppelOffre a = new AppelOffre(id, titre, description, quantite, dateLimite, valorisateurId);
        sa.modifier(a);
        System.out.println("AppelOffre modifie.");
    }

    private static void supprimerAppel(ServiceAppelOffre sa) throws Exception {
        int id = lireInt("id appel_offre a supprimer: ");
        sa.supprimer(id);
        System.out.println("AppelOffre supprime.");
    }

    private static void ajouterReponse(ServiceReponseOffre sr) throws Exception {
        double quantite = lireDouble("Quantite proposee: ");
        Timestamp dateSoumis = lireTimestamp("Date soumis (yyyy-mm-dd hh:mm:ss): ");
        String statut = lireTexteOptionnel("Statut (en attente/valide/refuse) [enter = en attente]: ");
        String message = lireTexteOptionnel("Message (optionnel): ");
        int appelOffreId = lireInt("appel_offre_id: ");
        int citoyenId = lireInt("citoyen_id: ");

        ReponseOffre r = new ReponseOffre(quantite, dateSoumis, statut, message, appelOffreId, citoyenId);
        sr.ajouter(r);
        System.out.println("ReponseOffre ajoutee.");
    }

    private static void modifierReponse(ServiceReponseOffre sr) throws Exception {
        int id = lireInt("id reponse_offre a modifier: ");
        double quantite = lireDouble("Nouvelle quantite proposee: ");
        Timestamp dateSoumis = lireTimestamp("Nouvelle date soumis (yyyy-mm-dd hh:mm:ss): ");
        String statut = lireTexteOptionnel("Nouveau statut (en attente/valide/refuse): ");
        String message = lireTexteOptionnel("Nouveau message (optionnel): ");
        int appelOffreId = lireInt("Nouveau appel_offre_id: ");
        int citoyenId = lireInt("Nouveau citoyen_id: ");

        ReponseOffre r = new ReponseOffre(id, quantite, dateSoumis, statut, message, appelOffreId, citoyenId);
        sr.modifier(r);
        System.out.println("ReponseOffre modifiee.");
    }

    private static void supprimerReponse(ServiceReponseOffre sr) throws Exception {
        int id = lireInt("id reponse_offre a supprimer: ");
        sr.supprimer(id);
        System.out.println("ReponseOffre supprimee.");
    }

    private static void accepterReponse(ServiceReponseOffre sr) throws Exception {
        int id = lireInt("id reponse_offre a accepter: ");
        sr.accepterReponse(id);
        System.out.println("ReponseOffre acceptee.");
    }

    private static void refuserReponse(ServiceReponseOffre sr) throws Exception {
        int id = lireInt("id reponse_offre a refuser: ");
        sr.refuserReponse(id);
        System.out.println("ReponseOffre refusee.");
    }

    private static void afficherAppels(List<AppelOffre> list) {
        if (list.isEmpty()) {
            System.out.println("Aucun appel_offre.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static void afficherReponses(List<ReponseOffre> list) {
        if (list.isEmpty()) {
            System.out.println("Aucune reponse_offre.");
            return;
        }
        list.forEach(System.out::println);
    }

    private static int lireInt(String label) {
        while (true) {
            try {
                System.out.print(label);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Valeur invalide. Entrez un entier.");
            }
        }
    }

    private static double lireDouble(String label) {
        while (true) {
            try {
                System.out.print(label);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.println("La valeur doit etre positive.");
                    continue;
                }
                return value;
            } catch (Exception e) {
                System.out.println("Valeur invalide. Entrez un nombre.");
            }
        }
    }

    private static Timestamp lireTimestamp(String label) {
        while (true) {
            try {
                System.out.print(label);
                return Timestamp.valueOf(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Format invalide. Utilisez yyyy-mm-dd hh:mm:ss");
            }
        }
    }

    private static String lireTexteObligatoire(String label) {
        while (true) {
            System.out.print(label);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Champ obligatoire.");
        }
    }

    private static String lireTexteOptionnel(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}
