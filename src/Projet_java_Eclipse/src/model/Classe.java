package model;

public class Classe {
    private int id;
    private String nom;
    private String niveau;
    private String filiere;
    private int capacite;
    private String anneeScolaire;
    private int nombreEtudiants;
    private String departement;
    private String description;

    public Classe() {}

    public Classe(int id, String nom, String niveau, int capacite, String description) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.capacite = capacite;
        this.description = description;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getNiveau() { return niveau; }
    public String getFiliere() { return filiere; }
    public int getCapacite() { return capacite; }
    public String getAnneeScolaire() { return anneeScolaire; }
    public int getNombreEtudiants() { return nombreEtudiants; }
    public String getDepartement() { return departement; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    public void setNombreEtudiants(int nombreEtudiants) { this.nombreEtudiants = nombreEtudiants; }
    public void setDepartement(String departement) { this.departement = departement; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return nom + " (" + niveau + ")";
    }
}
