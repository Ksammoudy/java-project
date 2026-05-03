package entities;

public class UserContact {
    private final int id;
    private final String fullName;
    private final String email;

    public UserContact(int id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}
