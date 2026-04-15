package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;

public class DashboardValorizerController {

    @FXML
    private Label headerEmailLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label nomLabel;

    @FXML
    private Label prenomLabel;

    @FXML
    private Label telephoneLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusBadgeLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user != null) {
            String email = user.getEmail() != null ? user.getEmail() : "valorizer@email.com";
            String nom = user.getNom() != null ? user.getNom() : "—";
            String prenom = user.getPrenom() != null ? user.getPrenom() : "—";
            String telephone = user.getTelephone() != null ? user.getTelephone() : "—";
            String statut = user.isActive() ? "Actif" : "Inactif";

            if (headerEmailLabel != null) {
                headerEmailLabel.setText(email);
            }

            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue " + prenom + " 👋");
            }

            if (emailLabel != null) {
                emailLabel.setText(email);
            }

            if (typeLabel != null) {
                typeLabel.setText("VALORIZER");
            }

            if (nomLabel != null) {
                nomLabel.setText(nom);
            }

            if (prenomLabel != null) {
                prenomLabel.setText(prenom);
            }

            if (telephoneLabel != null) {
                telephoneLabel.setText(telephone);
            }

            if (statusLabel != null) {
                statusLabel.setText(statut);
            }

            if (statusBadgeLabel != null) {
                statusBadgeLabel.setText(statut);
            }
        }
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardValorizer();
    }

    @FXML
    public void handleProfile() {
        Main.showProfileViewPage();
    }

    @FXML
    public void handleEditProfile() {
        Main.showProfileEditPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        Main.showLoginPage();
    }
}