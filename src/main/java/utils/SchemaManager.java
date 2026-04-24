package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SchemaManager {

    private static boolean initialized;

    private SchemaManager() {
    }

    public static synchronized void ensureCoreForeignKeys() throws SQLException {
        if (initialized) {
            return;
        }

        Connection cnx = MyConnection.getInstance().getConnection();
        ensureTableExists(cnx, "appel_offre");
        ensureTableExists(cnx, "reponse_offre");

        ensureForeignKey(
                cnx,
                "reponse_offre",
                "appel_offre_id",
                "appel_offre",
                "id",
                "fk_reponse_offre_appel",
                "CASCADE",
                "CASCADE"
        );

        boolean hasDedicatedValorisateurTable = tableExists(cnx, "valorisateur") && columnExists(cnx, "valorisateur", "id");
        boolean hasDedicatedCitoyenTable = tableExists(cnx, "citoyen") && columnExists(cnx, "citoyen", "id");

        if (hasDedicatedValorisateurTable) {
            ensureForeignKey(
                    cnx,
                    "appel_offre",
                    "valorisateur_id",
                    "valorisateur",
                    "id",
                    "fk_appel_offre_valorisateur",
                    "RESTRICT",
                    "RESTRICT"
            );
        }

        if (hasDedicatedCitoyenTable) {
            ensureForeignKey(
                    cnx,
                    "reponse_offre",
                    "citoyen_id",
                    "citoyen",
                    "id",
                    "fk_reponse_offre_citoyen",
                    "RESTRICT",
                    "RESTRICT"
            );
        }

        if (!hasDedicatedValorisateurTable || !hasDedicatedCitoyenTable) {
            String userTable = detectUserTable(cnx);
            if (userTable != null) {
                if (!hasDedicatedValorisateurTable) {
                    ensureForeignKey(
                            cnx,
                            "appel_offre",
                            "valorisateur_id",
                            userTable,
                            "id",
                            "fk_appel_offre_valorisateur",
                            "RESTRICT",
                            "CASCADE"
                    );
                }
                if (!hasDedicatedCitoyenTable) {
                    ensureForeignKey(
                            cnx,
                            "reponse_offre",
                            "citoyen_id",
                            userTable,
                            "id",
                            "fk_reponse_offre_citoyen",
                            "RESTRICT",
                            "CASCADE"
                    );
                }
            }
        }

        initialized = true;
    }

    private static void ensureTableExists(Connection cnx, String tableName) throws SQLException {
        if (!tableExists(cnx, tableName)) {
            throw new SQLException("Table introuvable: " + tableName);
        }
    }

    private static String detectUserTable(Connection cnx) throws SQLException {
        String[] candidates = new String[]{"user", "users", "utilisateur", "utilisateurs"};
        for (String table : candidates) {
            if (tableExists(cnx, table) && columnExists(cnx, table, "id")) {
                return table;
            }
        }
        return null;
    }

    private static boolean tableExists(Connection cnx, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER(?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, table);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean columnExists(Connection cnx, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND LOWER(table_name)=LOWER(?) AND LOWER(column_name)=LOWER(?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, table);
            pst.setString(2, column);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void ensureForeignKey(
            Connection cnx,
            String sourceTable,
            String sourceColumn,
            String targetTable,
            String targetColumn,
            String expectedConstraintName,
            String deleteRule,
            String updateRule
    ) throws SQLException {
        ensureTableExists(cnx, sourceTable);
        ensureTableExists(cnx, targetTable);
        if (!columnExists(cnx, sourceTable, sourceColumn)) {
            throw new SQLException("Colonne introuvable: " + sourceTable + "." + sourceColumn);
        }
        if (!columnExists(cnx, targetTable, targetColumn)) {
            throw new SQLException("Colonne introuvable: " + targetTable + "." + targetColumn);
        }

        ensureIndex(cnx, sourceTable, sourceColumn);

        ForeignKeyInfo existing = findForeignKey(cnx, sourceTable, sourceColumn);
        if (existing != null) {
            if (existing.matches(targetTable, targetColumn, deleteRule, updateRule)) {
                return;
            }
            try (Statement st = cnx.createStatement()) {
                st.execute("ALTER TABLE " + q(sourceTable) + " DROP FOREIGN KEY " + q(existing.constraintName));
            }
        }

        String safeConstraintName = truncateConstraintName(expectedConstraintName);
        String alter = "ALTER TABLE " + q(sourceTable)
                + " ADD CONSTRAINT " + q(safeConstraintName)
                + " FOREIGN KEY (" + q(sourceColumn) + ")"
                + " REFERENCES " + q(targetTable) + " (" + q(targetColumn) + ")"
                + " ON DELETE " + deleteRule
                + " ON UPDATE " + updateRule;
        try (Statement st = cnx.createStatement()) {
            st.execute(alter);
        }
    }

    private static void ensureIndex(Connection cnx, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND LOWER(table_name)=LOWER(?) AND LOWER(column_name)=LOWER(?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, table);
            pst.setString(2, column);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        String indexName = truncateConstraintName("idx_" + table + "_" + column);
        String ddl = "CREATE INDEX " + q(indexName) + " ON " + q(table) + " (" + q(column) + ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(ddl);
        }
    }

    private static ForeignKeyInfo findForeignKey(Connection cnx, String sourceTable, String sourceColumn) throws SQLException {
        String sql = "SELECT kcu.CONSTRAINT_NAME, kcu.REFERENCED_TABLE_NAME, kcu.REFERENCED_COLUMN_NAME, "
                + "rc.DELETE_RULE, rc.UPDATE_RULE "
                + "FROM information_schema.KEY_COLUMN_USAGE kcu "
                + "LEFT JOIN information_schema.REFERENTIAL_CONSTRAINTS rc "
                + "ON rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA "
                + "AND rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME "
                + "WHERE kcu.CONSTRAINT_SCHEMA = DATABASE() "
                + "AND LOWER(kcu.TABLE_NAME)=LOWER(?) "
                + "AND LOWER(kcu.COLUMN_NAME)=LOWER(?) "
                + "AND kcu.REFERENCED_TABLE_NAME IS NOT NULL";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, sourceTable);
            pst.setString(2, sourceColumn);
            try (ResultSet rs = pst.executeQuery()) {
                List<ForeignKeyInfo> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(new ForeignKeyInfo(
                            rs.getString("CONSTRAINT_NAME"),
                            rs.getString("REFERENCED_TABLE_NAME"),
                            rs.getString("REFERENCED_COLUMN_NAME"),
                            rs.getString("DELETE_RULE"),
                            rs.getString("UPDATE_RULE")
                    ));
                }
                return entries.isEmpty() ? null : entries.get(0);
            }
        }
    }

    private static String truncateConstraintName(String value) {
        if (value.length() <= 62) {
            return value;
        }
        return value.substring(0, 62);
    }

    private static String q(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private static final class ForeignKeyInfo {
        private final String constraintName;
        private final String targetTable;
        private final String targetColumn;
        private final String deleteRule;
        private final String updateRule;

        private ForeignKeyInfo(String constraintName, String targetTable, String targetColumn, String deleteRule, String updateRule) {
            this.constraintName = constraintName;
            this.targetTable = targetTable;
            this.targetColumn = targetColumn;
            this.deleteRule = deleteRule;
            this.updateRule = updateRule;
        }

        private boolean matches(String expectedTargetTable, String expectedTargetColumn, String expectedDeleteRule, String expectedUpdateRule) {
            return equalsIgnoreCase(targetTable, expectedTargetTable)
                    && equalsIgnoreCase(targetColumn, expectedTargetColumn)
                    && equalsIgnoreCase(deleteRule, expectedDeleteRule)
                    && equalsIgnoreCase(updateRule, expectedUpdateRule);
        }

        private boolean equalsIgnoreCase(String a, String b) {
            if (a == null || b == null) {
                return false;
            }
            return a.toLowerCase(Locale.ROOT).equals(b.toLowerCase(Locale.ROOT));
        }
    }
}
