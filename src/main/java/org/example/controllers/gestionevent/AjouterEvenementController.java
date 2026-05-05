package org.example.controllers.gestionevent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;
import java.sql.Date;
import java.time.LocalDate;

public class AjouterEvenementController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtNomOrg;
    @FXML private TextField txtLieu;

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
        // 1. Récupération
        String titre = (txtTitre != null) ? txtTitre.getText().trim() : "";
        String desc = (txtDescription != null) ? txtDescription.getText().trim() : "";
        String lieu = (txtLieu != null) ? txtLieu.getText().trim() : "";
        String nomOrg = (txtNomOrg != null) ? txtNomOrg.getText().trim() : "";

        // 2. Contrôle de saisie
        if (titre.isEmpty() || desc.isEmpty() || lieu.isEmpty() || nomOrg.isEmpty() || datePicker.getValue() == null) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs !");
            return;
        }

        // Titre: Lettres et espaces seulement
        if (!titre.matches("^[a-zA-Z\\s]+$")) {
            showAlert("Erreur de saisie", "Le titre ne doit contenir que des lettres.");
            return;
        }

        // Description: Min 20 caractères
        if (desc.length() < 20) {
            showAlert("Erreur de saisie", "La description doit avoir au moins 20 caractères.");
            return;
        }

        // Date: >= Aujourd'hui
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Erreur de date", "La date ne peut pas être passée.");
            return;
        }

        // 3. Action
        try {
            Date sqlDate = Date.valueOf(datePicker.getValue());
            EvenementServices se = new EvenementServices();

            if (isUpdate) {
                Evenement ev = new Evenement(eventId, titre, desc, sqlDate, 1, nomOrg, lieu);
                se.update(ev);
            } else {
                Evenement ev = new Evenement(titre, desc, nomOrg, sqlDate, lieu);
                se.create(ev);
            }
            handleCancel();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "L'enregistrement a échoué.");
        }
    }

    @FXML
    private void handleCancel() {
        try {
            String fxmlPath = "/org/example/views/event/AfficherEvenement.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) txtTitre.getScene().lookup("#contentArea");
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
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}