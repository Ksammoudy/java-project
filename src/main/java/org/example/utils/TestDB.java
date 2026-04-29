package org.example.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("🔍 TEST DE CONNEXION À LA BASE DE DONNÉES");
        System.out.println("========================================\n");

        // Test 1: Get connection
        System.out.println("1️⃣ Test de connexion...");
        Connection conn = DBConnection.getInstance().getConnection();

        if (conn == null) {
            System.out.println("❌ ÉCHEC: Impossible d'obtenir une connexion!");
            System.out.println("\nVérifiez que:");
            System.out.println("  - MySQL est démarré dans XAMPP (bouton Start)");
            System.out.println("  - Le port 3306 n'est pas bloqué");
            System.out.println("  - Les identifiants sont corrects (root / pas de mot de passe)");
            return;
        }

        System.out.println("✅ Connexion établie avec succès!\n");

        // Test 2: Check if database exists
        System.out.println("2️⃣ Vérification de la base de données 'pidev'...");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
            if (rs.next()) {
                String currentDb = rs.getString(1);
                System.out.println("   Base de données courante: " + currentDb);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur: " + e.getMessage());
        }

        // Test 3: Check zone_polluee table
        System.out.println("\n3️⃣ Vérification de la table 'zone_polluee'...");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM zone_polluee");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Table 'zone_polluee' existe avec " + count + " enregistrements");

                // Show some data
                if (count > 0) {
                    System.out.println("\n4️⃣ Affichage des zones existantes:");
                    rs = stmt.executeQuery("SELECT id, nom_zone, niveau_pollution FROM zone_polluee LIMIT 5");
                    System.out.println("   " + "-".repeat(50));
                    System.out.printf("   %-5s | %-25s | %-10s%n", "ID", "Nom", "Niveau");
                    System.out.println("   " + "-".repeat(50));
                    while (rs.next()) {
                        System.out.printf("   %-5d | %-25s | %-10d%n",
                                rs.getInt("id"),
                                rs.getString("nom_zone"),
                                rs.getInt("niveau_pollution"));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Table 'zone_polluee' non trouvée!");
            System.out.println("   Erreur: " + e.getMessage());
        }

        // Test 4: Check indicateur_impact table
        System.out.println("\n5️⃣ Vérification de la table 'indicateur_impact'...");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM indicateur_impact");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ Table 'indicateur_impact' existe avec " + count + " enregistrements");
            }
        } catch (Exception e) {
            System.out.println("❌ Table 'indicateur_impact' non trouvée!");
            System.out.println("   Erreur: " + e.getMessage());
        }

        // Test 5: Close connection
        System.out.println("\n========================================");
        System.out.println("✅ TEST TERMINÉ");
        System.out.println("========================================");

        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}