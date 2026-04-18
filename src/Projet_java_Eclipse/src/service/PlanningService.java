package service;

import dao.*;
import model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlanningService {
    
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;
    private CreneauDAO creneauDAO;
    private ClasseDAO classeDAO;
    private EnseignantDAO enseignantDAO;
    private EmploiDuTempsDAO emploiDuTempsDAO;
    private ConflitDetector conflitDetector;
    
    public PlanningService() {
        this.coursDAO = new CoursDAO();
        this.salleDAO = new SalleDAO();
        this.creneauDAO = new CreneauDAO();
        this.classeDAO = new ClasseDAO();
        this.enseignantDAO = new EnseignantDAO();
        this.emploiDuTempsDAO = new EmploiDuTempsDAO();
        this.conflitDetector = new ConflitDetector(coursDAO.getAll());
    }
    
    // ==================== MÉTHODES DE RÉCUPÉRATION ====================
    
    /**
     * Récupérer tous les cours
     */
    public List<Cours> getAllCours() {
        System.out.println("🔍 PlanningService.getAllCours() appelé");
        List<Cours> cours = coursDAO.getAll();
        System.out.println("✅ " + cours.size() + " cours trouvés");
        return cours;
    }
    
    /**
     * Récupérer un cours par ID
     */
    public Cours getCoursById(int id) {
        return coursDAO.getById(id);
    }
    
    /**
     * Récupérer les cours par enseignant
     */
    public List<Cours> getCoursByEnseignant(int enseignantId) {
        return coursDAO.getByEnseignant(enseignantId);
    }
    
    /**
     * Récupérer les cours par salle
     */
    public List<Cours> getCoursBySalle(int salleId) {
        return coursDAO.getBySalle(salleId);
    }
    
    /**
     * Récupérer les cours par date
     */
    public List<Cours> getCoursByDate(LocalDate date) {
        return coursDAO.getByDate(date);
    }
    
    /**
     * Récupérer les cours par période
     */
    public List<Cours> getCoursByPeriode(LocalDate debut, LocalDate fin) {
        return coursDAO.getByPeriode(debut, fin);
    }
    
    // ==================== CRUD COURS ====================
    
    /**
     * Créer un nouveau cours avec détection automatique des conflits
     */
    public String creerCours(Cours cours) {
        // Vérifier les conflits
        List<String> conflits = conflitDetector.verifierConflits(cours);
        
        if (!conflits.isEmpty()) {
            return "ERREUR: " + String.join(", ", conflits);
        }
        
        // Ajouter le cours
        if (coursDAO.ajouter(cours)) {
            // Mettre à jour le détecteur de conflits
            conflitDetector.ajouterCours(cours);
            return "SUCCES: Cours créé avec succès";
        }
        
        return "ERREUR: Impossible de créer le cours";
    }
    
    /**
     * Modifier un cours
     */
    public boolean modifierCours(Cours cours) {
        return coursDAO.modifier(cours);
    }
    
    /**
     * Supprimer un cours
     */
    public boolean supprimerCours(int id) {
        return coursDAO.supprimer(id);
    }
    
    // ==================== PLANIFICATION AUTOMATIQUE ====================
    
    /**
     * Planification automatique d'un cours
     */
    public String planifierAutomatiquement(String matiere, int enseignantId, int classeId, 
                                           String type, int capaciteRequise, List<String> equipementsRequis) {
        // Récupérer la classe
        Classe classe = classeDAO.getById(classeId);
        if (classe == null) {
            return "ERREUR: Classe non trouvée";
        }
        
        // Récupérer l'enseignant
        Enseignant enseignant = enseignantDAO.getById(enseignantId);
        if (enseignant == null) {
            return "ERREUR: Enseignant non trouvé";
        }
        
        // Chercher une salle disponible
        RechercheSalleService rechercheService = new RechercheSalleService();
        List<Salle> sallesDisponibles = rechercheService.rechercherSallesOptimisees(
            capaciteRequise, equipementsRequis, LocalDate.now(), null
        );
        
        if (sallesDisponibles.isEmpty()) {
            return "ERREUR: Aucune salle disponible";
        }
        
        // Prendre la première salle disponible
        Salle salleChoisie = sallesDisponibles.get(0);
        
        // Créer le cours
        Cours cours = new Cours();
        cours.setMatiere(matiere);
        cours.setEnseignantId(enseignantId);
        cours.setClasseId(classeId);
        cours.setSalleId(salleChoisie.getId());
        cours.setType(type);
        cours.setDate(LocalDate.now());
        cours.setCreneauId(1); // À améliorer
        
        return creerCours(cours);
    }
    
    // ==================== EMPLOIS DU TEMPS ====================
    
    /**
     * Obtenir l'emploi du temps pour une classe
     */
    public List<Cours> getEmploiDuTempsClasse(int classeId, LocalDate date) {
        List<Cours> tousLesCours = coursDAO.getByClasse(classeId);
        
        return tousLesCours.stream()
                .filter(c -> c.getDate() != null && c.getDate().equals(date))
                .sorted((c1, c2) -> Integer.compare(c1.getCreneauId(), c2.getCreneauId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtenir l'emploi du temps pour un enseignant
     */
    public List<Cours> getEmploiDuTempsEnseignant(int enseignantId, LocalDate date) {
        List<Cours> tousLesCours = coursDAO.getByEnseignant(enseignantId);
        
        return tousLesCours.stream()
                .filter(c -> c.getDate() != null && c.getDate().equals(date))
                .sorted((c1, c2) -> Integer.compare(c1.getCreneauId(), c2.getCreneauId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtenir l'emploi du temps pour une salle
     */
    public List<Cours> getEmploiDuTempsSalle(int salleId, LocalDate date) {
        List<Cours> tousLesCours = coursDAO.getBySalle(salleId);
        
        return tousLesCours.stream()
                .filter(c -> c.getDate() != null && c.getDate().equals(date))
                .sorted((c1, c2) -> Integer.compare(c1.getCreneauId(), c2.getCreneauId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Générer un emploi du temps complet pour une semaine
     */
    public EmploiDuTemps genererEmploiDuTempsSemaine(int classeId, LocalDate debutSemaine) {
        LocalDate finSemaine = debutSemaine.plusDays(4); // Lundi au Vendredi
        
        EmploiDuTemps edt = new EmploiDuTemps();
        edt.setNom("EDT Semaine " + debutSemaine.toString());
        edt.setPeriode("Semaine du " + debutSemaine + " au " + finSemaine);
        edt.setDateDebut(debutSemaine);
        edt.setDateFin(finSemaine);
        edt.setClasseId(classeId);
        
        // Récupérer tous les cours de la classe pour cette période
        List<Cours> coursDeLaSemaine = coursDAO.getByPeriode(debutSemaine, finSemaine).stream()
                .filter(c -> c.getClasseId() == classeId)
                .collect(Collectors.toList());
        
        edt.setCoursList(coursDeLaSemaine);
        
        // Sauvegarder l'EDT
        emploiDuTempsDAO.ajouter(edt);
        for (Cours cours : coursDeLaSemaine) {
            emploiDuTempsDAO.ajouterCours(edt.getId(), cours.getId());
        }
        
        return edt;
    }
    
    // ==================== GESTION DES CONFLITS ====================
    
    /**
     * Résoudre un conflit manuellement
     */
    public String resoudreConflit(int coursId1, int coursId2, int nouvelleSalleId, int nouveauCreneauId) {
        Cours cours1 = coursDAO.getById(coursId1);
        Cours cours2 = coursDAO.getById(coursId2);
        
        if (cours1 == null || cours2 == null) {
            return "ERREUR: Cours non trouvés";
        }
        
        // Modifier le deuxième cours
        cours2.setSalleId(nouvelleSalleId);
        cours2.setCreneauId(nouveauCreneauId);
        
        if (coursDAO.modifier(cours2)) {
            // Mettre à jour le détecteur
            conflitDetector.mettreAJour();
            return "SUCCES: Conflit résolu";
        }
        
        return "ERREUR: Impossible de résoudre le conflit";
    }
    
    /**
     * Obtenir tous les conflits
     */
    public List<String> getTousLesConflits() {
        return conflitDetector.getTousLesConflits(0); // À adapter
    }
}