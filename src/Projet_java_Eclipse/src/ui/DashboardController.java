package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.util.ResourceBundle;

public class SimpleController implements Initializable {
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("✅ Contrôleur simple initialisé");
    }
    
    @FXML
    private void handleOK() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Bravo ! Ça fonctionne !");
        alert.showAndWait();
    }
}