package dao;

import model.Salle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {
    
    private Connection connection;
    
    public SalleDAO() {
        this.connection = DatabaseConnection.getConnection();
        System.out.println("✅ SalleDAO initialisé");
    }
    
    public List<Salle> getAll() {
        List<Salle> salles = new ArrayList<>();
        String query = "SELECT * FROM salle";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                salles.add(mapResultSetToSalle(rs));
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur getAll salles: " + e.getMessage());
        }
        return salles;
    }
    
    public int getCapaciteTotale() {
        String query = "SELECT SUM(capacite) FROM salle";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur getCapaciteTotale: " + e.getMessage());
        }
        return 0;
    }
    
    private Salle mapResultSetToSalle(ResultSet rs) throws SQLException {
        Salle salle = new Salle();
        salle.setId(rs.getInt("id"));
        salle.setNumero(rs.getString("numero"));
        salle.setNom(rs.getString("nom"));
        salle.setCapacite(rs.getInt("capacite"));
        salle.setEtage(rs.getInt("etage"));
        salle.setType(rs.getString("type"));
        salle.setBatimentId(rs.getInt("batiment_id"));
        return salle;
    }
}