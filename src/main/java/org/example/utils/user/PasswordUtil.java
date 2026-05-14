package org.example.utils.user;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        // Normalize PHP-style $2y$ prefix to $2a$ which jBCrypt understands
        String normalizedHash = hashedPassword.startsWith("$2y$")
                ? "$2a$" + hashedPassword.substring(4)
                : hashedPassword;

        // Guard against non-BCrypt hashes stored in the DB (plain text, MD5, SHA, etc.)
        if (!normalizedHash.startsWith("$2a$") && !normalizedHash.startsWith("$2b$")) {
            System.err.println("⚠️ Password in DB is not a valid BCrypt hash for this user.");
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, normalizedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Invalid BCrypt hash format: " + e.getMessage());
            return false;
        }
    }
}