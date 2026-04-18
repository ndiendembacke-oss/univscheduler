package model;

public class SalleEquipement {
    private int salleId;
    private int equipementId;
    private int quantite;

    // Constructeur vide
    public SalleEquipement() {
    }

    // Constructeur avec paramètres
    public SalleEquipement(int salleId, int equipementId, int quantite) {
        this.salleId = salleId;
        this.equipementId = equipementId;
        this.quantite = quantite;
    }

    // Getters et Setters
    public int getSalleId() {
        return salleId;
    }

    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public int getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(int equipementId) {
        this.equipementId = equipementId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "Salle " + salleId + " -> Equipement " + equipementId + " (x" + quantite + ")";
    }
}
