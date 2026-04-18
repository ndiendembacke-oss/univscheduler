package service;

import model.Cours;
import model.Salle;
import model.Enseignant;
import model.Creneau;
import dao.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConflitDetector {

    private List<Cours> coursExistants;
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;
    private CreneauDAO creneauDAO;

    public ConflitDetector(List<Cours> coursExistants) {
        this.coursExistants = coursExistants;
        this.coursDAO = new CoursDAO();
        this.salleDAO = new SalleDAO();
        this.creneauDAO = new CreneauDAO();
    }

    /**
     * Vérifier tous les conflits pour un nouveau cours
     */
    public List<String> verifierConflits(Cours nouveauCours) {
        List<String> conflits = new ArrayList<>();

        // Conflit de salle
        String conflitSalle = verifierSalle(nouveauCours);
        if (conflitSalle != null)
            conflits.add(conflitSalle);

        // Conflit d'enseignant
        String conflitEnseignant = verifierEnseignant(nouveauCours);
        if (conflitEnseignant != null)
            conflits.add(conflitEnseignant);

        // Conflit de classe
        String conflitClasse = verifierClasse(nouveauCours);
        if (conflitClasse != null)
            conflits.add(conflitClasse);

        // Vérification de capacité
        String capaciteInsuffisante = verifierCapacite(nouveauCours);
        if (capaciteInsuffisante != null)
            conflits.add(capaciteInsuffisante);

        return conflits;
    }

    /**
     * Vérifier la disponibilité d'une salle
     */
    private String verifierSalle(Cours nouveauCours) {
        if (nouveauCours.getSalleId() <= 0)
            return null;

        for (Cours cours : coursExistants) {
            if (cours.getId() == nouveauCours.getId())
                continue; // Ignorer le cours lui-même

            if (cours.getSalleId() == nouveauCours.getSalleId() &&
                    cours.getDate() != null && nouveauCours.getDate() != null &&
                    cours.getDate().equals(nouveauCours.getDate()) &&
                    cours.getCreneauId() == nouveauCours.getCreneauId()) {

                Salle salle = salleDAO.getById(cours.getSalleId());
                return String.format("Conflit de salle: La salle %s est déjà occupée par %s",
                        salle != null ? salle.getNumero() : "inconnue",
                        cours.getMatiere());
            }
        }
        return null;
    }

    /**
     * Vérifier la disponibilité d'un enseignant
     */
    private String verifierEnseignant(Cours nouveauCours) {
        if (nouveauCours.getEnseignantId() <= 0)
            return null;

        for (Cours cours : coursExistants) {
            if (cours.getId() == nouveauCours.getId())
                continue;

            if (cours.getEnseignantId() == nouveauCours.getEnseignantId() &&
                    cours.getDate() != null && nouveauCours.getDate() != null &&
                    cours.getDate().equals(nouveauCours.getDate()) &&
                    cours.getCreneauId() == nouveauCours.getCreneauId()) {

                return String.format("Conflit enseignant: L'enseignant est déjà occupé par %s",
                        cours.getMatiere());
            }
        }
        return null;
    }

    /**
     * Vérifier la disponibilité d'une classe
     */
    private String verifierClasse(Cours nouveauCours) {
        if (nouveauCours.getClasseId() <= 0)
            return null;

        for (Cours cours : coursExistants) {
            if (cours.getId() == nouveauCours.getId())
                continue;

            if (cours.getClasseId() == nouveauCours.getClasseId() &&
                    cours.getDate() != null && nouveauCours.getDate() != null &&
                    cours.getDate().equals(nouveauCours.getDate()) &&
                    cours.getCreneauId() == nouveauCours.getCreneauId()) {

                return String.format("Conflit classe: La classe a déjà cours de %s",
                        cours.getMatiere());
            }
        }
        return null;
    }

    /**
     * Vérifier si la capacité de la salle est suffisante
     */
    private String verifierCapacite(Cours nouveauCours) {
        if (nouveauCours.getSalleId() <= 0)
            return null;

        Salle salle = salleDAO.getById(nouveauCours.getSalleId());
        if (salle == null)
            return null;

        // Récupérer le nombre d'étudiants de la classe
        // Note: Il faudrait une méthode pour obtenir le nombre d'étudiants

        return null; // À compléter
    }

    /**
     * Vérifier si une salle est disponible à un créneau donné
     */
    public boolean salleDisponible(int salleId, LocalDate date, int creneauId, int coursIdExclu) {
        for (Cours cours : coursExistants) {
            if (cours.getId() == coursIdExclu)
                continue;

            if (cours.getSalleId() == salleId &&
                    cours.getDate() != null && cours.getDate().equals(date) &&
                    cours.getCreneauId() == creneauId) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtenir tous les conflits d'un emploi du temps
     */
    public List<String> getTousLesConflits(int emploiDuTempsId) {
        List<String> conflits = new ArrayList<>();
        List<Cours> coursEDT = coursDAO.getAll(); // À filtrer par EDT

        for (int i = 0; i < coursEDT.size(); i++) {
            for (int j = i + 1; j < coursEDT.size(); j++) {
                Cours c1 = coursEDT.get(i);
                Cours c2 = coursEDT.get(j);

                // Même jour, même créneau
                if (c1.getDate() != null && c2.getDate() != null &&
                        c1.getDate().equals(c2.getDate()) &&
                        c1.getCreneauId() == c2.getCreneauId()) {

                    // Même salle
                    if (c1.getSalleId() == c2.getSalleId()) {
                        conflits.add(String.format("Conflit: %s et %s dans la même salle",
                                c1.getMatiere(), c2.getMatiere()));
                    }

                    // Même enseignant
                    if (c1.getEnseignantId() == c2.getEnseignantId()) {
                        conflits.add(String.format("Conflit: %s et %s avec le même enseignant",
                                c1.getMatiere(), c2.getMatiere()));
                    }

                    // Même classe
                    if (c1.getClasseId() == c2.getClasseId()) {
                        conflits.add(String.format("Conflit: %s et %s pour la même classe",
                                c1.getMatiere(), c2.getMatiere()));
                    }
                }
            }
        }

        return conflits;
    }

    /**
     * Mettre à jour la liste des cours
     */
    public void mettreAJour() {
        this.coursExistants = coursDAO.getAll();
    }

    /**
     * Ajouter un nouveau cours à la liste
     */
    public void ajouterCours(Cours cours) {
        this.coursExistants.add(cours);
    }
}