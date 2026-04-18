package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Reservation;
import service.ReservationService;
import ui.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML
    private ComboBox<String> salleCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> heureDebutCombo;
    @FXML
    private ComboBox<Integer> dureeCombo;
    @FXML
    private TextArea motifArea;
    @FXML
    private Label disponibiliteLabel;
    @FXML
    private ListView<String> reservationsJourList;

    @FXML
    private ComboBox<String> filtreStatutCombo;
    @FXML
    private DatePicker filtreDateDebut;
    @FXML
    private DatePicker filtreDateFin;
    @FXML
    private TableView<Reservation> reservationsTable;
    @FXML
    private TableColumn<Reservation, Integer> idCol;
    @FXML
    private TableColumn<Reservation, Integer> salleResCol;
    @FXML
    private TableColumn<Reservation, String> dateResCol;
    @FXML
    private TableColumn<Reservation, String> heureResCol;
    @FXML
    private TableColumn<Reservation, String> motifResCol;
    @FXML
    private TableColumn<Reservation, String> statutResCol;

    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label confirmeesLabel;
    @FXML
    private Label attenteLabel;
    @FXML
    private Label annuleesLabel;

    private ReservationService reservationService;
    private ObservableList<Reservation> reservationsObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reservationService = new ReservationService();
        reservationsObservable = FXCollections.observableArrayList();

        initialiserComposants();
        initialiserTableau();
        chargerReservations();
    }

    private void initialiserComposants() {
        salleCombo.setItems(FXCollections.observableArrayList(
                "A101 - Amphi A (200 places)",
                "A102 - Salle TD (40 places)",
                "B201 - Labo Info (20 places)",
                "B202 - Salle Réseaux (25 places)",
                "C105 - Salle TP (30 places)"));

        heureDebutCombo.setItems(FXCollections.observableArrayList(
                "08:00", "09:00", "10:00", "11:00", "12:00",
                "14:00", "15:00", "16:00", "17:00", "18:00"));
        heureDebutCombo.setValue("10:00");

        dureeCombo.setItems(FXCollections.observableArrayList(
                30, 60, 90, 120, 150, 180, 240));
        dureeCombo.setValue(60);

        datePicker.setValue(LocalDate.now());

        filtreStatutCombo.setItems(FXCollections.observableArrayList(
                "Tous", "EN_ATTENTE", "CONFIRMEE", "ANNULEE", "TERMINEE"));
        filtreStatutCombo.setValue("Tous");

        filtreDateDebut.setValue(LocalDate.now().minusWeeks(1));
        filtreDateFin.setValue(LocalDate.now().plusWeeks(1));

        disponibiliteLabel.setText("Vérifiez la disponibilité avant de réserver");
    }

    private void initialiserTableau() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        salleResCol.setCellValueFactory(new PropertyValueFactory<>("salleId"));
        dateResCol.setCellValueFactory(new PropertyValueFactory<>("dateFormatee"));
        heureResCol.setCellValueFactory(new PropertyValueFactory<>("periode"));
        motifResCol.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutResCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Style de la colonne statut
        statutResCol.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "CONFIRMEE":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "EN_ATTENTE":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "ANNULEE":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        reservationsTable.setItems(reservationsObservable);
    }

    private void chargerReservations() {
        // Simuler des réservations
        for (int i = 1; i <= 3; i++) {
            Reservation r = new Reservation();
            r.setId(i);
            r.setSalleId(100 + i);
            r.setDate(LocalDate.now().plusDays(i));
            r.setHeureDebut(LocalTime.of(10, 0));
            r.setDuree(120);
            r.setMotif("Réunion projet " + i);
            r.setStatut(i == 1 ? "CONFIRMEE" : (i == 2 ? "EN_ATTENTE" : "ANNULEE"));
            reservationsObservable.add(r);
        }

        mettreAJourStatistiques();

        // Simuler des réservations du jour
        reservationsJourList.getItems().addAll(
                "10h-12h: Salle A101 - Réunion pédagogique",
                "14h-16h: Salle B201 - TP Java");
    }

    private void mettreAJourStatistiques() {
        totalReservationsLabel.setText(String.valueOf(reservationsObservable.size()));

        long confirmees = reservationsObservable.stream()
                .filter(r -> "CONFIRMEE".equals(r.getStatut())).count();
        long attente = reservationsObservable.stream()
                .filter(r -> "EN_ATTENTE".equals(r.getStatut())).count();
        long annulees = reservationsObservable.stream()
                .filter(r -> "ANNULEE".equals(r.getStatut())).count();

        confirmeesLabel.setText(String.valueOf(confirmees));
        attenteLabel.setText(String.valueOf(attente));
        annuleesLabel.setText(String.valueOf(annulees));
    }

    @FXML
    private void handleVerifierDisponibilite() {
        if (salleCombo.getValue() == null || datePicker.getValue() == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une salle et une date");
            return;
        }

        disponibiliteLabel.setText("✓ Salle disponible à ce créneau !");
        disponibiliteLabel.setStyle(
                "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 10; -fx-background-radius: 5;");
    }

    @FXML
    private void handleReserver() {
        if (salleCombo.getValue() == null || motifArea.getText().isEmpty()) {
            AlertUtil.showError("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation(
                "Confirmation",
                "Voulez-vous confirmer cette réservation ?");

        if (confirm) {
            AlertUtil.showInformation("Succès", "Réservation effectuée avec succès !");
            viderFormulaire();
        }
    }

    @FXML
    private void handleAnnuler() {
        viderFormulaire();
    }

    @FXML
    private void handleFiltrer() {
        AlertUtil.showInformation("Filtre", "Application des filtres...");
    }

    @FXML
    private void handleConfirmer() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AlertUtil.showInformation("Succès", "Réservation confirmée");
        } else {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une réservation");
        }
    }

    @FXML
    private void handleAnnulerReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = AlertUtil.showConfirmation(
                    "Annulation",
                    "Voulez-vous vraiment annuler cette réservation ?");
            if (confirm) {
                AlertUtil.showInformation("Succès", "Réservation annulée");
            }
        } else {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une réservation");
        }
    }

    @FXML
    private void handleVoirDetails() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AlertUtil.showInformation(
                    "Détails de la réservation",
                    "ID: " + selected.getId() + "\n" +
                            "Salle: " + selected.getSalleId() + "\n" +
                            "Date: " + selected.getDateFormatee() + "\n" +
                            "Horaire: " + selected.getPeriode() + "\n" +
                            "Motif: " + selected.getMotif() + "\n" +
                            "Statut: " + selected.getStatut());
        }
    }

    private void viderFormulaire() {
        motifArea.clear();
        datePicker.setValue(LocalDate.now());
        heureDebutCombo.setValue("10:00");
        dureeCombo.setValue(60);
        disponibiliteLabel.setText("Vérifiez la disponibilité avant de réserver");
        disponibiliteLabel.setStyle("");
    }
}