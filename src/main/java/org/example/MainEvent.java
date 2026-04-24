package org.example;

import org.example.utils.DBConnection;
import java.sql.Connection;

public class MainEvent {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();

                if (conn != null) {
                    System.out.println("Connexion réussie à la base de données ✅");
                } else {
                    System.out.println("Connexion échouée ❌");
                }

            } catch (Exception e) {
                System.out.println("Erreur de connexion ❌");
                e.printStackTrace();
            }
        }
    }
