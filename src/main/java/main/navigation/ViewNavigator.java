package main.navigation;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public final class ViewNavigator {

    private static final Duration TRANSITION_DURATION = Duration.millis(220);

    private ViewNavigator() {
    }

    public static void navigate(ActionEvent event, String fxmlPath, String title) {
        if (event == null || !(event.getSource() instanceof Node)) {
            throw new IllegalArgumentException("ActionEvent invalide pour la navigation.");
        }
        navigate((Node) event.getSource(), fxmlPath, title);
    }

    public static void navigate(Node sourceNode, String fxmlPath, String title) {
        try {
            if (sourceNode == null || sourceNode.getScene() == null) {
                throw new IllegalStateException("Source de navigation indisponible.");
            }

            URL url = ViewNavigator.class.getResource(fxmlPath);
            if (url == null) {
                throw new IllegalStateException("FXML introuvable: " + fxmlPath);
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();

            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(TRANSITION_DURATION, root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de vue", e.getMessage());
        } catch (Exception e) {
            showError("Erreur de navigation", e.getMessage());
        }
    }

    public static void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }
}
