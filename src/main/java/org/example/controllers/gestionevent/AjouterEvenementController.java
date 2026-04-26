package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;
import java.sql.Date;

public class AjouterEvenementController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtNomOrg;
    @FXML private TextField txtLieu; // Marbout bel fx:id="txtLieu" fil FXML

    @FXML private Label labelTitrePage;
    @FXML private Button btnValider;

    private boolean isUpdate = false;
    private int eventId;

    public void initData(Evenement ev) {
        if (ev != null) {
            this.isUpdate = true;
            this.eventId = ev.getId();

            txtTitre.setText(ev.getTitre());
            txtDescription.setText(ev.getDescription());
            txtLieu.setText(ev.getLieu());
            txtNomOrg.setText(ev.getNomOrganisateur());

            if (ev.getDate() != null) {
                datePicker.setValue(ev.getDate().toLocalDate());
            }

            if (labelTitrePage != null) labelTitrePage.setText("Modification de l'Événement");
            if (btnValider != null) {
                btnValider.setText("Modifier");
                btnValider.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    private void handleSave() {
        String titre = txtTitre.getText();
        String desc = txtDescription.getText();
        String lieu = txtLieu.getText(); // 👈 Salla7na: jebna el text mel TextField
        String nomOrg = txtNomOrg.getText();

        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date !");
            return;
        }

        Date sqlDate = Date.valueOf(datePicker.getValue());
        EvenementServices se = new EvenementServices();

        try {
            if (isUpdate) {
                // MODE MODIFICATION
                // Tartib mta3 el Constructeur avec ID (7 paramètres):
                // (id, titre, description, date, idOrganisateur, nomOrganisateur, lieu)
                Evenement ev = new Evenement(eventId, titre, desc, sqlDate, 1, nomOrg, lieu);
                se.update(ev);
            } else {
                // MODE AJOUT
                // Tartib mta3 el Constructeur sans ID (5 paramètres dhibet kima fil Model):
                // (titre, description, nomOrganisateur, date, lieu)
                Evenement ev = new Evenement(titre, desc, nomOrg, sqlDate, lieu);
                se.create(ev);
            }
            handleCancel();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "L'action a échoué: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            String fxmlPath = "/org/example/views/event/AfficherEvenement.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            AnchorPane contentArea = (AnchorPane) txtTitre.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}