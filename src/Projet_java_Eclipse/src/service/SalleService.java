package service;

import dao.SalleDAO;
import dao.EquipementDAO;
import model.Salle;
import model.Equipement;
import java.util.List;

public class SalleService {
    private SalleDAO salleDAO;
    private EquipementDAO equipementDAO;

    public SalleService() {
        this.salleDAO = new SalleDAO();
        this.equipementDAO = new EquipementDAO();
    }

    public List<Salle> getAll() {
        return salleDAO.getAll();
    }

    public Salle getById(int id) {
        return salleDAO.getById(id);
    }

    public List<Salle> rechercher(Integer capaciteMin, String type, Integer etage, Integer batimentId, List<String> equipements) {
        return salleDAO.rechercher(capaciteMin, type, etage, batimentId, equipements);
    }

    public boolean ajouterSalle(Salle salle) {
        return salleDAO.ajouter(salle);
    }

    public boolean modifierSalle(Salle salle) {
        return salleDAO.modifier(salle);
    }

    public boolean supprimerSalle(int id) {
        return salleDAO.supprimer(id);
    }

    public int getCapaciteTotale() {
        return salleDAO.getCapaciteTotale();
    }
}
