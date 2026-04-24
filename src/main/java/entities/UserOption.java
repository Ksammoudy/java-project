package entities;

public class UserOption {
    private final int id;
    private final String label;
    private final String rolesText;

    public UserOption(int id, String label, String rolesText) {
        this.id = id;
        this.label = label;
        this.rolesText = rolesText;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getRolesText() {
        return rolesText;
    }

    @Override
    public String toString() {
        return label;
    }
}
