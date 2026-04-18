package model;

import java.util.ArrayList;
import java.util.List;

public class Salle {
    private int id;
    private String numero;
    private String nom;
    private int capacite;
    private int etage;
    private String type; // "TD", "TP", "AMPHI", "REUNION"
    private int batimentId;
    private List<Equipement> equipements;
    
    // Constructeur vide
    public Salle() {
        this.equipements = new ArrayList<>();
    }
    
    // Constructeur avec paramètres
    public Salle(int id, String numero, String nom, int capacite, int etage, String type, int batimentId) {
        this.id = id;
        this.numero = numero;
        this.nom = nom;
        this.capacite = capacite;
        this.etage = etage;
        this.type = type;
        this.batimentId = batimentId;
        this.equipements = new ArrayList<>();
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public int getCapacite() {
        return capacite;
    }
    
    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }
    
    public int getEtage() {
        return etage;
    }
    
    public void setEtage(int etage) {
        this.etage = etage;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getBatimentId() {
        return batimentId;
    }
    
    public void setBatimentId(int batimentId) {
        this.batimentId = batimentId;
    }
    
    public List<Equipement> getEquipements() {
        return equipements;
    }
    
    public void setEquipements(List<Equipement> equipements) {
        this.equipements = equipements;
    }
    
    // Méthode pour ajouter un équipement
    public void ajouterEquipement(Equipement equipement) {
        this.equipements.add(equipement);
    }
    
    // Méthode pour vérifier si la salle a un équipement spécifique
    public boolean aEquipement(String nomEquipement) {
        for (Equipement e : equipements) {
            if (e.getNom().equalsIgnoreCase(nomEquipement)) {
                return true;
            }
        }
        return false;
    }
    
    // Méthodes pour l'affichage dans les tableaux
    public String getBatimentNom() {
        return "Bâtiment " + batimentId;
    }
    
    public String getEquipementsString() {
        if (equipements.isEmpty()) {
            return "Aucun";
        }
        StringBuilder sb = new StringBuilder();
        for (Equipement e : equipements) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getNom());
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return (nom != null ? nom : "Salle " + numero) + " (Cap: " + capacite + ")";
    }
}