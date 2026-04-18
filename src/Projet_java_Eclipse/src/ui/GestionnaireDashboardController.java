package ui;

import dao.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionnaireDashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    
    // ========== EMPLOI DU TEMPS ==========
    @FXML private ComboBox<String> classeCombo;
    @FXML private DatePicker datePicker;
    @FXML private TableView<ObservableList<String>> tableViewEmploi;
    @FXML private Button btnGenererEmploi;
    
    // ========== GESTION DES COURS ==========
    @FXML private TableView<ObservableList<String>> tableViewCours;
    @FXML private Button btnAjouterCours;
    @FXML private Button btnModifierCours;
    @FXML private Button btnSupprimerCours;
    @FXML private Button btnActualiserCours;
    
    // ========== GESTION DES CONFLITS ==========
    @FXML private TableView<ObservableList<String>> tableViewConflits;
    @FXML private Button btnVerifierConflits;
    @FXML private Button btnResoudreConflit;
    @FXML private Label conflitsLabel;
    
    // ========== SALLES LIBRES ==========
    @FXML private ComboBox<String> salleJourCombo;
    @FXML private TableView<ObservableList<String>> tableViewSallesLibres;
    @FXML private Button btnRechercherSalles;
    
    @FXML private Button btnDeconnexion;
    
    private String userEmail;
    
    public void setUserEmail(String email) {
        this.userEmail = email;
        chargerInfos();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== GESTIONNAIRE DASHBOARD COMPLET ===");
        
        // Initialisation
        datePicker.setValue(LocalDate.now());
        
        salleJourCombo.setItems(FXCollections.observableArrayList(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));
        salleJourCombo.setValue("Lundi");
        
        // Charger les classes
        chargerClasses();
        
        // Actions des boutons
        btnGenererEmploi.setOnAction(e -> genererEmploiDuTemps());
        btnAjouterCours.setOnAction(e -> ajouterCours());
        btnModifierCours.setOnAction(e -> modifierCours());
        btnSupprimerCours.setOnAction(e -> supprimerCours());
        btnActualiserCours.setOnAction(e -> chargerTousLesCours());
        btnVerifierConflits.setOnAction(e -> verifierConflits());
        btnResoudreConflit.setOnAction(e -> resoudreConflit());
        btnRechercherSalles.setOnAction(e -> rechercherSallesLibres());
        btnDeconnexion.setOnAction(e -> deconnexion());
        
        // Chargement initial
        chargerTousLesCours();
        verifierConflits();
        rechercherSallesLibres();
        
        // Générer l'emploi du temps pour la date par défaut
        genererEmploiDuTemps();
    }
    
    private void chargerInfos() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT nom FROM utilisateur WHERE email = ?")) {
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                welcomeLabel.setText("👋 " + rs.getString("nom") + " (Gestionnaire)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            if (!classes.isEmpty()) {
                classeCombo.setValue(classes.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ========== 1. GÉNÉRER EMPLOI DU TEMPS (CORRIGÉ) ==========
    
    private void genererEmploiDuTemps() {
        String classe = classeCombo.getValue();
        if (classe == null) {
            alert("Veuillez sélectionner une classe");
            return;
        }
        
        LocalDate date = datePicker.getValue();
        if (date == null) {
            date = LocalDate.now();
            datePicker.setValue(date);
        }
        
        String jour = traduireJour(date.getDayOfWeek().toString());
        String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        int classeId = getClasseId(classe);
        if (classeId == 0) {
            alert("Classe non trouvée dans la base");
            return;
        }
        
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
            
            System.out.println("Exécution requête: " + query);
            System.out.println("Paramètres: classeId=" + classeId + ", jour=" + jour);
            
            ResultSet rs = stmt.executeQuery();
            
            tableViewEmploi.getColumns().clear();
            
            String[] colonnes = {"Matière", "Heure", "Durée", "Salle", "Enseignant"};
            for (int i = 0; i < colonnes.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colonnes[i]);
                col.setCellValueFactory(c -> {
                    if (c.getValue() != null && c.getValue().size() > idx) {
                        return new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx));
                    }
                    return new javafx.beans.property.SimpleStringProperty("");
                });
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
                alert("Aucun cours pour " + classe + " le " + jour + " " + dateStr);
            } else {
                alert(count + " cours trouvés pour " + classe + " le " + jour + " " + dateStr);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            alert("Erreur SQL: " + e.getMessage());
        }
    }
    
    // ========== 2. CRUD COURS ==========
    
    private void chargerTousLesCours() {
        String query = "SELECT c.id, c.matiere, u.nom as enseignant, s.numero as salle, cl.nom as classe, " +
                      "c.jour, c.heure_debut, c.duree " +
                      "FROM cours c " +
                      "LEFT JOIN utilisateur u ON c.enseignant_id = u.id " +
                      "LEFT JOIN salle s ON c.salle_id = s.id " +
                      "LEFT JOIN classe cl ON c.classe_id = cl.id " +
                      "ORDER BY c.id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableViewCours.getColumns().clear();
            String[] colonnes = {"ID", "Matière", "Enseignant", "Salle", "Classe", "Jour", "Heure", "Durée"};
            for (int i = 0; i < colonnes.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colonnes[i]);
                col.setCellValueFactory(c -> {
                    if (c.getValue() != null && c.getValue().size() > idx) {
                        return new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx));
                    }
                    return new javafx.beans.property.SimpleStringProperty("");
                });
                col.setPrefWidth(90);
                tableViewCours.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id")));
                row.add(rs.getString("matiere") != null ? rs.getString("matiere") : "-");
                row.add(rs.getString("enseignant") != null ? rs.getString("enseignant") : "-");
                row.add(rs.getInt("salle") > 0 ? "Salle " + rs.getInt("salle") : "-");
                row.add(rs.getString("classe") != null ? rs.getString("classe") : "-");
                row.add(rs.getString("jour") != null ? rs.getString("jour") : "-");
                row.add(rs.getString("heure_debut") != null ? rs.getString("heure_debut") : "-");
                row.add(rs.getInt("duree") + "h");
                data.add(row);
            }
            tableViewCours.setItems(data);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void ajouterCours() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("Création d'un nouveau cours");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField matiereField = new TextField();
        matiereField.setPromptText("Matière");
        
        ComboBox<String> enseignantCombo = new ComboBox<>();
        enseignantCombo.setItems(getEnseignants());
        enseignantCombo.setPromptText("Choisir un enseignant");
        
        ComboBox<String> salleCombo = new ComboBox<>();
        salleCombo.setItems(getSalles());
        salleCombo.setPromptText("Choisir une salle");
        
        ComboBox<String> classeCombo = new ComboBox<>();
        classeCombo.setItems(getClasses());
        classeCombo.setPromptText("Choisir une classe");
        
        ComboBox<String> jourCombo = new ComboBox<>();
        jourCombo.setItems(FXCollections.observableArrayList(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));
        jourCombo.setPromptText("Choisir un jour");
        
        TextField heureField = new TextField("08:00");
        heureField.setPromptText("HH:MM");
        
        TextField dureeField = new TextField("2");
        dureeField.setPromptText("Heures");
        
        grid.add(new Label("Matière:"), 0, 0); grid.add(matiereField, 1, 0);
        grid.add(new Label("Enseignant:"), 0, 1); grid.add(enseignantCombo, 1, 1);
        grid.add(new Label("Salle:"), 0, 2); grid.add(salleCombo, 1, 2);
        grid.add(new Label("Classe:"), 0, 3); grid.add(classeCombo, 1, 3);
        grid.add(new Label("Jour:"), 0, 4); grid.add(jourCombo, 1, 4);
        grid.add(new Label("Heure:"), 0, 5); grid.add(heureField, 1, 5);
        grid.add(new Label("Durée (h):"), 0, 6); grid.add(dureeField, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO cours (matiere, enseignant_id, salle_id, classe_id, jour, heure_debut, duree) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, matiereField.getText());
                stmt.setInt(2, getEnseignantId(enseignantCombo.getValue()));
                stmt.setInt(3, getSalleId(salleCombo.getValue()));
                stmt.setInt(4, getClasseId(classeCombo.getValue()));
                stmt.setString(5, jourCombo.getValue());
                stmt.setString(6, heureField.getText() + ":00");
                stmt.setInt(7, Integer.parseInt(dureeField.getText()));
                stmt.executeUpdate();
                
                alert("✅ Cours ajouté avec succès !");
                chargerTousLesCours();
                verifierConflits();
                
            } catch (Exception e) {
                alert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    private void modifierCours() {
        ObservableList<String> selected = tableViewCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Veuillez sélectionner un cours à modifier");
            return;
        }
        
        String id = selected.get(0);
        String matiere = selected.get(1);
        String enseignant = selected.get(2);
        String salle = selected.get(3).replace("Salle ", "");
        String classe = selected.get(4);
        String jour = selected.get(5);
        String heure = selected.get(6).replace(":00", "");
        String duree = selected.get(7).replace("h", "");
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le cours");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField matiereField = new TextField(matiere);
        
        ComboBox<String> enseignantCombo = new ComboBox<>();
        enseignantCombo.setItems(getEnseignants());
        if (!enseignant.equals("-")) {
            enseignantCombo.setValue(enseignant);
        }
        
        ComboBox<String> salleCombo = new ComboBox<>();
        salleCombo.setItems(getSalles());
        if (!salle.equals("-")) {
            salleCombo.setValue("Salle " + salle);
        }
        
        ComboBox<String> classeCombo = new ComboBox<>();
        classeCombo.setItems(getClasses());
        if (!classe.equals("-")) {
            classeCombo.setValue(classe);
        }
        
        ComboBox<String> jourCombo = new ComboBox<>();
        jourCombo.setItems(FXCollections.observableArrayList(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"));
        if (!jour.equals("-")) {
            jourCombo.setValue(jour);
        }
        
        TextField heureField = new TextField(heure);
        TextField dureeField = new TextField(duree);
        
        grid.add(new Label("Matière:"), 0, 0); grid.add(matiereField, 1, 0);
        grid.add(new Label("Enseignant:"), 0, 1); grid.add(enseignantCombo, 1, 1);
        grid.add(new Label("Salle:"), 0, 2); grid.add(salleCombo, 1, 2);
        grid.add(new Label("Classe:"), 0, 3); grid.add(classeCombo, 1, 3);
        grid.add(new Label("Jour:"), 0, 4); grid.add(jourCombo, 1, 4);
        grid.add(new Label("Heure:"), 0, 5); grid.add(heureField, 1, 5);
        grid.add(new Label("Durée:"), 0, 6); grid.add(dureeField, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE cours SET matiere=?, enseignant_id=?, salle_id=?, classe_id=?, jour=?, heure_debut=?, duree=? WHERE id=?")) {
                
                stmt.setString(1, matiereField.getText());
                stmt.setInt(2, getEnseignantId(enseignantCombo.getValue()));
                stmt.setInt(3, getSalleId(salleCombo.getValue()));
                stmt.setInt(4, getClasseId(classeCombo.getValue()));
                stmt.setString(5, jourCombo.getValue());
                stmt.setString(6, heureField.getText() + ":00");
                stmt.setInt(7, Integer.parseInt(dureeField.getText()));
                stmt.setInt(8, Integer.parseInt(id));
                stmt.executeUpdate();
                
                alert("✅ Cours modifié avec succès !");
                chargerTousLesCours();
                verifierConflits();
                
            } catch (Exception e) {
                alert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    private void supprimerCours() {
        ObservableList<String> selected = tableViewCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Veuillez sélectionner un cours à supprimer");
            return;
        }
        
        String id = selected.get(0);
        String matiere = selected.get(1);
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le cours " + matiere + " ?");
        confirm.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM cours WHERE id=?")) {
                
                stmt.setInt(1, Integer.parseInt(id));
                stmt.executeUpdate();
                
                alert("✅ Cours supprimé avec succès !");
                chargerTousLesCours();
                verifierConflits();
                
            } catch (SQLException e) {
                alert("❌ Erreur: " + e.getMessage());
            }
        }
    }
    
    // ========== 3. RÉSOLUTION DES CONFLITS ==========
    
    private void verifierConflits() {
        String query = "SELECT c1.id as id1, c1.matiere as cours1, " +
                      "c2.id as id2, c2.matiere as cours2, " +
                      "c1.jour, s.numero as salle " +
                      "FROM cours c1 " +
                      "JOIN cours c2 ON c1.id < c2.id " +
                      "JOIN salle s ON c1.salle_id = s.id " +
                      "WHERE c1.salle_id = c2.salle_id " +
                      "AND c1.jour = c2.jour " +
                      "AND ((c1.heure_debut < ADDTIME(c2.heure_debut, SEC_TO_TIME(c2.duree*3600))) " +
                      "AND (c2.heure_debut < ADDTIME(c1.heure_debut, SEC_TO_TIME(c1.duree*3600))))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            tableViewConflits.getColumns().clear();
            String[] colonnes = {"ID1", "Cours 1", "ID2", "Cours 2", "Jour", "Salle"};
            for (int i = 0; i < colonnes.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colonnes[i]);
                col.setCellValueFactory(c -> {
                    if (c.getValue() != null && c.getValue().size() > idx) {
                        return new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx));
                    }
                    return new javafx.beans.property.SimpleStringProperty("");
                });
                col.setPrefWidth(100);
                tableViewConflits.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            int count = 0;
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id1")));
                row.add(rs.getString("cours1"));
                row.add(String.valueOf(rs.getInt("id2")));
                row.add(rs.getString("cours2"));
                row.add(rs.getString("jour"));
                row.add("Salle " + rs.getInt("salle"));
                data.add(row);
                count++;
            }
            tableViewConflits.setItems(data);
            conflitsLabel.setText("🔴 " + count + " conflit(s)");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void resoudreConflit() {
        ObservableList<String> selected = tableViewConflits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélectionnez un conflit à résoudre");
            return;
        }
        
        String id1 = selected.get(0);
        String cours1 = selected.get(1);
        String id2 = selected.get(2);
        String cours2 = selected.get(3);
        String jour = selected.get(4);
        String salle = selected.get(5);
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Changer salle pour " + cours1, 
            "Changer salle pour " + cours1,
            "Changer salle pour " + cours2,
            "Changer horaire de " + cours1,
            "Changer horaire de " + cours2);
        
        dialog.setTitle("Résolution de conflit");
        dialog.setHeaderText("Conflit entre " + cours1 + " et " + cours2 + " le " + jour + " en " + salle);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(action -> {
            if (action.startsWith("Changer salle pour " + cours1)) {
                changerSalle(id1, jour);
            } else if (action.startsWith("Changer salle pour " + cours2)) {
                changerSalle(id2, jour);
            } else if (action.startsWith("Changer horaire de " + cours1)) {
                changerHoraire(id1);
            } else if (action.startsWith("Changer horaire de " + cours2)) {
                changerHoraire(id2);
            }
        });
    }
    
    private void changerSalle(String coursId, String jour) {
        String query = "SELECT s.id, s.numero FROM salle s " +
                      "WHERE s.id NOT IN (SELECT salle_id FROM cours WHERE jour = ?) " +
                      "ORDER BY s.numero";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, jour);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<String> salles = FXCollections.observableArrayList();
            while (rs.next()) {
                salles.add("Salle " + rs.getInt("numero"));
            }
            
            if (salles.isEmpty()) {
                alert("Aucune salle libre ce jour !");
                return;
            }
            
            ChoiceDialog<String> choixSalle = new ChoiceDialog<>(salles.get(0), salles);
            choixSalle.setTitle("Changer de salle");
            choixSalle.setHeaderText("Choisissez une nouvelle salle");
            
            Optional<String> nouvelleSalle = choixSalle.showAndWait();
            nouvelleSalle.ifPresent(salleStr -> {
                int nouvelleSalleId = getSalleId(salleStr);
                
                try (Connection conn2 = DatabaseConnection.getConnection();
                     PreparedStatement update = conn2.prepareStatement(
                        "UPDATE cours SET salle_id = ? WHERE id = ?")) {
                    update.setInt(1, nouvelleSalleId);
                    update.setInt(2, Integer.parseInt(coursId));
                    update.executeUpdate();
                    alert("✅ Cours déplacé vers " + salleStr);
                    verifierConflits();
                    chargerTousLesCours();
                } catch (SQLException e) {
                    alert("❌ Erreur: " + e.getMessage());
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void changerHoraire(String coursId) {
        TextInputDialog dialog = new TextInputDialog("08:00");
        dialog.setTitle("Changer l'horaire");
        dialog.setHeaderText("Nouvel horaire (format HH:MM)");
        dialog.setContentText("Heure:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouvelleHeure -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE cours SET heure_debut = ? WHERE id = ?")) {
                stmt.setString(1, nouvelleHeure + ":00");
                stmt.setInt(2, Integer.parseInt(coursId));
                stmt.executeUpdate();
                alert("✅ Horaire modifié !");
                verifierConflits();
                chargerTousLesCours();
            } catch (SQLException e) {
                alert("❌ Erreur: " + e.getMessage());
            }
        });
    }
    
    // ========== 4. SALLES LIBRES ==========
    
    private void rechercherSallesLibres() {
        String jour = salleJourCombo.getValue();
        
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
                col.setCellValueFactory(c -> {
                    if (c.getValue() != null && c.getValue().size() > idx) {
                        return new javafx.beans.property.SimpleStringProperty(c.getValue().get(idx));
                    }
                    return new javafx.beans.property.SimpleStringProperty("");
                });
                col.setPrefWidth(120);
                tableViewSallesLibres.getColumns().add(col);
            }
            
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add("Salle " + rs.getInt("numero"));
                row.add(rs.getInt("capacite") + " places");
                row.add(rs.getString("type") != null ? rs.getString("type") : "-");
                row.add(rs.getString("batiment") != null ? rs.getString("batiment") : "-");
                data.add(row);
            }
            tableViewSallesLibres.setItems(data);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ========== UTILITAIRES ==========
    
    private ObservableList<String> getEnseignants() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT nom FROM utilisateur WHERE role = 'enseignant' ORDER BY nom")) {
            while (rs.next()) {
                list.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private ObservableList<String> getSalles() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT numero FROM salle ORDER BY numero")) {
            while (rs.next()) {
                list.add("Salle " + rs.getInt("numero"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private ObservableList<String> getClasses() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nom FROM classe ORDER BY id")) {
            while (rs.next()) {
                list.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private int getEnseignantId(String nom) {
        if (nom == null || nom.isEmpty() || nom.equals("-")) return 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM utilisateur WHERE nom = ?")) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private int getSalleId(String salleStr) {
        try {
            if (salleStr == null || salleStr.isEmpty() || salleStr.equals("-")) return 0;
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
    
    private int getClasseId(String nom) {
        if (nom == null || nom.isEmpty() || nom.equals("-")) return 0;
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
    
    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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