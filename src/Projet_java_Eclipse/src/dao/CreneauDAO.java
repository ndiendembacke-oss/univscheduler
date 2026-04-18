package dao;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Creneau;

public class CreneauDAO {
    
    private Connection connection;
    
    public CreneauDAO() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    // ==================== CRUD ====================
    
    // CREATE - Ajouter un créneau
    public boolean ajouter(Creneau creneau) {
        String query = "INSERT INTO creneau (jour, heure_debut, duree) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, creneau.getJour().toString());
            pstmt.setTime(2, Time.valueOf(creneau.getHeureDebut()));
            pstmt.setInt(3, creneau.getDuree());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    creneau.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // READ - Récupérer un créneau par ID
    public Creneau getById(int id) {
        String query = "SELECT * FROM creneau WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCreneau(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // READ - Récupérer tous les créneaux
    public List<Creneau> getAll() {
        List<Creneau> creneaux = new ArrayList<>();
        String query = "SELECT * FROM creneau ORDER BY FIELD(jour, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), heure_debut";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                creneaux.add(mapResultSetToCreneau(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return creneaux;
    }
    
    // READ - Récupérer les créneaux par jour
    public List<Creneau> getByJour(DayOfWeek jour) {
        List<Creneau> creneaux = new ArrayList<>();
        String query = "SELECT * FROM creneau WHERE jour = ? ORDER BY heure_debut";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, jour.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                creneaux.add(mapResultSetToCreneau(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return creneaux;
    }
    
    // READ - Créneaux disponibles pour une salle à une date donnée
    public List<Creneau> getDisponiblesPourSalle(int salleId, LocalDate date) {
        List<Creneau> creneaux = new ArrayList<>();
        String query = "SELECT c.* FROM creneau c " +
                       "WHERE c.id NOT IN (" +
                       "   SELECT co.creneau_id FROM cours co " +
                       "   WHERE co.salle_id = ? AND co.date = ?" +
                       ") AND c.id NOT IN (" +
                       "   SELECT r.creneau_id FROM reservation r " +
                       "   WHERE r.salle_id = ? AND r.date = ?" +
                       ") ORDER BY c.jour, c.heure_debut";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salleId);
            pstmt.setString(2, date.toString());
            pstmt.setInt(3, salleId);
            pstmt.setString(4, date.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                creneaux.add(mapResultSetToCreneau(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return creneaux;
    }
    
    // UPDATE - Modifier un créneau
    public boolean modifier(Creneau creneau) {
        String query = "UPDATE creneau SET jour = ?, heure_debut = ?, duree = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, creneau.getJour().toString());
            pstmt.setTime(2, Time.valueOf(creneau.getHeureDebut()));
            pstmt.setInt(3, creneau.getDuree());
            pstmt.setInt(4, creneau.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // DELETE - Supprimer un créneau
    public boolean supprimer(int id) {
        String query = "DELETE FROM creneau WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ==================== MÉTHODES SPÉCIFIQUES ====================
    
    // Vérifier si un créneau est utilisé
    public boolean estUtilise(int id) {
        String query = "SELECT COUNT(*) FROM cours WHERE creneau_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Convertir ResultSet en objet Creneau
    private Creneau mapResultSetToCreneau(ResultSet rs) throws SQLException {
        Creneau creneau = new Creneau();
        creneau.setId(rs.getInt("id"));
        creneau.setJour(rs.getString("jour"));
        creneau.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        creneau.setDuree(rs.getInt("duree"));
        return creneau;
    }
}