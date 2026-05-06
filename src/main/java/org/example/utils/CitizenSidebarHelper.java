package org.example.utils;

import javafx.scene.control.Button;

/**
 * Etat visuel du menu citoyen : un bouton actif, les autres en retrait (theme {@code sidebar-nav}).
 */
public final class CitizenSidebarHelper {

    private CitizenSidebarHelper() {
    }

    /**
     * @param active le bouton du menu courant
     * @param all    tous les boutons de navigation (y compris {@code active})
     */
    public static void applyActive(Button active, Button... all) {
        for (Button b : all) {
            if (b == null) {
                continue;
            }
            b.getStyleClass().removeAll("active", "muted");
            b.getStyleClass().add(b == active ? "active" : "muted");
        }
    }
}
