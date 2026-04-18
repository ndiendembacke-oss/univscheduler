package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Salle;
import service.RechercheSalleService;
import ui.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class RechercheSalleController implements Initializable {

    @FXML
    private RadioButton rechercheMaintenant;
    @FXML
    private RadioButton rechercheSpecifique;
    @FXML
    private ToggleGroup rechercheGroupe;

    @FXML
    private TextField capaciteMaintenant;
    @FXML
    private ListView<String> equipementsMaintenant;

    @FXML
    private DatePicker dateSpecifique;
    @FXML
    private ComboBox<String> heureSpecifique;
    @FXML
    private ComboBox<Integer> dureeSpecifique;

    @FXML
    private Label resultatCountLabel;
    @FXML
    private TableView<Salle> resultatsTable;
    @FXML
    private TableColumn<Salle, String> salleCol;
    @FXML
    private TableColumn<Salle, String> batimentCol;
    @FXML
    private TableColumn<Salle, Integer> capaciteCol;
    @FXML
    private TableColumn<Salle, Integer> etageCol;
    @FXML
    private TableColumn<Salle, String> typeCol;
    @FXML
    private TableColumn<Salle, String> equipementsCol;
    @FXML
    private TableColumn<Salle, String> disponibiliteCol;

    private RechercheSalleService rechercheService;
    private ObservableList<Salle> sallesObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rechercheService = new RechercheSalleService();
        sallesObservable = FXCollections.observableArrayList();

        initialiserComposants();
        initialiserTableau();

        // Liaison des radio buttons
        rechercheMaintenant.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dateSpecifique.setDisable(newVal);
            heureSpecifique.setDisable(newVal);
            dureeSpecifique.setDisable(newVal);
        });
    }

    private void initialiserComposants() {
        // Équipements disponibles
        equipementsMaintenant.getItems().addAll(
                "Vidéoprojecteur", "Tableau interactif", "Climatisation",
                "WiFi", "Prise électrique", "Ordinateur");
        equipementsMaintenant.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Heures
        heureSpecifique.setItems(FXCollections.observableArrayList(
                "08:00", "09:00", "10:00", "11:00", "12:00",
                "14:00", "15:00", "16:00", "17:00", "18:00"));
        heureSpecifique.setValue("10:00");

        // Durées
        dureeSpecifique.setItems(FXCollections.observableArrayList(
                30, 60, 90, 120, 150, 180, 240));
        dureeSpecifique.setValue(60);

        dateSpecifique.setValue(LocalDate.now());
        capaciteMaintenant.setText("0");
    }

    private void initialiserTableau() {
        salleCol.setCellValueFactory(new PropertyValueFactory<>("numero"));
        batimentCol.setCellValueFactory(new PropertyValueFactory<>("batimentNom"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        etageCol.setCellValueFactory(new PropertyValueFactory<>("etage"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        equipementsCol.setCellValueFactory(new PropertyValueFactory<>("equipementsString"));

        // Colonne disponibilité avec style
        disponibiliteCol.setCellFactory(column -> new TableCell<Salle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText("Disponible");
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                }
            }
        });

        resultatsTable.setItems(sallesObservable);
    }

    @FXML
    private void handleRechercher() {
        sallesObservable.clear();

        int capacite = 0;
        try {
            capacite = Integer.parseInt(capaciteMaintenant.getText());
        } catch (NumberFormatException e) {
            capacite = 0;
        }

        // Simuler des résultats
        for (int i = 1; i <= 5; i++) {
            Salle s = new Salle();
            s.setId(i);
            s.setNumero("A" + (100 + i));
            s.setNom("Salle " + (100 + i));
            s.setCapacite(20 + i * 10);
            s.setEtage(i % 3);
            s.setType(i % 2 == 0 ? "TD" : "TP");
            s.setBatimentId(1);
            sallesObservable.add(s);
        }

        resultatCountLabel.setText(sallesObservable.size() + " salle(s) trouvée(s)");
    }

    @FXML
    private void handleReinitialiser() {
        capaciteMaintenant.setText("0");
        equipementsMaintenant.getSelectionModel().clearSelection();
        dateSpecifique.setValue(LocalDate.now());
        heureSpecifique.setValue("10:00");
        dureeSpecifique.setValue(60);
        sallesObservable.clear();
        resultatCountLabel.setText("0 salle(s) trouvée(s)");
    }
}