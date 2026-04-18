package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import service.PlanningService;
import ui.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML
    private DatePicker semainePicker;
    @FXML
    private GridPane planningGrid;
    @FXML
    private ToggleGroup vueGroup;

    private PlanningService planningService;

    private final String[] jours = { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi" };
    private final String[] heures = { "08h", "09h", "10h", "11h", "12h", "14h", "15h", "16h", "17h" };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        planningService = new PlanningService();
        semainePicker.setValue(LocalDate.now());

        initialiserPlanning();
    }

    private void initialiserPlanning() {
        planningGrid.getChildren().clear();
        planningGrid.setHgap(2);
        planningGrid.setVgap(2);
        planningGrid.setStyle("-fx-background-color: #ddd; -fx-padding: 10;");

        // Ajouter les en-têtes de jours
        for (int j = 0; j < jours.length; j++) {
            Label jourLabel = new Label(jours[j]);
            jourLabel.setStyle(
                    "-fx-font-weight: bold; -fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10;");
            jourLabel.setMaxWidth(Double.MAX_VALUE);
            jourLabel.setAlignment(Pos.CENTER);
            planningGrid.add(jourLabel, j + 1, 0);
        }

        // Ajouter les heures
        for (int h = 0; h < heures.length; h++) {
            Label heureLabel = new Label(heures[h]);
            heureLabel.setStyle(
                    "-fx-font-weight: bold; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 10;");
            heureLabel.setMaxWidth(Double.MAX_VALUE);
            heureLabel.setAlignment(Pos.CENTER);
            planningGrid.add(heureLabel, 0, h + 1);
        }

        // Ajouter les cellules de cours
        for (int j = 0; j < jours.length; j++) {
            for (int h = 0; h < heures.length; h++) {
                VBox cell = creerCellule(j, h);
                planningGrid.add(cell, j + 1, h + 1);
            }
        }
    }

    private VBox creerCellule(int jour, int heure) {
        VBox cell = new VBox(5);
        cell.setStyle("-fx-background-color: white; -fx-padding: 5; -fx-min-height: 60; -fx-min-width: 120;");
        cell.setAlignment(Pos.TOP_LEFT);

        // Simuler des cours pour démonstration
        if (jour == 0 && heure == 2) { // Lundi 10h
            ajouterCours(cell, "Java", "Salle A101", "#3498db");
        } else if (jour == 1 && heure == 3) { // Mardi 11h
            ajouterCours(cell, "BDD", "Salle B204", "#2ecc71");
        } else if (jour == 2 && heure == 5) { // Mercredi 14h
            ajouterCours(cell, "Réseaux", "Salle C105", "#e74c3c");
        }

        cell.setOnMouseClicked(event -> {
            AlertUtil.showInformation("Créneau",
                    jours[jour] + " " + heures[heure] + "\nCliquez pour ajouter un cours");
        });

        return cell;
    }

    private void ajouterCours(VBox cell, String matiere, String salle, String couleur) {
        Label matiereLabel = new Label(matiere);
        matiereLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

        Label salleLabel = new Label(salle);
        salleLabel.setStyle("-fx-font-size: 10px;");

        cell.getChildren().addAll(matiereLabel, salleLabel);
    }

    @FXML
    private void handleSemainePrecedente() {
        semainePicker.setValue(semainePicker.getValue().minusWeeks(1));
        initialiserPlanning();
    }

    @FXML
    private void handleSemaineSuivante() {
        semainePicker.setValue(semainePicker.getValue().plusWeeks(1));
        initialiserPlanning();
    }

    @FXML
    private void handleAujourdhui() {
        semainePicker.setValue(LocalDate.now());
        initialiserPlanning();
    }
}