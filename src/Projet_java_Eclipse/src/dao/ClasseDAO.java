package dao;

import model.Classe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseDAO {
    
    private Connection connection;
    
    public ClasseDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    // ==================== CRUD ====================
    
    // CREATE - Ajouter une classe
    public boolean ajouter(Classe classe) {
        String query = "INSERT INTO classe (nom, niveau, filiere, nombre_etudiants, annee_scolaire, departement, description) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, classe.getNom());
            pstmt.setString(2, classe.getNiveau());
            pstmt.setString(3, classe.getFiliere());
            pstmt.setInt(4, classe.getNombreEtudiants());
            pstmt.setString(5, classe.getAnneeScolaire());
            pstmt.setString(6, classe.getDepartement());
            pstmt.setString(7, classe.getDescription());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    classe.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // READ - Récupérer une classe par ID
    public Classe getById(int id) {
        String query = "SELECT * FROM classe WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClasse(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // READ - Récupérer toutes les classes
    public List<Classe> getAll() {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe ORDER BY niveau, nom";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                classes.add(mapResultSetToClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    // READ - Récupérer les classes par niveau
    public List<Classe> getByNiveau(String niveau) {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe WHERE niveau = ? ORDER BY nom";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, niveau);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    // READ - Récupérer les classes par département
    public List<Classe> getByDepartement(String departement) {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe WHERE departement = ? ORDER BY niveau, nom";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, departement);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    // READ - Récupérer les classes par année scolaire
    public List<Classe> getByAnneeScolaire(String anneeScolaire) {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe WHERE annee_scolaire = ? ORDER BY niveau, nom";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, anneeScolaire);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    // UPDATE - Modifier une classe
    public boolean modifier(Classe classe) {
        String query = "UPDATE classe SET nom = ?, niveau = ?, filiere = ?, " +
                       "nombre_etudiants = ?, annee_scolaire = ?, departement = ?, " +
                       "description = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, classe.getNom());
            pstmt.setString(2, classe.getNiveau());
            pstmt.setString(3, classe.getFiliere());
            pstmt.setInt(4, classe.getNombreEtudiants());
            pstmt.setString(5, classe.getAnneeScolaire());
            pstmt.setString(6, classe.getDepartement());
            pstmt.setString(7, classe.getDescription());
            pstmt.setInt(8, classe.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // DELETE - Supprimer une classe
    public boolean supprimer(int id) {
        String query = "DELETE FROM classe WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ==================== MÉTHODES SPÉCIFIQUES ====================
    
    // Rechercher des classes par nom
    public List<Classe> rechercherParNom(String recherche) {
        List<Classe> classes = new ArrayList<>();
        String query = "SELECT * FROM classe WHERE nom LIKE ? OR niveau LIKE ? ORDER BY niveau, nom";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String pattern = "%" + recherche + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                classes.add(mapResultSetToClasse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    // Compter le nombre total d'étudiants
    public int compterTotalEtudiants() {
        String query = "SELECT SUM(nombre_etudiants) FROM classe";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Compter le nombre de classes
    public int compterTotal() {
        String query = "SELECT COUNT(*) FROM classe";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Compter par niveau
    public int compterParNiveau(String niveau) {
        String query = "SELECT COUNT(*) FROM classe WHERE niveau = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, niveau);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Obtenir les statistiques par niveau
    public List<NiveauStat> getStatistiquesParNiveau() {
        List<NiveauStat> stats = new ArrayList<>();
        String query = "SELECT niveau, COUNT(*) as nb_classes, SUM(nombre_etudiants) as total_etudiants " +
                       "FROM classe GROUP BY niveau ORDER BY niveau";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                stats.add(new NiveauStat(
                    rs.getString("niveau"),
                    rs.getInt("nb_classes"),
                    rs.getInt("total_etudiants")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    // Vérifier si une classe a des cours
    public boolean aDesCours(int classeId) {
        String query = "SELECT COUNT(*) FROM cours WHERE classe_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, classeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Classe interne pour les statistiques
    public static class NiveauStat {
        private String niveau;
        private int nombreClasses;
        private int totalEtudiants;
        
        public NiveauStat(String niveau, int nombreClasses, int totalEtudiants) {
            this.niveau = niveau;
            this.nombreClasses = nombreClasses;
            this.totalEtudiants = totalEtudiants;
        }
        
        public String getNiveau() { return niveau; }
        public int getNombreClasses() { return nombreClasses; }
        public int getTotalEtudiants() { return totalEtudiants; }
    }
    
    private Classe mapResultSetToClasse(ResultSet rs) throws SQLException {
        Classe classe = new Classe();
        classe.setId(rs.getInt("id"));
        classe.setNom(rs.getString("nom"));
        classe.setNiveau(rs.getString("niveau"));
        classe.setFiliere(rs.getString("filiere"));
        classe.setNombreEtudiants(rs.getInt("nombre_etudiants"));
        classe.setAnneeScolaire(rs.getString("annee_scolaire"));
        classe.setDepartement(rs.getString("departement"));
        classe.setDescription(rs.getString("description"));
        return classe;
    }
}