package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
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

public class ModifierParticipationController implements Initializable {

    @FXML private ComboBox<Evenement> comboEvenement;
    @FXML private TextField txtNomCitoyen;
    @FXML private DatePicker datePicker;

    private final ParticipationServices ps = new ParticipationServices();
    private final EvenementServices es = new EvenementServices();

    // 🆔 Stocker l'ID de la participation à modifier
    private int currentParticipationId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEvenements();
    }

    // ✅ 1. La méthode Magique pour charger les données
    public void initData(Participation p) {
        this.currentParticipationId = p.getId(); // Stocker l'ID pour le Update
        txtNomCitoyen.setText(p.getNomCitoyen());
        datePicker.setValue(p.getDateInscription().toLocalDate());

        // Sélectionner l'événement correspondant dans le ComboBox
        for (Evenement ev : comboEvenement.getItems()) {
            if (ev.getId() == p.getIdEvenement()) {
                comboEvenement.setValue(ev);
                break;
            }
        }
    }

    private void loadEvenements() {
        try {
            ObservableList<Evenement> list = FXCollections.observableArrayList(es.read());
            comboEvenement.setItems(list);
            comboEvenement.setConverter(new javafx.util.StringConverter<Evenement>() {
                @Override public String toString(Evenement ev) { return (ev == null) ? "" : ev.getTitre(); }
                @Override public Evenement fromString(String string) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            Evenement selectedEv = comboEvenement.getValue();
            String nomCitoyen = txtNomCitoyen.getText();
            LocalDate dateInscrit = datePicker.getValue();

            // Contrôle de saisie
            if (selectedEv == null || nomCitoyen == null || nomCitoyen.trim().isEmpty() || dateInscrit == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            if (!nomCitoyen.matches("^[a-zA-Z\\s]+$")) {
                showAlert("Erreur", "Le nom ne doit contenir que des lettres.");
                return;
            }

            // Préparation de l'objet Participation modifié
            Participation p = new Participation();
            p.setId(currentParticipationId); // Important pour le WHERE id=? fil SQL
            p.setNomCitoyen(nomCitoyen);
            p.setIdEvenement(selectedEv.getId());
            p.setNomEvenement(selectedEv.getTitre());
            p.setDateInscription(Date.valueOf(dateInscrit));
            p.setEmail("contact@wastewise.tn");

            // Appel au Service Update
            ps.update(p);

            System.out.println("✅ Participation mise à jour !");
            handleCancel(); // Retour à la liste

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/event/AfficherParticipations.fxml"));
            AnchorPane contentArea = (AnchorPane) comboEvenement.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
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