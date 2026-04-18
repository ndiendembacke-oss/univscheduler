package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Salle;
import service.SalleService;
import ui.utils.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GestionSallesController implements Initializable {
    
    @FXML private TableView<Salle> sallesTable;
    @FXML private TableColumn<Salle, Integer> idCol;
    @FXML private TableColumn<Salle, String> numeroCol;
    @FXML private TableColumn<Salle, String> nomCol;
    @FXML private TableColumn<Salle, Integer> capaciteCol;
    @FXML private TableColumn<Salle, Integer> etageCol;
    @FXML private TableColumn<Salle, String> typeCol;
    @FXML private TableColumn<Salle, String> batimentCol;
    @FXML private TableColumn<Salle, String> equipementsCol;
    
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> typeFiltre;
    @FXML private TextField capaciteMinFiltre;
    @FXML private ComboBox<String> batimentFiltre;
    @FXML private ComboBox<Integer> etageFiltre;
    @FXML private ListView<String> equipementsFiltre;
    
    @FXML private Label totalSallesLabel;
    @FXML private Label capaciteTotaleLabel;
    
    private SalleService salleService;
    private ObservableList<Salle> sallesObservable;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salleService = new SalleService();
        sallesObservable = FXCollections.observableArrayList();
        
        initialiserTableau();
        initialiserFiltres();
        chargerSalles();
    }
    
    private void initialiserTableau() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroCol.setCellValueFactory(new PropertyValueFactory<>("numero"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        etageCol.setCellValueFactory(new PropertyValueFactory<>("etage"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        batimentCol.setCellValueFactory(new PropertyValueFactory<>("batimentNom"));
        equipementsCol.setCellValueFactory(new PropertyValueFactory<>("equipementsString"));
        
        sallesTable.setItems(sallesObservable);
    }
    
    private void initialiserFiltres() {
        typeFiltre.setItems(FXCollections.observableArrayList("Tous", "TD", "TP", "AMPHI"));
        batimentFiltre.setItems(FXCollections.observableArrayList("Tous", "Bâtiment A", "Bâtiment B", "Bâtiment C"));
        etageFiltre.setItems(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5));
        
        equipementsFiltre.getItems().addAll(
            "Vidéoprojecteur", "Tableau interactif", "Climatisation", "WiFi", "Prise électrique"
        );
        equipementsFiltre.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    private void chargerSalles() {
        try {
            List<Salle> sallesList = salleService.getAllSalles();
            
            if (sallesList != null && !sallesList.isEmpty()) {
                sallesObservable.clear();
                sallesObservable.addAll(sallesList);
                System.out.println("✅ " + sallesList.size() + " salles chargées");
            } else {
                System.out.println("⚠️ Aucune salle trouvée");
                // Données simulées
                for (int i = 1; i <= 5; i++) {
                    Salle s = new Salle();
                    s.setId(i);
                    s.setNumero("A" + (100 + i));
                    s.setNom("Salle A" + (100 + i));
                    s.setCapacite(20 + i * 10);
                    s.setEtage(i % 3);
                    s.setType(i % 2 == 0 ? "TD" : "TP");
                    s.setBatimentId(1);
                    sallesObservable.add(s);
                }
            }
            
            totalSallesLabel.setText("Total salles: " + sallesObservable.size());
            
        } catch (Exception e) {
            System.out.println("❌ Erreur chargement salles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAjouterSalle() {
        AlertUtil.showInformation("Ajout", "Fonctionnalité à venir...");
    }
    
    @FXML
    private void handleModifierSalle() {
        Salle selected = sallesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AlertUtil.showInformation("Modification", "Modification de la salle: " + selected.getNom());
        } else {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une salle");
        }
    }
    
    @FXML
    private void handleSupprimerSalle() {
        Salle selected = sallesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = AlertUtil.showConfirmation("Confirmation", 
                "Voulez-vous vraiment supprimer la salle " + selected.getNom() + " ?");
            if (confirm) {
                sallesObservable.remove(selected);
                AlertUtil.showInformation("Succès", "Salle supprimée");
            }
        } else {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une salle");
        }
    }
    
    @FXML
    private void handleRechercher() {
        String recherche = rechercheField.getText();
        AlertUtil.showInformation("Recherche", "Recherche de: " + recherche);
    }
    
    @FXML
    private void handleAppliquerFiltres() {
        String type = typeFiltre.getValue();
        String capacite = capaciteMinFiltre.getText();
        
        ObservableList<Salle> filtrees = FXCollections.observableArrayList();
        
        for (Salle s : sallesObservable) {
            boolean correspond = true;
            
            if (type != null && !type.equals("Tous") && !type.equals(s.getType())) {
                correspond = false;
            }
            
            if (correspond && capacite != null && !capacite.isEmpty()) {
                try {
                    int capMin = Integer.parseInt(capacite);
                    if (s.getCapacite() < capMin) correspond = false;
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
            
            if (correspond) filtrees.add(s);
        }
        
        sallesTable.setItems(filtrees);
        AlertUtil.showInformation("Filtres", filtrees.size() + " salle(s) trouvée(s)");
    }
    
    @FXML
    private void handleReinitialiserFiltres() {
        typeFiltre.setValue(null);
        capaciteMinFiltre.clear();
        batimentFiltre.setValue(null);
        etageFiltre.setValue(null);
        equipementsFiltre.getSelectionModel().clearSelection();
        sallesTable.setItems(sallesObservable);
    }
    
    @FXML
    private void handleImporter() {
        AlertUtil.showInformation("Import", "Fonctionnalité à venir...");
    }
    
    @FXML
    private void handleExporter() {
        AlertUtil.showInformation("Export", "Fonctionnalité à venir...");
    }
}