package service;

import dao.*;
import model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationService {

    private ReservationDAO reservationDAO;
    private SalleDAO salleDAO;
    private NotificationService notificationService;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.salleDAO = new SalleDAO();
        this.notificationService = new NotificationService();
    }

    /**
     * Créer une nouvelle réservation
     */
    public String creerReservation(int utilisateurId, int salleId, LocalDate date,
            LocalTime heureDebut, int duree, String motif) {
        // Vérifier que la salle existe
        Salle salle = salleDAO.getById(salleId);
        if (salle == null) {
            return "ERREUR: Salle non trouvée";
        }

        // Vérifier la disponibilité
        if (!reservationDAO.estSalleDisponible(salleId, date, heureDebut, duree)) {
            return "ERREUR: La salle n'est pas disponible à ce créneau";
        }

        // Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setUtilisateurId(utilisateurId);
        reservation.setSalleId(salleId);
        reservation.setDate(date);
        reservation.setHeureDebut(heureDebut);
        reservation.setDuree(duree);
        reservation.setMotif(motif);
        reservation.setStatut("EN_ATTENTE");

        if (reservationDAO.ajouter(reservation)) {
            return "SUCCES: Réservation créée (ID: " + reservation.getId() + ")";
        }

        return "ERREUR: Impossible de créer la réservation";
    }

    /**
     * Confirmer une réservation
     */
    public String confirmerReservation(int reservationId) {
        Reservation reservation = reservationDAO.getById(reservationId);
        if (reservation == null) {
            return "ERREUR: Réservation non trouvée";
        }

        if (reservationDAO.changerStatut(reservationId, "CONFIRMEE")) {
            notificationService.notifierReservationConfirmee(reservation);
            return "SUCCES: Réservation confirmée";
        }

        return "ERREUR: Impossible de confirmer la réservation";
    }

    /**
     * Annuler une réservation
     */
    public String annulerReservation(int reservationId) {
        if (reservationDAO.changerStatut(reservationId, "ANNULEE")) {
            return "SUCCES: Réservation annulée";
        }
        return "ERREUR: Impossible d'annuler la réservation";
    }

    /**
     * Obtenir les réservations d'un utilisateur
     */
    public List<Reservation> getReservationsUtilisateur(int utilisateurId) {
        return reservationDAO.getByUtilisateur(utilisateurId);
    }

    /**
     * Obtenir les réservations d'une salle
     */
    public List<Reservation> getReservationsSalle(int salleId, LocalDate date) {
        return reservationDAO.getBySalle(salleId).stream()
                .filter(r -> r.getDate().equals(date))
                .toList();
    }

    /**
     * Vérifier les conflits pour une nouvelle réservation
     */
    public List<Reservation> verifierConflits(Reservation nouvelleReservation) {
        return reservationDAO.getConflits(nouvelleReservation);
    }

    /**
     * Proposer des créneaux alternatifs
     */
    public String proposerAlternatives(int salleId, LocalDate date, LocalTime heureDebut, int duree) {
        StringBuilder propositions = new StringBuilder("Créneaux alternatifs disponibles:\n");
        
        LocalTime[] creneauxPossibles = {
            LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(14, 0), LocalTime.of(16, 0)
        };
        
        for (LocalTime creneau : creneauxPossibles) {
            if (!creneau.equals(heureDebut) && 
                reservationDAO.estSalleDisponible(salleId, date, creneau, duree)) {
                propositions.append(creneau.format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
            }
        }
        
        return propositions.length() > 0 ? propositions.toString() : "Aucun créneau alternatif disponible";
    }
}

