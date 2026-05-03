package controllers.appeloffre;

import entities.AppelOffre;
import entities.UserOption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceUserDirectory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private ComboBox<String> cbValorisateur;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblCtxAppelId;
    @FXML
    private Label lblCtxEtat;
    @FXML
    private Label lblCtxDateLimite;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceUserDirectory serviceUserDirectory = new ServiceUserDirectory();
    private final DateTimeFormatter ctxDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Map<String, Integer> valorisateurIdByLabel = new LinkedHashMap<>();
    private Integer currentId;

    @FXML
    public void initialize() {
        try {
            initialiserValorisateurs();
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

        int linkedResponses = 0;
        try {
            linkedResponses = serviceAppelOffre.compterReponsesLiees(currentId);
        } catch (Exception ignored) {
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer cet appel d'offre ?");
        if (linkedResponses > 0) {
            confirm.setContentText("Cette action est irreversible.\n"
                    + linkedResponses + " reponse(s) liee(s) seront aussi supprimee(s).");
        } else {
            confirm.setContentText("Cette action est irreversible.");
        }
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            serviceAppelOffre.supprimer(currentId);
            if (linkedResponses > 0) {
                AppelOffreFlowState.setFlashMessage("Appel d'offre supprime avec succes (" + linkedResponses + " reponse(s) liee(s) supprimee(s)).");
            } else {
                AppelOffreFlowState.setFlashMessage("Appel d'offre supprime avec succes.");
            }
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
        setSelectedValorisateurById(a.getValorisateurId());
        if (a.getDateLimite() != null) {
            dpDateLimite.setValue(a.getDateLimite().toLocalDateTime().toLocalDate());
        }
        updateContextCard(a);
        lblMessage.setText("Vous modifiez l'appel #" + a.getId());
    }

    private void updateContextCard(AppelOffre a) {
        if (lblCtxAppelId != null) {
            lblCtxAppelId.setText("#" + a.getId());
        }
        if (lblCtxEtat != null) {
            boolean actif = a.getDateLimite() != null && a.getDateLimite().after(new Timestamp(System.currentTimeMillis()));
            lblCtxEtat.setText(actif ? "Actif" : "Expire");
        }
        if (lblCtxDateLimite != null) {
            if (a.getDateLimite() == null) {
                lblCtxDateLimite.setText("-");
            } else {
                lblCtxDateLimite.setText(ctxDateFormatter.format(a.getDateLimite().toLocalDateTime()));
            }
        }
    }

    private AppelOffre construireDepuisFormulaire(int id) {
        String titre = lireTexteObligatoire(txtTitre.getText(), "Titre obligatoire.");
        String description = lireTexteObligatoire(txtDescription.getText(), "Description obligatoire.");
        double quantite = lireDoublePositif(txtQuantiteDemandee.getText(), "Quantite demandee invalide.");
        int valorisateurId = lireValorisateurSelectionne("Valorisateur invalide.");
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

    private void initialiserValorisateurs() {
        cbValorisateur.setEditable(false);
        cbValorisateur.setPromptText("Selectionnez un valorisateur");
        valorisateurIdByLabel.clear();
        try {
            List<UserOption> users = serviceUserDirectory.recupererValorisateurs();
            for (UserOption u : users) {
                valorisateurIdByLabel.put(u.getLabel(), u.getId());
            }
            cbValorisateur.getItems().setAll(valorisateurIdByLabel.keySet());
        } catch (Exception e) {
            lblMessage.setText("Erreur chargement valorisateurs: " + e.getMessage());
        }
    }

    private int lireValorisateurSelectionne(String message) {
        String label = cbValorisateur.getValue();
        if (label == null) {
            throw new IllegalArgumentException(message);
        }
        Integer id = valorisateurIdByLabel.get(label);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }

    private void setSelectedValorisateurById(int id) {
        String found = null;
        for (Map.Entry<String, Integer> entry : valorisateurIdByLabel.entrySet()) {
            if (entry.getValue() != null && entry.getValue() == id) {
                found = entry.getKey();
                break;
            }
        }
        if (found != null) {
            cbValorisateur.setValue(found);
            return;
        }
        String fallback = "Valorisateur #" + id;
        valorisateurIdByLabel.put(fallback, id);
        if (!cbValorisateur.getItems().contains(fallback)) {
            cbValorisateur.getItems().add(fallback);
        }
        cbValorisateur.setValue(fallback);
    }
}
