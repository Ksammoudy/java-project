package org.example.models;

public class SocialLoginResult {
    private final boolean existingUser;
    private final User user;
    private final String email;
    private final String fullName;
    private final String provider;

    public SocialLoginResult(boolean existingUser, User user, String email, String fullName, String provider) {
        this.existingUser = existingUser;
        this.user = user;
        this.email = email;
        this.fullName = fullName;
        this.provider = provider;
    }

    public boolean isExistingUser() {
        return existingUser;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProvider() {
        return provider;
    }
}