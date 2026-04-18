package service;

import dao.UtilisateurDAO;
import model.Utilisateur;
import java.util.List;

public class AdministrateurService {
    
    private UtilisateurDAO utilisateurDAO;
    
    public AdministrateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }
    
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurDAO.getAll();
    }
    
    public Utilisateur getUtilisateurById(int id) {
        return utilisateurDAO.getById(id);
    }
    
    public boolean ajouterUtilisateur(Utilisateur utilisateur) {
        return utilisateurDAO.ajouter(utilisateur);
    }
    
    public boolean modifierUtilisateur(Utilisateur utilisateur) {
        return utilisateurDAO.modifier(utilisateur);
    }
    
    public boolean supprimerUtilisateur(int id) {
        return utilisateurDAO.supprimer(id);
    }
    
    public List<Utilisateur> rechercherUtilisateurs(String recherche) {
        return utilisateurDAO.rechercherParNom(recherche);
    }
    
    public List<Utilisateur> getUtilisateursByRole(String role) {
        return utilisateurDAO.getByRole(role);
    }
}