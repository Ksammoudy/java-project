package controllers.reponseoffre;

import entities.AppelOffre;
import entities.ReponseOffre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReponseOffreCreateController {

    private static final String STYLE_TEXT_FIELD_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 15px;";
    private static final String STYLE_TEXT_AREA_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_COMBO_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_FIELD_ERROR_SUFFIX = " -fx-border-color: #DC2626; -fx-border-width: 1.8;";
    private static final String STYLE_MSG_INFO =
            "-fx-background-color: #E9F7EE; -fx-text-fill: #1D5B41; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 12 10 12;";
    private static final String STYLE_MSG_ERROR =
            "-fx-background-color: #FEECEC; -fx-text-fill: #B91C1C; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 12 10 12;";

    @FXML
    private TextField txtQuantiteProposee;
    @FXML
    private TextArea txtMessage;
    @FXML
    private ComboBox<String> cbAppelOffre;
    @FXML
    private ComboBox<String> cbCitoyen;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblQuantiteError;
    @FXML
    private Label lblAppelError;
    @FXML
    private Label lblCitoyenError;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();

    @FXML
    public void initialize() {
        applyDefaultStyles();
        resetValidationUi();
        initialiserCombos();
        setInfo("Saisissez les informations de votre reponse d'offre.");
    }

    @FXML
    private void onEnregistrer(ActionEvent event) {
        try {
            ReponseOffre r = construireDepuisFormulaire();
            serviceReponseOffre.ajouter(r);
            ReponseOffreFlowState.setFlashMessage("Reponse enregistree avec succes.");
            ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        ReponseOffreFlowState.setFlashMessage("Creation annulee.");
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
    }

    @FXML
    private void onOpenDashboard(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/Dashboard.fxml", "WasteWise - Dashboard");
    }

    @FXML
    private void onOpenAppelOffre(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
    }

    @FXML
    private void onOpenReponseCreate(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreCreate.fxml", "WasteWise - Creer une reponse d'offre");
    }

    @FXML
    private void onOpenReponseList(ActionEvent event) {
        ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
    }

    private void initialiserCombos() {
        cbAppelOffre.setEditable(true);
        cbCitoyen.setEditable(true);
        cbAppelOffre.setPromptText("Ex: 14 - Titre appel");
        cbCitoyen.setPromptText("Ex: 3");

        try {
            List<AppelOffre> appels = serviceAppelOffre.recupererTout();
            ObservableList<String> items = FXCollections.observableArrayList();
            for (AppelOffre a : appels) {
                items.add(a.getId() + " - " + a.getTitre());
            }
            cbAppelOffre.setItems(items);
            if (!items.isEmpty()) {
                cbAppelOffre.setValue(items.get(0));
            }
        } catch (Exception ignored) {
        }

        try {
            List<ReponseOffre> reponses = serviceReponseOffre.recupererTout();
            Set<String> uniqueCitoyens = new LinkedHashSet<>();
            for (ReponseOffre r : reponses) {
                uniqueCitoyens.add(String.valueOf(r.getCitoyenId()));
            }
            ObservableList<String> citoyens = FXCollections.observableArrayList(uniqueCitoyens);
            cbCitoyen.setItems(citoyens);
            if (!citoyens.isEmpty()) {
                cbCitoyen.setValue(citoyens.get(0));
            }
        } catch (Exception ignored) {
        }
    }

    private ReponseOffre construireDepuisFormulaire() {
        resetValidationUi();
        boolean hasError = false;

        Double quantite = parsePositiveDouble(txtQuantiteProposee.getText());
        if (quantite == null) {
            showFieldError(txtQuantiteProposee, lblQuantiteError, "Entrez une quantite numerique > 0.");
            hasError = true;
        }

        Integer appelId = parseIdFromCombo(cbAppelOffre);
        if (appelId == null) {
            showFieldError(cbAppelOffre, lblAppelError, "Selectionnez ou saisissez un appel valide.");
            hasError = true;
        }

        Integer citoyenId = parseIdFromCombo(cbCitoyen);
        if (citoyenId == null) {
            showFieldError(cbCitoyen, lblCitoyenError, "Selectionnez ou saisissez un citoyen valide.");
            hasError = true;
        }

        if (hasError) {
            throw new IllegalArgumentException("Veuillez corriger les champs en rouge.");
        }

        String msg = txtMessage.getText() == null ? null : txtMessage.getText().trim();
        if (msg != null && msg.isEmpty()) {
            msg = null;
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return new ReponseOffre(
                quantite,
                now,
                ReponseOffre.STATUT_EN_ATTENTE,
                msg,
                appelId,
                citoyenId
        );
    }

    private Double parsePositiveDouble(String value) {
        try {
            double v = Double.parseDouble(value == null ? "" : value.trim());
            return v > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIdFromCombo(ComboBox<String> combo) {
        String value = combo.getEditor().getText();
        if (value == null || value.trim().isEmpty()) {
            value = combo.getValue();
        }
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        String firstToken = text.contains("-") ? text.substring(0, text.indexOf('-')).trim() : text;
        try {
            int id = Integer.parseInt(firstToken);
            return id > 0 ? id : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void applyDefaultStyles() {
        txtQuantiteProposee.setStyle(STYLE_TEXT_FIELD_OK);
        txtMessage.setStyle(STYLE_TEXT_AREA_OK);
        cbAppelOffre.setStyle(STYLE_COMBO_OK);
        cbCitoyen.setStyle(STYLE_COMBO_OK);
    }

    private void resetValidationUi() {
        applyDefaultStyles();
        hideErrorLabel(lblQuantiteError);
        hideErrorLabel(lblAppelError);
        hideErrorLabel(lblCitoyenError);
    }

    private void hideErrorLabel(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showFieldError(TextField field, Label errorLabel, String message) {
        field.setStyle(STYLE_TEXT_FIELD_OK + STYLE_FIELD_ERROR_SUFFIX);
        showErrorLabel(errorLabel, message);
    }

    private void showFieldError(ComboBox<String> field, Label errorLabel, String message) {
        field.setStyle(STYLE_COMBO_OK + STYLE_FIELD_ERROR_SUFFIX);
        showErrorLabel(errorLabel, message);
    }

    private void showErrorLabel(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void setInfo(String message) {
        lblMessage.setStyle(STYLE_MSG_INFO);
        lblMessage.setText(message);
    }

    private void setError(String message) {
        lblMessage.setStyle(STYLE_MSG_ERROR);
        lblMessage.setText(message);
    }
}
