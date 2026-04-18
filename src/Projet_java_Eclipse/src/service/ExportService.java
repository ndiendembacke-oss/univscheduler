package service;

import model.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private PlanningService planningService;

    public ExportService() {
        this.planningService = new PlanningService();
    }

    /**
     * Exporter l'emploi du temps en CSV
     */
    public boolean exporterEDTEnCSV(List<Cours> coursList, String cheminFichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            // En-tête CSV
            writer.println("Matière;Enseignant;Classe;Salle;Date;Heure;Durée;Type");

            // Données
            for (Cours cours : coursList) {
                writer.println(String.format("%s;%d;%d;%d;%s;%d;%d;%s",
                        cours.getMatiere(),
                        cours.getEnseignantId(),
                        cours.getClasseId(),
                        cours.getSalleId(),
                        cours.getDate(),
                        cours.getCreneauId(),
                        cours.getDureeEnMinutes(),
                        cours.getType()));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter l'emploi du temps en TXT (format lisible)
     */
    public boolean exporterEDTEnTXT(EmploiDuTemps edt, String cheminFichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            writer.println("=".repeat(80));
            writer.println("EMPLOI DU TEMPS - " + edt.getNom());
            writer.println("=".repeat(80));
            writer.println("Période: " + edt.getPeriode());
            writer.println("Classe: " + edt.getClasseId());
            writer.println("=".repeat(80));
            writer.println();

            // Organiser par jour
            String[] jours = { "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI" };

            for (String jour : jours) {
                writer.println(jour);
                writer.println("-".repeat(40));

                boolean aDesCours = false;
                for (Cours cours : edt.getCoursList()) {
                    // À améliorer avec les vrais jours
                    writer.println(String.format("  %s - %s", cours.getMatiere(), cours.getType()));
                    aDesCours = true;
                }

                if (!aDesCours) {
                    writer.println("  Pas de cours");
                }
                writer.println();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter les statistiques en CSV
     */
    public boolean exporterStatistiquesEnCSV(StatistiquesService statsService, String cheminFichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            var stats = statsService.getStatistiquesGlobales();

            writer.println("Indicateur;Valeur");
            for (var entry : stats.entrySet()) {
                writer.println(entry.getKey() + ";" + entry.getValue());
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter la liste des salles en CSV
     */
    public boolean exporterSallesEnCSV(List<Salle> salles, String cheminFichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            writer.println("ID;Numéro;Nom;Capacité;Étage;Type;Bâtiment;Équipements");

            for (Salle salle : salles) {
                StringBuilder equipements = new StringBuilder();
                for (Equipement eq : salle.getEquipements()) {
                    if (equipements.length() > 0)
                        equipements.append(", ");
                    equipements.append(eq.getNom());
                }

                writer.println(String.format("%d;%s;%s;%d;%d;%s;%d;%s",
                        salle.getId(),
                        salle.getNumero(),
                        salle.getNom(),
                        salle.getCapacite(),
                        salle.getEtage(),
                        salle.getType(),
                        salle.getBatimentId(),
                        equipements.toString()));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter en HTML (pour affichage web)
     */
    public boolean exporterEDTEnHTML(List<Cours> coursList, String cheminFichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(cheminFichier))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Emploi du temps</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial; margin: 20px; }");
            writer.println("table { border-collapse: collapse; width: 100%; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #4CAF50; color: white; }");
            writer.println("tr:nth-child(even) { background-color: #f2f2f2; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<h1>Emploi du temps</h1>");
            writer.println("<table>");
            writer.println("<tr><th>Matière</th><th>Date</th><th>Salle</th><th>Type</th></tr>");

            for (Cours cours : coursList) {
                writer.println(String.format("<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td></tr>",
                        cours.getMatiere(),
                        cours.getDate(),
                        cours.getSalleId(),
                        cours.getType()));
            }

            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exporter en PDF (simulation - nécessite une bibliothèque PDF)
     */
    public boolean exporterEnPDF(String contenu, String cheminFichier) {
        // À implémenter avec iText ou autre bibliothèque PDF
        System.out.println("Export PDF simulé vers: " + cheminFichier);
        System.out.println("Contenu: " + contenu);
        return true;
    }
}