package controllers.appeloffre;

import entities.AppelOffre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

public class AppelOffreEditController {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtQuantiteDemandee;
    @FXML
    private DatePicker dpDateLimite;
    @FXML
    private TextField txtValorisateurId;
    @FXML
    private Label lblMessage;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private Integer currentId;

    @FXML
    public void initialize() {
        try {
            Integer selectedId = AppelOffreFlowState.consumeSelectedAppelId();
            if (selectedId == null) {
                lblMessage.setText("Aucun appel selectionne. Retournez a la liste.");
                return;
            }
            currentId = selectedId;
            chargerAppel(currentId);
        } catch (Exception e) {
            lblMessage.setText("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void onMettreAJour(ActionEvent event) {
        if (currentId == null) {
            lblMessage.setText("Modification impossible: aucun appel selectionne.");
            return;
        }

        try {
            AppelOffre a = construireDepuisFormulaire(currentId);
            serviceAppelOffre.modifier(a);
            AppelOffreFlowState.setFlashMessage("Appel d'offre mis a jour avec succes.");
            ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
        } catch (Exception e) {
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onSupprimer(ActionEvent event) {
        if (currentId == null) {
            lblMessage.setText("Suppression impossible: aucun appel selectionne.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer cet appel d'offre ?");
        confirm.setContentText("Cette action est irreversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            serviceAppelOffre.supprimer(currentId);
            AppelOffreFlowState.setFlashMessage("Appel d'offre supprime avec succes.");
            ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
        } catch (Exception e) {
            lblMessage.setText("Erreur suppression: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
    }

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
    }

    @FXML
    private void onOpenList(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
    }

    @FXML
    private void onOpenCreate(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreCreate.fxml", "WasteWise - Creer un appel d'offre");
    }

    private void chargerAppel(int id) throws Exception {
        AppelOffre a = serviceAppelOffre.recupererParId(id);
        if (a == null) {
            throw new IllegalStateException("Appel d'offre introuvable.");
        }

        txtTitre.setText(a.getTitre());
        txtDescription.setText(a.getDescription());
        txtQuantiteDemandee.setText(String.valueOf(a.getQuantiteDemandee()));
        txtValorisateurId.setText(String.valueOf(a.getValorisateurId()));
        if (a.getDateLimite() != null) {
            dpDateLimite.setValue(a.getDateLimite().toLocalDateTime().toLocalDate());
        }
        lblMessage.setText("Vous modifiez l'appel #" + a.getId());
    }

    private AppelOffre construireDepuisFormulaire(int id) {
        String titre = lireTexteObligatoire(txtTitre.getText(), "Titre obligatoire.");
        String description = lireTexteObligatoire(txtDescription.getText(), "Description obligatoire.");
        double quantite = lireDoublePositif(txtQuantiteDemandee.getText(), "Quantite demandee invalide.");
        int valorisateurId = lireEntierPositif(txtValorisateurId.getText(), "Valorisateur id invalide.");
        LocalDate date = dpDateLimite.getValue();
        if (date == null) {
            throw new IllegalArgumentException("Date limite obligatoire.");
        }

        Timestamp dateLimite = Timestamp.valueOf(date.atTime(23, 59, 59));
        return new AppelOffre(id, titre, description, quantite, dateLimite, valorisateurId);
    }

    private String lireTexteObligatoire(String value, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private double lireDoublePositif(String value, String message) {
        try {
            double v = Double.parseDouble(value == null ? "" : value.trim());
            if (v <= 0) {
                throw new IllegalArgumentException(message);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private int lireEntierPositif(String value, String message) {
        try {
            int v = Integer.parseInt(value == null ? "" : value.trim());
            if (v <= 0) {
                throw new IllegalArgumentException(message);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }
}
