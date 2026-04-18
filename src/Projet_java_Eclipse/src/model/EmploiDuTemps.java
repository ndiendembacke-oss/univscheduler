package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmploiDuTemps {
    private int id;
    private String nom;
    private String periode; // "Semaine 1", "Mars 2024", etc.
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int classeId; // Pour quel classe
    private List<Cours> coursList;
    private Map<DayOfWeek, List<Cours>> coursParJour;

    // Constructeur vide
    public EmploiDuTemps() {
        this.coursList = new ArrayList<>();
        this.coursParJour = new HashMap<>();
        initialiserMap();
    }

    // Constructeur avec paramètres
    public EmploiDuTemps(int id, String nom, String periode, LocalDate dateDebut, LocalDate dateFin, int classeId) {
        this.id = id;
        this.nom = nom;
        this.periode = periode;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.classeId = classeId;
        this.coursList = new ArrayList<>();
        this.coursParJour = new HashMap<>();
        initialiserMap();
    }

    private void initialiserMap() {
        coursParJour.put(DayOfWeek.MONDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.TUESDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.WEDNESDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.THURSDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.FRIDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.SATURDAY, new ArrayList<>());
        coursParJour.put(DayOfWeek.SUNDAY, new ArrayList<>());
    }

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

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public int getClasseId() {
        return classeId;
    }

    public void setClasseId(int classeId) {
        this.classeId = classeId;
    }

    public List<Cours> getCoursList() {
        return coursList;
    }

    public void setCoursList(List<Cours> coursList) {
        this.coursList = coursList;
        organiserCoursParJour();
    }

    public void ajouterCours(Cours cours, Creneau creneau) {
        cours.setCreneauId(creneau.getId());
        this.coursList.add(cours);

        DayOfWeek jour = creneau.getJour();
        if (coursParJour.containsKey(jour)) {
            coursParJour.get(jour).add(cours);
        }
    }

    public void supprimerCours(int coursId) {
        coursList.removeIf(c -> c.getId() == coursId);

        for (DayOfWeek jour : coursParJour.keySet()) {
            coursParJour.get(jour).removeIf(c -> c.getId() == coursId);
        }
    }

    private void organiserCoursParJour() {
        initialiserMap();

        for (Cours cours : coursList) {
            // Implémentation simplifiée
        }
    }

    public List<Cours> getCoursDuJour(DayOfWeek jour) {
        return coursParJour.getOrDefault(jour, new ArrayList<>());
    }

    public List<Cours> getCoursDuJour(LocalDate date) {
        return getCoursDuJour(date.getDayOfWeek());
    }

    public List<Cours> getCoursPourSalle(int salleId) {
        List<Cours> resultat = new ArrayList<>();
        for (Cours cours : coursList) {
            if (cours.getSalleId() == salleId) {
                resultat.add(cours);
            }
        }
        return resultat;
    }

    public List<Cours> getCoursPourEnseignant(int enseignantId) {
        List<Cours> resultat = new ArrayList<>();
        for (Cours cours : coursList) {
            if (cours.getEnseignantId() == enseignantId) {
                resultat.add(cours);
            }
        }
        return resultat;
    }

    public List<String> detecterConflits(List<Creneau> tousCreneaux, List<Salle> toutesSalles) {
        List<String> conflits = new ArrayList<>();

        for (int i = 0; i < coursList.size(); i++) {
            for (int j = i + 1; j < coursList.size(); j++) {
                Cours c1 = coursList.get(i);
                Cours c2 = coursList.get(j);

                if (c1.getSalleId() == c2.getSalleId() && c1.getCreneauId() == c2.getCreneauId()) {
                    conflits.add("Conflit salle");
                }

                if (c1.getEnseignantId() == c2.getEnseignantId() && c1.getCreneauId() == c2.getCreneauId()) {
                    conflits.add("Conflit enseignant");
                }

                if (c1.getClasseId() == c2.getClasseId() && c1.getCreneauId() == c2.getCreneauId()) {
                    conflits.add("Conflit classe");
                }
            }
        }

        return conflits;
    }

    public int getNombreCours() {
        return coursList.size();
    }

    public Map<String, Integer> getStatistiquesParMatiere() {
        Map<String, Integer> stats = new HashMap<>();
        for (Cours cours : coursList) {
            stats.put(cours.getMatiere(), stats.getOrDefault(cours.getMatiere(), 0) + 1);
        }
        return stats;
    }

    public double getTauxRemplissage() {
        int nombreJours = 5;
        int creneauxParJour = 8;
        int capaciteMax = nombreJours * creneauxParJour;

        return capaciteMax == 0 ? 0 : (coursList.size() * 100.0) / capaciteMax;
    }

    @Override
    public String toString() {
        return nom + " (" + periode + ") - " + coursList.size() + " cours";
    }
}
