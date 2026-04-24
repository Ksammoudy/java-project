package controllers.reponseoffre;

import entities.AppelOffre;
import entities.ReponseOffre;
import entities.UserOption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReponseOffreEditController {

    private static final String STYLE_TEXT_FIELD_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 15px;";
    private static final String STYLE_TEXT_AREA_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_COMBO_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_FIELD_ERROR_SUFFIX = " -fx-border-color: #DC2626; -fx-border-width: 1.8;";
    private static final String STYLE_MSG_INFO =
            "-fx-background-color: #EEF7F1; -fx-text-fill: #1D5B41; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 12 10 12;";
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
    private Label lblCtxReponseId;
    @FXML
    private Label lblCtxStatut;
    @FXML
    private Label lblCtxAppelLie;

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();
    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceUserDirectory serviceUserDirectory = new ServiceUserDirectory();
    private final Map<Integer, AppelOffre> appelsById = new LinkedHashMap<>();
    private final Map<String, Integer> appelIdByLabel = new LinkedHashMap<>();
    private final Map<String, Integer> citoyenIdByLabel = new LinkedHashMap<>();

    private Integer currentId;
    private Timestamp currentDateSoumis;
    private String currentStatut;

    @FXML
    public void initialize() {
        applyDefaultStyles();
        resetValidationUi();
        initialiserCombos();

        Integer selectedId = ReponseOffreFlowState.consumeSelectedReponseId();
        if (selectedId == null) {
            setError("Aucune reponse selectionnee. Retournez a la liste.");
            return;
        }
        currentId = selectedId;
        chargerReponse(currentId);
    }

    @FXML
    private void onMettreAJour(ActionEvent event) {
        if (currentId == null) {
            setError("Modification impossible: aucune reponse selectionnee.");
            return;
        }

        try {
            ReponseOffre r = construireDepuisFormulaire();
            serviceReponseOffre.modifier(r);
            ReponseOffreFlowState.setFlashMessage("Reponse mise a jour avec succes.");
            ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onSupprimer(ActionEvent event) {
        if (currentId == null) {
            setError("Suppression impossible: aucune reponse selectionnee.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer cette reponse d'offre ?");
        confirm.setContentText("Cette action est irreversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            serviceReponseOffre.supprimer(currentId);
            ReponseOffreFlowState.setFlashMessage("Reponse supprimee avec succes.");
            ViewNavigator.navigate(event, "/fxml/reponseoffre/ReponseOffreList.fxml", "WasteWise - Liste des reponses d'offre");
        } catch (Exception e) {
            setError("Erreur suppression: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
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
        cbAppelOffre.setPromptText("Selectionnez un appel d'offre");
        cbCitoyen.setPromptText("Selectionnez un citoyen");
        cbAppelOffre.valueProperty().addListener((obs, oldV, newV) -> {
            Integer id = parseSelectionId(cbAppelOffre, appelIdByLabel);
            if (id != null && lblCtxAppelLie != null) {
                lblCtxAppelLie.setText(String.valueOf(id));
            }
        });
    }

    private void chargerReponse(int id) {
        try {
            ReponseOffre r = serviceReponseOffre.recupererParId(id);
            if (r == null) {
                throw new IllegalStateException("Reponse introuvable.");
            }

            currentDateSoumis = r.getDateSoumis() == null ? Timestamp.valueOf(LocalDateTime.now()) : r.getDateSoumis();
            currentStatut = r.getStatut();

            hydrateComboValues(r);
            txtQuantiteProposee.setText(String.valueOf(r.getQuantiteProposee()));
            txtMessage.setText(r.getMessage() == null ? "" : r.getMessage());
            updateContextCard(r);
            setInfo("Edition reponse #" + r.getId() + " - statut actuel: " + normaliserStatut(r.getStatut()));
        } catch (Exception e) {
            setError("Erreur chargement: " + e.getMessage());
        }
    }

    private void updateContextCard(ReponseOffre r) {
        if (lblCtxReponseId != null) {
            lblCtxReponseId.setText("#" + r.getId());
        }
        if (lblCtxStatut != null) {
            lblCtxStatut.setText(toDisplayStatut(normaliserStatut(r.getStatut())));
        }
        if (lblCtxAppelLie != null) {
            lblCtxAppelLie.setText(String.valueOf(r.getAppelOffreId()));
        }
    }

    private void hydrateComboValues(ReponseOffre current) {
        appelIdByLabel.clear();
        citoyenIdByLabel.clear();
        appelsById.clear();

        try {
            List<AppelOffre> appels = serviceAppelOffre.recupererTout();
            for (AppelOffre a : appels) {
                appelsById.put(a.getId(), a);
                String label = "#" + a.getId() + " - " + a.getTitre();
                appelIdByLabel.put(label, a.getId());
            }
        } catch (Exception e) {
            setError("Impossible de charger les appels d'offre: " + e.getMessage());
        }

        try {
            List<UserOption> citoyens = serviceUserDirectory.recupererCitoyens();
            for (UserOption c : citoyens) {
                citoyenIdByLabel.put(c.getLabel(), c.getId());
            }
        } catch (Exception e) {
            setError("Impossible de charger les citoyens: " + e.getMessage());
        }

        cbAppelOffre.getItems().setAll(appelIdByLabel.keySet());
        cbCitoyen.getItems().setAll(citoyenIdByLabel.keySet());
        setSelectionById(cbAppelOffre, appelIdByLabel, current.getAppelOffreId(), "Appel");
        setSelectionById(cbCitoyen, citoyenIdByLabel, current.getCitoyenId(), "Citoyen");

        Integer appelSelected = parseSelectionId(cbAppelOffre, appelIdByLabel);
        if (appelSelected != null && lblCtxAppelLie != null) {
            lblCtxAppelLie.setText(String.valueOf(appelSelected));
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

        Timestamp dateSoumis = currentDateSoumis == null ? Timestamp.valueOf(LocalDateTime.now()) : currentDateSoumis;
        String statut = currentStatut == null ? ReponseOffre.STATUT_EN_ATTENTE : currentStatut;

        return new ReponseOffre(
                currentId,
                quantite,
                dateSoumis,
                statut,
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

    private void setSelectionById(ComboBox<String> combo, Map<String, Integer> idByLabel, int id, String fallbackPrefix) {
        for (Map.Entry<String, Integer> entry : idByLabel.entrySet()) {
            Integer value = entry.getValue();
            if (value != null && value == id) {
                combo.setValue(entry.getKey());
                return;
            }
        }
        String fallback = fallbackPrefix + " #" + id;
        idByLabel.put(fallback, id);
        if (!combo.getItems().contains(fallback)) {
            combo.getItems().add(fallback);
        }
        combo.setValue(fallback);
    }

    private String normaliserStatut(String s) {
        if (s == null) {
            return "en attente";
        }
        String x = s.trim().toLowerCase().replace('_', ' ');
        if ("valide".equals(x) || "validee".equals(x) || "acceptee".equals(x)) {
            return "valide";
        }
        if ("refuse".equals(x) || "refusee".equals(x) || "rejetee".equals(x)) {
            return "refuse";
        }
        return "en attente";
    }

    private String toDisplayStatut(String statutNormalise) {
        if ("valide".equals(statutNormalise)) {
            return "Validee";
        }
        if ("refuse".equals(statutNormalise)) {
            return "Refusee";
        }
        return "En attente";
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
