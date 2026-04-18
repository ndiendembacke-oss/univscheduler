package ui;

import dao.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EtudiantDashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label infoLabel;
    
    // Emploi du temps
    @FXML private ComboBox<String> classeCombo;
    @FXML private DatePicker datePickerEmploi;
    @FXML private TableView<ObservableList<String>> tableViewEmploi;
    @FXML private Button btnVoirEmploi;
    
    // Salles libres (nouvel onglet)
    @FXML private DatePicker datePickerSalle;
    @FXML private TableView<ObservableList<String>> tableViewSallesLibres;
    @FXML private Button btnRechercherSalles;
    
    @FXML private Button btnDeconnexion;
    
    private String userEmail;
    private String userNom;
    private int userId;
    private String userClasse;
    private int classeId;
    
    public void setUserEmail(String email) {
        this.userEmail = email;
        chargerInfosEtudiant();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== ETUDIANT DASHBOARD ===");
        
        // Initialisation des dates
        datePickerEmploi.setValue(LocalDate.now());
        datePickerSalle.setValue(LocalDate.now());
        
        // Charger les classes pour le combo
        chargerClasses();
        
        // Actions
        btnVoirEmploi.setOnAction(e -> voirEmploiDuTemps());
        btnRechercherSalles.setOnAction(e -> rechercherSallesLibres());
        btnDeconnexion.setOnAction(e -> deconnexion());
    }
    
    private void chargerInfosEtudiant() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Récupérer l'utilisateur
            String userQuery = "SELECT id, nom FROM utilisateur WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, userEmail);
            ResultSet rsUser = userStmt.executeQuery();
            
            if (rsUser.next()) {
                userId = rsUser.getInt("id");
                userNom = rsUser.getString("nom");
                welcomeLabel.setText("👋 Bonjour " + userNom);
                
                // Récupérer sa classe via la table etudiant
                String classeQuery = "SELECT c.id, c.nom FROM classe c " +
                                     "JOIN etudiant e ON c.id = e.classe_id " +
                                     "WHERE e.utilisateur_id = ?";
                PreparedStatement classeStmt = conn.prepareStatement(classeQuery);
                classeStmt.setInt(1, userId);
                ResultSet rsClasse = classeStmt.executeQuery();
                
                if (rsClasse.next()) {
                    classeId = rsClasse.getInt("id");
                    userClasse = rsClasse.getString("nom");
                    infoLabel.setText("Classe: " + userClasse);
                    // Sélectionner la classe dans le combo
                    classeCombo.setValue(userClasse);
                } else {
                    infoLabel.setText("Aucune classe assignée");
                }
                
                // Charger l'emploi du temps
                voirEmploiDuTemps();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur chargement infos: " + e.getMessage());
        }
    }
    
    private void chargerClasses() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nom FROM classe ORDER BY id")) {
            ObservableList<String> classes = FXCollections.observableArrayList();
            while (rs.next()) {
                classes.add(rs.getString("nom"));
            }
            classeCombo.setItems(classes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ========== EMPLOI DU TEMPS ==========
    
    private void voirEmploiDuTemps() {
        String classe = classeCombo.getValue();
        if (classe == null) return;
        
        LocalDate date = datePickerEmploi.getValue();
        if (date == null) {
            date = LocalDate.now();
            datePickerEmploi.setValue(date);
        }
        
        String jour = traduireJour(date.getDayOfWeek().toString());
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        int classeId = getClasseId(classe);
        if (classeId == 0) return;
        
        String query = "SELECT c.matiere, c.heure_debut, c.duree, s.numero as salle, u.nom as enseignant " +
                      "FROM cours c " +
                      "LEFT JOIN salle s ON c.salle_id = s.id " +
                      "LEFT JOIN utilisateur u ON c.enseignant_id = u.id " +
                      "WHERE c.classe_id = ? AND c.jour = ? " +
                      "ORDER BY c.heure_debut";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, classeId);
            stmt.setString(2, jour);
            ResultSet rs = stmt.executeQuery();
            
            tableViewEmploi.getColumns().clear();
            
            String[] colonnes = {"Matière", "Heure", "Durée", "Salle", "Enseignant"};
            for (int i = 0; i < colonnes.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colonnes[i]);
                col.setCellValueFactory(c -> 
                    new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx))
                );
                col.setPrefWidth(120);
                tableViewEmploi.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            int count = 0;
            
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("matiere") != null ? rs.getString("matiere") : "-");
                row.add(rs.getString("heure_debut") != null ? rs.getString("heure_debut") : "-");
                row.add(rs.getInt("duree") + "h");
                row.add(rs.getInt("salle") > 0 ? "Salle " + rs.getInt("salle") : "-");
                row.add(rs.getString("enseignant") != null ? rs.getString("enseignant") : "-");
                data.add(row);
                count++;
            }
            
            tableViewEmploi.setItems(data);
            
            if (count == 0) {
                showInfo("Aucun cours pour " + classe + " le " + jour + " " + dateStr);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    
    // ========== RECHERCHER SALLES LIBRES ==========
    
    private void rechercherSallesLibres() {
        LocalDate date = datePickerSalle.getValue();
        if (date == null) {
            date = LocalDate.now();
            datePickerSalle.setValue(date);
        }
        
        String jour = traduireJour(date.getDayOfWeek().toString());
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        String query = "SELECT s.numero, s.capacite, s.type, b.nom as batiment " +
                      "FROM salle s " +
                      "LEFT JOIN batiment b ON s.batiment_id = b.id " +
                      "WHERE s.id NOT IN (SELECT salle_id FROM cours WHERE jour = ?) " +
                      "ORDER BY s.numero";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, jour);
            ResultSet rs = stmt.executeQuery();
            
            tableViewSallesLibres.getColumns().clear();
            
            String[] colonnes = {"Salle", "Capacité", "Type", "Bâtiment"};
            for (int i = 0; i < colonnes.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colonnes[i]);
                col.setCellValueFactory(c -> 
                    new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx))
                );
                col.setPrefWidth(120);
                tableViewSallesLibres.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            int count = 0;
            
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add("Salle " + rs.getInt("numero"));
                row.add(rs.getInt("capacite") + " places");
                row.add(rs.getString("type") != null ? rs.getString("type") : "-");
                row.add(rs.getString("batiment") != null ? rs.getString("batiment") : "-");
                data.add(row);
                count++;
            }
            
            tableViewSallesLibres.setItems(data);
            showInfo(count + " salle(s) libre(s) le " + jour + " " + dateStr);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    
    // ========== UTILITAIRES ==========
    
    private int getClasseId(String nom) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM classe WHERE nom = ?")) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private String traduireJour(String jourEn) {
        switch(jourEn) {
            case "MONDAY": return "Lundi";
            case "TUESDAY": return "Mardi";
            case "WEDNESDAY": return "Mercredi";
            case "THURSDAY": return "Jeudi";
            case "FRIDAY": return "Vendredi";
            case "SATURDAY": return "Samedi";
            default: return jourEn;
        }
    }
    
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private void deconnexion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("UNIV-SCHEDULER - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}