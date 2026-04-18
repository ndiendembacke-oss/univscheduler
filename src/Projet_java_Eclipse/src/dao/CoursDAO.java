package dao;

import model.Cours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {
    
    private Connection connection;
    
    public CoursDAO() {
        this.connection = DatabaseConnection.getConnection();
        System.out.println("✅ CoursDAO initialisé");
    }
    
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String query = "SELECT * FROM cours";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setMatiere(rs.getString("matiere"));
                cours.setEnseignantId(rs.getInt("enseignant_id"));
                cours.setSalleId(rs.getInt("salle_id"));
                cours.setType(rs.getString("type"));
                coursList.add(cours);
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur getAll cours: " + e.getMessage());
        }
        return coursList;
    }
}