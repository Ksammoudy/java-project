package org.example.services;

import org.example.models.User;
import org.example.utils.DBConnection;
import org.example.utils.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements CRUD<User> {

    private static UserService instance;
    private final Connection cnx;

    private UserService() {
        cnx = DBConnection.getInstance().getConnection();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    @Override
    public void create(User user) throws SQLException {
        if (user == null) {
            throw new SQLException("Utilisateur null.");
        }

        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String telephone = user.getTelephone() != null ? user.getTelephone().trim() : "";
        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "CITIZEN";
        String password = user.getPassword();

        if (email.isEmpty() || !isValidEmail(email)) {
            throw new SQLException("Email invalide.");
        }

        if (nom.isEmpty() || !isValidName(nom)) {
            throw new SQLException("Nom invalide.");
        }

        if (prenom.isEmpty() || !isValidName(prenom)) {
            throw new SQLException("Prénom invalide.");
        }

        if (!telephone.isEmpty() && !isValidPhone(telephone)) {
            throw new SQLException("Téléphone invalide.");
        }

        if (!isValidType(type)) {
            throw new SQLException("Type utilisateur invalide.");
        }

        if (password == null || password.isBlank() || password.length() < 6) {
            throw new SQLException("Mot de passe invalide.");
        }

        if (getUserByEmail(email) != null) {
            throw new SQLException("Email déjà utilisé.");
        }

        String sql = "INSERT INTO `user` " +
                "(email, roles, password, nom, prenom, telephone, type, created_at, is_active, face_embedding, face_updated_at, last_seen_at, google_authenticator_secret, is_two_factor_enabled, is_verified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, user.getRoles() == null || user.getRoles().isBlank() ? "[]" : user.getRoles());
            ps.setString(3, PasswordUtil.hashPassword(password));
            ps.setString(4, nom);
            ps.setString(5, prenom);
            ps.setString(6, telephone.isEmpty() ? null : telephone);
            ps.setString(7, type);
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

            ps.executeUpdate();
        }
    }

    @Override
    public List<User> read() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM `user` ORDER BY id ASC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    @Override
    public void update(User user) throws SQLException {
        if (user == null) {
            throw new SQLException("Utilisateur null.");
        }

        if (user.getId() <= 0) {
            throw new SQLException("ID utilisateur invalide.");
        }

        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String telephone = user.getTelephone() != null ? user.getTelephone().trim() : "";
        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";

        if (email.isEmpty() || !isValidEmail(email)) {
            throw new SQLException("Email invalide.");
        }

        if (nom.isEmpty() || !isValidName(nom)) {
            throw new SQLException("Nom invalide.");
        }

        if (prenom.isEmpty() || !isValidName(prenom)) {
            throw new SQLException("Prénom invalide.");
        }

        if (!telephone.isEmpty() && !isValidPhone(telephone)) {
            throw new SQLException("Téléphone invalide.");
        }

        if (!isValidType(type)) {
            throw new SQLException("Type utilisateur invalide.");
        }

        if (emailExistsForAnotherUser(email, user.getId())) {
            throw new SQLException("Email déjà utilisé par un autre utilisateur.");
        }

        String sql = "UPDATE `user` SET " +
                "email=?, roles=?, nom=?, prenom=?, telephone=?, type=?, " +
                "is_active=?, face_embedding=?, face_updated_at=?, last_seen_at=?, " +
                "google_authenticator_secret=?, is_two_factor_enabled=?, is_verified=? " +
                "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, user.getRoles() == null || user.getRoles().isBlank() ? "[]" : user.getRoles());
            ps.setString(3, nom);
            ps.setString(4, prenom);
            ps.setString(5, telephone.isEmpty() ? null : telephone);
            ps.setString(6, type);
            ps.setBoolean(7, user.isActive());
            ps.setString(8, user.getFaceEmbedding());
            ps.setTimestamp(9, user.getFaceUpdatedAt() != null ? Timestamp.valueOf(user.getFaceUpdatedAt()) : null);
            ps.setTimestamp(10, user.getLastSeenAt() != null ? Timestamp.valueOf(user.getLastSeenAt()) : null);
            ps.setString(11, user.getGoogleAuthenticatorSecret());
            ps.setBoolean(12, user.isTwoFactorEnabled());
            ps.setBoolean(13, user.isVerified());
            ps.setInt(14, user.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("ID invalide.");
        }

        String sql = "DELETE FROM `user` WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean addUser(User user) {
        try {
            create(user);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur addUser : " + e.getMessage());
            return false;
        }
    }

    public String registerUser(String nom, String prenom, String email, String telephone,
                               String type, String password, String confirmPassword,
                               boolean agreeTerms, String faceEmbedding) {

        nom = nom != null ? nom.trim() : "";
        prenom = prenom != null ? prenom.trim() : "";
        email = email != null ? email.trim().toLowerCase() : "";
        telephone = telephone != null ? telephone.trim() : "";
        type = type != null ? type.trim().toUpperCase() : "CITIZEN";

        if (nom.isEmpty()) {
            return "Veuillez saisir votre nom.";
        }
        if (!isValidName(nom)) {
            return "Nom invalide.";
        }

        if (prenom.isEmpty()) {
            return "Veuillez saisir votre prénom.";
        }
        if (!isValidName(prenom)) {
            return "Prénom invalide.";
        }

        if (email.isEmpty()) {
            return "Veuillez saisir un email.";
        }
        if (!isValidEmail(email)) {
            return "Email invalide.";
        }

        if (!telephone.isEmpty() && !isValidPhone(telephone)) {
            return "Téléphone invalide.";
        }

        if (!isValidType(type)) {
            return "Type d'utilisateur invalide.";
        }

        if (!agreeTerms) {
            return "Vous devez accepter les conditions.";
        }

        if (password == null || password.isBlank()) {
            return "Veuillez saisir un mot de passe.";
        }

        if (password.length() < 6) {
            return "Votre mot de passe doit contenir au moins 6 caractères.";
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
        try {
            return read();
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllUsers : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM `user` WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
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

        String sql = "SELECT * FROM `user` WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
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
        try {
            update(user);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateUser : " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfile(User user) {
        if (user == null || user.getId() <= 0) {
            System.out.println("❌ Utilisateur invalide.");
            return false;
        }

        String nom = user.getNom() != null ? user.getNom().trim() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String telephone = user.getTelephone() != null ? user.getTelephone().trim() : "";

        if (nom.isEmpty() || !isValidName(nom)) {
            System.out.println("❌ Nom invalide.");
            return false;
        }

        if (prenom.isEmpty() || !isValidName(prenom)) {
            System.out.println("❌ Prénom invalide.");
            return false;
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            System.out.println("❌ Email invalide.");
            return false;
        }

        if (!telephone.isEmpty() && !isValidPhone(telephone)) {
            System.out.println("❌ Téléphone invalide.");
            return false;
        }

        if (emailExistsForAnotherUser(email, user.getId())) {
            System.out.println("❌ Email déjà utilisé par un autre utilisateur.");
            return false;
        }

        String sql = "UPDATE `user` SET nom = ?, prenom = ?, email = ?, telephone = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setString(4, telephone.isEmpty() ? null : telephone);
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateProfile : " + e.getMessage());
        }

        return false;
    }

    public boolean updateUserByAdmin(User user) {
        if (user == null || user.getId() <= 0) {
            System.out.println("❌ Utilisateur invalide.");
            return false;
        }

        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String nom = user.getNom() != null ? user.getNom().trim() : "";
        String prenom = user.getPrenom() != null ? user.getPrenom().trim() : "";
        String telephone = user.getTelephone() != null ? user.getTelephone().trim() : "";
        String type = user.getType() != null ? user.getType().trim().toUpperCase() : "";

        if (email.isEmpty() || !isValidEmail(email)) {
            System.out.println("❌ Email invalide.");
            return false;
        }

        if (nom.isEmpty() || !isValidName(nom)) {
            System.out.println("❌ Nom invalide.");
            return false;
        }

        if (prenom.isEmpty() || !isValidName(prenom)) {
            System.out.println("❌ Prénom invalide.");
            return false;
        }

        if (!telephone.isEmpty() && !isValidPhone(telephone)) {
            System.out.println("❌ Téléphone invalide.");
            return false;
        }

        if (!isValidType(type)) {
            System.out.println("❌ Type utilisateur invalide.");
            return false;
        }

        if (emailExistsForAnotherUser(email, user.getId())) {
            System.out.println("❌ Email déjà utilisé par un autre utilisateur.");
            return false;
        }

        String sql = "UPDATE `user` SET email = ?, nom = ?, prenom = ?, telephone = ?, type = ?, is_active = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, nom);
            ps.setString(3, prenom);
            ps.setString(4, telephone.isEmpty() ? null : telephone);
            ps.setString(5, type);
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateUserByAdmin : " + e.getMessage());
            return false;
        }
    }

    public boolean updateFaceData(User user) {
        if (user == null || user.getId() <= 0) {
            System.out.println("❌ Utilisateur invalide pour updateFaceData.");
            return false;
        }

        String sql = "UPDATE `user` SET face_embedding = ?, face_updated_at = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getFaceEmbedding());
            ps.setTimestamp(2, user.getFaceUpdatedAt() != null
                    ? Timestamp.valueOf(user.getFaceUpdatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateFaceData : " + e.getMessage());
            return false;
        }
    }

    public boolean clearFaceData(int userId) {
        if (userId <= 0) {
            return false;
        }

        String sql = "UPDATE `user` SET face_embedding = NULL, face_updated_at = NULL WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur clearFaceData : " + e.getMessage());
            return false;
        }
    }

    public boolean updateTwoFactorData(User user) {
        if (user == null || user.getId() <= 0) {
            System.out.println("❌ Utilisateur invalide pour updateTwoFactorData.");
            return false;
        }

        String sql = "UPDATE `user` SET google_authenticator_secret = ?, is_two_factor_enabled = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getGoogleAuthenticatorSecret());
            ps.setBoolean(2, user.isTwoFactorEnabled());
            ps.setInt(3, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updateTwoFactorData : " + e.getMessage());
            return false;
        }
    }

    public boolean emailExistsForAnotherUser(String email, int currentUserId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT id FROM `user` WHERE email = ? AND id <> ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setInt(2, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur emailExistsForAnotherUser : " + e.getMessage());
        }

        return false;
    }

    public boolean updatePassword(int id, String newPlainPassword) {
        if (newPlainPassword == null || newPlainPassword.isBlank()) {
            System.out.println("❌ Nouveau mot de passe vide.");
            return false;
        }

        if (newPlainPassword.length() < 6) {
            System.out.println("❌ Nouveau mot de passe trop court.");
            return false;
        }

        String sql = "UPDATE `user` SET password = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPlainPassword));
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur updatePassword : " + e.getMessage());
        }

        return false;
    }

    public boolean deleteUser(int id) {
        try {
            delete(id);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur deleteUser : " + e.getMessage());
            return false;
        }
    }

    public boolean toggleActive(int id) {
        String sql = "UPDATE `user` SET is_active = NOT is_active WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur toggleActive : " + e.getMessage());
        }

        return false;
    }

    public boolean setUserActiveStatus(int targetUserId, boolean active, User currentAdmin) {
        if (currentAdmin == null) {
            System.out.println("❌ Aucun admin connecté.");
            return false;
        }

        if (currentAdmin.getType() == null || !currentAdmin.getType().equalsIgnoreCase("ADMIN")) {
            System.out.println("❌ Accès refusé : ADMIN requis.");
            return false;
        }

        User targetUser = getUserById(targetUserId);
        if (targetUser == null) {
            System.out.println("❌ Utilisateur introuvable.");
            return false;
        }

        if (currentAdmin.getId() == targetUser.getId()) {
            System.out.println("❌ Impossible de modifier votre propre compte.");
            return false;
        }

        String type = targetUser.getType() != null ? targetUser.getType().toUpperCase() : "";
        if (!type.equals("CITIZEN") && !type.equals("VALORIZER")) {
            System.out.println("❌ Action autorisée seulement sur citoyen / valorisateur.");
            return false;
        }

        String sql = "UPDATE `user` SET is_active = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, targetUserId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erreur setUserActiveStatus : " + e.getMessage());
        }

        return false;
    }

    public boolean updateLastSeen(int id) {
        String sql = "UPDATE `user` SET last_seen_at = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
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
            System.out.println("❌ Mot de passe incorrect.");
            return null;
        }

        if (!user.isVerified()) {
            System.out.println("⚠️ Compte non vérifié.");
        }

        updateLastSeen(user.getId());
        user.setLastSeenAt(LocalDateTime.now());

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

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidName(String value) {
        return value != null && value.matches("[A-Za-zÀ-ÿ\\s-]{2,30}");
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{8}");
    }

    private boolean isValidType(String type) {
        return "ADMIN".equals(type) || "VALORIZER".equals(type) || "CITIZEN".equals(type);
    }
}