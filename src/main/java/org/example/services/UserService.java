package org.example.services;

import org.example.models.User;
import org.example.utils.DBConnection;
import org.example.utils.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static UserService instance;

    private UserService() {
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO user (email, roles, password, nom, prenom, telephone, type, created_at, is_active, face_embedding, face_updated_at, last_seen_at, google_authenticator_secret, is_two_factor_enabled, is_verified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getRoles() == null ? "[]" : user.getRoles());
            ps.setString(3, PasswordUtil.hashPassword(user.getPassword()));
            ps.setString(4, user.getNom());
            ps.setString(5, user.getPrenom());
            ps.setString(6, user.getTelephone());
            ps.setString(7, user.getType());
            ps.setTimestamp(8, Timestamp.valueOf(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()
            ));
            ps.setBoolean(9, user.isActive());
            ps.setString(10, user.getFaceEmbedding());
            ps.setTimestamp(11, user.getFaceUpdatedAt() != null ? Timestamp.valueOf(user.getFaceUpdatedAt()) : null);
            ps.setTimestamp(12, user.getLastSeenAt() != null ? Timestamp.valueOf(user.getLastSeenAt()) : null);
            ps.setString(13, user.getGoogleAuthenticatorSecret());
            ps.setBoolean(14, user.isTwoFactorEnabled());
            ps.setBoolean(15, user.isVerified());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur ajouté avec succès.");
                return true;
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Erreur : email déjà utilisé.");
        } catch (SQLException e) {
            System.out.println("❌ Erreur addUser : " + e.getMessage());
        }

        return false;
    }

    public String registerUser(String nom, String prenom, String email, String telephone,
                               String type, String password, String confirmPassword,
                               boolean agreeTerms, String faceEmbedding) {

        nom = nom != null ? nom.trim() : "";
        prenom = prenom != null ? prenom.trim() : "";
        email = email != null ? email.trim().toLowerCase() : "";
        telephone = telephone != null ? telephone.trim() : "";
        type = type != null ? type.trim().toUpperCase() : "";

        if (nom.isEmpty()) {
            return "Veuillez saisir votre nom.";
        }
        if (nom.length() < 2 || nom.length() > 120) {
            return "Le nom doit contenir entre 2 et 120 caractères.";
        }

        if (prenom.isEmpty()) {
            return "Veuillez saisir votre prénom.";
        }
        if (prenom.length() < 2 || prenom.length() > 120) {
            return "Le prénom doit contenir entre 2 et 120 caractères.";
        }

        if (email.isEmpty()) {
            return "Veuillez saisir un email.";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Email invalide.";
        }

        if (!telephone.isEmpty() && !telephone.matches("^[0-9+\\s\\-]{8,30}$")) {
            return "Téléphone invalide (8 à 30 caractères, chiffres + espaces + + -).";
        }

        if (type.isEmpty()) {
            type = "CITIZEN";
        }

        if (!type.equals("CITIZEN") && !type.equals("VALORIZER")) {
            return "Type d'utilisateur invalide.";
        }

        if (!agreeTerms) {
            return "Vous devez accepter les conditions.";
        }

        if (password == null || password.isBlank()) {
            return "Veuillez saisir un mot de passe.";
        }

        if (password.length() < 8) {
            return "Votre mot de passe doit contenir au moins 8 caractères.";
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            return "Les mots de passe ne correspondent pas.";
        }

        if (getUserByEmail(email) != null) {
            return "Cet email existe déjà.";
        }

        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setTelephone(telephone.isEmpty() ? null : telephone);
        user.setType(type);
        user.setRoles("[]");
        user.setPassword(password);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setVerified(false);
        user.setTwoFactorEnabled(false);
        user.setGoogleAuthenticatorSecret(null);
        user.setLastSeenAt(null);
        user.setFaceUpdatedAt(null);
        user.setFaceEmbedding(faceEmbedding != null && !faceEmbedding.isBlank() ? faceEmbedding.trim() : null);

        boolean inserted = addUser(user);
        return inserted ? "SUCCESS" : "Erreur lors de la création du compte.";
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id ASC";

        try (Statement st = DBConnection.getInstance().getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllUsers : " + e.getMessage());
        }

        return users;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getUserById : " + e.getMessage());
        }

        return null;
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String sql = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur getUserByEmail : " + e.getMessage());
        }

        return null;
    }

    public User findByEmail(String email) {
        return getUserByEmail(email);
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE user SET email=?, roles=?, nom=?, prenom=?, telephone=?, type=?, created_at=?, is_active=?, face_embedding=?, face_updated_at=?, last_seen_at=?, google_authenticator_secret=?, is_two_factor_enabled=?, is_verified=? WHERE id=?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getRoles() == null ? "[]" : user.getRoles());
            ps.setString(3, user.getNom());
            ps.setString(4, user.getPrenom());
            ps.setString(5, user.getTelephone());
            ps.setString(6, user.getType());
            ps.setTimestamp(7, Timestamp.valueOf(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()
            ));
            ps.setBoolean(8, user.isActive());
            ps.setString(9, user.getFaceEmbedding());
            ps.setTimestamp(10, user.getFaceUpdatedAt() != null ? Timestamp.valueOf(user.getFaceUpdatedAt()) : null);
            ps.setTimestamp(11, user.getLastSeenAt() != null ? Timestamp.valueOf(user.getLastSeenAt()) : null);
            ps.setString(12, user.getGoogleAuthenticatorSecret());
            ps.setBoolean(13, user.isTwoFactorEnabled());
            ps.setBoolean(14, user.isVerified());
            ps.setInt(15, user.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur mis à jour avec succès.");
                return true;
            } else {
                System.out.println("⚠️ Utilisateur introuvable.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur updateUser : " + e.getMessage());
        }

        return false;
    }

    public boolean updatePassword(int id, String newPlainPassword) {
        if (newPlainPassword == null || newPlainPassword.isBlank()) {
            System.out.println("❌ Nouveau mot de passe vide.");
            return false;
        }

        String sql = "UPDATE user SET password = ? WHERE id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPlainPassword));
            ps.setInt(2, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Mot de passe mis à jour.");
                return true;
            } else {
                System.out.println("⚠️ Utilisateur introuvable.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur updatePassword : " + e.getMessage());
        }

        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Utilisateur supprimé.");
                return true;
            } else {
                System.out.println("⚠️ Utilisateur introuvable.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur deleteUser : " + e.getMessage());
        }

        return false;
    }

    public boolean toggleActive(int id) {
        String sql = "UPDATE user SET is_active = NOT is_active WHERE id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Statut utilisateur modifié.");
                return true;
            } else {
                System.out.println("⚠️ Utilisateur introuvable.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur toggleActive : " + e.getMessage());
        }

        return false;
    }

    public boolean updateLastSeen(int id) {
        String sql = "UPDATE user SET last_seen_at = ? WHERE id = ?";

        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Erreur updateLastSeen : " + e.getMessage());
        }

        return false;
    }

    public User login(String email, String plainPassword) {
        if (email == null || email.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            System.out.println("❌ Email ou mot de passe vide.");
            return null;
        }

        User user = getUserByEmail(email);

        if (user == null) {
            System.out.println("❌ Identifiants invalides.");
            return null;
        }

        if (!user.isActive()) {
            System.out.println("❌ Compte désactivé.");
            return null;
        }

        boolean passwordOk = PasswordUtil.checkPassword(plainPassword, user.getPassword());
        if (!passwordOk) {
            System.out.println("❌ Mot de passe incorrect ou hash invalide en base.");
            return null;
        }

        if (!user.isVerified()) {
            System.out.println("⚠️ Compte non vérifié.");
        }

        System.out.println("✅ Connexion réussie.");
        return user;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setRoles(rs.getString("roles"));
        user.setPassword(rs.getString("password"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setTelephone(rs.getString("telephone"));
        user.setType(rs.getString("type"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        user.setActive(rs.getBoolean("is_active"));
        user.setFaceEmbedding(rs.getString("face_embedding"));

        Timestamp faceUpdatedAt = rs.getTimestamp("face_updated_at");
        if (faceUpdatedAt != null) {
            user.setFaceUpdatedAt(faceUpdatedAt.toLocalDateTime());
        }

        Timestamp lastSeenAt = rs.getTimestamp("last_seen_at");
        if (lastSeenAt != null) {
            user.setLastSeenAt(lastSeenAt.toLocalDateTime());
        }

        user.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
        user.setTwoFactorEnabled(rs.getBoolean("is_two_factor_enabled"));
        user.setVerified(rs.getBoolean("is_verified"));

        return user;
    }
}