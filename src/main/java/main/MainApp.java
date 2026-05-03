package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            URL fxmlUrl = MainApp.class.getResource("/fxml/Dashboard.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML introuvable: /fxml/Dashboard.fxml");
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(fxmlLoader.load(), 1360, 800);
            stage.setTitle("WasteWise - Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur de chargement UI: " + e.getMessage());
            Scene errorScene = new Scene(new StackPane(errorLabel), 700, 180);
            stage.setTitle("WasteWise - Erreur");
            stage.setScene(errorScene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
