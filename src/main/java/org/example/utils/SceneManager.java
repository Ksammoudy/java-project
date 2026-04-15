package org.example.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class SceneManager {

    private SceneManager() {
    }

    public static void loadInto(Stage stage, String fxmlPath, String title, int width, int height) throws IOException {
        URL resource = SceneManager.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML introuvable: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Scene scene = new Scene(loader.load(), width, height);

        URL cssUrl = SceneManager.class.getResource("/org/example/styles/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(title);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void replaceRoot(Node currentNode, String fxmlPath) throws IOException {
        URL resource = SceneManager.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("FXML introuvable: " + fxmlPath);
        }

        Parent root = FXMLLoader.load(resource);
        currentNode.getScene().setRoot(root);
    }
}
