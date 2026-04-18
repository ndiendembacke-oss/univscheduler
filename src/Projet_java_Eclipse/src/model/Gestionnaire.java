package model;

import java.util.ArrayList;
import java.util.List;

public class Gestionnaire extends Utilisateur {
    private String departement;
    private String fonction;
    private String telephone;
    private List<Integer> emploisDuTempsGeres; // IDs des EDT qu'il gère
    private List<Integer> sallesGerees; // IDs des salles dont il est responsable

    // Constructeur vide
    public Gestionnaire() {
        super();
        this.setRole("GESTIONNAIRE");
        this.emploisDuTempsGeres = new ArrayList<>();
        this.sallesGerees = new ArrayList<>();
    }

    // Constructeur avec paramètres de base
    public Gestionnaire(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "GESTIONNAIRE");
        this.emploisDuTempsGeres = new ArrayList<>();
        this.sallesGerees = new ArrayList<>();
    }

    // Constructeur complet
    public Gestionnaire(int id, String nom, String prenom, String email, String motDePasse,
            String departement, String fonction, String telephone) {
        super(id, nom, prenom, email, motDePasse, "GESTIONNAIRE");
        this.departement = departement;
        this.fonction = fonction;
        this.telephone = telephone;
        this.emploisDuTempsGeres = new ArrayList<>();
        this.sallesGerees = new ArrayList<>();
    }

    // Getters et Setters spécifiques
    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<Integer> getEmploisDuTempsGeres() {
        return emploisDuTempsGeres;
    }

    public void setEmploisDuTempsGeres(List<Integer> emploisDuTempsGeres) {
        this.emploisDuTempsGeres = emploisDuTempsGeres;
    }

    public List<Integer> getSallesGerees() {
        return sallesGerees;
    }

    public void setSallesGerees(List<Integer> sallesGerees) {
        this.sallesGerees = sallesGerees;
    }

    // Méthodes pour gérer les emplois du temps
    public void ajouterEmploiDuTemps(int emploiDuTempsId) {
        if (!this.emploisDuTempsGeres.contains(emploiDuTempsId)) {
            this.emploisDuTempsGeres.add(emploiDuTempsId);
        }
    }

    public void supprimerEmploiDuTemps(int emploiDuTempsId) {
        this.emploisDuTempsGeres.remove(Integer.valueOf(emploiDuTempsId));
    }

    public boolean gereEmploiDuTemps(int emploiDuTempsId) {
        return this.emploisDuTempsGeres.contains(emploiDuTempsId);
    }

    // Méthodes pour gérer les salles
    public void ajouterSalle(int salleId) {
        if (!this.sallesGerees.contains(salleId)) {
            this.sallesGerees.add(salleId);
        }
    }

    public void supprimerSalle(int salleId) {
        this.sallesGerees.remove(Integer.valueOf(salleId));
    }

    public boolean gereSalle(int salleId) {
        return this.sallesGerees.contains(salleId);
    }

    // Méthodes métier du gestionnaire
    public boolean peutModifierEmploiDuTemps(int emploiDuTempsId) {
        return this.emploisDuTempsGeres.contains(emploiDuTempsId) ||
                this.getRole().equals("ADMINISTRATEUR");
    }

    public boolean peutResoudreConflit(Cours cours1, Cours cours2) {
        // Un gestionnaire peut résoudre les conflits pour les EDT qu'il gère
        return this.emploisDuTempsGeres.contains(cours1.getEmploiDuTempsId()) ||
                this.emploisDuTempsGeres.contains(cours2.getEmploiDuTempsId());
    }

    // Statistiques du gestionnaire
    public int getNombreEmploisDuTempsGeres() {
        return emploisDuTempsGeres.size();
    }

    public int getNombreSallesGerees() {
        return sallesGerees.size();
    }

    // Validation des données
    public boolean validerNouveauCours(Cours cours, Salle salle, Creneau creneau) {
        // Vérifier que la salle est dans ses salles gérées
        if (!this.sallesGerees.contains(salle.getId())) {
            return false;
        }

        // Vérifier que la capacité est suffisante
        // Note: il faudrait récupérer la classe pour connaître le nombre d'étudiants
        // Pour l'instant, on fait une validation simple
        if (salle.getCapacite() < 10) { // Minimum 10 places
            return false;
        }

        return true;
    }

    // Assignation manuelle d'une salle
    public String assignerSalleManuellement(Cours cours, Salle salle) {
        if (this.sallesGerees.contains(salle.getId())) {
            cours.setSalleId(salle.getId());
            return "Salle assignée avec succès";
        } else {
            return "Vous n'avez pas les droits pour assigner cette salle";
        }
    }

    // Génération de rapport
    public String genererRapportOccupation(List<Cours> coursList, List<Salle> sallesList) {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT D'OCCUPATION DES SALLES ===\n");
        rapport.append("Gestionnaire: ").append(this.getPrenom()).append(" ").append(this.getNom()).append("\n");
        rapport.append("Département: ").append(departement).append("\n\n");

        int totalCours = 0;
        for (Salle salle : sallesList) {
            if (this.sallesGerees.contains(salle.getId())) {
                long coursDansSalle = coursList.stream()
                        .filter(c -> c.getSalleId() == salle.getId())
                        .count();
                totalCours += coursDansSalle;

                rapport.append("Salle ").append(salle.getNumero())
                        .append(" (").append(salle.getNom()).append("): ")
                        .append(coursDansSalle).append(" cours\n");
            }
        }

        rapport.append("\nTotal cours planifiés: ").append(totalCours);
        return rapport.toString();
    }

    @Override
    public String getTableauDeBord() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TABLEAU DE BORD GESTIONNAIRE ===\n");
        sb.append("Nom: ").append(this.getPrenom()).append(" ").append(this.getNom()).append("\n");
        sb.append("Département: ").append(departement).append("\n");
        sb.append("Fonction: ").append(fonction).append("\n");
        sb.append("Email: ").append(this.getEmail()).append("\n");
        sb.append("Téléphone: ").append(telephone).append("\n\n");
        sb.append("Statistiques:\n");
        sb.append("- Emplois du temps gérés: ").append(getNombreEmploisDuTempsGeres()).append("\n");
        sb.append("- Salles gérées: ").append(getNombreSallesGerees()).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString() + " - " + fonction + " (" + departement + ")";
    }
}