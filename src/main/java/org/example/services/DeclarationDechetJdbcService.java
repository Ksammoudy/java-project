package org.example.services;

import org.example.entities.DeclarationDechet;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DeclarationDechetJdbcService extends AbstractJdbcService implements BaseCrudService<DeclarationDechet> {
    private static final String TABLE_NAME = "declaration_dechet";
    private static final String QR_API_BASE_URL = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=";

    private static volatile boolean qrSchemaInitialized;

    private static final String SELECT_WITH_JOINS = """
        SELECT d.id, d.description, d.statut, d.type_dechet_id, td.libelle AS type_dechet_libelle,
               d.photo, d.latitude, d.longitude, d.quantite, d.unite, d.created_at, d.score_ia,
               d.points_attribues, d.qr_code, d.qr_url, d.validated_by_qr, d.validated_at, d.valorisateur_id,
               d.citoyen_id, u.email AS citoyen_email,
               d.valorisateur_confirmateur_id, d.date_confirmation, d.statut_historique, d.deleted_at
        FROM declaration_dechet d
        LEFT JOIN type_dechet td ON td.id = d.type_dechet_id
        LEFT JOIN `user` u ON u.id = d.citoyen_id
        """;

    public enum QrValidationStatus {
        INVALID,
        ALREADY_VALIDATED,
        VALIDATED
    }

    public record QrValidationResult(QrValidationStatus status, DeclarationDechet declaration) {
    }

    @Override
    public List<DeclarationDechet> findAll() throws SQLException {
        ensureQrSchemaAndBackfill();
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
        ensureQrSchemaAndBackfill();
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
        ensureQrSchemaAndBackfill();
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

    public Optional<DeclarationDechet> findByQrCode(String qrCode) throws SQLException {
        ensureQrSchemaAndBackfill();
        String normalizedQrCode = normalizeQrCodeValue(qrCode);
        if (normalizedQrCode == null) {
            return Optional.empty();
        }

        String sql = SELECT_WITH_JOINS + " WHERE d.qr_code = ? LIMIT 1";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, normalizedQrCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    public QrValidationResult validateDeclarationByQrCode(String qrCode, int valorisateurId) throws SQLException {
        ensureQrSchemaAndBackfill();
        String normalizedQrCode = normalizeQrCodeValue(qrCode);
        if (normalizedQrCode == null) {
            return new QrValidationResult(QrValidationStatus.INVALID, null);
        }

        Optional<DeclarationDechet> found = findByQrCode(normalizedQrCode);
        if (found.isEmpty()) {
            return new QrValidationResult(QrValidationStatus.INVALID, null);
        }

        DeclarationDechet declaration = found.get();
        if (isAlreadyValidated(declaration)) {
            return new QrValidationResult(QrValidationStatus.ALREADY_VALIDATED, declaration);
        }

        LocalDateTime now = LocalDateTime.now();
        String sql = """
                UPDATE declaration_dechet
                SET statut = ?,
                    validated_by_qr = 1,
                    validated_at = ?,
                    valorisateur_id = ?,
                    valorisateur_confirmateur_id = ?,
                    date_confirmation = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, "VALIDATED");
            statement.setTimestamp(2, toTimestamp(now));
            statement.setInt(3, valorisateurId);
            statement.setInt(4, valorisateurId);
            statement.setTimestamp(5, toTimestamp(now));
            statement.setInt(6, declaration.getId());
            int affected = statement.executeUpdate();
            if (affected <= 0) {
                return new QrValidationResult(QrValidationStatus.INVALID, null);
            }
        }

        DeclarationDechet updated = findById(declaration.getId()).orElse(declaration);
        return new QrValidationResult(QrValidationStatus.VALIDATED, updated);
    }

    @Override
    public DeclarationDechet create(DeclarationDechet entity) throws SQLException {
        ensureQrSchemaAndBackfill();

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
        if (entity.getValidatedByQr() == null) {
            entity.setValidatedByQr(false);
        }

        validateMandatoryEntity(entity);

        Connection connection = getConnection();
        if (connection == null) {
            throw new SQLException("Connexion JDBC indisponible.");
        }
        logConnectionDiagnostics(connection);
        ensureTypeDechetExists(connection, entity.getTypeDechetId());

        TableSchema schema = loadTableSchema(connection);
        InsertPlan plan = buildInsertPlan(schema.columns());
        applyDefaultsForSchema(entity, schema, connection, plan);
        validateNotNullColumns(entity, schema, plan);
        logBeforeInsert(entity, plan);

        try (PreparedStatement statement = connection.prepareStatement(plan.sql(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < plan.binders().size(); i++) {
                plan.binders().get(i).bind(statement, i + 1, entity);
            }

            int affected = statement.executeUpdate();
            if (affected <= 0) {
                throw new SQLException("Aucune ligne n'a ete inseree dans declaration_dechet.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

            if (entity.getId() != null) {
                String qrCode = buildQrCode(entity.getId(), System.currentTimeMillis());
                String qrUrl = buildQrUrl(qrCode);
                updateQrFields(connection, entity.getId(), qrCode, qrUrl);
                entity.setQrCode(qrCode);
                entity.setQrUrl(qrUrl);
            }

            logLastInsertedRow(connection);
            return entity;
        } catch (SQLException ex) {
            logSqlException(ex);
            throw ex;
        }
    }

    @Override
    public boolean update(DeclarationDechet entity) throws SQLException {
        ensureQrSchemaAndBackfill();
        String sql = """
            UPDATE declaration_dechet
            SET description = ?, statut = ?, type_dechet_id = ?, photo = ?, latitude = ?, longitude = ?,
                quantite = ?, unite = ?, created_at = ?, score_ia = ?, points_attribues = ?, qr_code = ?,
                qr_url = ?, validated_by_qr = ?, validated_at = ?, valorisateur_id = ?, citoyen_id = ?,
                valorisateur_confirmateur_id = ?, date_confirmation = ?, statut_historique = ?, deleted_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            bindEntity(statement, entity);
            statement.setInt(22, entity.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        ensureQrSchemaAndBackfill();
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
        statement.setString(13, entity.getQrUrl());
        statement.setInt(14, toTinyInt(entity.getValidatedByQr()));
        statement.setTimestamp(15, toTimestamp(entity.getValidatedAt()));
        statement.setObject(16, entity.getValorisateurId());
        statement.setObject(17, entity.getCitoyenId());
        statement.setObject(18, entity.getValorisateurConfirmateurId());
        statement.setTimestamp(19, toTimestamp(entity.getDateConfirmation()));
        statement.setString(20, entity.getStatutHistoriqueJson());
        statement.setTimestamp(21, toTimestamp(entity.getDeletedAt()));
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
            SELECT id, description, statut, latitude, longitude, quantite, unite, created_at, type_dechet_id, points_attribues, qr_code, qr_url
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
                        + ", points_attribues=" + rs.getInt("points_attribues")
                        + ", qr_code=" + rs.getString("qr_code")
                        + ", qr_url=" + rs.getString("qr_url"));
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
        binders.add((s, i, e) -> s.setTimestamp(i, toTimestamp(e.getCreatedAt())));

        if (columns.contains("citoyen_id")) {
            insertColumns.add("citoyen_id");
            binders.add((s, i, e) -> s.setObject(i, e.getCitoyenId()));
        }
        if (columns.contains("score_ia")) {
            insertColumns.add("score_ia");
            binders.add((s, i, e) -> s.setObject(i, e.getScoreIa()));
        }
        String aiLabelColumn = preferExisting(columns,
                "ai_detected_label", "label_ia", "prediction_label", "detected_label", "ai_label");
        if (aiLabelColumn != null) {
            insertColumns.add(aiLabelColumn);
            binders.add((s, i, e) -> s.setString(i, e.getAiDetectedLabel()));
        }
        if (columns.contains("points_attribues")) {
            insertColumns.add("points_attribues");
            binders.add((s, i, e) -> s.setObject(i, e.getPointsAttribues()));
        }
        if (columns.contains("qr_code")) {
            insertColumns.add("qr_code");
            binders.add((s, i, e) -> s.setString(i, e.getQrCode()));
        }
        if (columns.contains("qr_url")) {
            insertColumns.add("qr_url");
            binders.add((s, i, e) -> s.setString(i, e.getQrUrl()));
        }
        if (columns.contains("validated_by_qr")) {
            insertColumns.add("validated_by_qr");
            binders.add((s, i, e) -> s.setInt(i, toTinyInt(e.getValidatedByQr())));
        }
        if (columns.contains("validated_at")) {
            insertColumns.add("validated_at");
            binders.add((s, i, e) -> s.setTimestamp(i, toTimestamp(e.getValidatedAt())));
        }
        if (columns.contains("valorisateur_id")) {
            insertColumns.add("valorisateur_id");
            binders.add((s, i, e) -> s.setObject(i, e.getValorisateurId()));
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
                    String key = col.toLowerCase(Locale.ROOT);
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
            if (columns.contains(c.toLowerCase(Locale.ROOT))) {
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
        String normalized = column.toLowerCase(Locale.ROOT);
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

        ColumnMeta validatedByQr = schema.meta("validated_by_qr");
        if (validatedByQr != null && validatedByQr.notNullableWithoutDefault() && entity.getValidatedByQr() == null) {
            entity.setValidatedByQr(false);
        }

        ColumnMeta citoyen = schema.meta("citoyen_id");
        if (citoyen != null && citoyen.notNullableWithoutDefault() && entity.getCitoyenId() == null) {
            entity.setCitoyenId(resolveFallbackCitoyenId(connection));
        }
    }

    private Integer resolveFallbackCitoyenId(Connection connection) {
        String sqlDemo = "SELECT id FROM `user` WHERE id = 1 LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sqlDemo);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
            // fallback below
        }

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
            case "ai_detected_label", "label_ia", "prediction_label", "detected_label", "ai_label" ->
                    entity.getAiDetectedLabel();
            case "points_attribues" -> entity.getPointsAttribues();
            case "qr_code" -> entity.getQrCode();
            case "qr_url" -> entity.getQrUrl();
            case "validated_by_qr" -> entity.getValidatedByQr();
            case "validated_at" -> entity.getValidatedAt();
            case "valorisateur_id" -> entity.getValorisateurId();
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
                + ", citoyenId=" + entity.getCitoyenId()
                + ", qrCode=" + entity.getQrCode()
                + ", qrUrl=" + entity.getQrUrl());
    }

    private void logSqlException(SQLException ex) {
        System.err.println("[DeclarationDechet][SQL] Erreur SQL : " + ex.getMessage());
        System.err.println("[DeclarationDechet][SQL] SQLState=" + ex.getSQLState() + ", ErrorCode=" + ex.getErrorCode());
    }

    private void ensureQrSchemaAndBackfill() throws SQLException {
        if (qrSchemaInitialized) {
            return;
        }

        synchronized (DeclarationDechetJdbcService.class) {
            if (qrSchemaInitialized) {
                return;
            }

            Connection connection = getConnection();
            ensureColumnExists(connection, "qr_code", "VARCHAR(255) NULL");
            ensureColumnExists(connection, "qr_url", "TEXT NULL");
            ensureColumnExists(connection, "validated_by_qr", "TINYINT DEFAULT 0");
            ensureColumnExists(connection, "validated_at", "DATETIME NULL");
            ensureColumnExists(connection, "valorisateur_id", "INT NULL");

            normalizeDuplicateQrCodes(connection);
            ensureUniqueIndexOnQrCode(connection);
            backfillQrFields(connection);

            qrSchemaInitialized = true;
        }
    }

    private void ensureColumnExists(Connection connection, String column, String ddlDefinition) throws SQLException {
        if (columnExists(connection, TABLE_NAME, column)) {
            return;
        }
        String sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + column + " " + ddlDefinition;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void ensureUniqueIndexOnQrCode(Connection connection) throws SQLException {
        if (hasUniqueIndexOnColumn(connection, TABLE_NAME, "qr_code")) {
            return;
        }

        String sql = "CREATE UNIQUE INDEX uq_declaration_dechet_qr_code ON declaration_dechet (qr_code)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private boolean hasUniqueIndexOnColumn(Connection connection, String table, String column) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND LOWER(table_name) = LOWER(?)
                  AND LOWER(column_name) = LOWER(?)
                  AND non_unique = 0
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND LOWER(table_name) = LOWER(?)
                  AND LOWER(column_name) = LOWER(?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private void normalizeDuplicateQrCodes(Connection connection) throws SQLException {
        String duplicatesSql = """
                SELECT qr_code
                FROM declaration_dechet
                WHERE qr_code IS NOT NULL AND TRIM(qr_code) <> ''
                GROUP BY qr_code
                HAVING COUNT(*) > 1
                """;

        List<String> duplicates = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(duplicatesSql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                duplicates.add(resultSet.getString(1));
            }
        }

        for (String duplicate : duplicates) {
            String selectIds = "SELECT id FROM declaration_dechet WHERE qr_code = ? ORDER BY id ASC";
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(selectIds)) {
                statement.setString(1, duplicate);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ids.add(resultSet.getInt(1));
                    }
                }
            }

            for (int i = 1; i < ids.size(); i++) {
                Integer id = ids.get(i);
                String newQrCode = buildQrCode(id, System.currentTimeMillis() + i);
                String newQrUrl = buildQrUrl(newQrCode);
                updateQrFields(connection, id, newQrCode, newQrUrl);
            }
        }
    }

    private void backfillQrFields(Connection connection) throws SQLException {
        String sql = "SELECT id, qr_code, qr_url FROM declaration_dechet ORDER BY id ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Integer id = resultSet.getObject("id", Integer.class);
                if (id == null) {
                    continue;
                }

                String qrCode = normalizeQrCodeValue(resultSet.getString("qr_code"));
                String qrUrl = normalizeQrCodeValue(resultSet.getString("qr_url"));

                boolean needsUpdate = false;
                if (qrCode == null) {
                    qrCode = buildQrCode(id, System.currentTimeMillis() + id);
                    needsUpdate = true;
                }

                String expectedQrUrl = buildQrUrl(qrCode);
                if (qrUrl == null || !qrUrl.equals(expectedQrUrl)) {
                    qrUrl = expectedQrUrl;
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    updateQrFields(connection, id, qrCode, qrUrl);
                }
            }
        }
    }

    private void updateQrFields(Connection connection, int id, String qrCode, String qrUrl) throws SQLException {
        String sql = "UPDATE declaration_dechet SET qr_code = ?, qr_url = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, qrCode);
            statement.setString(2, qrUrl);
            statement.setInt(3, id);
            statement.executeUpdate();
        }
    }

    public static String extractQrCodeFromQrLink(String input) {
        String normalized = normalizeQrCodeValue(input);
        if (normalized == null) {
            return null;
        }

        if (normalized.startsWith("DECL-")) {
            return normalized;
        }

        int dataIndex = normalized.indexOf("data=");
        if (dataIndex < 0) {
            return normalized.startsWith("DECL-") ? normalized : null;
        }

        String data = normalized.substring(dataIndex + 5);
        int amp = data.indexOf('&');
        if (amp >= 0) {
            data = data.substring(0, amp);
        }
        String decoded = URLDecoder.decode(data, StandardCharsets.UTF_8);
        return normalizeQrCodeValue(decoded);
    }

    private static String buildQrCode(int declarationId, long timestampMillis) {
        return "DECL-" + declarationId + "-" + timestampMillis;
    }

    public static String buildQrUrl(String qrCode) {
        return QR_API_BASE_URL + URLEncoder.encode(qrCode, StandardCharsets.UTF_8);
    }

    private static String normalizeQrCodeValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isAlreadyValidated(DeclarationDechet declaration) {
        String status = declaration.getStatut() == null ? "" : declaration.getStatut().trim().toUpperCase(Locale.ROOT);
        return Boolean.TRUE.equals(declaration.getValidatedByQr())
                || "VALIDATED".equals(status)
                || declaration.getValidatedAt() != null;
    }

    private int toTinyInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement statement, int index, DeclarationDechet entity) throws SQLException;
    }

    private record InsertPlan(String sql, List<String> columns, List<Binder> binders) {
    }

    private record TableSchema(Set<String> columns, Map<String, ColumnMeta> metas) {
        private ColumnMeta meta(String column) {
            return metas.get(column.toLowerCase(Locale.ROOT));
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
        declaration.setAiDetectedLabel(readFirstExistingString(resultSet,
                "ai_detected_label", "label_ia", "prediction_label", "detected_label", "ai_label"));
        declaration.setScoreIa(resultSet.getObject("score_ia", Double.class));
        declaration.setPointsAttribues(resultSet.getObject("points_attribues", Integer.class));
        declaration.setQrCode(resultSet.getString("qr_code"));
        declaration.setQrUrl(resultSet.getString("qr_url"));

        Integer validatedByQrInt = resultSet.getObject("validated_by_qr", Integer.class);
        declaration.setValidatedByQr(validatedByQrInt != null && validatedByQrInt == 1);

        declaration.setValidatedAt(getLocalDateTime(resultSet, "validated_at"));
        declaration.setValorisateurId(resultSet.getObject("valorisateur_id", Integer.class));
        declaration.setCitoyenId(resultSet.getObject("citoyen_id", Integer.class));
        declaration.setCitoyenEmail(resultSet.getString("citoyen_email"));
        declaration.setValorisateurConfirmateurId(resultSet.getObject("valorisateur_confirmateur_id", Integer.class));
        declaration.setDateConfirmation(getLocalDateTime(resultSet, "date_confirmation"));
        declaration.setStatutHistoriqueJson(resultSet.getString("statut_historique"));
        declaration.setDeletedAt(getLocalDateTime(resultSet, "deleted_at"));
        return declaration;
    }

    private String readFirstExistingString(ResultSet resultSet, String... columns) {
        for (String column : columns) {
            try {
                return resultSet.getString(column);
            } catch (SQLException ignored) {
                // Try next candidate.
            }
        }
        return null;
    }
}
