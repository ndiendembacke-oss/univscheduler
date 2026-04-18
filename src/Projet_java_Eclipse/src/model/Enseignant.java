package model;

public class Enseignant extends Utilisateur {
    private String specialite;
    private String numeroBureau;
    private String departement;

    public Enseignant() {
        super();
        this.setRole("ENSEIGNANT");
    }

    public Enseignant(int id, String nom, String prenom, String email,
            String motDePasse, String specialite, String numeroBureau) {
        super(id, nom, prenom, email, motDePasse, "ENSEIGNANT");
        this.specialite = specialite;
        this.numeroBureau = numeroBureau;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getNumeroBureau() {
        return numeroBureau;
    }

    public void setNumeroBureau(String numeroBureau) {
        this.numeroBureau = numeroBureau;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    @Override
    public String toString() {
        return super.toString() + " - " + specialite;
    }
}
