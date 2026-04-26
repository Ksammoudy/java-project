package org.example.controllers.gestionevent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField; // 👈 Zid hedhi
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
    @FXML private ComboBox<String> comboCitoyen;
    @FXML private DatePicker datePicker;
    @FXML private TextField emailInput; // 👈 1. Zid l-email fil FXML mte3ek zeda

    private final ParticipationServices ps = new ParticipationServices();
    private final EvenementServices es = new EvenementServices();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEvenements();
        // Simulation mta3 citoyens
        comboCitoyen.setItems(FXCollections.observableArrayList("Islem", "Ahmed", "Sarra"));
        datePicker.setValue(LocalDate.now());
    }

    private void loadEvenements() {
        try {
            ObservableList<Evenement> list = FXCollections.observableArrayList(es.read());
            comboEvenement.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try {
            Evenement selectedEv = comboEvenement.getValue();
            String selectedCitoyen = comboCitoyen.getValue();
            String email = (emailInput != null) ? emailInput.getText() : "contact@wastewise.tn"; // Fallback
            Date sqlDate = Date.valueOf(datePicker.getValue());

            if (selectedEv == null || selectedCitoyen == null) {
                System.err.println("❌ Lezem t-ekhtar événement w citoyen!");
                return;
            }

            // 🛠️ ISLA7 EL CONSTRUCTEUR: Lezem nfass l'ordre mta3 el Model jdid
            Participation p = new Participation();
            p.setDateInscription(sqlDate);
            p.setIdEvenement(selectedEv.getId());
            p.setIdCitoyen(1); // Simulation
            p.setNomCitoyen(selectedCitoyen);
            p.setNomEvenement(selectedEv.getTitre()); // Thabbet esm el getter: selectedEv.getNom()
            p.setEmail(email); // 👈 Zidna l-email hne

            // 2. Appel au Service (create tawa fiha l-email)
            ps.create(p);

            System.out.println("✅ Participation ajoutée avec succès !");
            handleCancel(); // Yarja3 lel affichage

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/views/event/AfficherParticipations.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ Fichier AfficherParticipations.fxml mal9itech!");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            // Hne thabbet dhibet elli el Scene mte3ek fiha AnchorPane esmou contentArea
            AnchorPane contentArea = (AnchorPane) comboEvenement.getScene().lookup("#contentArea");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                // Ken mal9ach contentArea (Dashboard), i-beddel el scene l-kol
                comboEvenement.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}