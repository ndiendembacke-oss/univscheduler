package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Cours;
import model.Creneau;
import service.CoursService;
import service.ConflitDetector;
import ui.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GestionCoursController implements Initializable {

    @FXML
    private TextField matiereField;
    @FXML
    private ComboBox<String> enseignantField;
    @FXML
    private ComboBox<String> classeField;
    @FXML
    private TextField groupeField;
    @FXML
    private ComboBox<String> typeField;
    @FXML
    private DatePicker dateField;
    @FXML
    private ComboBox<String> creneauField;
    @FXML
    private ComboBox<String> salleField;
    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> filtreCoursCombo;
    @FXML
    private TextField rechercheCoursField;
    @FXML
    private TableView<Cours> coursTable;
    @FXML
    private TableColumn<Cours, Integer> idCol;
    @FXML
    private TableColumn<Cours, String> matiereCol;
    @FXML
    private TableColumn<Cours, String> enseignantCol;
    @FXML
    private TableColumn<Cours, String> classeCol;
    @FXML
    private TableColumn<Cours, LocalDate> dateCol;
    @FXML
    private TableColumn<Cours, String> horaireCol;
    @FXML
    private TableColumn<Cours, Integer> salleCol;
    @FXML
    private TableColumn<Cours, String> typeCoursCol;

    private CoursService coursService;
    private ConflitDetector conflitDetector;
    private ObservableList<Cours> coursObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coursService = new CoursService();
        coursObservable = FXCollections.observableArrayList();

        initialiserComposants();
        initialiserTableau();
        chargerCours();
    }

    private void initialiserComposants() {
        typeField.setItems(FXCollections.observableArrayList("CM", "TD", "TP"));

        enseignantField.setItems(FXCollections.observableArrayList(
                "Jean Dupont", "Marie Martin", "Pierre Durand", "Sophie Bernard"));

        classeField.setItems(FXCollections.observableArrayList(
                "L3 Informatique", "M1 Réseaux", "L2 Maths", "L1 Physique"));

        creneauField.setItems(FXCollections.observableArrayList(
                "Lundi 08h-10h", "Lundi 10h-12h", "Lundi 14h-16h", "Lundi 16h-18h",
                "Mardi 08h-10h", "Mardi 10h-12h", "Mardi 14h-16h", "Mardi 16h-18h",
                "Mercredi 08h-10h", "Mercredi 10h-12h", "Mercredi 14h-16h", "Mercredi 16h-18h",
                "Jeudi 08h-10h", "Jeudi 10h-12h", "Jeudi 14h-16h", "Jeudi 16h-18h",
                "Vendredi 08h-10h", "Vendredi 10h-12h", "Vendredi 14h-16h"));

        salleField.setItems(FXCollections.observableArrayList(
                "A101 (Cap: 50)", "A102 (Cap: 40)", "B201 (Cap: 30)",
                "B202 (Cap: 30)", "C105 (Cap: 20)", "Amphi A (Cap: 200)"));

        filtreCoursCombo.setItems(FXCollections.observableArrayList(
                "Tous", "Par classe", "Par enseignant", "Par date"));

        dateField.setValue(LocalDate.now());
    }

    private void initialiserTableau() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        matiereCol.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        enseignantCol.setCellValueFactory(new PropertyValueFactory<>("enseignantNom"));
        classeCol.setCellValueFactory(new PropertyValueFactory<>("classeNom"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        horaireCol.setCellValueFactory(new PropertyValueFactory<>("horaire"));
        salleCol.setCellValueFactory(new PropertyValueFactory<>("salleId"));
        typeCoursCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        coursTable.setItems(coursObservable);
    }

    private void chargerCours() {
        // Simuler des données
        for (int i = 1; i <= 5; i++) {
            Cours c = new Cours();
            c.setId(i);
            c.setMatiere("Java " + i);
            c.setEnseignantId(101);
            c.setClasseId(201);
            c.setDate(LocalDate.now().plusDays(i));
            c.setSalleId(300 + i);
            c.setType(i % 2 == 0 ? "CM" : "TP");
            coursObservable.add(c);
        }
    }

    @FXML
    private void handleVerifierConflits() {
        AlertUtil.showInformation("Vérification", "Aucun conflit détecté pour ce cours.");
    }

    @FXML
    private void handleEnregistrerCours() {
        if (matiereField.getText().isEmpty()) {
            AlertUtil.showError("Erreur", "Veuillez saisir une matière");
            return;
        }

        AlertUtil.showInformation("Succès", "Cours enregistré avec succès !");
        viderFormulaire();
    }

    @FXML
    private void handleAnnuler() {
        viderFormulaire();
    }

    @FXML
    private void handleRechercherCours() {
        String recherche = rechercheCoursField.getText();
        AlertUtil.showInformation("Recherche", "Recherche de: " + recherche);
    }

    private void viderFormulaire() {
        matiereField.clear();
        groupeField.clear();
        descriptionField.clear();
        dateField.setValue(LocalDate.now());
    }
}