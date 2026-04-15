package org.example.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    void loadReturnsProjectDefaultsWhenNoOverrideIsProvided() {
        DatabaseConfig config = DatabaseConfig.load();

        assertEquals("root", config.username());
        assertEquals("pidev", config.databaseName());
        assertTrue(config.url().contains("jdbc:mysql://127.0.0.1:3306/pidev"));
    }
}
