package main.navigation;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public final class ViewNavigator {

    private static final Duration TRANSITION_DURATION = Duration.millis(220);

    private ViewNavigator() {
    }

    public static void navigate(ActionEvent event, String fxmlPath, String title) {
        navigate(resolveSourceNode(event), fxmlPath, title);
    }

    public static void navigate(Node sourceNode, String fxmlPath, String title) {
        try {
            if (fxmlPath == null || fxmlPath.isBlank()) {
                throw new IllegalArgumentException("Chemin FXML invalide.");
            }

            URL url = ViewNavigator.class.getResource(normalizePath(fxmlPath));
            if (url == null) {
                throw new IllegalStateException("FXML introuvable: " + fxmlPath);
            }

            Stage stage = resolveStage(sourceNode);
            if (stage == null) {
                throw new IllegalStateException("Fenetre de navigation indisponible.");
            }

            double width = stage.getScene() != null ? stage.getScene().getWidth() : 0;
            double height = stage.getScene() != null ? stage.getScene().getHeight() : 0;

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = (width > 0 && height > 0) ? new Scene(root, width, height) : new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            if (!stage.isMaximized()) {
                stage.centerOnScreen();
            }

            root.setOpacity(0);
            FadeTransition ft = new FadeTransition(TRANSITION_DURATION, root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de vue", buildErrorMessage(e));
        } catch (Exception e) {
            showError("Erreur de navigation", buildErrorMessage(e));
        }
    }

    public static void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }

    private static Node resolveSourceNode(ActionEvent event) {
        if (event == null) {
            return null;
        }
        if (event.getSource() instanceof Node node) {
            return node;
        }
        if (event.getTarget() instanceof Node node) {
            return node;
        }
        return null;
    }

    private static Stage resolveStage(Node sourceNode) {
        if (sourceNode != null && sourceNode.getScene() != null && sourceNode.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }

        for (Window window : Window.getWindows()) {
            if (window instanceof Stage stage && window.isShowing()) {
                return stage;
            }
        }
        return null;
    }

    private static String normalizePath(String fxmlPath) {
        return fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
    }

    private static String buildErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Erreur inconnue.";
        }
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = root.getMessage();
        return message == null || message.isBlank() ? root.toString() : message;
    }
}
