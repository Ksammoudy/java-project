package org.example.services;

import org.example.entities.DeclarationDechet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DeclarationDechetJdbcService extends AbstractJdbcService implements BaseCrudService<DeclarationDechet> {
    private static final String TABLE_NAME = "declaration_dechet";

    private static final String SELECT_WITH_JOINS = """
        SELECT d.id, d.description, d.statut, d.type_dechet_id, td.libelle AS type_dechet_libelle,
               d.photo, d.latitude, d.longitude, d.quantite, d.unite, d.created_at, d.score_ia,
               d.points_attribues, d.qr_code, d.citoyen_id, u.email AS citoyen_email,
               d.valorisateur_confirmateur_id, d.date_confirmation, d.statut_historique, d.deleted_at
        FROM declaration_dechet d
        LEFT JOIN type_dechet td ON td.id = d.type_dechet_id
        LEFT JOIN `user` u ON u.id = d.citoyen_id
        """;

    @Override
    public List<DeclarationDechet> findAll() throws SQLException {
        List<DeclarationDechet> declarations = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(SELECT_WITH_JOINS + " ORDER BY d.created_at DESC, d.id DESC");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                declarations.add(mapRow(resultSet));
            }
        }
        return declarations;
    }

    @Override
    public Optional<DeclarationDechet> findById(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(SELECT_WITH_JOINS + " WHERE d.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Declarations d'un citoyen (hors fiches archivees / soft-delete).
     */
    public List<DeclarationDechet> findByCitoyenId(int citoyenId) throws SQLException {
        List<DeclarationDechet> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " WHERE d.citoyen_id = ? AND d.deleted_at IS NULL ORDER BY d.created_at DESC, d.id DESC";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, citoyenId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapRow(resultSet));
                }
            }
        }
        return list;
    }

    @Override
    public DeclarationDechet create(DeclarationDechet entity) throws SQLException {
        System.out.println("[DeclarationDechet][DEBUG] Methode create() appelee");
        if (entity == null) {
            throw new SQLException("DeclarationDechet null.");
        }

        if (entity.getStatut() == null || entity.getStatut().isBlank()) {
            entity.setStatut("EN_ATTENTE");
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        if (entity.getPointsAttribues() == null) {
            entity.setPointsAttribues(0);
        }

        validateMandatoryEntity(entity);

        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Connexion JDBC indisponible.");
        }
        logConnectionDiagnostics(connection);
        ensureTypeDechetExists(connection, entity.getTypeDechetId());

        String sql = """
            INSERT INTO declaration_dechet (
                description,
                statut,
                photo,
                latitude,
                longitude,
                quantite,
                unite,
                created_at,
                type_dechet_id,
                score_ia,
                citoyen_id,
                points_attribues,
                qr_code,
                valorisateur_confirmateur_id,
                date_confirmation,
                statut_historique,
                deleted_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Date createdAtDate = Date.valueOf(entity.getCreatedAt().toLocalDate());

        System.out.println("[DeclarationDechet][DEBUG] Requete INSERT fixe: " + sql.replace('\n', ' ').replace('\r', ' '));
        System.out.println("[DeclarationDechet][DEBUG] p1 description=" + entity.getDescription());
        System.out.println("[DeclarationDechet][DEBUG] p2 statut=" + entity.getStatut());
        System.out.println("[DeclarationDechet][DEBUG] p3 photo=" + entity.getPhoto());
        System.out.println("[DeclarationDechet][DEBUG] p4 latitude=" + entity.getLatitude());
        System.out.println("[DeclarationDechet][DEBUG] p5 longitude=" + entity.getLongitude());
        System.out.println("[DeclarationDechet][DEBUG] p6 quantite=" + entity.getQuantite());
        System.out.println("[DeclarationDechet][DEBUG] p7 unite=" + entity.getUnite());
        System.out.println("[DeclarationDechet][DEBUG] p8 created_at(Date)=" + createdAtDate);
        System.out.println("[DeclarationDechet][DEBUG] p9 type_dechet_id=" + entity.getTypeDechetId());
        System.out.println("[DeclarationDechet][DEBUG] p10 score_ia=" + entity.getScoreIa());
        System.out.println("[DeclarationDechet][DEBUG] p11 citoyen_id=" + entity.getCitoyenId());
        System.out.println("[DeclarationDechet][DEBUG] p12 points_attribues=" + entity.getPointsAttribues());
        System.out.println("[DeclarationDechet][DEBUG] p13 qr_code=" + entity.getQrCode());
        System.out.println("[DeclarationDechet][DEBUG] p14 valorisateur_confirmateur_id=" + entity.getValorisateurConfirmateurId());
        System.out.println("[DeclarationDechet][DEBUG] p15 date_confirmation=" + entity.getDateConfirmation());
        System.out.println("[DeclarationDechet][DEBUG] p16 statut_historique=" + entity.getStatutHistoriqueJson());
        System.out.println("[DeclarationDechet][DEBUG] p17 deleted_at=" + entity.getDeletedAt());

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getDescription());
            statement.setString(2, entity.getStatut());
            statement.setString(3, entity.getPhoto());
            statement.setDouble(4, entity.getLatitude());
            statement.setDouble(5, entity.getLongitude());
            statement.setDouble(6, entity.getQuantite());
            statement.setString(7, entity.getUnite());
            statement.setDate(8, createdAtDate);
            statement.setInt(9, entity.getTypeDechetId());
            statement.setObject(10, entity.getScoreIa());
            statement.setObject(11, entity.getCitoyenId());
            statement.setInt(12, entity.getPointsAttribues());
            statement.setString(13, entity.getQrCode());
            statement.setObject(14, entity.getValorisateurConfirmateurId());
            statement.setTimestamp(15, toTimestamp(entity.getDateConfirmation()));
            statement.setString(16, entity.getStatutHistoriqueJson());
            statement.setTimestamp(17, toTimestamp(entity.getDeletedAt()));

            boolean autoCommit = connection.getAutoCommit();
            System.out.println("[DeclarationDechet][DEBUG] AutoCommit = " + autoCommit);
            System.out.println("[DeclarationDechet][DEBUG] Avant executeUpdate");
            int affected = statement.executeUpdate();
            if (!autoCommit) {
                connection.commit();
                System.out.println("[DeclarationDechet][DEBUG] commit() execute (autoCommit=false)");
            }
            System.out.println("[DeclarationDechet][DEBUG] executeUpdate affectedRows=" + affected);
            if (affected <= 0) {
                throw new SQLException("Aucune ligne n'a ete inseree dans declaration_dechet.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                    System.out.println("[DeclarationDechet][DEBUG] ID genere=" + entity.getId());
                }
            }

            logLastInsertedRow(connection);
        } catch (SQLException ex) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    System.err.println("[DeclarationDechet][DEBUG] rollback() execute suite a erreur SQL");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("[DeclarationDechet][DEBUG] Echec rollback: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            System.err.println("[DeclarationDechet][SQL] SQL ERROR: " + ex.getMessage());
            System.err.println("[DeclarationDechet][SQL] SQL STATE = " + ex.getSQLState());
            System.err.println("[DeclarationDechet][SQL] ERROR CODE = " + ex.getErrorCode());
            ex.printStackTrace();
            throw ex;
        }

        return entity;
    }

    @Override
    public boolean update(DeclarationDechet entity) throws SQLException {
        String sql = """
            UPDATE declaration_dechet
            SET description = ?, statut = ?, type_dechet_id = ?, photo = ?, latitude = ?, longitude = ?,
                quantite = ?, unite = ?, created_at = ?, score_ia = ?, points_attribues = ?, qr_code = ?,
                citoyen_id = ?, valorisateur_confirmateur_id = ?, date_confirmation = ?, statut_historique = ?,
                deleted_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            bindEntity(statement, entity);
            statement.setInt(18, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM declaration_dechet WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindEntity(PreparedStatement statement, DeclarationDechet entity) throws SQLException {
        statement.setString(1, entity.getDescription());
        statement.setString(2, entity.getStatut());
        statement.setObject(3, entity.getTypeDechetId());
        statement.setString(4, entity.getPhoto());
        statement.setObject(5, entity.getLatitude());
        statement.setObject(6, entity.getLongitude());
        statement.setObject(7, entity.getQuantite());
        statement.setString(8, entity.getUnite());
        statement.setTimestamp(9, toTimestamp(entity.getCreatedAt()));
        statement.setObject(10, entity.getScoreIa());
        statement.setObject(11, entity.getPointsAttribues());
        statement.setString(12, entity.getQrCode());
        statement.setObject(13, entity.getCitoyenId());
        statement.setObject(14, entity.getValorisateurConfirmateurId());
        statement.setTimestamp(15, toTimestamp(entity.getDateConfirmation()));
        statement.setString(16, entity.getStatutHistoriqueJson());
        statement.setTimestamp(17, toTimestamp(entity.getDeletedAt()));
    }

    private void validateMandatoryEntity(DeclarationDechet entity) throws SQLException {
        if (entity.getDescription() == null || entity.getDescription().isBlank()) {
            throw new SQLException("Champ obligatoire manquant: description.");
        }
        if (entity.getStatut() == null || entity.getStatut().isBlank()) {
            throw new SQLException("Champ obligatoire manquant: statut.");
        }
        if (entity.getTypeDechetId() == null) {
            throw new SQLException("Champ obligatoire manquant: type_dechet_id/id_type.");
        }
        if (entity.getTypeDechetId() <= 0) {
            throw new SQLException("Champ obligatoire invalide: type_dechet_id doit etre > 0.");
        }
        if (entity.getLatitude() == null || entity.getLongitude() == null) {
            throw new SQLException("Champs obligatoires manquants: latitude/longitude.");
        }
        if (entity.getQuantite() == null || entity.getQuantite() <= 0) {
            throw new SQLException("Champ obligatoire invalide: quantite doit etre > 0.");
        }
        if (entity.getUnite() == null || entity.getUnite().isBlank()) {
            throw new SQLException("Champ obligatoire manquant: unite.");
        }
        if (entity.getCreatedAt() == null) {
            throw new SQLException("Champ obligatoire manquant: created_at/date_declaration.");
        }
        if (entity.getPointsAttribues() == null) {
            throw new SQLException("Champ obligatoire manquant: points_attribues.");
        }
    }

    private void logConnectionDiagnostics(Connection connection) throws SQLException {
        String jdbcUrl = connection.getMetaData().getURL();
        boolean closed = connection.isClosed();
        System.out.println("[DeclarationDechet][DEBUG] JDBC URL = " + jdbcUrl);
        System.out.println("[DeclarationDechet][DEBUG] Connection ouverte = " + !closed);
        try (PreparedStatement ps = connection.prepareStatement("SELECT DATABASE()");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("[DeclarationDechet][DEBUG] Base active = " + rs.getString(1));
            }
        }
    }

    private void ensureTypeDechetExists(Connection connection, Integer typeDechetId) throws SQLException {
        String sql = "SELECT id FROM type_dechet WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, typeDechetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("FK invalide: type_dechet_id=" + typeDechetId + " n'existe pas dans type_dechet.");
                }
            }
        }
    }

    private void logLastInsertedRow(Connection connection) {
        String sql = """
            SELECT id, description, statut, latitude, longitude, quantite, unite, created_at, type_dechet_id, points_attribues
            FROM declaration_dechet
            ORDER BY id DESC
            LIMIT 1
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("[DeclarationDechet][DEBUG] Derniere ligne DB -> id=" + rs.getInt("id")
                        + ", description=" + rs.getString("description")
                        + ", statut=" + rs.getString("statut")
                        + ", lat=" + rs.getDouble("latitude")
                        + ", lon=" + rs.getDouble("longitude")
                        + ", quantite=" + rs.getDouble("quantite")
                        + ", unite=" + rs.getString("unite")
                        + ", created_at=" + rs.getDate("created_at")
                        + ", type_dechet_id=" + rs.getInt("type_dechet_id")
                        + ", points_attribues=" + rs.getInt("points_attribues"));
            } else {
                System.out.println("[DeclarationDechet][DEBUG] Aucune ligne trouvee apres INSERT.");
            }
        } catch (SQLException ex) {
            System.err.println("[DeclarationDechet][DEBUG] Impossible de lire la derniere ligne inseree: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private InsertPlan buildInsertPlan(Set<String> columns) throws SQLException {
        String typeColumn = preferExisting(columns, "type_dechet_id", "id_type");
        if (typeColumn == null) {
            throw new SQLException("Aucune colonne type detectee (attendu: type_dechet_id ou id_type).");
        }
        String dateColumn = preferExisting(columns, "created_at", "date_declaration");
        if (dateColumn == null) {
            throw new SQLException("Aucune colonne date detectee (attendu: created_at ou date_declaration).");
        }

        List<String> insertColumns = new ArrayList<>();
        List<Binder> binders = new ArrayList<>();

        addRequired(insertColumns, binders, columns, "description", (s, i, e) -> s.setString(i, e.getDescription()));
        addRequired(insertColumns, binders, columns, "statut", (s, i, e) -> s.setString(i, e.getStatut()));
        insertColumns.add(typeColumn);
        binders.add((s, i, e) -> s.setObject(i, e.getTypeDechetId()));

        if (columns.contains("photo")) {
            insertColumns.add("photo");
            binders.add((s, i, e) -> s.setString(i, e.getPhoto()));
        }

        addRequired(insertColumns, binders, columns, "latitude", (s, i, e) -> s.setObject(i, e.getLatitude()));
        addRequired(insertColumns, binders, columns, "longitude", (s, i, e) -> s.setObject(i, e.getLongitude()));
        addRequired(insertColumns, binders, columns, "quantite", (s, i, e) -> s.setObject(i, e.getQuantite()));
        addRequired(insertColumns, binders, columns, "unite", (s, i, e) -> s.setString(i, e.getUnite()));

        insertColumns.add(dateColumn);
        binders.add((s, i, e) -> {
            if ("date_declaration".equals(dateColumn)) {
                s.setTimestamp(i, toTimestamp(e.getCreatedAt()));
            } else {
                s.setTimestamp(i, toTimestamp(e.getCreatedAt()));
            }
        });

        if (columns.contains("citoyen_id")) {
            insertColumns.add("citoyen_id");
            binders.add((s, i, e) -> s.setObject(i, e.getCitoyenId()));
        }
        if (columns.contains("score_ia")) {
            insertColumns.add("score_ia");
            binders.add((s, i, e) -> s.setObject(i, e.getScoreIa()));
        }
        if (columns.contains("points_attribues")) {
            insertColumns.add("points_attribues");
            binders.add((s, i, e) -> s.setObject(i, e.getPointsAttribues()));
        }
        if (columns.contains("qr_code")) {
            insertColumns.add("qr_code");
            binders.add((s, i, e) -> s.setString(i, e.getQrCode()));
        }
        if (columns.contains("valorisateur_confirmateur_id")) {
            insertColumns.add("valorisateur_confirmateur_id");
            binders.add((s, i, e) -> s.setObject(i, e.getValorisateurConfirmateurId()));
        }
        if (columns.contains("date_confirmation")) {
            insertColumns.add("date_confirmation");
            binders.add((s, i, e) -> s.setTimestamp(i, toTimestamp(e.getDateConfirmation())));
        }
        if (columns.contains("statut_historique")) {
            insertColumns.add("statut_historique");
            binders.add((s, i, e) -> s.setString(i, e.getStatutHistoriqueJson()));
        } else if (columns.contains("statut_historique_json")) {
            insertColumns.add("statut_historique_json");
            binders.add((s, i, e) -> s.setString(i, e.getStatutHistoriqueJson()));
        }
        if (columns.contains("deleted_at")) {
            insertColumns.add("deleted_at");
            binders.add((s, i, e) -> s.setTimestamp(i, toTimestamp(e.getDeletedAt())));
        }

        String placeholders = String.join(", ", insertColumns.stream().map(c -> "?").toList());
        String sql = "INSERT INTO declaration_dechet (" + String.join(", ", insertColumns) + ") VALUES (" + placeholders + ")";
        return new InsertPlan(sql, insertColumns, binders);
    }

    private TableSchema loadTableSchema(Connection connection) throws SQLException {
        Set<String> columns = new HashSet<>();
        Map<String, ColumnMeta> metas = new LinkedHashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, TABLE_NAME, "%")) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if (col != null) {
                    String key = col.toLowerCase();
                    columns.add(key);
                    int nullableCode = rs.getInt("NULLABLE");
                    boolean nullable = nullableCode == DatabaseMetaData.columnNullable;
                    String defaultValue = rs.getString("COLUMN_DEF");
                    String autoInc = rs.getString("IS_AUTOINCREMENT");
                    boolean autoIncrement = "YES".equalsIgnoreCase(autoInc);
                    metas.put(key, new ColumnMeta(key, nullable, defaultValue, autoIncrement));
                }
            }
        }
        if (columns.isEmpty()) {
            throw new SQLException("Impossible de lire les colonnes de la table declaration_dechet.");
        }
        return new TableSchema(columns, metas);
    }

    private static String preferExisting(Set<String> columns, String... candidates) {
        for (String c : candidates) {
            if (columns.contains(c.toLowerCase())) {
                return c;
            }
        }
        return null;
    }

    private static void addRequired(List<String> columnsOut,
                                    List<Binder> bindersOut,
                                    Set<String> tableColumns,
                                    String column,
                                    Binder binder) throws SQLException {
        String normalized = column.toLowerCase();
        if (!tableColumns.contains(normalized)) {
            throw new SQLException("Colonne obligatoire absente dans declaration_dechet: " + column);
        }
        columnsOut.add(column);
        bindersOut.add(binder);
    }

    private void applyDefaultsForSchema(DeclarationDechet entity,
                                        TableSchema schema,
                                        Connection connection,
                                        InsertPlan plan) throws SQLException {
        if ((entity.getStatut() == null || entity.getStatut().isBlank()) && plan.columns().contains("statut")) {
            entity.setStatut("EN_ATTENTE");
        }
        if (entity.getCreatedAt() == null && (plan.columns().contains("created_at") || plan.columns().contains("date_declaration"))) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        ColumnMeta photo = schema.meta("photo");
        if (photo != null && photo.notNullableWithoutDefault() && entity.getPhoto() == null) {
            entity.setPhoto("");
        }

        ColumnMeta pointsAttribues = schema.meta("points_attribues");
        if (pointsAttribues != null && pointsAttribues.notNullableWithoutDefault() && entity.getPointsAttribues() == null) {
            entity.setPointsAttribues(0);
        }

        ColumnMeta citoyen = schema.meta("citoyen_id");
        if (citoyen != null && citoyen.notNullableWithoutDefault() && entity.getCitoyenId() == null) {
            entity.setCitoyenId(resolveFallbackCitoyenId(connection));
        }
    }

    private Integer resolveFallbackCitoyenId(Connection connection) {
        String sqlCitizen = "SELECT id FROM `user` WHERE UPPER(type) IN ('CITIZEN','CITOYEN') ORDER BY id ASC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sqlCitizen);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
            // fallback below
        }

        String sqlAnyUser = "SELECT id FROM `user` ORDER BY id ASC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sqlAnyUser);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
            // no fallback available
        }
        return null;
    }

    private void validateNotNullColumns(DeclarationDechet entity, TableSchema schema, InsertPlan plan) throws SQLException {
        for (String column : plan.columns()) {
            ColumnMeta meta = schema.meta(column);
            if (meta == null || !meta.notNullableWithoutDefault()) {
                continue;
            }
            Object value = extractEntityValueForColumn(entity, column);
            if (value == null) {
                throw new SQLException("Colonne obligatoire NULL detectee avant INSERT: " + column);
            }
            if (value instanceof String s && s.isBlank()) {
                throw new SQLException("Colonne obligatoire vide detectee avant INSERT: " + column);
            }
        }
    }

    private Object extractEntityValueForColumn(DeclarationDechet entity, String column) {
        return switch (column) {
            case "description" -> entity.getDescription();
            case "statut" -> entity.getStatut();
            case "type_dechet_id", "id_type" -> entity.getTypeDechetId();
            case "photo" -> entity.getPhoto();
            case "latitude" -> entity.getLatitude();
            case "longitude" -> entity.getLongitude();
            case "quantite" -> entity.getQuantite();
            case "unite" -> entity.getUnite();
            case "created_at", "date_declaration" -> entity.getCreatedAt();
            case "score_ia" -> entity.getScoreIa();
            case "points_attribues" -> entity.getPointsAttribues();
            case "qr_code" -> entity.getQrCode();
            case "citoyen_id" -> entity.getCitoyenId();
            case "valorisateur_confirmateur_id" -> entity.getValorisateurConfirmateurId();
            case "date_confirmation" -> entity.getDateConfirmation();
            case "statut_historique", "statut_historique_json" -> entity.getStatutHistoriqueJson();
            case "deleted_at" -> entity.getDeletedAt();
            default -> null;
        };
    }

    private void logBeforeInsert(DeclarationDechet entity, InsertPlan plan) {
        System.out.println("[DeclarationDechet][DEBUG] Colonnes table detectees pour INSERT: " + plan.columns());
        System.out.println("[DeclarationDechet][DEBUG] Requete INSERT: " + plan.sql());
        System.out.println("DEBUG DECLARATION : " + entity);
        System.out.println("[DeclarationDechet][DEBUG] Valeurs formulaire -> type="
                + entity.getTypeDechetId()
                + ", quantite=" + entity.getQuantite()
                + ", unite=" + entity.getUnite()
                + ", description=" + entity.getDescription()
                + ", latitude=" + entity.getLatitude()
                + ", longitude=" + entity.getLongitude()
                + ", photo=" + entity.getPhoto()
                + ", statut=" + entity.getStatut()
                + ", createdAt=" + entity.getCreatedAt()
                + ", citoyenId=" + entity.getCitoyenId());
    }

    private void logSqlException(SQLException ex) {
        System.err.println("[DeclarationDechet][SQL] Erreur SQL : " + ex.getMessage());
        System.err.println("[DeclarationDechet][SQL] SQLState=" + ex.getSQLState() + ", ErrorCode=" + ex.getErrorCode());
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement statement, int index, DeclarationDechet entity) throws SQLException;
    }

    private record InsertPlan(String sql, List<String> columns, List<Binder> binders) {
    }

    private record TableSchema(Set<String> columns, Map<String, ColumnMeta> metas) {
        private ColumnMeta meta(String column) {
            return metas.get(column.toLowerCase());
        }
    }

    private record ColumnMeta(String name, boolean nullable, String defaultValue, boolean autoIncrement) {
        private boolean notNullableWithoutDefault() {
            return !nullable && defaultValue == null && !autoIncrement;
        }
    }

    private DeclarationDechet mapRow(ResultSet resultSet) throws SQLException {
        DeclarationDechet declaration = new DeclarationDechet();
        declaration.setId(resultSet.getInt("id"));
        declaration.setDescription(resultSet.getString("description"));
        declaration.setStatut(resultSet.getString("statut"));
        declaration.setTypeDechetId(resultSet.getObject("type_dechet_id", Integer.class));
        declaration.setTypeDechetLibelle(resultSet.getString("type_dechet_libelle"));
        declaration.setPhoto(resultSet.getString("photo"));
        declaration.setLatitude(resultSet.getObject("latitude", Double.class));
        declaration.setLongitude(resultSet.getObject("longitude", Double.class));
        declaration.setQuantite(resultSet.getObject("quantite", Double.class));
        declaration.setUnite(resultSet.getString("unite"));
        declaration.setCreatedAt(getLocalDateTime(resultSet, "created_at"));
        declaration.setScoreIa(resultSet.getObject("score_ia", Double.class));
        declaration.setPointsAttribues(resultSet.getObject("points_attribues", Integer.class));
        declaration.setQrCode(resultSet.getString("qr_code"));
        declaration.setCitoyenId(resultSet.getObject("citoyen_id", Integer.class));
        declaration.setCitoyenEmail(resultSet.getString("citoyen_email"));
        declaration.setValorisateurConfirmateurId(resultSet.getObject("valorisateur_confirmateur_id", Integer.class));
        declaration.setDateConfirmation(getLocalDateTime(resultSet, "date_confirmation"));
        declaration.setStatutHistoriqueJson(resultSet.getString("statut_historique"));
        declaration.setDeletedAt(getLocalDateTime(resultSet, "deleted_at"));
        return declaration;
    }
}
