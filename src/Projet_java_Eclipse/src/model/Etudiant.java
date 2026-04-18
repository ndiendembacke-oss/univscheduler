package model;

public class Etudiant extends Utilisateur {
    private String numeroEtudiant;
    private String niveau;
    private int classeId;

    // Constructeur vide
    public Etudiant() {
        super();
        this.setRole("ETUDIANT");
    }

    // Constructeur avec paramètres
    public Etudiant(int id, String nom, String prenom, String email,
            String motDePasse, String numeroEtudiant, String niveau, int classeId) {
        super(id, nom, prenom, email, motDePasse, "ETUDIANT");
        this.numeroEtudiant = numeroEtudiant;
        this.niveau = niveau;
        this.classeId = classeId;
    }

    // Getters et Setters
    public String getNumeroEtudiant() {
        return numeroEtudiant;
    }

    public void setNumeroEtudiant(String numeroEtudiant) {
        this.numeroEtudiant = numeroEtudiant;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getClasseId() {
        return classeId;
    }

    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }
}
