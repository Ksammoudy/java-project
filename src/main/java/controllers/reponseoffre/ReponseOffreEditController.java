package controllers.reponseoffre;

import entities.ReponseOffre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.navigation.ViewNavigator;
import services.ServiceReponseOffre;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final ServiceReponseOffre serviceReponseOffre = new ServiceReponseOffre();

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
        cbAppelOffre.setEditable(true);
        cbCitoyen.setEditable(true);
        cbAppelOffre.setPromptText("ID appel d'offre");
        cbCitoyen.setPromptText("ID citoyen");
    }

    private void chargerReponse(int id) {
        try {
            ReponseOffre r = serviceReponseOffre.recupererParId(id);
            if (r == null) {
                throw new IllegalStateException("Reponse introuvable.");
            }

            currentDateSoumis = r.getDateSoumis() == null ? Timestamp.valueOf(LocalDateTime.now()) : r.getDateSoumis();
            currentStatut = r.getStatut();

            txtQuantiteProposee.setText(String.valueOf(r.getQuantiteProposee()));
            txtMessage.setText(r.getMessage() == null ? "" : r.getMessage());
            cbAppelOffre.setValue(String.valueOf(r.getAppelOffreId()));
            cbCitoyen.setValue(String.valueOf(r.getCitoyenId()));

            hydrateComboValues(r);
            setInfo("Edition reponse #" + r.getId() + " - statut actuel: " + normaliserStatut(r.getStatut()));
        } catch (Exception e) {
            setError("Erreur chargement: " + e.getMessage());
        }
    }

    private void hydrateComboValues(ReponseOffre current) {
        try {
            List<ReponseOffre> all = serviceReponseOffre.recupererTout();

            Set<String> appels = new LinkedHashSet<>();
            Set<String> citoyens = new LinkedHashSet<>();
            for (ReponseOffre r : all) {
                appels.add(String.valueOf(r.getAppelOffreId()));
                citoyens.add(String.valueOf(r.getCitoyenId()));
            }
            appels.add(String.valueOf(current.getAppelOffreId()));
            citoyens.add(String.valueOf(current.getCitoyenId()));

            ObservableList<String> itemsAppels = FXCollections.observableArrayList(appels);
            ObservableList<String> itemsCitoyens = FXCollections.observableArrayList(citoyens);
            cbAppelOffre.setItems(itemsAppels);
            cbCitoyen.setItems(itemsCitoyens);
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
