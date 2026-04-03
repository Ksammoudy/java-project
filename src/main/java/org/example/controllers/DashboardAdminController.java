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
    private Label adminEmailLabel;

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

        if (user == null || user.getType() == null || !user.getType().equalsIgnoreCase("ADMIN")) {
            Main.showLoginPage();
            return;
        }

        String email = user.getEmail() != null ? user.getEmail() : "—";

        if (adminEmailLabel != null) {
            adminEmailLabel.setText(email);
        }

        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue Admin 👋");
        }

        if (todayDateLabel != null) {
            todayDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(userService.getAllUsers().size()));
        }
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardAdmin();
    }

    @FXML
    public void handleUsers() {
        System.out.println("Ouvrir gestion utilisateurs");
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}