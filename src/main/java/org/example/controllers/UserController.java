package org.example.controllers;

import org.example.models.User;
import org.example.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class UserController {

    private final UserService userService = UserService.getInstance();

    public void createUser() {
        User user = new User();
        user.setEmail("testjava@gmail.com");
        user.setRoles("[]");
        user.setPassword("123456");
        user.setNom("Khalil");
        user.setPrenom("Java");
        user.setTelephone("26212423");
        user.setType("CITIZEN");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setFaceEmbedding(null);
        user.setFaceUpdatedAt(null);
        user.setLastSeenAt(null);
        user.setGoogleAuthenticatorSecret(null);
        user.setTwoFactorEnabled(false);
        user.setVerified(false);

        userService.addUser(user);
    }

    public void showAllUsers() {
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé.");
            return;
        }

        for (User user : users) {
            System.out.println(user);
        }
    }

    public void showUserById(int id) {
        User user = userService.getUserById(id);

        if (user != null) {
            System.out.println(user);
        } else {
            System.out.println("Utilisateur non trouvé.");
        }
    }

    public void updateUserExample(int id) {
        User user = userService.getUserById(id);

        if (user != null) {
            user.setNom("Nom modifié");
            user.setPrenom("Prenom modifié");
            user.setTelephone("99999999");
            user.setType("CITIZEN");
            user.setVerified(true);

            userService.updateUser(user);
        } else {
            System.out.println("Utilisateur non trouvé.");
        }
    }

    public void updatePasswordExample(int id) {
        userService.updatePassword(id, "nouveauMotDePasse123");
    }

    public void deleteUser(int id) {
        userService.deleteUser(id);
    }

    public void toggleUser(int id) {
        userService.toggleActive(id);
    }

    private String getDashboard(User user) {
        return switch (user.getType()) {
            case "ADMIN" -> "Dashboard ADMIN";
            case "VALORIZER" -> "Dashboard VALORIZER";
            default -> "Dashboard CITIZEN";
        };
    }

    public void loginInteractive() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== LOGIN ===");

        System.out.print("Email : ");
        String email = scanner.nextLine();

        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        User user = userService.login(email, password);

        if (user != null) {
            System.out.println("👤 Bienvenue " + user.getNom() + " " + user.getPrenom());
            System.out.println("🔐 Session ouverte");
            System.out.println("➡️ Redirection vers : " + getDashboard(user));
        }
    }
}