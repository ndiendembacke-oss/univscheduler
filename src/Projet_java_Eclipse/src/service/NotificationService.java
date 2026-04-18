package service;

import model.*;
import dao.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationService {

    private UtilisateurDAO utilisateurDAO;
    private CoursDAO coursDAO;
    private ReservationDAO reservationDAO;

    // Simuler l'envoi d'emails (à remplacer par un vrai service email)
    public NotificationService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.coursDAO = new CoursDAO();
        this.reservationDAO = new ReservationDAO();
    }

    /**
     * Notifier un changement de salle
     */
    public void notifierChangementSalle(int coursId, int ancienneSalleId, int nouvelleSalleId) {
        Cours cours = coursDAO.getById(coursId);
        if (cours == null)
            return;

        Utilisateur enseignant = utilisateurDAO.getById(cours.getEnseignantId());
        List<Utilisateur> etudiants = getEtudiantsDeLaClasse(cours.getClasseId());

        String sujet = "Changement de salle pour " + cours.getMatiere();
        String message = String.format(
                "Le cours de %s prévu le %s a changé de salle.\n" +
                        "Ancienne salle: %d\n" +
                        "Nouvelle salle: %d\n" +
                        "Merci de votre compréhension.",
                cours.getMatiere(),
                cours.getDate(),
                ancienneSalleId,
                nouvelleSalleId);

        // Envoyer à l'enseignant
        envoyerEmail(enseignant, sujet, message);

        // Envoyer aux étudiants
        for (Utilisateur etudiant : etudiants) {
            envoyerEmail(etudiant, sujet, message);
        }
    }

    /**
     * Notifier un nouveau cours
     */
    public void notifierNouveauCours(Cours cours) {
        Utilisateur enseignant = utilisateurDAO.getById(cours.getEnseignantId());
        List<Utilisateur> etudiants = getEtudiantsDeLaClasse(cours.getClasseId());

        String sujet = "Nouveau cours ajouté: " + cours.getMatiere();
        String message = String.format(
                "Un nouveau cours a été planifié:\n" +
                        "Matière: %s\n" +
                        "Date: %s\n" +
                        "Salle: %d\n" +
                        "Type: %s",
                cours.getMatiere(),
                cours.getDate(),
                cours.getSalleId(),
                cours.getType());

        envoyerEmail(enseignant, sujet, message);
        for (Utilisateur etudiant : etudiants) {
            envoyerEmail(etudiant, sujet, message);
        }
    }

    /**
     * Notifier une réservation confirmée
     */
    public void notifierReservationConfirmee(Reservation reservation) {
        Utilisateur utilisateur = utilisateurDAO.getById(reservation.getUtilisateurId());

        String sujet = "Réservation confirmée";
        String message = String.format(
                "Votre réservation a été confirmée:\n" +
                        "Salle: %d\n" +
                        "Date: %s\n" +
                        "Heure: %s - %s\n" +
                        "Motif: %s",
                reservation.getSalleId(),
                reservation.getDateFormatee(),
                reservation.getHeureDebutFormatee(),
                reservation.getHeureFinFormatee(),
                reservation.getMotif());

        envoyerEmail(utilisateur, sujet, message);
    }

    /**
     * Notifier un conflit détecté
     */
    public void notifierConflit(List<String> conflits) {
        // Notifier les gestionnaires
        List<Utilisateur> gestionnaires = utilisateurDAO.getByRole("GESTIONNAIRE");

        String sujet = "Alerte: Conflit d'emploi du temps détecté";
        String message = "Les conflits suivants ont été détectés:\n\n";
        for (String conflit : conflits) {
            message += "- " + conflit + "\n";
        }

        for (Utilisateur gestionnaire : gestionnaires) {
            envoyerEmail(gestionnaire, sujet, message);
        }
    }

    /**
     * Rappel de réservation (30 min avant)
     */
    public void envoyerRappelsReservations() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dans30Minutes = maintenant.plusMinutes(30);

        List<Reservation> reservationsProches = getReservationsEntre(maintenant, dans30Minutes);

        for (Reservation reservation : reservationsProches) {
            Utilisateur utilisateur = utilisateurDAO.getById(reservation.getUtilisateurId());

            String sujet = "Rappel: Votre réservation commence dans 30 minutes";
            String message = String.format(
                    "Rappel: Vous avez une réservation dans 30 minutes:\n" +
                            "Salle: %d\n" +
                            "Heure: %s\n" +
                            "Motif: %s",
                    reservation.getSalleId(),
                    reservation.getHeureDebutFormatee(),
                    reservation.getMotif());

            envoyerEmail(utilisateur, sujet, message);
        }
    }

    /**
     * Notifier un problème technique
     */
    public void notifierProblemeTechnique(int salleId, String probleme, int utilisateurId) {
        Utilisateur signalant = utilisateurDAO.getById(utilisateurId);
        List<Utilisateur> admins = utilisateurDAO.getByRole("ADMINISTRATEUR");

        String sujet = "Problème technique signalé - Salle " + salleId;
        String message = String.format(
                "Un problème technique a été signalé:\n" +
                        "Salle: %d\n" +
                        "Problème: %s\n" +
                        "Signalé par: %s %s\n" +
                        "Date: %s",
                salleId,
                probleme,
                signalant.getPrenom(),
                signalant.getNom(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        for (Utilisateur admin : admins) {
            envoyerEmail(admin, sujet, message);
        }
    }

    /**
     * Simuler l'envoi d'email
     */
    private void envoyerEmail(Utilisateur utilisateur, String sujet, String message) {
        if (utilisateur == null)
            return;

        System.out.println("=".repeat(50));
        System.out.println("EMAIL envoyé à: " + utilisateur.getEmail());
        System.out.println("Sujet: " + sujet);
        System.out.println("Message:\n" + message);
        System.out.println("=".repeat(50));

        // Ici, vous intégreriez un vrai service d'envoi d'emails
        // JavaMailSender, etc.
    }

    private List<Utilisateur> getEtudiantsDeLaClasse(int classeId) {
        // À implémenter: récupérer les étudiants d'une classe
        return List.of();
    }

    private List<Reservation> getReservationsEntre(LocalDateTime debut, LocalDateTime fin) {
        // À implémenter: récupérer les réservations entre deux dates
        return List.of();
    }
}