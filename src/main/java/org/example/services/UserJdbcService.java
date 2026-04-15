package org.example.services;

import org.example.entities.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserJdbcService extends AbstractJdbcService implements BaseCrudService<User> {

    private static final String BASE_SELECT = """
        SELECT id, email, roles, password, nom, prenom, telephone, adresse, photo_profil,
               notify_validation, notify_points, notify_refus, notify_nouvelles_declarations,
               langue, theme, unite_preferee, date_inscription, derniere_connexion, statut_centre,
               capacite_max_journaliere, organisation_centre, zone_couverture, types_dechets_acceptes,
               stripe_connect_account_id
        FROM `user`
        """;

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        }
        return users;
    }

    @Override
    public Optional<User> findById(int id) throws SQLException {
        try (PreparedStatement statement = getConnection().prepareStatement(BASE_SELECT + " WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public User create(User entity) {
        throw new UnsupportedOperationException("Creation utilisateur non exposee dans ce socle.");
    }

    @Override
    public boolean update(User entity) {
        throw new UnsupportedOperationException("Mise a jour utilisateur non exposee dans ce socle.");
    }

    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException("Suppression utilisateur non exposee dans ce socle.");
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setEmail(resultSet.getString("email"));
        user.setRolesJson(resultSet.getString("roles"));
        user.setPassword(resultSet.getString("password"));
        user.setNom(resultSet.getString("nom"));
        user.setPrenom(resultSet.getString("prenom"));
        user.setTelephone(resultSet.getString("telephone"));
        user.setAdresse(resultSet.getString("adresse"));
        user.setPhotoProfil(resultSet.getString("photo_profil"));
        user.setNotifyValidation(resultSet.getBoolean("notify_validation"));
        user.setNotifyPoints(resultSet.getBoolean("notify_points"));
        user.setNotifyRefus(resultSet.getBoolean("notify_refus"));
        user.setNotifyNouvellesDeclarations(resultSet.getBoolean("notify_nouvelles_declarations"));
        user.setLangue(resultSet.getString("langue"));
        user.setTheme(resultSet.getString("theme"));
        user.setUnitePreferee(resultSet.getString("unite_preferee"));
        user.setDateInscription(getLocalDateTime(resultSet, "date_inscription"));
        user.setDerniereConnexion(getLocalDateTime(resultSet, "derniere_connexion"));
        user.setStatutCentre(resultSet.getString("statut_centre"));
        user.setCapaciteMaxJournaliere(resultSet.getBigDecimal("capacite_max_journaliere"));
        user.setOrganisationCentre(resultSet.getString("organisation_centre"));
        user.setZoneCouverture(resultSet.getString("zone_couverture"));
        user.setTypesDechetsAcceptes(resultSet.getString("types_dechets_acceptes"));
        user.setStripeConnectAccountId(resultSet.getString("stripe_connect_account_id"));
        return user;
    }
}
