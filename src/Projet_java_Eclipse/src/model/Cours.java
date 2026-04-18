package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Cours {
    private int id;
    private String matiere;
    private int enseignantId;
    private int classeId;
    private String groupe;
    private int salleId;
    private int creneauId;
    private LocalDate date;
    private String type; // "CM", "TD", "TP"
    private String description;
    
    // Constructeur vide
    public Cours() {}
    
    // Constructeur avec paramètres
    public Cours(int id, String matiere, int enseignantId, int classeId, 
                 int salleId, int creneauId, String type) {
        this.id = id;
        this.matiere = matiere;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.salleId = salleId;
        this.creneauId = creneauId;
        this.type = type;
    }
    
    // Constructeur avec date
    public Cours(int id, String matiere, int enseignantId, int classeId, 
                 int salleId, LocalDate date, int creneauId, String type) {
        this.id = id;
        this.matiere = matiere;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.salleId = salleId;
        this.date = date;
        this.creneauId = creneauId;
        this.type = type;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getMatiere() {
        return matiere;
    }
    
    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }
    
    public int getEnseignantId() {
        return enseignantId;
    }
    
    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }
    
    public int getClasseId() {
        return classeId;
    }
    
    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }
    
    public String getGroupe() {
        return groupe;
    }
    
    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }
    
    public int getSalleId() {
        return salleId;
    }
    
    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }
    
    public int getCreneauId() {
        return creneauId;
    }
    
    public void setCreneauId(int creneauId) {
        this.creneauId = creneauId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Méthode pour obtenir la durée en minutes
    public long getDureeEnMinutes() {
        // Par défaut, un cours dure 2h (120 minutes)
        // À améliorer avec les vrais créneaux
        return 120;
    }
    
    // Méthode pour obtenir l'ID de l'emploi du temps (ajoutée pour Gestionnaire)
    public int getEmploiDuTempsId() {
        // Pour l'instant, on retourne 0 par défaut
        return 0;
    }
    
    // Méthodes pour l'affichage dans les tableaux
    public String getJour() {
        if (date != null) {
            return date.getDayOfWeek().toString();
        }
        return "";
    }
    
    public String getHoraire() {
        // Simuler un horaire basé sur creneauId
        switch (creneauId) {
            case 1: return "08:00 - 10:00";
            case 2: return "10:00 - 12:00";
            case 3: return "14:00 - 16:00";
            case 4: return "16:00 - 18:00";
            default: return "08:00 - 10:00";
        }
    }
    
    public String getEnseignant() {
        return "Enseignant " + enseignantId;
    }
    
    public String getClasse() {
        return "Classe " + classeId;
    }
    
    @Override
    public String toString() {
        return matiere + " (" + type + ")";
    }
}