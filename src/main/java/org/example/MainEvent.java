package org.example;

import org.example.utils.DBConnection;
import org.example.services.gestionevent.EvenementServices;
import org.example.models.gestionevent.Evenement;

import java.sql.Connection;
import java.util.List;

public class MainEvent {
    public static void main(String[] args) {

        try {
            // 🔹 TEST CONNECTION
            Connection conn = DBConnection.getInstance().getConnection();

            if (conn != null) {
                System.out.println("Connexion réussie à la base de données ✅");
            } else {
                System.out.println("Connexion échouée ❌");
                return;
            }

            // 🔹 TEST READ (affichage données DB)
            EvenementServices service = new EvenementServices();
            List<Evenement> list = service.read();

            System.out.println("\n📌 Liste des événements :");
            for (Evenement e : list) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println("Erreur ❌");
            e.printStackTrace();
        }
    }
}