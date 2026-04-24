package services;

import entities.UserOption;
import utils.MyConnection;
import utils.SchemaManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceUserDirectory {

    public static final String ROLE_VALORISATEUR = "VALORISATEUR";
    public static final String ROLE_CITOYEN = "CITOYEN";

    private Connection cnx;

    public ServiceUserDirectory() {
        this.cnx = null;
    }

    private Connection getConnection() throws SQLException {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyConnection.getInstance().getConnection();
            }
            if (cnx == null || cnx.isClosed()) {
                throw new SQLException("Connexion JDBC fermee ou indisponible.");
            }
            return cnx;
        } catch (IllegalStateException e) {
            throw new SQLException("Connexion JDBC indisponible: " + e.getMessage(), e);
        }
    }

    public List<UserOption> recupererValorisateurs() throws SQLException {
        List<UserOption> fromValorisateurTable = recupererDepuisTableValorisateur();
        if (!fromValorisateurTable.isEmpty()) {
            return fromValorisateurTable;
        }

        List<UserOption> list = recupererParRole(ROLE_VALORISATEUR);
        if (!list.isEmpty()) {
            return list;
        }
        return recupererDistinctIdsDepuis("appel_offre", "valorisateur_id", "Valorisateur");
    }

    public List<UserOption> recupererCitoyens() throws SQLException {
        List<UserOption> fromCitoyenTable = recupererDepuisTableCitoyen();
        if (!fromCitoyenTable.isEmpty()) {
            return fromCitoyenTable;
        }

        List<UserOption> list = recupererParRole(ROLE_CITOYEN);
        if (!list.isEmpty()) {
            return list;
        }
        return recupererDistinctIdsDepuis("reponse_offre", "citoyen_id", "Citoyen");
    }

    private List<UserOption> recupererDepuisTableValorisateur() throws SQLException {
        if (!tableExists("valorisateur") || !columnExists("valorisateur", "id")) {
            return new ArrayList<>();
        }

        List<String> columns = getColumns("valorisateur");
        String societeColumn = firstColumnStartingWith(columns, "nom_soc");
        String emailColumn = firstExisting(columns, "email", "mail");

        String sql = "SELECT "
                + q("id") + " AS id_value, "
                + exprOrNull(societeColumn) + " AS name_value, "
                + exprOrNull(emailColumn) + " AS email_value "
                + "FROM " + q("valorisateur") + " ORDER BY " + q("id");

        List<UserOption> list = new ArrayList<>();
        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_value");
                String name = trimToNull(rs.getString("name_value"));
                String email = trimToNull(rs.getString("email_value"));
                String label = buildLabel(id, name == null ? "Valorisateur" : name, email);
                list.add(new UserOption(id, label, ROLE_VALORISATEUR));
            }
        }
        return list;
    }

    private List<UserOption> recupererDepuisTableCitoyen() throws SQLException {
        if (!tableExists("citoyen") || !columnExists("citoyen", "id")) {
            return new ArrayList<>();
        }

        List<String> columns = getColumns("citoyen");
        String nomColumn = firstExisting(columns, "nom", "name");
        String prenomColumn = firstExisting(columns, "prenom", "first_name", "firstname");
        String emailColumn = firstExisting(columns, "email", "mail");

        String sql = "SELECT "
                + q("id") + " AS id_value, "
                + exprOrNull(nomColumn) + " AS nom_value, "
                + exprOrNull(prenomColumn) + " AS prenom_value, "
                + exprOrNull(emailColumn) + " AS email_value "
                + "FROM " + q("citoyen") + " ORDER BY " + q("id");

        List<UserOption> list = new ArrayList<>();
        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_value");
                String nom = trimToNull(rs.getString("nom_value"));
                String prenom = trimToNull(rs.getString("prenom_value"));
                String email = trimToNull(rs.getString("email_value"));

                String fullName;
                if (nom == null && prenom == null) {
                    fullName = "Citoyen #" + id;
                } else if (nom == null) {
                    fullName = prenom;
                } else if (prenom == null) {
                    fullName = nom;
                } else {
                    fullName = nom + " " + prenom;
                }

                String label = buildLabel(id, fullName, email);
                list.add(new UserOption(id, label, ROLE_CITOYEN));
            }
        }
        return list;
    }

    private List<UserOption> recupererParRole(String roleKeyword) throws SQLException {
        try {
            SchemaManager.ensureCoreForeignKeys();
        } catch (Exception e) {
            System.err.println("[WARN] SchemaManager ignore in ServiceUserDirectory: " + e.getMessage());
        }
        String userTable = detectUserTable();
        if (userTable == null) {
            return new ArrayList<>();
        }

        List<String> columns = getColumns(userTable);
        String idColumn = firstExisting(columns, "id");
        if (idColumn == null) {
            return new ArrayList<>();
        }
        String nameColumn = firstExisting(columns, "nom", "username", "full_name", "name", "prenom");
        String emailColumn = firstExisting(columns, "email", "mail");
        String rolesColumn = firstExisting(columns, "roles", "role", "type", "user_type");

        String sql = "SELECT "
                + q(idColumn) + " AS id_value, "
                + exprOrNull(nameColumn) + " AS name_value, "
                + exprOrNull(emailColumn) + " AS email_value, "
                + exprOrNull(rolesColumn) + " AS roles_value "
                + "FROM " + q(userTable) + " ORDER BY " + q(idColumn);

        List<UserOption> all = new ArrayList<>();
        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_value");
                String name = trimToNull(rs.getString("name_value"));
                String email = trimToNull(rs.getString("email_value"));
                String rolesText = trimToNull(rs.getString("roles_value"));

                String label = buildLabel(id, name, email);
                all.add(new UserOption(id, label, rolesText));
            }
        }

        if (all.isEmpty()) {
            return all;
        }

        List<UserOption> filtered = new ArrayList<>();
        for (UserOption option : all) {
            if (matchesRole(option.getRolesText(), roleKeyword)) {
                filtered.add(option);
            }
        }

        if (!filtered.isEmpty()) {
            return filtered;
        }

        return all;
    }

    private List<UserOption> recupererDistinctIdsDepuis(String table, String idColumn, String prefix) throws SQLException {
        List<UserOption> list = new ArrayList<>();
        if (!tableExists(table) || !columnExists(table, idColumn)) {
            return list;
        }

        String sql = "SELECT DISTINCT " + q(idColumn) + " AS id_value FROM " + q(table) + " WHERE " + q(idColumn)
                + " IS NOT NULL ORDER BY " + q(idColumn);
        try (PreparedStatement pst = getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_value");
                list.add(new UserOption(id, prefix + " #" + id, null));
            }
        }
        return list;
    }

    private String detectUserTable() throws SQLException {
        String[] candidates = new String[]{"user", "users", "utilisateur", "utilisateurs"};
        for (String table : candidates) {
            if (tableExists(table) && columnExists(table, "id")) {
                return table;
            }
        }
        return null;
    }

    private List<String> getColumns(String tableName) throws SQLException {
        List<String> cols = new ArrayList<>();
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND LOWER(table_name)=LOWER(?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, tableName);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cols.add(rs.getString("column_name").toLowerCase(Locale.ROOT));
                }
            }
        }
        return cols;
    }

    private boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND LOWER(table_name)=LOWER(?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, tableName);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND LOWER(table_name)=LOWER(?) AND LOWER(column_name)=LOWER(?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String firstExisting(List<String> availableColumns, String... candidates) {
        for (String c : candidates) {
            if (availableColumns.contains(c.toLowerCase(Locale.ROOT))) {
                return c;
            }
        }
        return null;
    }

    private String firstColumnStartingWith(List<String> availableColumns, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        for (String c : availableColumns) {
            if (c.startsWith(normalizedPrefix)) {
                return c;
            }
        }
        return null;
    }

    private boolean matchesRole(String rolesText, String roleKeyword) {
        if (rolesText == null || roleKeyword == null) {
            return false;
        }
        String normalized = rolesText.toUpperCase(Locale.ROOT);
        return normalized.contains(roleKeyword.toUpperCase(Locale.ROOT));
    }

    private String buildLabel(int id, String name, String email) {
        String base = name == null ? "Utilisateur #" + id : name;
        String withId = base + " (#" + id + ")";
        if (email != null) {
            return withId + " - " + email;
        }
        return withId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String exprOrNull(String column) {
        return column == null ? "NULL" : q(column);
    }

    private String q(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }
}
