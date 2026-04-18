package service;

import dao.*;
import model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RechercheSalleService {
    
    private SalleDAO salleDAO;
    private EquipementDAO equipementDAO;
    private CoursDAO coursDAO;
    private ReservationDAO reservationDAO;
    
    public RechercheSalleService() {
        this.salleDAO = new SalleDAO();
        this.equipementDAO = new EquipementDAO();
        this.coursDAO = new CoursDAO();
        this.reservationDAO = new ReservationDAO();
    }
    
    /**
     * Récupérer toutes les salles
     */
    public List<Salle> getAllSalles() {
        return salleDAO.getAll();
    }
    
    /**
     * Récupérer une salle par son ID
     */
    public Salle getSalleById(int id) {
        return salleDAO.getById(id);
    }
    
    /**
     * Recherche simple par capacité
     */
    public List<Salle> rechercherParCapacite(int capaciteMin) {
        return salleDAO.getByCapaciteMin(capaciteMin);
    }
    
    /**
     * Recherche par type de salle
     */
    public List<Salle> rechercherParType(String type) {
        return salleDAO.getByType(type);
    }
    
    /**
     * Recherche par équipements
     */
    public List<Salle> rechercherParEquipements(List<String> equipements) {
        return salleDAO.rechercher(null, null, null, null, equipements);
    }
    
    /**
     * Recherche avancée avec tous les critères
     */
    public List<Salle> rechercherAvancee(Integer capaciteMin, String type, 
                                         Integer etage, Integer batimentId,
                                         List<String> equipements) {
        return salleDAO.rechercher(capaciteMin, type, etage, batimentId, equipements);
    }
    
    /**
     * Rechercher les salles disponibles maintenant
     */
    public List<Salle> rechercherDisponiblesMaintenant() {
        LocalDate aujourdhui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();
        
        List<Salle> toutesLesSalles = salleDAO.getAll();
        List<Salle> disponibles = new ArrayList<>();
        
        for (Salle salle : toutesLesSalles) {
            if (estSalleDisponible(salle.getId(), aujourdhui, maintenant, 60)) {
                disponibles.add(salle);
            }
        }
        
        return disponibles;
    }
    
    /**
     * Rechercher les salles disponibles à une date et heure précises
     */
    public List<Salle> rechercherDisponibles(LocalDate date, LocalTime heureDebut, int duree) {
        List<Salle> toutesLesSalles = salleDAO.getAll();
        List<Salle> disponibles = new ArrayList<>();
        
        for (Salle salle : toutesLesSalles) {
            if (estSalleDisponible(salle.getId(), date, heureDebut, duree)) {
                disponibles.add(salle);
            }
        }
        
        return disponibles;
    }
    
    /**
     * Vérifier si une salle est disponible
     */
    public boolean estSalleDisponible(int salleId, LocalDate date, LocalTime heureDebut, int duree) {
        // Vérifier les cours
        List<Cours> coursDuJour = coursDAO.getBySalle(salleId).stream()
                .filter(c -> c.getDate() != null && c.getDate().equals(date))
                .collect(Collectors.toList());
        
        for (Cours cours : coursDuJour) {
            // Simplification: on suppose que les cours durent 2h
            LocalTime heureDebutCours = LocalTime.of(8 + cours.getCreneauId(), 0);
            LocalTime heureFinCours = heureDebutCours.plusHours(2);
            LocalTime heureFin = heureDebut.plusMinutes(duree);
            
            if (chevauchement(heureDebutCours, heureFinCours, heureDebut, heureFin)) {
                return false;
            }
        }
        
        // Vérifier les réservations
        // À implémenter avec ReservationDAO
        // return reservationDAO.estSalleDisponible(salleId, date, heureDebut, duree);
        
        return true; // Simplifié pour l'instant
    }
    
    /**
     * Suggérer des salles optimisées (juste assez grandes)
     */
    public List<Salle> rechercherSallesOptimisees(int capaciteRequise, 
                                                  List<String> equipementsRequis,
                                                  LocalDate date, 
                                                  LocalTime heureDebut) {
        List<Salle> sallesCorrespondantes = salleDAO.rechercher(capaciteRequise, null, null, null, equipementsRequis);
        
        // Trier par capacité croissante pour avoir les salles juste assez grandes
        sallesCorrespondantes.sort((s1, s2) -> Integer.compare(s1.getCapacite(), s2.getCapacite()));
        
        if (date != null && heureDebut != null) {
            // Filtrer celles disponibles
            return sallesCorrespondantes.stream()
                    .filter(s -> estSalleDisponible(s.getId(), date, heureDebut, 120)) // 2h par défaut
                    .collect(Collectors.toList());
        }
        
        return sallesCorrespondantes;
    }
    
    /**
     * Rechercher des salles alternatives pour un cours
     */
    public List<Salle> trouverAlternatives(Cours cours) {
        List<Salle> alternatives = new ArrayList<>();
        
        if (cours.getSalleId() <= 0) return alternatives;
        
        Salle salleActuelle = salleDAO.getById(cours.getSalleId());
        if (salleActuelle == null) return alternatives;
        
        // Chercher des salles avec des caractéristiques similaires
        List<Salle> candidates = salleDAO.rechercher(
                salleActuelle.getCapacite() - 10, // Un peu moins
                salleActuelle.getType(),
                null, null,
                null // Équipements similaires à vérifier
        );
        
        // Filtrer celles disponibles au même créneau
        LocalTime heureDebut = LocalTime.of(8, 0); // À adapter selon le créneau
        
        for (Salle candidate : candidates) {
            if (candidate.getId() != salleActuelle.getId() &&
                estSalleDisponible(candidate.getId(), cours.getDate(), heureDebut, (int)cours.getDureeEnMinutes())) {
                alternatives.add(candidate);
            }
        }
        
        return alternatives;
    }
    
    /**
     * Taux d'occupation d'une salle
     */
    public double getTauxOccupationSalle(int salleId, LocalDate debut, LocalDate fin) {
        // Simplifié pour l'instant
        return 45.5;
    }
    
    /**
     * Obtenir les heures de pointe
     */
    public List<String> getHeuresDePointe(LocalDate date) {
        List<String> heuresPointe = new ArrayList<>();
        List<Cours> coursDuJour = coursDAO.getByDate(date);
        
        int[] compteurParHeure = new int[24];
        
        for (Cours cours : coursDuJour) {
            int heure = 8 + cours.getCreneauId(); // Simplification
            if (heure < 24) {
                compteurParHeure[heure]++;
            }
        }
        
        // Trouver les heures avec le plus de cours
        int max = 0;
        for (int i = 8; i <= 18; i++) { // Heures de cours typiques
            if (compteurParHeure[i] > max) {
                max = compteurParHeure[i];
            }
        }
        
        for (int i = 8; i <= 18; i++) {
            if (compteurParHeure[i] == max && max > 0) {
                heuresPointe.add(i + "h - " + (i+1) + "h");
            }
        }
        
        return heuresPointe;
    }
    
    private boolean chevauchement(LocalTime d1, LocalTime f1, LocalTime d2, LocalTime f2) {
        return d1.isBefore(f2) && f1.isAfter(d2);
    }
}