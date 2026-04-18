package ui;

import dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.AuthentificationService;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private AuthentificationService authService;
    private static String currentUserEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("LoginController initialisé");
        
        authService = new AuthentificationService();
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        // Tester la connexion BD au démarrage
        testConnexionBD();
        
        // Action du bouton de connexion
        if (loginButton != null) {
            loginButton.setOnAction(event -> handleLogin());
        }
        
        // Permettre la connexion avec la touche Entrée
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
        if (emailField != null) {
            emailField.setOnAction(event -> {
                if (passwordField != null) passwordField.requestFocus();
            });
        }
    }
    
    private void testConnexionBD() {
        try {
            System.out.println("Chargement du driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver chargé, tentative de connexion...");
            
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ Connexion réussie !");
                conn.close();
            } else {
                System.out.println("❌ Échec de connexion");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL: " + e.getMessage());
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Afficher le chargement
        loginButton.setDisable(true);
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        messageLabel.setText("Connexion en cours...");
        messageLabel.setStyle("-fx-text-fill: blue;");

        // Effectuer la connexion dans un thread séparé
        new Thread(() -> {
            try {
                System.out.println("Tentative de connexion pour: " + email);
                
                // Authentifier l'utilisateur
                String role = authService.authentifier(email, password);
                
                // Mettre à jour l'interface dans le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    if (role != null) {
                        System.out.println("✅ Connexion réussie pour: " + email);
                        System.out.println("Rôle: " + role);
                        
                        // Sauvegarder l'email pour les autres contrôleurs
                        currentUserEmail = email;
                        
                        messageLabel.setText("Connexion réussie! Redirection...");
                        messageLabel.setStyle("-fx-text-fill: green;");
                        
                        // Rediriger vers le tableau de bord approprié
                        redirigerVersDashboard(role);
                    } else {
                        System.out.println("❌ Échec de connexion pour: " + email);
                        messageLabel.setText("Email ou mot de passe incorrect");
                        messageLabel.setStyle("-fx-text-fill: red;");
                        loginButton.setDisable(false);
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        passwordField.clear();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.out.println("❌ Erreur: " + e.getMessage());
                    messageLabel.setText("Erreur de connexion: " + e.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
                    loginButton.setDisable(false);
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void redirigerVersDashboard(String role) {
        try {
            String fxmlFile = null;
            String title = null;
            FXMLLoader loader = null;

            // Déterminer la vue en fonction du rôle
            if ("administrateur".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role)) {
                fxmlFile = "/fxml/AdminView.fxml";
                title = "Administrateur";
                System.out.println("✅ Redirection vers: AdminView.fxml");
                
                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("UNIV-SCHEDULER - " + title);
                stage.setMaximized(true);
                stage.show();
                
            } else if ("gestionnaire".equalsIgnoreCase(role)) {
                fxmlFile = "/fxml/GestionnaireDashboardView.fxml";
                title = "Gestionnaire";
                System.out.println("✅ Redirection vers: GestionnaireDashboardView.fxml");
                
                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                
                // Passer l'email au contrôleur gestionnaire
                GestionnaireDashboardController gestionnaireCtrl = loader.getController();
                gestionnaireCtrl.setUserEmail(currentUserEmail);
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("UNIV-SCHEDULER - " + title);
                stage.setMaximized(true);
                stage.show();
                
            } else if ("enseignant".equalsIgnoreCase(role)) {
                fxmlFile = "/fxml/EnseignantDashboardView.fxml";
                title = "Enseignant";
                System.out.println("✅ Redirection vers: EnseignantDashboardView.fxml");
                
                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                
                // Passer l'email au contrôleur enseignant
                EnseignantDashboardController enseignantCtrl = loader.getController();
                enseignantCtrl.setUserEmail(currentUserEmail);
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("UNIV-SCHEDULER - " + title);
                stage.setMaximized(true);
                stage.show();
                
            } else if ("etudiant".equalsIgnoreCase(role)) {
                fxmlFile = "/fxml/EtudiantDashboardView.fxml";
                title = "Étudiant";
                System.out.println("✅ Redirection vers: EtudiantDashboardView.fxml");
                
                loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                
                // Passer l'email au contrôleur étudiant
                EtudiantDashboardController etudiantCtrl = loader.getController();
                etudiantCtrl.setUserEmail(currentUserEmail);
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("UNIV-SCHEDULER - " + title);
                stage.setMaximized(true);
                stage.show();
                
            } else {
                messageLabel.setText("Rôle non reconnu: " + role);
                loginButton.setDisable(false);
                if (loadingIndicator != null) loadingIndicator.setVisible(false);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
            messageLabel.setText("Erreur de chargement: " + e.getMessage());
            loginButton.setDisable(false);
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleQuitter() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quitter l'application");
        alert.setContentText("Êtes-vous sûr de vouloir quitter ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }
}