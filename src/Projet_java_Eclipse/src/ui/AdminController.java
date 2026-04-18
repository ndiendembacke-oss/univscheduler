package ui;

import dao.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private TableView<ObservableList<String>> tableView;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    
    // Statistiques
    @FXML private Label totalUsersLabel, adminsLabel, gestionnairesLabel, enseignantsLabel, etudiantsLabel;
    @FXML private Label batimentsLabel, sallesLabel, equipementsLabel, emploisLabel, coursLabel;
    @FXML private Label creneauxLabel, reservationsLabel, tauxOccupationLabel;
    
    // Boutons
    @FXML private Button btnDashboard, btnUsers, btnBatiments, btnSalles, btnEquipements;
    @FXML private Button btnCreneaux, btnReservations, btnEmplois, btnCours, btnStatistiques;
    @FXML private Button btnConfig, btnExport, btnDeconnexion;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnExporterPDF, btnExporterExcel;
    
    private String currentTable = "utilisateur";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INTERFACE ADMIN COMPLÈTE AVEC CRUD ===");
        
        // Navigation principale
        btnDashboard.setOnAction(e -> chargerDashboard());
        btnUsers.setOnAction(e -> { currentTable = "utilisateur"; chargerUtilisateurs(); });
        btnBatiments.setOnAction(e -> { currentTable = "batiment"; chargerBatiments(); });
        btnSalles.setOnAction(e -> { currentTable = "salle"; chargerSalles(); });
        btnEquipements.setOnAction(e -> { currentTable = "equipement"; chargerEquipements(); });
        btnCreneaux.setOnAction(e -> { currentTable = "creneau"; chargerCreneaux(); });
        btnReservations.setOnAction(e -> { currentTable = "reservation"; chargerReservations(); });
        btnEmplois.setOnAction(e -> { currentTable = "emploi_du_temps"; chargerEmplois(); });
        btnCours.setOnAction(e -> { currentTable = "cours"; chargerCours(); });
        btnStatistiques.setOnAction(e -> { currentTable = "statistiques"; chargerStatistiques(); });
        btnDeconnexion.setOnAction(e -> handleDeconnexion());
        
        // ✅ Boutons Config et Export
        btnConfig.setOnAction(e -> handleConfig());
        btnExport.setOnAction(e -> handleExport());
        
        // ✅ Boutons CRUD réactivés
        btnAjouter.setOnAction(e -> ajouter());
        btnModifier.setOnAction(e -> modifier());
        btnSupprimer.setOnAction(e -> supprimer());
        
        // ✅ Boutons d'export
        btnExporterPDF.setOnAction(e -> exporterFichier("pdf"));
        btnExporterExcel.setOnAction(e -> exporterFichier("csv"));
        
        // Charger dashboard au démarrage
        chargerDashboard();
    }
    
    // ========== TABLEAU DE BORD ==========
    
    private void chargerDashboard() {
        titleLabel.setText("🏠 TABLEAU DE BORD");
        currentTable = "dashboard";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String statsQuery = "SELECT " +
                "(SELECT COUNT(*) FROM utilisateur) as total_u, " +
                "(SELECT COUNT(*) FROM utilisateur WHERE role='administrateur') as adm, " +
                "(SELECT COUNT(*) FROM utilisateur WHERE role='gestionnaire') as gest, " +
                "(SELECT COUNT(*) FROM utilisateur WHERE role='enseignant') as ens, " +
                "(SELECT COUNT(*) FROM utilisateur WHERE role='etudiant') as etu, " +
                "(SELECT COUNT(*) FROM batiment) as bat, " +
                "(SELECT COUNT(*) FROM salle) as sal, " +
                "(SELECT COUNT(*) FROM equipement) as eq, " +
                "(SELECT COUNT(*) FROM cours) as crs, " +
                "(SELECT COUNT(*) FROM creneau) as creneaux, " +
                "(SELECT COUNT(*) FROM reservation) as reserv";
            
            ResultSet rs = stmt.executeQuery(statsQuery);
            if (rs.next()) {
                totalUsersLabel.setText("Total: " + rs.getInt("total_u"));
                adminsLabel.setText("Admins: " + rs.getInt("adm"));
                gestionnairesLabel.setText("Gestionnaires: " + rs.getInt("gest"));
                enseignantsLabel.setText("Enseignants: " + rs.getInt("ens"));
                etudiantsLabel.setText("Étudiants: " + rs.getInt("etu"));
                batimentsLabel.setText("Bâtiments: " + rs.getInt("bat"));
                sallesLabel.setText("Salles: " + rs.getInt("sal"));
                equipementsLabel.setText("Équipements: " + rs.getInt("eq"));
                coursLabel.setText("Cours: " + rs.getInt("crs"));
                creneauxLabel.setText("Créneaux: " + rs.getInt("creneaux"));
                reservationsLabel.setText("Réservations: " + rs.getInt("reserv"));
            }
            
            // Graphiques
            rs = stmt.executeQuery("SELECT role, COUNT(*) as count FROM utilisateur GROUP BY role");
            
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Utilisateurs");
            
            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                pieData.add(new PieChart.Data(role + " (" + count + ")", count));
                series.getData().add(new XYChart.Data<>(role, count));
            }
            
            pieChart.setData(pieData);
            pieChart.setTitle("Répartition des utilisateurs");
            
            barChart.getData().clear();
            barChart.getData().add(series);
            barChart.setTitle("Utilisateurs par rôle");
            
        } catch (SQLException e) {
            showAlert("Erreur dashboard: " + e.getMessage());
        }
    }
    
    // ========== CHARGEMENT DES TABLES ==========
    
    private void chargerUtilisateurs() {
        titleLabel.setText("👥 UTILISATEURS");
        chargerTable("SELECT id, nom, email, role FROM utilisateur");
    }
    
    private void chargerBatiments() {
        titleLabel.setText("🏢 BÂTIMENTS");
        chargerTable("SELECT id, nom, localisation, etages FROM batiment");
    }
    
    private void chargerSalles() {
        titleLabel.setText("🚪 SALLES");
        chargerTable("SELECT id, numero, capacite, type, batiment_id FROM salle");
    }
    
    private void chargerEquipements() {
        titleLabel.setText("💻 ÉQUIPEMENTS");
        chargerTable("SELECT id, nom FROM equipement");
    }
    
    private void chargerCreneaux() {
        titleLabel.setText("⏰ CRÉNEAUX");
        chargerTable("SELECT * FROM creneau");
    }
    
    private void chargerReservations() {
        titleLabel.setText("📝 RÉSERVATIONS");
        chargerTable("SELECT * FROM reservation");
    }
    
    private void chargerEmplois() {
        titleLabel.setText("📅 EMPLOIS DU TEMPS");
        chargerTable("SELECT id, nom, periode, date_debut, date_fin FROM emploi_du_temps");
    }
    
    private void chargerCours() {
        titleLabel.setText("📚 COURS");
        chargerTable("SELECT id, matiere, jour, heure_debut, duree FROM cours");
    }
    
    private void chargerStatistiques() {
        titleLabel.setText("📊 STATISTIQUES");
        String query = 
            "SELECT 'Utilisateurs' as item, COUNT(*) as valeur FROM utilisateur " +
            "UNION SELECT 'Admins', COUNT(*) FROM utilisateur WHERE role='administrateur' " +
            "UNION SELECT 'Gestionnaires', COUNT(*) FROM utilisateur WHERE role='gestionnaire' " +
            "UNION SELECT 'Enseignants', COUNT(*) FROM utilisateur WHERE role='enseignant' " +
            "UNION SELECT 'Étudiants', COUNT(*) FROM utilisateur WHERE role='etudiant' " +
            "UNION SELECT 'Bâtiments', COUNT(*) FROM batiment " +
            "UNION SELECT 'Salles', COUNT(*) FROM salle " +
            "UNION SELECT 'Équipements', COUNT(*) FROM equipement";
        chargerTable(query);
    }
    
    private void chargerTable(String query) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableView.getColumns().clear();
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            
            for (int i = 1; i <= colCount; i++) {
                final int j = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(meta.getColumnName(i));
                col.setCellValueFactory(c -> 
                    new javafx.beans.property.SimpleStringProperty(c.getValue().get(j-1))
                );
                col.setPrefWidth(120);
                tableView.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getString(i) != null ? rs.getString(i) : "-");
                }
                data.add(row);
            }
            
            tableView.setItems(data);
            
        } catch (SQLException e) {
            showAlert("Erreur chargement: " + e.getMessage());
        }
    }
    
    // ========== CRUD ==========
    
    private void ajouter() {
        if (!currentTable.equals("utilisateur")) {
            showAlert("Ajout disponible uniquement pour la table Utilisateurs");
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Création d'un nouvel utilisateur");
        
        TextField nomField = new TextField(); nomField.setPromptText("Nom");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        PasswordField mdpField = new PasswordField(); mdpField.setPromptText("Mot de passe");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.setItems(FXCollections.observableArrayList("administrateur", "gestionnaire", "enseignant", "etudiant"));
        roleBox.setValue("etudiant");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Nom:"), nomField,
            new Label("Email:"), emailField,
            new Label("Mot de passe:"), mdpField,
            new Label("Rôle:"), roleBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)")) {
                
                stmt.setString(1, nomField.getText());
                stmt.setString(2, emailField.getText());
                stmt.setString(3, mdpField.getText());
                stmt.setString(4, roleBox.getValue());
                stmt.executeUpdate();
                
                showAlert("✅ Utilisateur ajouté avec succès !");
                chargerUtilisateurs();
                chargerDashboard();
                
            } catch (SQLException e) {
                showAlert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    private void modifier() {
        if (!currentTable.equals("utilisateur")) {
            showAlert("Modification disponible uniquement pour la table Utilisateurs");
            return;
        }
        
        ObservableList<String> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un utilisateur à modifier");
            return;
        }
        
        String id = selected.get(0);
        String nomActuel = selected.get(1);
        String emailActuel = selected.get(2);
        String roleActuel = selected.get(3);
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modification de l'utilisateur");
        
        TextField nomField = new TextField(nomActuel);
        TextField emailField = new TextField(emailActuel);
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.setItems(FXCollections.observableArrayList("administrateur", "gestionnaire", "enseignant", "etudiant"));
        roleBox.setValue(roleActuel);
        
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Nom:"), nomField,
            new Label("Email:"), emailField,
            new Label("Rôle:"), roleBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE utilisateur SET nom=?, email=?, role=? WHERE id=?")) {
                
                stmt.setString(1, nomField.getText());
                stmt.setString(2, emailField.getText());
                stmt.setString(3, roleBox.getValue());
                stmt.setInt(4, Integer.parseInt(id));
                stmt.executeUpdate();
                
                showAlert("✅ Utilisateur modifié avec succès !");
                chargerUtilisateurs();
                chargerDashboard();
                
            } catch (SQLException e) {
                showAlert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    private void supprimer() {
        if (!currentTable.equals("utilisateur")) {
            showAlert("Suppression disponible uniquement pour la table Utilisateurs");
            return;
        }
        
        ObservableList<String> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un utilisateur à supprimer");
            return;
        }
        
        String id = selected.get(0);
        String nom = selected.get(1);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur " + nom);
        confirm.setContentText("Êtes-vous sûr ?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM utilisateur WHERE id=?")) {
                
                stmt.setInt(1, Integer.parseInt(id));
                stmt.executeUpdate();
                
                showAlert("✅ Utilisateur supprimé avec succès !");
                chargerUtilisateurs();
                chargerDashboard();
                
            } catch (SQLException e) {
                showAlert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    // ========== CONFIGURATION ==========
    
    @FXML
    private void handleConfig() {
        TabPane configPane = new TabPane();
        
        Tab typesTab = new Tab("Types d'équipements");
        typesTab.setContent(new Label("Configuration des types d'équipements à venir"));
        
        Tab paramsTab = new Tab("Paramètres généraux");
        paramsTab.setContent(new Label("Paramètres généraux à venir"));
        
        Tab backupTab = new Tab("Sauvegarde");
        backupTab.setContent(new Label("Sauvegarde de la base de données à venir"));
        
        configPane.getTabs().addAll(typesTab, paramsTab, backupTab);
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configuration du système");
        dialog.setHeaderText("Paramètres de l'application");
        dialog.getDialogPane().setContent(configPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    // ========== MENU EXPORT ==========
    
    @FXML
    private void handleExport() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PDF", "PDF", "Excel (CSV)", "TXT");
        dialog.setTitle("Export des données");
        dialog.setHeaderText("Choisissez le format d'export");
        dialog.setContentText("Format:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(format -> {
            if (format.equals("PDF")) {
                exporterFichier("pdf");
            } else if (format.equals("Excel (CSV)")) {
                exporterFichier("csv");
            } else if (format.equals("TXT")) {
                exporterFichier("txt");
            }
        });
    }
    
    // ========== EXPORT VERS FICHIER ==========
    
    private void exporterFichier(String format) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisissez le dossier de destination");
        
        Stage stage = (Stage) btnExporterPDF.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        
        if (selectedDirectory == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String nomFichier = selectedDirectory.getAbsolutePath() + File.separator + 
                           "export_" + timestamp + "." + format;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(nomFichier))) {
            
            writer.println("=== " + titleLabel.getText() + " ===");
            writer.println("Généré le: " + new Date());
            writer.println();
            
            StringBuilder headerLine = new StringBuilder();
            for (TableColumn<?, ?> col : tableView.getColumns()) {
                headerLine.append(col.getText()).append(format.equals("csv") ? ";" : "\t");
            }
            writer.println(headerLine.toString());
            
            for (ObservableList<String> row : tableView.getItems()) {
                StringBuilder dataLine = new StringBuilder();
                for (String cell : row) {
                    dataLine.append(cell).append(format.equals("csv") ? ";" : "\t");
                }
                writer.println(dataLine.toString());
            }
            
            writer.flush();
            showAlert("✅ Fichier exporté : " + nomFichier);
            
        } catch (Exception e) {
            showAlert("❌ Erreur export: " + e.getMessage());
        }
    }
    
    // ========== DÉCONNEXION ==========
    
    private void handleDeconnexion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("UNIV-SCHEDULER - Connexion");
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage());
        }
    }
    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}