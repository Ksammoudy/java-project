package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.models.gestionevent.Participation;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.ParticipationServices;
import org.example.services.gestionevent.EvenementServices;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterParticipationController implements Initializable {

    @FXML private ComboBox<Evenement> comboEvenement;
    @FXML private TextField txtNomCitoyen;
    @FXML private TextField txtEmail; // Zid hatha fil FXML mte3ek (fx:id="txtEmail")
    @FXML private DatePicker datePicker;

    private final ParticipationServices ps = new ParticipationServices();
    private final EvenementServices es = new EvenementServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEvenements();
        datePicker.setValue(LocalDate.now());

        // Bloquer visuellement les dates passées
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void loadEvenements() {
        try {
            ObservableList<Evenement> list = FXCollections.observableArrayList(es.read());
            comboEvenement.setItems(list);
            comboEvenement.setConverter(new javafx.util.StringConverter<Evenement>() {
                @Override
                public String toString(Evenement ev) { return (ev == null) ? "" : ev.getTitre(); }
                @Override
                public Evenement fromString(String string) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSave() {
        try {
            Evenement selectedEv = comboEvenement.getValue();
            String nomCitoyen = (txtNomCitoyen != null) ? txtNomCitoyen.getText().trim() : "";
            LocalDate dateInscrit = datePicker.getValue();

            // ✅ NA7INA EL EMAIL MEL VALIDATION KHATER MAHOUSH MAWJOUD FIL FXML
            if (selectedEv == null || nomCitoyen.isEmpty() || dateInscrit == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            if (!nomCitoyen.matches("^[a-zA-Z\\s]+$")) {
                showAlert("Erreur de saisie", "Le nom ne doit contenir que des lettres.");
                return;
            }

            // Enregistrement
            Participation p = new Participation();
            p.setDateInscription(java.sql.Date.valueOf(dateInscrit));
            p.setIdEvenement(selectedEv.getId());
            p.setNomCitoyen(nomCitoyen);
            p.setNomEvenement(selectedEv.getTitre());
            p.setEmail("contact@wastewise.tn"); // 👈 7attina email par défaut tawa

            ps.create(p);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("✅ Participation ajoutée avec succès !");
            success.showAndWait();

            handleCancel();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/event/AfficherParticipations.fxml"));
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) txtNomCitoyen.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}