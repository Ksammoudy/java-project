package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private AnchorPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charji el page acceuil automatique awel ma t-runni
        loadPage("acceuil");
    }

    @FXML
    private void handleNavigationDashboard(ActionEvent event) {
        loadPage("acceuil");
    }

    @FXML
    private void handleNavigationEvent(ActionEvent event) {
        loadPage("AfficherEvenement");
    }

    @FXML
    private void handleNavigationParticipation(ActionEvent event) {
        loadPage("AfficherParticipations");
    }

    // 📅 EL IZÉFA EL JDIDA LEL CALENDRIER
    @FXML
    private void handleNavigationCalendrier(ActionEvent event) {
        loadPage("Calendrier");
    }

    private void loadPage(String pageName) {
        try {
            // Path mrigel 100% kima tfahemna
            String path = "/org/example/views/event/" + pageName + ".fxml";
            URL fxmlUrl = getClass().getResource(path);

            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR: Fichier introuvable à: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Nadhfou el blassa
            contentArea.getChildren().clear();

            // Beich el page j-jdida takhou el blassa el kol (Responsive)
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            contentArea.getChildren().add(root);

            System.out.println("✅ Page " + pageName + " chargée avec succès !");

        } catch (IOException e) {
            System.err.println("❌ Mochkla fil chargement mte3 el page " + pageName);
            e.printStackTrace();
        }
    }
}