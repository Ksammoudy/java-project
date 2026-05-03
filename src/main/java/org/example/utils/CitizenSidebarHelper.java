package org.example.utils;

import javafx.scene.control.Button;

/**
 * Utilitaire pour gérer l'état actif de la sidebar citoyen.
 */
public class CitizenSidebarHelper {

    public static void applyActive(Button active, Button... allButtons) {
        for (Button btn : allButtons) {
            btn.getStyleClass().removeAll("active");
        }
        if (active != null) {
            active.getStyleClass().add("active");
        }
    }
}
