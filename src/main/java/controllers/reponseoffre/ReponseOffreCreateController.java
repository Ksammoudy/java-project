package controllers.reponseoffre;

import entities.AppelOffre;
import entities.ReponseOffre;
import entities.UserOption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceReponseOffre;
import services.ServiceUserDirectory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    @FXML
    private Label lblCtxAppel;
    @FXML
    private Label lblCtxQuantiteDemandee;
    @FXML
    private Label lblCtxDateLimite;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceUserDirectory serviceUserDirectory = new ServiceUserDirectory();
    private final Map<Integer, AppelOffre> appelsById = new HashMap<>();
    private final Map<String, Integer> appelIdByLabel = new LinkedHashMap<>();
    private final Map<String, Integer> citoyenIdByLabel = new LinkedHashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        cbAppelOffre.setEditable(false);
        cbCitoyen.setEditable(false);
        cbAppelOffre.setPromptText("Selectionnez un appel");
        cbCitoyen.setPromptText("Selectionnez un citoyen");
        resetContextCard();
        appelIdByLabel.clear();
        citoyenIdByLabel.clear();

        try {
            List<AppelOffre> appels = serviceAppelOffre.recupererTout();
            for (AppelOffre a : appels) {
                appelsById.put(a.getId(), a);
                String label = "#" + a.getId() + " - " + a.getTitre();
                appelIdByLabel.put(label, a.getId());
            }
            cbAppelOffre.getItems().setAll(appelIdByLabel.keySet());
            if (!cbAppelOffre.getItems().isEmpty()) {
                cbAppelOffre.setValue(cbAppelOffre.getItems().get(0));
                updateContextCardFromSelection();
            }
            cbAppelOffre.valueProperty().addListener((obs, oldV, newV) -> updateContextCardFromSelection());
        } catch (Exception e) {
            setError("Impossible de charger les appels d'offre: " + e.getMessage());
        }

        try {
            List<UserOption> citoyens = serviceUserDirectory.recupererCitoyens();
            for (UserOption c : citoyens) {
                citoyenIdByLabel.put(c.getLabel(), c.getId());
            }
            cbCitoyen.getItems().setAll(citoyenIdByLabel.keySet());
            if (!cbCitoyen.getItems().isEmpty()) {
                cbCitoyen.setValue(cbCitoyen.getItems().get(0));
            }
        } catch (Exception e) {
            setError("Impossible de charger les citoyens: " + e.getMessage());
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

        Integer appelId = parseSelectionId(cbAppelOffre, appelIdByLabel);
        if (appelId == null) {
            showFieldError(cbAppelOffre, lblAppelError, "Selectionnez un appel valide.");
            hasError = true;
        }

        Integer citoyenId = parseSelectionId(cbCitoyen, citoyenIdByLabel);
        if (citoyenId == null) {
            showFieldError(cbCitoyen, lblCitoyenError, "Selectionnez un citoyen valide.");
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

    private Integer parseSelectionId(ComboBox<String> combo, Map<String, Integer> idByLabel) {
        String value = combo.getValue();
        if (value == null) {
            return null;
        }
        Integer mapped = idByLabel.get(value);
        return (mapped != null && mapped > 0) ? mapped : null;
    }

    private void updateContextCardFromSelection() {
        Integer appelId = parseSelectionId(cbAppelOffre, appelIdByLabel);
        if (appelId == null) {
            resetContextCard();
            return;
        }
        AppelOffre a = appelsById.get(appelId);
        if (a == null) {
            lblCtxAppel.setText("Appel #" + appelId);
            lblCtxQuantiteDemandee.setText("-");
            lblCtxDateLimite.setText("-");
            return;
        }

        lblCtxAppel.setText("Appel #" + a.getId() + " - " + a.getTitre());
        lblCtxQuantiteDemandee.setText(String.format("%.2f kg", a.getQuantiteDemandee()));
        if (a.getDateLimite() != null) {
            lblCtxDateLimite.setText(dateFormatter.format(a.getDateLimite().toLocalDateTime()));
        } else {
            lblCtxDateLimite.setText("-");
        }
    }

    private void resetContextCard() {
        if (lblCtxAppel != null) {
            lblCtxAppel.setText("Aucun appel selectionne");
        }
        if (lblCtxQuantiteDemandee != null) {
            lblCtxQuantiteDemandee.setText("-");
        }
        if (lblCtxDateLimite != null) {
            lblCtxDateLimite.setText("-");
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
