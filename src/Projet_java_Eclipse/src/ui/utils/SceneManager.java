package ui.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.MainApp;

import java.io.IOException;

public class SceneManager {

    /**
     * Ouvrir une nouvelle fenêtre
     */
    public static void ouvrirFenetre(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    /**
     * Ouvrir une nouvelle fenêtre avec contrôleur
     */
    public static <T> T ouvrirFenetreAvecControleur(String fxml, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
            return null;
        }
    }

    /**
     * Charger une vue sans l'afficher
     */
    public static <T> T chargerVue(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxml));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fermer la fenêtre actuelle
     */
    public static void fermerFenetre(Stage stage) {
        stage.close();
    }
}