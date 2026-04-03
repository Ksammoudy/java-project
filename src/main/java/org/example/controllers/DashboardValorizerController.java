package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class DashboardValorizerController {

    @FXML
    private Label emailLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user == null) {
            Main.showLoginPage();
            return;
        }

        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");
        typeLabel.setText(user.getType() != null ? user.getType() : "VALORIZER");
        statusLabel.setText(user.isActive() ? "Actif" : "Désactivé");

        nomLabel.setText(user.getNom() != null ? user.getNom() : "—");
        prenomLabel.setText(user.getPrenom() != null ? user.getPrenom() : "—");
        telephoneLabel.setText(user.getTelephone() != null ? user.getTelephone() : "—");

        String prenom = user.getPrenom() != null ? user.getPrenom() : "";
        welcomeLabel.setText("Bienvenue " + prenom + " 👋");
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleProfile() {
        System.out.println("Ouvrir profil valorisateur");
    }

    @FXML
    public void handleEditProfile() {
        System.out.println("Ouvrir modification profil valorisateur");
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}