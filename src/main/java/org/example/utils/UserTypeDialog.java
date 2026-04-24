package org.example.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

public class UserTypeDialog {

    public static String showDialog() {
        final String[] selectedType = {null};

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Choisir le type d'utilisateur");

        Label title = new Label("Choisissez votre type d'utilisateur");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitle = new Label("Veuillez sélectionner le type de compte à créer.");
        subtitle.setWrapText(true);

        Button citizenBtn = new Button("Citizen");
        citizenBtn.setMaxWidth(Double.MAX_VALUE);
        citizenBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        citizenBtn.setOnAction(e -> {
            selectedType[0] = "CITIZEN";
            stage.close();
        });

        Button valorizerBtn = new Button("Valorizer");
        valorizerBtn.setMaxWidth(Double.MAX_VALUE);
        valorizerBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        valorizerBtn.setOnAction(e -> {
            selectedType[0] = "VALORIZER";
            stage.close();
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(12, title, subtitle, citizenBtn, valorizerBtn, cancelBtn);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(350);

        stage.setScene(new Scene(root));
        stage.showAndWait();

        return selectedType[0];
    }
}