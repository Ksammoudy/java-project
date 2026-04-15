package org.example.services;

import org.example.entities.TypeDechet;
import org.example.utils.DBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assumptions;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TypeDechetJdbcServiceTest {

    private static final String TEST_PREFIX = "TEST_JUNIT_TYPE_DECHET_";

    private static TypeDechetJdbcService service;

    @BeforeAll
    static void setup() {
        service = new TypeDechetJdbcService();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (!DBConnection.getInstance().testConnection()) {
            return;
        }

        for (TypeDechet typeDechet : service.findAll()) {
            if (typeDechet.getLibelle() != null && typeDechet.getLibelle().startsWith(TEST_PREFIX)) {
                service.delete(typeDechet.getId());
            }
        }
    }

    @Test
    @Order(1)
    void testAjouterTypeDechet() throws SQLException {
        assumeDatabaseAvailable();

        TypeDechet typeDechet = new TypeDechet();
        typeDechet.setLibelle(TEST_PREFIX + "AJOUT");
        typeDechet.setValeurPointsKg(2.5);
        typeDechet.setDescriptionTri("Ajout depuis JUnit");

        service.create(typeDechet);
        List<TypeDechet> types = service.findAll();

        assertFalse(types.isEmpty());
        assertTrue(types.stream().anyMatch(t -> (TEST_PREFIX + "AJOUT").equals(t.getLibelle())));
    }

    @Test
    @Order(2)
    void testModifierTypeDechet() throws SQLException {
        assumeDatabaseAvailable();

        TypeDechet typeDechet = new TypeDechet();
        typeDechet.setLibelle(TEST_PREFIX + "UPDATE_SOURCE");
        typeDechet.setValeurPointsKg(1.5);
        typeDechet.setDescriptionTri("Avant modification");
        service.create(typeDechet);

        typeDechet.setLibelle(TEST_PREFIX + "UPDATE_TARGET");
        typeDechet.setValeurPointsKg(4.0);
        typeDechet.setDescriptionTri("Apres modification");
        boolean updated = service.update(typeDechet);

        List<TypeDechet> types = service.findAll();

        assertTrue(updated);
        assertTrue(types.stream().anyMatch(t -> (TEST_PREFIX + "UPDATE_TARGET").equals(t.getLibelle())));
    }

    @Test
    @Order(3)
    void testSupprimerTypeDechet() throws SQLException {
        assumeDatabaseAvailable();

        TypeDechet typeDechet = new TypeDechet();
        typeDechet.setLibelle(TEST_PREFIX + "DELETE");
        typeDechet.setValeurPointsKg(3.0);
        typeDechet.setDescriptionTri("Suppression depuis JUnit");
        service.create(typeDechet);

        boolean deleted = service.delete(typeDechet.getId());
        List<TypeDechet> types = service.findAll();

        assertTrue(deleted);
        assertFalse(types.stream().anyMatch(t -> typeDechet.getId().equals(t.getId())));
    }

    @Test
    @Order(4)
    void calculatePointsReturnsRoundedPoints() {
        TypeDechet typeDechet = new TypeDechet();
        typeDechet.setValeurPointsKg(2.5);

        int points = service.calculatePoints(typeDechet, 3.2);

        assertEquals(8, points);
    }

    private void assumeDatabaseAvailable() {
        try {
            Assumptions.assumeTrue(DBConnection.getInstance().testConnection(), "Base MySQL non disponible pour les tests CRUD.");
        } catch (RuntimeException exception) {
            Assumptions.abort("Base MySQL non disponible pour les tests CRUD.");
        }
    }
}
