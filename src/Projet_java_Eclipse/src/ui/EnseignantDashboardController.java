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

public class EnseignantDashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label infoLabel;
    
    // ========== EMPLOI DU TEMPS ==========
    @FXML private DatePicker datePickerEmploi;
    @FXML private TableView<ObservableList<String>> tableViewEmploi;
    @FXML private Button btnVoirEmploi;
    
    // ========== SALLES LIBRES ==========
    @FXML private DatePicker datePickerSalle;
    @FXML private TableView<ObservableList<String>> tableViewSallesLibres;
    @FXML private Button btnRechercherSalles;
    
    // ========== RÉSERVATION ==========
    @FXML private ComboBox<String> salleReservationCombo;
    @FXML private DatePicker datePickerReservation;
    @FXML private TextField heureReservationField;
    @FXML private TextField dureeReservationField;
    @FXML private TextArea motifReservationArea;
    @FXML private Button btnReserver;
    
    // ========== SIGNALEMENT ==========
    @FXML private ComboBox<String> salleProblemeCombo;
    @FXML private TextArea descriptionProblemeArea;
    @FXML private Button btnSignaler;
    
    @FXML private Button btnDeconnexion;
    
    private String userEmail;
    private String userNom;
    private int userId;
    
    public void setUserEmail(String email) {
        this.userEmail = email;
        chargerInfosEnseignant();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== ENSEIGNANT DASHBOARD ===");
        
        // Initialisation des dates
        datePickerEmploi.setValue(LocalDate.now());
        datePickerSalle.setValue(LocalDate.now());
        datePickerReservation.setValue(LocalDate.now());
        
        heureReservationField.setText("10:00");
        dureeReservationField.setText("2");
        
        // Charger les salles pour réservation et signalement
        chargerSalles();
        
        // Actions
        btnVoirEmploi.setOnAction(e -> voirEmploiDuTemps());
        btnRechercherSalles.setOnAction(e -> rechercherSallesLibres());
        btnReserver.setOnAction(e -> reserverSalle());
        btnSignaler.setOnAction(e -> signalerProbleme());
        btnDeconnexion.setOnAction(e -> deconnexion());
    }
    
    private void chargerInfosEnseignant() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, nom FROM utilisateur WHERE email = ?")) {
            
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                userId = rs.getInt("id");
                userNom = rs.getString("nom");
                welcomeLabel.setText("👋 Bienvenue Professeur " + userNom);
                infoLabel.setText("Espace Enseignant");
                
                // Charger l'emploi du temps par défaut
                voirEmploiDuTemps();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void chargerSalles() {
        ObservableList<String> salles = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT numero FROM salle ORDER BY numero")) {
            
            while (rs.next()) {
                salles.add("Salle " + rs.getInt("numero"));
            }
            
            salleReservationCombo.setItems(salles);
            salleProblemeCombo.setItems(salles);
            
            if (!salles.isEmpty()) {
                salleReservationCombo.setValue(salles.get(0));
                salleProblemeCombo.setValue(salles.get(0));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ========== 1. VOIR EMPLOI DU TEMPS ==========
    
    private void voirEmploiDuTemps() {
        LocalDate date = datePickerEmploi.getValue();
        if (date == null) {
            date = LocalDate.now();
            datePickerEmploi.setValue(date);
        }
        
        String jour = traduireJour(date.getDayOfWeek().toString());
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        String query = "SELECT c.matiere, c.heure_debut, c.duree, s.numero as salle, cl.nom as classe " +
                      "FROM cours c " +
                      "LEFT JOIN salle s ON c.salle_id = s.id " +
                      "LEFT JOIN classe cl ON c.classe_id = cl.id " +
                      "WHERE c.enseignant_id = ? AND c.jour = ? " +
                      "ORDER BY c.heure_debut";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, jour);
            
            ResultSet rs = stmt.executeQuery();
            
            tableViewEmploi.getColumns().clear();
            
            String[] colonnes = {"Matière", "Heure", "Durée", "Salle", "Classe"};
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
                row.add(rs.getString("classe") != null ? rs.getString("classe") : "-");
                data.add(row);
                count++;
            }
            
            tableViewEmploi.setItems(data);
            
            if (count == 0) {
                showInfo("Aucun cours le " + jour + " " + dateStr);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    
    // ========== 2. RECHERCHER SALLES LIBRES ==========
    
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
    
    // ========== 3. RÉSERVER UNE SALLE ==========
    
    private void reserverSalle() {
        String salleStr = salleReservationCombo.getValue();
        LocalDate date = datePickerReservation.getValue();
        String heure = heureReservationField.getText().trim();
        String dureeStr = dureeReservationField.getText().trim();
        String motif = motifReservationArea.getText().trim();
        
        if (salleStr == null || date == null || heure.isEmpty() || dureeStr.isEmpty() || motif.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        
        int salleId = getSalleId(salleStr);
        String jour = traduireJour(date.getDayOfWeek().toString());
        int duree;
        
        try {
            duree = Integer.parseInt(dureeStr);
        } catch (NumberFormatException e) {
            showError("La durée doit être un nombre");
            return;
        }
        
        // Vérifier si la salle est libre (conflit avec les cours)
        String verifQuery = "SELECT COUNT(*) FROM cours WHERE salle_id = ? AND jour = ? " +
                           "AND ((heure_debut < ? + INTERVAL ? HOUR) " +
                           "AND (? < heure_debut + INTERVAL duree HOUR))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement verifStmt = conn.prepareStatement(verifQuery)) {
            
            verifStmt.setInt(1, salleId);
            verifStmt.setString(2, jour);
            verifStmt.setString(3, heure + ":00");
            verifStmt.setInt(4, duree);
            verifStmt.setString(5, heure + ":00");
            
            ResultSet rs = verifStmt.executeQuery();
            rs.next();
            int conflitsCours = rs.getInt(1);
            
            if (conflitsCours > 0) {
                showError("Cette salle est déjà occupée par un cours à cette heure !");
                return;
            }
            
            // Vérifier aussi les réservations existantes
            String verifResaQuery = "SELECT COUNT(*) FROM reservation WHERE salle_id = ? AND date = ? " +
                                   "AND ((heure_debut < ? + INTERVAL ? HOUR) " +
                                   "AND (? < heure_debut + INTERVAL duree HOUR))";
            
            try (PreparedStatement verifResaStmt = conn.prepareStatement(verifResaQuery)) {
                verifResaStmt.setInt(1, salleId);
                verifResaStmt.setString(2, date.toString());
                verifResaStmt.setString(3, heure + ":00");
                verifResaStmt.setInt(4, duree);
                verifResaStmt.setString(5, heure + ":00");
                
                ResultSet rs2 = verifResaStmt.executeQuery();
                rs2.next();
                int conflitsResa = rs2.getInt(1);
                
                if (conflitsResa > 0) {
                    showError("Cette salle est déjà réservée à cette heure !");
                    return;
                }
            }
            
            // Créer la réservation AVEC motif
            String insertQuery = "INSERT INTO reservation (salle_id, utilisateur_id, date, heure_debut, duree, motif) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, salleId);
                insertStmt.setInt(2, userId);
                insertStmt.setString(3, date.toString());
                insertStmt.setString(4, heure + ":00");
                insertStmt.setInt(5, duree);
                insertStmt.setString(6, motif);
                insertStmt.executeUpdate();
                
                showInfo("✅ Réservation effectuée avec succès !");
                motifReservationArea.clear();
                heureReservationField.setText("10:00");
                dureeReservationField.setText("2");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    
    // ========== 4. SIGNALER UN PROBLÈME ==========
    
    private void signalerProbleme() {
        String salleStr = salleProblemeCombo.getValue();
        String description = descriptionProblemeArea.getText().trim();
        
        if (salleStr == null || description.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        
        int salleId = getSalleId(salleStr);
        
        // Vérifier si la table probleme_technique existe
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "probleme_technique", null);
            
            if (tables.next()) {
                // La table existe
                String insertQuery = "INSERT INTO probleme_technique (salle_id, enseignant_id, date_signalement, description, etat) " +
                                    "VALUES (?, ?, CURDATE(), ?, 'En attente')";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, salleId);
                    stmt.setInt(2, userId);
                    stmt.setString(3, description);
                    stmt.executeUpdate();
                    
                    showInfo("✅ Problème signalé ! L'administrateur va être informé.");
                    descriptionProblemeArea.clear();
                }
            } else {
                // Table n'existe pas, on simule
                showInfo("✅ Problème signalé (simulation).");
                descriptionProblemeArea.clear();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur: " + e.getMessage());
        }
    }
    
    // ========== UTILITAIRES ==========
    
    private int getSalleId(String salleStr) {
        try {
            // Extrait le numéro de la salle (ex: "Salle 42" -> 42)
            int num = Integer.parseInt(salleStr.replaceAll("[^0-9]", ""));
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM salle WHERE numero = ?")) {
                stmt.setInt(1, num);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
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