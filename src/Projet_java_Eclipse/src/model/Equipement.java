package model;

public class Equipement {
    private int id;
    private String nom;
    private String description;
    private boolean fonctionnel;

    // Constructeur vide
    public Equipement() {
    }

    // Constructeur avec paramètres
    public Equipement(int id, String nom, String description, boolean fonctionnel) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.fonctionnel = fonctionnel;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFonctionnel() {
        return fonctionnel;
    }

    public void setFonctionnel(boolean fonctionnel) {
        this.fonctionnel = fonctionnel;
    }

    @Override
    public String toString() {
        return nom + (fonctionnel ? " (OK)" : " (HS)");
    }
}
