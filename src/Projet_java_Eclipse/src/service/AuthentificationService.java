package service;

import dao.UtilisateurDAO;
import model.Utilisateur;

public class AuthentificationService {
    
    private UtilisateurDAO utilisateurDAO;
    
    public AuthentificationService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }
    
    public String authentifier(String email, String password) {
        Utilisateur u = utilisateurDAO.getByEmail(email);
        if (u == null) return null;
        if (u.getMotDePasse().equals(password)) return u.getRole();
        return null;
    }
}