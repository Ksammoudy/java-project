package controllers.appeloffre;

import entities.AppelOffre;
import entities.UserOption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import main.navigation.ViewNavigator;
import services.ServiceAppelOffre;
import services.ServiceUserDirectory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppelOffreCreateController {

    private static final String STYLE_TEXT_FIELD_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 15px;";
    private static final String STYLE_TEXT_AREA_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_DATE_PICKER_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_COMBO_OK =
            "-fx-background-color: #F7FAF8; -fx-border-color: #C2D6CA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14px;";
    private static final String STYLE_FIELD_ERROR_SUFFIX =
            " -fx-border-color: #DC2626; -fx-border-width: 1.8;";
    private static final String STYLE_MSG_INFO =
            "-fx-background-color: #E9F7EE; -fx-text-fill: #1D5B41; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 9 12 9 12;";
    private static final String STYLE_MSG_ERROR =
            "-fx-background-color: #FEECEC; -fx-text-fill: #B91C1C; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 9 12 9 12;";

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
    private Label lblTitreError;
    @FXML
    private Label lblDescriptionError;
    @FXML
    private Label lblQuantiteError;
    @FXML
    private Label lblDateError;
    @FXML
    private Label lblValorisateurError;
    @FXML
    private Label lblPreviewTitre;
    @FXML
    private Label lblPreviewQuantite;
    @FXML
    private Label lblPreviewDate;
    @FXML
    private Label lblPreviewValorisateur;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ServiceUserDirectory serviceUserDirectory = new ServiceUserDirectory();
    private final DateTimeFormatter previewDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<String, Integer> valorisateurIdByLabel = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        applyDefaultFieldStyles();
        resetValidationUi();
        dpDateLimite.setValue(LocalDate.now().plusDays(1));
        initialiserValorisateurs();
        initialiserPreview();
        setInfo("Saisissez les informations de votre appel d'offre.");
    }

    @FXML
    private void onEnregistrer(ActionEvent event) {
        try {
            AppelOffre appel = construireDepuisFormulaire();
            serviceAppelOffre.ajouter(appel);
            AppelOffreFlowState.setFlashMessage("Appel d'offre enregistre avec succes.");
            ViewNavigator.navigate(event, "/fxml/appeloffre/AppelOffreList.fxml", "WasteWise - Liste des appels d'offre");
        } catch (Exception e) {
            setError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        AppelOffreFlowState.setFlashMessage("Creation annulee.");
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

    private AppelOffre construireDepuisFormulaire() {
        resetValidationUi();

        boolean hasError = false;

        String titre = lireTexte(txtTitre.getText());
        if (titre.isEmpty()) {
            showFieldError(txtTitre, lblTitreError, "Le titre est obligatoire.");
            hasError = true;
        }

        String description = lireTexte(txtDescription.getText());
        if (description.isEmpty()) {
            showFieldError(txtDescription, lblDescriptionError, "La description est obligatoire.");
            hasError = true;
        }

        Double quantite = parsePositiveDouble(txtQuantiteDemandee.getText());
        if (quantite == null) {
            showFieldError(txtQuantiteDemandee, lblQuantiteError, "Entrez une quantite numerique > 0.");
            hasError = true;
        }

        Integer valorisateurId = parseValorisateurId();
        if (valorisateurId == null) {
            showFieldError(cbValorisateur, lblValorisateurError, "Selectionnez un valorisateur.");
            hasError = true;
        }

        LocalDate date = dpDateLimite.getValue();
        if (date == null) {
            showFieldError(dpDateLimite, lblDateError, "La date limite est obligatoire.");
            hasError = true;
        } else if (!date.isAfter(LocalDate.now())) {
            showFieldError(dpDateLimite, lblDateError, "La date limite doit etre dans le futur.");
            hasError = true;
        }

        if (hasError) {
            throw new IllegalArgumentException("Veuillez corriger les champs en rouge.");
        }

        Timestamp dateLimite = Timestamp.valueOf(date.atTime(23, 59, 59));
        return new AppelOffre(titre, description, quantite, dateLimite, valorisateurId);
    }

    private String lireTexte(String value) {
        return value == null ? "" : value.trim();
    }

    private Double parsePositiveDouble(String value) {
        try {
            double v = Double.parseDouble(lireTexte(value));
            if (v <= 0) {
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyDefaultFieldStyles() {
        txtTitre.setStyle(STYLE_TEXT_FIELD_OK);
        txtQuantiteDemandee.setStyle(STYLE_TEXT_FIELD_OK);
        cbValorisateur.setStyle(STYLE_COMBO_OK);
        txtDescription.setStyle(STYLE_TEXT_AREA_OK);
        dpDateLimite.setStyle(STYLE_DATE_PICKER_OK);
    }

    private void initialiserPreview() {
        updatePreviewCard();
        txtTitre.textProperty().addListener((obs, oldV, newV) -> updatePreviewCard());
        txtQuantiteDemandee.textProperty().addListener((obs, oldV, newV) -> updatePreviewCard());
        cbValorisateur.valueProperty().addListener((obs, oldV, newV) -> updatePreviewCard());
        dpDateLimite.valueProperty().addListener((obs, oldV, newV) -> updatePreviewCard());
    }

    private void updatePreviewCard() {
        if (lblPreviewTitre != null) {
            String titre = lireTexte(txtTitre.getText());
            lblPreviewTitre.setText(titre.isEmpty() ? "Titre non renseigne" : titre);
        }
        if (lblPreviewQuantite != null) {
            Double q = parsePositiveDouble(txtQuantiteDemandee.getText());
            lblPreviewQuantite.setText(q == null ? "-" : String.format("%.2f kg", q));
        }
        if (lblPreviewDate != null) {
            LocalDate d = dpDateLimite.getValue();
            lblPreviewDate.setText(d == null ? "-" : previewDateFormatter.format(d));
        }
        if (lblPreviewValorisateur != null) {
            String label = cbValorisateur.getValue();
            lblPreviewValorisateur.setText(label == null || label.isBlank() ? "-" : label);
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
            if (!cbValorisateur.getItems().isEmpty()) {
                cbValorisateur.setValue(cbValorisateur.getItems().get(0));
            }
        } catch (Exception e) {
            setError("Impossible de charger les valorisateurs: " + e.getMessage());
        }
    }

    private Integer parseValorisateurId() {
        String label = cbValorisateur.getValue();
        if (label == null) {
            return null;
        }
        return valorisateurIdByLabel.get(label);
    }

    private void resetValidationUi() {
        applyDefaultFieldStyles();
        hideErrorLabel(lblTitreError);
        hideErrorLabel(lblDescriptionError);
        hideErrorLabel(lblQuantiteError);
        hideErrorLabel(lblDateError);
        hideErrorLabel(lblValorisateurError);
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

    private void showFieldError(TextArea field, Label errorLabel, String message) {
        field.setStyle(STYLE_TEXT_AREA_OK + STYLE_FIELD_ERROR_SUFFIX);
        showErrorLabel(errorLabel, message);
    }

    private void showFieldError(DatePicker field, Label errorLabel, String message) {
        field.setStyle(STYLE_DATE_PICKER_OK + STYLE_FIELD_ERROR_SUFFIX);
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
