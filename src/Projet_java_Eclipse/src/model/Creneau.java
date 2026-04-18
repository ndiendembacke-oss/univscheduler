package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Creneau {
    private int id;
    private DayOfWeek jour; // LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE
    private LocalTime heureDebut;
    private int duree; // en minutes

    // Constructeur vide
    public Creneau() {
    }

    // Constructeur avec paramètres
    public Creneau(int id, DayOfWeek jour, LocalTime heureDebut, int duree) {
        this.id = id;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.duree = duree;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DayOfWeek getJour() {
        return jour;
    }

    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }

    // Setter avec String (pratique pour la conversion)
    public void setJour(String jour) {
        this.jour = DayOfWeek.valueOf(jour.toUpperCase());
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    // Setter avec String (format "HH:mm")
    public void setHeureDebut(String heureDebut) {
        this.heureDebut = LocalTime.parse(heureDebut, DateTimeFormatter.ofPattern("HH:mm"));
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    // Méthodes utilitaires
    public LocalTime getHeureFin() {
        return heureDebut.plusMinutes(duree);
    }

    public String getHeureFinFormatee() {
        return getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getHeureDebutFormatee() {
        return heureDebut.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getJourEnFrancais() {
        switch (jour) {
            case MONDAY:
                return "Lundi";
            case TUESDAY:
                return "Mardi";
            case WEDNESDAY:
                return "Mercredi";
            case THURSDAY:
                return "Jeudi";
            case FRIDAY:
                return "Vendredi";
            case SATURDAY:
                return "Samedi";
            case SUNDAY:
                return "Dimanche";
            default:
                return "";
        }
    }

    // Vérifier si deux créneaux se chevauchent
    public boolean chevauche(Creneau autre) {
        if (this.jour != autre.jour) {
            return false;
        }

        LocalTime fin1 = this.getHeureFin();
        LocalTime fin2 = autre.getHeureFin();

        return (this.heureDebut.isBefore(fin2) && fin1.isAfter(autre.heureDebut));
    }

    @Override
    public String toString() {
        return getJourEnFrancais() + " " + getHeureDebutFormatee() + "-" + getHeureFinFormatee() +
                " (" + duree + " min)";
    }
}