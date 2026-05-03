package org.example.services;

import org.example.models.User;
import org.example.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FaceLoginService {

    private static final double THRESHOLD = 0.45;

    public User findUserByFace(String loginEmbeddingJson) {
        double[] loginEmbedding = parseEmbedding(loginEmbeddingJson);

        if (loginEmbedding == null || loginEmbedding.length == 0) {
            System.out.println("Login embedding invalide.");
            return null;
        }

        System.out.println("Login embedding length = " + loginEmbedding.length);

        List<User> users = getUsersWithFace();

        System.out.println("Users with face = " + users.size());

        User bestUser = null;
        double bestDistance = Double.MAX_VALUE;

        for (User user : users) {
            String storedJson = user.getFaceEmbedding();

            System.out.println("--------------------------------------");
            System.out.println("User = " + user.getEmail());

            if (storedJson == null || storedJson.isBlank()) {
                System.out.println("Stored embedding NULL ou vide.");
                continue;
            }

            System.out.println("Stored embedding sample = " +
                    storedJson.substring(0, Math.min(50, storedJson.length())));

            double[] storedEmbedding = parseEmbedding(storedJson);

            if (storedEmbedding == null || storedEmbedding.length == 0) {
                System.out.println("Stored embedding invalide.");
                continue;
            }

            System.out.println("Stored embedding length = " + storedEmbedding.length);

            if (loginEmbedding.length != storedEmbedding.length) {
                System.out.println("Longueurs différentes, comparaison ignorée.");
                continue;
            }

            double distance = cosineDistance(loginEmbedding, storedEmbedding);

            System.out.println("Cosine distance = " + distance);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestUser = user;
            }
        }

        System.out.println("Best distance = " + bestDistance);

        if (bestUser != null && bestDistance <= THRESHOLD) {
            System.out.println("Visage reconnu : " + bestUser.getEmail());
            return bestUser;
        }

        System.out.println("Aucun visage reconnu.");
        return null;
    }

    private List<User> getUsersWithFace() {
        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, email, roles, password, nom, prenom, telephone, type,
                       created_at, is_active, face_embedding, face_updated_at,
                       last_seen_at, google_authenticator_secret,
                       is_two_factor_enabled, is_verified
                FROM user
                WHERE face_embedding IS NOT NULL
                  AND TRIM(face_embedding) <> ''
                """;

        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();

                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRoles(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setTelephone(rs.getString("telephone"));
                user.setType(rs.getString("type"));
                user.setActive(rs.getBoolean("is_active"));
                user.setFaceEmbedding(rs.getString("face_embedding"));
                user.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
                user.setTwoFactorEnabled(rs.getBoolean("is_two_factor_enabled"));
                user.setVerified(rs.getBoolean("is_verified"));

                if (rs.getTimestamp("created_at") != null) {
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }

                if (rs.getTimestamp("face_updated_at") != null) {
                    user.setFaceUpdatedAt(rs.getTimestamp("face_updated_at").toLocalDateTime());
                }

                if (rs.getTimestamp("last_seen_at") != null) {
                    user.setLastSeenAt(rs.getTimestamp("last_seen_at").toLocalDateTime());
                }

                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    private double[] parseEmbedding(String json) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }

            json = json.trim();

            if (!json.startsWith("[") || !json.endsWith("]")) {
                System.out.println("Format embedding incorrect.");
                return null;
            }

            json = json.substring(1, json.length() - 1);

            if (json.isBlank()) {
                return null;
            }

            String[] parts = json.split(",");
            double[] embedding = new double[parts.length];

            for (int i = 0; i < parts.length; i++) {
                embedding[i] = Double.parseDouble(parts[i].trim());
            }

            return embedding;

        } catch (Exception e) {
            System.out.println("Erreur parseEmbedding : " + e.getMessage());
            return null;
        }
    }

    private double cosineDistance(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return Double.MAX_VALUE;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return Double.MAX_VALUE;
        }

        return 1.0 - (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}