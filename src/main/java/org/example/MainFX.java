package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        System.out.println(getClass().getResource("/admin.fxml"));

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/admin.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Event System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
