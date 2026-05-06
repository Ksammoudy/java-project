package main.navigation;

import javafx.event.ActionEvent;
import org.example.controllers.AppShellController;

/**
 * Remplace l'ancien ViewNavigator (qui ouvrait une nouvelle fenêtre).
 * Maintenant toutes les navigations se font dans l'AppShell existant.
 */
public class ViewNavigator {

    /**
     * Charge un fragment FXML dans la zone centrale de l'AppShell.
     * Compatible avec l'ancienne signature ViewNavigator.navigate(event, path, title).
     */
    public static void navigate(ActionEvent event, String fxmlPath, String title) {
        loadInShell(fxmlPath);
    }

    /**
     * Surcharge pour les appels depuis des boutons (pas d'ActionEvent).
     */
    public static void navigate(javafx.scene.Node source, String fxmlPath, String title) {
        loadInShell(fxmlPath);
    }

    private static void loadInShell(String fxmlPath) {
        AppShellController shell = AppShellController.getInstance();
        if (shell != null) {
            shell.loadContent(fxmlPath);
        } else {
            System.err.println("AppShell non initialisé — navigation impossible vers : " + fxmlPath);
        }
    }
}
