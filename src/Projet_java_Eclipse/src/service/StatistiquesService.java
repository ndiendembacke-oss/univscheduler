package service;

import dao.SalleDAO;
import dao.CoursDAO;
import dao.UtilisateurDAO;
import dao.ReservationDAO;
import model.Salle;
import model.Cours;
import model.Utilisateur;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatistiquesService {
    
    private SalleDAO salleDAO;
    private CoursDAO coursDAO;
    private UtilisateurDAO utilisateurDAO;
    private ReservationDAO reservationDAO;
    
    public StatistiquesService() {
        this.salleDAO = new SalleDAO();
        this.coursDAO = new CoursDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.reservationDAO = new ReservationDAO();
        System.out.println("✅ StatistiquesService initialisé");
    }
    
    public Map<String, Object> getStatistiquesGlobales() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Utilisateurs
            int totalUsers = utilisateurDAO.compterTotal();
            stats.put("total_utilisateurs", totalUsers);
            stats.put("total_administrateurs", utilisateurDAO.compterParRole("ADMINISTRATEUR"));
            stats.put("total_gestionnaires", utilisateurDAO.compterParRole("GESTIONNAIRE"));
            stats.put("total_enseignants", utilisateurDAO.compterParRole("ENSEIGNANT"));
            stats.put("total_etudiants", utilisateurDAO.compterParRole("ETUDIANT"));
            
            // Salles
            List<Salle> salles = salleDAO.getAll();
            stats.put("total_salles", salles != null ? salles.size() : 0);
            stats.put("capacite_totale", salleDAO.getCapaciteTotale());
            
            // Compter par type de salle
            int sallesTd = 0, sallesTp = 0, sallesAmphi = 0;
            if (salles != null) {
                for (Salle s : salles) {
                    String type = s.getType();
                    if ("TD".equals(type)) sallesTd++;
                    else if ("TP".equals(type)) sallesTp++;
                    else if ("AMPHI".equals(type)) sallesAmphi++;
                }
            }
            stats.put("salles_td", sallesTd);
            stats.put("salles_tp", sallesTp);
            stats.put("salles_amphi", sallesAmphi);
            
            // Cours
            List<Cours> cours = coursDAO.getAll();
            stats.put("total_cours", cours != null ? cours.size() : 0);
            stats.put("cours_cm", 0);
            stats.put("cours_td", 0);
            stats.put("cours_tp", 0);
            
            // Réservations
            stats.put("total_reservations", reservationDAO != null ? reservationDAO.compterTotal() : 0);
            stats.put("reservations_confirmees", 0);
            stats.put("reservations_attente", 0);
            stats.put("reservations_annulees", 0);
            
        } catch (Exception e) {
            System.out.println("❌ Erreur dans getStatistiquesGlobales: " + e.getMessage());
            e.printStackTrace();
            
            // Valeurs par défaut
            stats.put("total_utilisateurs", 0);
            stats.put("total_administrateurs", 0);
            stats.put("total_gestionnaires", 0);
            stats.put("total_enseignants", 0);
            stats.put("total_etudiants", 0);
            stats.put("total_salles", 0);
            stats.put("capacite_totale", 0);
            stats.put("salles_td", 0);
            stats.put("salles_tp", 0);
            stats.put("salles_amphi", 0);
            stats.put("total_cours", 0);
            stats.put("cours_cm", 0);
            stats.put("cours_td", 0);
            stats.put("cours_tp", 0);
            stats.put("total_reservations", 0);
            stats.put("reservations_confirmees", 0);
            stats.put("reservations_attente", 0);
            stats.put("reservations_annulees", 0);
        }
        
        return stats;
    }
}