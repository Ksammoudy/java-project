package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainEventFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        System.out.println(getClass().getResource("/org/example/views/event/RoleSelection.fxml"));
        //System.out.println(getClass().getResource("/org/example/views/event/admin.fxml"));

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/views/event/RoleSelection.fxml")
                //getClass().getResource("/org/example/views/event/admin.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("GESTION EVENEMENT");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
