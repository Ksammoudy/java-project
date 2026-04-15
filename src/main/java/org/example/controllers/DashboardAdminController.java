package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.Main;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardAdminController {

    @FXML
    private Label adminEmailHeaderLabel;

    @FXML
    private Label adminEmailCardLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label todayDateLabel;

    @FXML
    private Label welcomeLabel;

    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();

        if (user != null) {
            String email = user.getEmail() != null ? user.getEmail() : "admin@email.com";
            String prenom = user.getPrenom() != null && !user.getPrenom().isEmpty()
                    ? user.getPrenom()
                    : "Admin";

            if (adminEmailHeaderLabel != null) {
                adminEmailHeaderLabel.setText(email);
            }

            if (adminEmailCardLabel != null) {
                adminEmailCardLabel.setText(email);
            }

            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue " + prenom + " 👋");
            }
        }

        if (todayDateLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            todayDateLabel.setText(LocalDate.now().format(formatter));
        }

        if (totalUsersLabel != null) {
            try {
                totalUsersLabel.setText(String.valueOf(userService.getAllUsers().size()));
            } catch (Exception e) {
                totalUsersLabel.setText("0");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    public void handleUsers() {
        Main.showAdminUsersPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        Main.showLoginPage();
    }
}