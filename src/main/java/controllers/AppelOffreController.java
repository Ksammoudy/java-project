package controllers;

import entities.AppelOffre;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceAppelOffre;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AppelOffreController {

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
    private TextField txtRechercheTitre;

    @FXML
    private Label lblInfo;
    @FXML
    private Label lblSelectionState;
    @FXML
    private Label lblTotalAppels;
    @FXML
    private Label lblTotalNonExpires;

    @FXML
    private TableView<AppelOffre> tableAppelOffres;
    @FXML
    private TableColumn<AppelOffre, Integer> colId;
    @FXML
    private TableColumn<AppelOffre, String> colTitre;
    @FXML
    private TableColumn<AppelOffre, String> colDescription;
    @FXML
    private TableColumn<AppelOffre, Double> colQuantiteDemandee;
    @FXML
    private TableColumn<AppelOffre, String> colDateLimite;
    @FXML
    private TableColumn<AppelOffre, Integer> colValorisateurId;

    private final ServiceAppelOffre serviceAppelOffre = new ServiceAppelOffre();
    private final ObservableList<AppelOffre> data = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_VIEW_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        initialiserColonnes();
        initialiserSelectionTable();
        chargerTout();
    }

    private void initialiserColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQuantiteDemandee.setCellValueFactory(new PropertyValueFactory<>("quantiteDemandee"));
        colValorisateurId.setCellValueFactory(new PropertyValueFactory<>("valorisateurId"));

        colDateLimite.setCellValueFactory(cellData -> {
            AppelOffre item = cellData.getValue();
            Timestamp ts = item == null ? null : item.getDateLimite();
            String text = ts == null ? "" : DATE_VIEW_FORMAT.format(ts.toLocalDateTime());
            return new SimpleStringProperty(text);
        });

        tableAppelOffres.setItems(data);
    }

    private void initialiserSelectionTable() {
        tableAppelOffres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                setSelectionState("Aucun appel selectionne");
                return;
            }
            remplirFormulaireDepuisSelection(selected);
            setInfo("Appel selectionne: id=" + selected.getId());
            setSelectionState("ID " + selected.getId() + " - " + selected.getTitre());
        });
    }

    private void remplirFormulaireDepuisSelection(AppelOffre a) {
        txtTitre.setText(a.getTitre());
        txtDescription.setText(a.getDescription());
        txtQuantiteDemandee.setText(String.valueOf(a.getQuantiteDemandee()));
        txtValorisateurId.setText(String.valueOf(a.getValorisateurId()));
        dpDateLimite.setValue(a.getDateLimite() == null ? null : a.getDateLimite().toLocalDateTime().toLocalDate());
    }

    private void chargerTout() {
        try {
            List<AppelOffre> list = serviceAppelOffre.recupererTout();
            data.setAll(list);
            updateResumeCounters(list);
            setInfo("Chargement OK: " + list.size() + " appel(s) d'offre.");
        } catch (Exception e) {
            gererErreur("Impossible de charger les appels d'offre.", e);
        }
    }

    @FXML
    private void onAjouter() {
        try {
            AppelOffre a = lireFormulaireSansId();
            serviceAppelOffre.ajouter(a);
            chargerTout();
            onReinitialiser();
            setInfo("Ajout effectue avec succes.");
        } catch (Exception e) {
            gererErreur("Ajout impossible.", e);
        }
    }

    @FXML
    private void onModifier() {
        AppelOffre selected = tableAppelOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setInfo("Selectionnez un appel a modifier.");
            return;
        }

        try {
            AppelOffre a = lireFormulaireAvecId(selected.getId());
            serviceAppelOffre.modifier(a);
            chargerTout();
            selectionnerParId(a.getId());
            setInfo("Modification effectuee avec succes.");
        } catch (Exception e) {
            gererErreur("Modification impossible.", e);
        }
    }

    @FXML
    private void onSupprimer() {
        AppelOffre selected = tableAppelOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setInfo("Selectionnez un appel a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer l'appel d'offre id=" + selected.getId() + " ?");
        confirm.setContentText("Cette action est irreversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            setInfo("Suppression annulee.");
            return;
        }

        try {
            serviceAppelOffre.supprimer(selected.getId());
            chargerTout();
            onReinitialiser();
            setInfo("Suppression effectuee avec succes.");
        } catch (Exception e) {
            gererErreur("Suppression impossible.", e);
        }
    }

    @FXML
    private void onReinitialiser() {
        txtTitre.clear();
        txtDescription.clear();
        txtQuantiteDemandee.clear();
        txtValorisateurId.clear();
        dpDateLimite.setValue(null);
        tableAppelOffres.getSelectionModel().clearSelection();
        setSelectionState("Aucun appel selectionne");
        setInfo("Formulaire reinitialise.");
    }

    @FXML
    private void onActualiser() {
        chargerTout();
    }

    @FXML
    private void onRechercher() {
        String motCle = txtRechercheTitre.getText() == null ? "" : txtRechercheTitre.getText().trim();
        if (motCle.isEmpty()) {
            chargerTout();
            setInfo("Recherche vide: affichage complet.");
            return;
        }

        try {
            List<AppelOffre> list = serviceAppelOffre.rechercherParTitre(motCle);
            data.setAll(list);
            updateResumeCounters(list);
            setInfo("Resultat recherche: " + list.size() + " appel(s).");
        } catch (Exception e) {
            gererErreur("Recherche impossible.", e);
        }
    }

    @FXML
    private void onNonExpires() {
        try {
            List<AppelOffre> list = serviceAppelOffre.recupererAppelsNonExpires();
            data.setAll(list);
            updateResumeCounters(list);
            setInfo("Filtres non expires: " + list.size() + " appel(s).");
        } catch (Exception e) {
            gererErreur("Filtre non expires impossible.", e);
        }
    }

    @FXML
    private void onRepondreSelectionne() {
        AppelOffre selected = tableAppelOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setInfo("Selectionnez un appel avant de repondre.");
            return;
        }
        setInfo("Pret a ouvrir la vue reponse pour appel id=" + selected.getId() + ".");
    }

    private AppelOffre lireFormulaireSansId() {
        String titre = lireTexteObligatoire(txtTitre, "Titre obligatoire.");
        String description = lireTexteObligatoire(txtDescription, "Description obligatoire.");
        double quantite = lireDoublePositif(txtQuantiteDemandee, "Quantite demandee invalide.");
        int valorisateurId = lireEntierPositif(txtValorisateurId, "Valorisateur id invalide.");
        Timestamp dateLimite = lireDateLimite(dpDateLimite, "Date limite obligatoire.");

        return new AppelOffre(titre, description, quantite, dateLimite, valorisateurId);
    }

    private AppelOffre lireFormulaireAvecId(int id) {
        AppelOffre a = lireFormulaireSansId();
        a.setId(id);
        return a;
    }

    private String lireTexteObligatoire(TextField field, String messageErreur) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(messageErreur);
        }
        return value;
    }

    private String lireTexteObligatoire(TextArea field, String messageErreur) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(messageErreur);
        }
        return value;
    }

    private double lireDoublePositif(TextField field, String messageErreur) {
        try {
            double value = Double.parseDouble(field.getText().trim());
            if (value <= 0) {
                throw new IllegalArgumentException(messageErreur);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(messageErreur);
        }
    }

    private int lireEntierPositif(TextField field, String messageErreur) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value <= 0) {
                throw new IllegalArgumentException(messageErreur);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(messageErreur);
        }
    }

    private Timestamp lireDateLimite(DatePicker picker, String messageErreur) {
        LocalDate date = picker.getValue();
        if (date == null) {
            throw new IllegalArgumentException(messageErreur);
        }
        return Timestamp.valueOf(date.atTime(23, 59, 59));
    }

    private void selectionnerParId(int id) {
        for (AppelOffre a : data) {
            if (a.getId() == id) {
                tableAppelOffres.getSelectionModel().select(a);
                tableAppelOffres.scrollTo(a);
                return;
            }
        }
    }

    private void setInfo(String message) {
        if (lblInfo != null) {
            lblInfo.setText(message);
        }
    }

    private void setSelectionState(String message) {
        if (lblSelectionState != null) {
            lblSelectionState.setText(message);
        }
    }

    private void updateResumeCounters(List<AppelOffre> list) {
        int total = list == null ? 0 : list.size();
        long nonExpires = 0;
        if (list != null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            nonExpires = list.stream()
                    .filter(a -> a != null && a.getDateLimite() != null && a.getDateLimite().after(now))
                    .count();
        }

        if (lblTotalAppels != null) {
            lblTotalAppels.setText(String.valueOf(total));
        }
        if (lblTotalNonExpires != null) {
            lblTotalNonExpires.setText(String.valueOf(nonExpires));
        }
    }

    private void gererErreur(String contexte, Exception e) {
        setInfo(contexte + " " + e.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(contexte);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
