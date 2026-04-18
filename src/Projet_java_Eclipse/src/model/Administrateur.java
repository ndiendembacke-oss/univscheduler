
package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Administrateur extends Utilisateur {

    private String niveauAcces; // "SUPER_ADMIN", "ADMIN", "ADMIN_TECHNIQUE"
    private List<String> permissions;
    private LocalDateTime derniereConnexion;
    private String departement;

    // Constructeur vide
    public Administrateur() {
        super();
        this.setRole("ADMINISTRATEUR");
        this.niveauAcces = "ADMIN";
        this.permissions = new ArrayList<>();
        initialiserPermissionsParDefaut();
    }

    // Constructeur avec paramètres de base
    public Administrateur(int id, String nom, String prenom, String email, String motDePasse) {
        super(id, nom, prenom, email, motDePasse, "ADMINISTRATEUR");
        this.niveauAcces = "ADMIN";
        this.permissions = new ArrayList<>();
        initialiserPermissionsParDefaut();
    }

    // Constructeur complet
    public Administrateur(int id, String nom, String prenom, String email, String motDePasse,
            String niveauAcces, String departement) {
        super(id, nom, prenom, email, motDePasse, "ADMINISTRATEUR");
        this.niveauAcces = niveauAcces;
        this.departement = departement;
        this.permissions = new ArrayList<>();
        initialiserPermissionsParDefaut();
    }

    // Initialiser les permissions par défaut selon le niveau d'accès
    private void initialiserPermissionsParDefaut() {
        // Permissions de base pour tous les admins
        permissions.add("CONSULTER_STATISTIQUES");
        permissions.add("VOIR_UTILISATEURS");

        if ("SUPER_ADMIN".equals(niveauAcces)) {
            // Super admin a toutes les permissions
            permissions.add("GERER_UTILISATEURS");
            permissions.add("SUPPRIMER_UTILISATEURS");
            permissions.add("GERER_BATIMENTS");
            permissions.add("GERER_SALLES");
            permissions.add("GERER_EQUIPEMENTS");
            permissions.add("CONFIGURER_SYSTEME");
            permissions.add("VOIR_LOGS");
            permissions.add("SAUVEGARDER_BD");
            permissions.add("RESTAURER_BD");
        } else if ("ADMIN_TECHNIQUE".equals(niveauAcces)) {
            // Admin technique gère l'infrastructure
            permissions.add("GERER_BATIMENTS");
            permissions.add("GERER_SALLES");
            permissions.add("GERER_EQUIPEMENTS");
            permissions.add("VOIR_LOGS");
        } else {
            // Admin standard
            permissions.add("GERER_UTILISATEURS");
            permissions.add("GERER_BATIMENTS");
            permissions.add("GERER_SALLES");
            permissions.add("GERER_EQUIPEMENTS");
        }
    }

    // Getters et Setters spécifiques
    public String getNiveauAcces() {
        return niveauAcces;
    }

    public void setNiveauAcces(String niveauAcces) {
        this.niveauAcces = niveauAcces;
        this.permissions.clear();
        initialiserPermissionsParDefaut();
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    // Méthodes de vérification des permissions
    public boolean aPermission(String permission) {
        return permissions.contains(permission);
    }

    public void ajouterPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void retirerPermission(String permission) {
        permissions.remove(permission);
    }

    // ===== GESTION DES UTILISATEURS =====

    public boolean peutGererUtilisateurs() {
        return aPermission("GERER_UTILISATEURS");
    }

    public String creerUtilisateur(Utilisateur nouvelUtilisateur) {
        if (!peutGererUtilisateurs()) {
            return "ERREUR: Vous n'avez pas la permission de créer des utilisateurs";
        }
        // Ici on appellerait le DAO pour sauvegarder
        return "SUCCES: Utilisateur " + nouvelUtilisateur.getEmail() + " créé";
    }

    public String modifierUtilisateur(Utilisateur utilisateur) {
        if (!peutGererUtilisateurs()) {
            return "ERREUR: Vous n'avez pas la permission de modifier des utilisateurs";
        }
        return "SUCCES: Utilisateur " + utilisateur.getId() + " modifié";
    }

    public String supprimerUtilisateur(int utilisateurId) {
        if (!aPermission("SUPPRIMER_UTILISATEURS")) {
            return "ERREUR: Vous n'avez pas la permission de supprimer des utilisateurs";
        }
        return "SUCCES: Utilisateur " + utilisateurId + " supprimé";
    }

    public List<Utilisateur> filtrerUtilisateursParRole(List<Utilisateur> tousLesUtilisateurs, String role) {
        List<Utilisateur> filtres = new ArrayList<>();
        for (Utilisateur u : tousLesUtilisateurs) {
            if (u.getRole().equals(role)) {
                filtres.add(u);
            }
        }
        return filtres;
    }

    // ===== GESTION DES BÂTIMENTS =====

    public boolean peutGererBatiments() {
        return aPermission("GERER_BATIMENTS");
    }

    public String ajouterBatiment(Batiment batiment) {
        if (!peutGererBatiments()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Bâtiment " + batiment.getNom() + " ajouté";
    }

    public String modifierBatiment(Batiment batiment) {
        if (!peutGererBatiments()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Bâtiment " + batiment.getId() + " modifié";
    }

    public String supprimerBatiment(int batimentId) {
        if (!peutGererBatiments()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Bâtiment " + batimentId + " supprimé";
    }

    // ===== GESTION DES SALLES =====

    public boolean peutGererSalles() {
        return aPermission("GERER_SALLES");
    }

    public String ajouterSalle(Salle salle) {
        if (!peutGererSalles()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Salle " + salle.getNumero() + " ajoutée";
    }

    public String modifierSalle(Salle salle) {
        if (!peutGererSalles()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Salle " + salle.getId() + " modifiée";
    }

    public String supprimerSalle(int salleId) {
        if (!peutGererSalles()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Salle " + salleId + " supprimée";
    }

    // ===== GESTION DES ÉQUIPEMENTS =====

    public boolean peutGererEquipements() {
        return aPermission("GERER_EQUIPEMENTS");
    }

    public String ajouterEquipement(Equipement equipement) {
        if (!peutGererEquipements()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Équipement " + equipement.getNom() + " ajouté";
    }

    public String modifierEquipement(Equipement equipement) {
        if (!peutGererEquipements()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Équipement " + equipement.getId() + " modifié";
    }

    public String supprimerEquipement(int equipementId) {
        if (!peutGererEquipements()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Équipement " + equipementId + " supprimé";
    }

    // ===== STATISTIQUES ET RAPPORTS =====

    public Map<String, Object> getStatistiquesGlobales(List<Utilisateur> utilisateurs,
            List<Salle> salles,
            List<Cours> cours,
            List<Reservation> reservations) {
        Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("total_utilisateurs", utilisateurs.size());
        stats.put("total_enseignants", compterParRole(utilisateurs, "ENSEIGNANT"));
        stats.put("total_etudiants", compterParRole(utilisateurs, "ETUDIANT"));
        stats.put("total_gestionnaires", compterParRole(utilisateurs, "GESTIONNAIRE"));
        stats.put("total_salles", salles.size());
        stats.put("total_cours", cours.size());
        stats.put("total_reservations", reservations.size());

        // Cours par type
        long cm = cours.stream().filter(c -> "CM".equals(c.getType())).count();
        long td = cours.stream().filter(c -> "TD".equals(c.getType())).count();
        long tp = cours.stream().filter(c -> "TP".equals(c.getType())).count();

        stats.put("cours_CM", cm);
        stats.put("cours_TD", td);
        stats.put("cours_TP", tp);

        return stats;
    }

    private long compterParRole(List<Utilisateur> utilisateurs, String role) {
        return utilisateurs.stream().filter(u -> role.equals(u.getRole())).count();
    }

    public String genererRapportComplet() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("========================================\n");
        rapport.append("RAPPORT D'ADMINISTRATION - UNIV-SCHEDULER\n");
        rapport.append("========================================\n");
        rapport.append("Généré par: ").append(this.getPrenom()).append(" ").append(this.getNom()).append("\n");
        rapport.append("Niveau d'accès: ").append(niveauAcces).append("\n");
        rapport.append("Date: ").append(LocalDateTime.now()).append("\n\n");

        rapport.append("Résumé des permissions:\n");
        for (String perm : permissions) {
            rapport.append("- ").append(perm).append("\n");
        }

        return rapport.toString();
    }

    // ===== CONFIGURATION SYSTÈME =====

    public boolean peutConfigurerSysteme() {
        return aPermission("CONFIGURER_SYSTEME");
    }

    public String configurerParametre(String cle, String valeur) {
        if (!peutConfigurerSysteme()) {
            return "ERREUR: Permission refusée";
        }
        // Ici on sauvegarderait dans un fichier de config
        return "SUCCES: Paramètre " + cle + " = " + valeur + " enregistré";
    }

    // ===== SAUVEGARDE BASE DE DONNÉES =====

    public boolean peutSauvegarderBD() {
        return aPermission("SAUVEGARDER_BD");
    }

    public String sauvegarderBaseDonnees(String cheminFichier) {
        if (!peutSauvegarderBD()) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Base de données sauvegardée dans " + cheminFichier;
    }

    public String restaurerBaseDonnees(String cheminFichier) {
        if (!aPermission("RESTAURER_BD")) {
            return "ERREUR: Permission refusée";
        }
        return "SUCCES: Base de données restaurée depuis " + cheminFichier;
    }

    // ===== LOGS SYSTÈME =====

    public boolean peutVoirLogs() {
        return aPermission("VOIR_LOGS");
    }

    public List<String> consulterLogs(int nombreLignes) {
        if (!peutVoirLogs()) {
            return List.of("ERREUR: Permission refusée");
        }
        // Simuler des logs
        List<String> logs = new ArrayList<>();
        logs.add("[INFO] " + LocalDateTime.now() + " - Connexion admin " + this.getEmail());
        logs.add("[INFO] " + LocalDateTime.now().minusMinutes(5) + " - Sauvegarde effectuée");
        logs.add("[WARN] " + LocalDateTime.now().minusHours(1) + " - Tentative de connexion échouée");
        return logs;
    }

    // ===== GESTION DES CONFLITS =====

    public String resoudreConflitGlobal(Cours cours1, Cours cours2, Salle nouvelleSalle) {
        if (nouvelleSalle == null) {
            return "ERREUR: Salle non spécifiée";
        }

        cours2.setSalleId(nouvelleSalle.getId());
        return "SUCCES: Conflit résolu - Cours déplacé en salle " + nouvelleSalle.getNumero();
    }

    @Override
    public String getTableauDeBord() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════╗\n");
        sb.append("║   TABLEAU DE BORD ADMINISTRATEUR   ║\n");
        sb.append("╚════════════════════════════════════╝\n\n");

        sb.append("Informations personnelles:\n");
        sb.append("├─ Nom: ").append(this.getPrenom()).append(" ").append(this.getNom()).append("\n");
        sb.append("├─ Email: ").append(this.getEmail()).append("\n");
        sb.append("├─ Niveau d'accès: ").append(niveauAcces).append("\n");
        sb.append("├─ Département: ").append(departement).append("\n");
        sb.append("└─ Dernière connexion: ")
                .append(derniereConnexion != null ? derniereConnexion : "Première connexion").append("\n\n");

        sb.append("Permissions (").append(permissions.size()).append("):\n");
        for (int i = 0; i < permissions.size(); i++) {
            String prefix = (i == permissions.size() - 1) ? "└─ " : "├─ ";
            sb.append(prefix).append(permissions.get(i)).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString() + " - " + niveauAcces + " (" + departement + ")";
    }
}