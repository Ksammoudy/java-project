package org.example.controllers.gestionevent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.models.gestionevent.Evenement;
import org.example.services.gestionevent.EvenementServices;

import java.sql.SQLException;

public class ModifierOrgController {

    @FXML private TextField txtTitre, txtLieu, txtNomOrg;
    @FXML private TextArea txtDesc;
    @FXML private DatePicker datePicker;
    @FXML private Button btnAnnuler, btnModifier;

    private final EvenementServices service = new EvenementServices();
    private Evenement currentEvenement;

    // --- 1. HEDHI EL MÉTHODE EL LEZMA BECH TA9RA EL DATA ---
    public void initData(Evenement ev) {
        this.currentEvenement = ev;

        // N-7ottou el klam el 9dim fil fields
        txtTitre.setText(ev.getTitre());
        txtLieu.setText(ev.getLieu());
        txtNomOrg.setText(ev.getNomOrganisateur());
        txtDesc.setText(ev.getDescription());

        // Conversion mta3 java.sql.Date l-LocalDate lel DatePicker
        if (ev.getDate() != null) {
            datePicker.setValue(((java.sql.Date) ev.getDate()).toLocalDate());
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            // 1. Mise à jour mta3 el objet
            currentEvenement.setTitre(txtTitre.getText());
            currentEvenement.setLieu(txtLieu.getText());
            currentEvenement.setNomOrganisateur(txtNomOrg.getText());
            currentEvenement.setDescription(txtDesc.getText());

            if (datePicker.getValue() != null) {
                currentEvenement.setDate(java.sql.Date.valueOf(datePicker.getValue()));
            }

            // 2. Appel lel Service
            service.update(currentEvenement);

            // 3. Success Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Événement modifié avec succès !");
            alert.showAndWait();

            // 4. Arja3 lel Home (Optional)
            handleAnnuler(event);

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur SQL : " + e.getMessage());
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        // Hne d-rajjou lel Home (OrganisateurHome)
        // Lezem d-koun 3andek el logic mta3 el navigation hne kima fil Home
        System.out.println("Retour à la liste...");
    }
}