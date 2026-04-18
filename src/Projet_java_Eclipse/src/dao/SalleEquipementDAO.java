package dao;

import model.SalleEquipement;
import model.Equipement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleEquipementDAO {

    private Connection connection;

    public SalleEquipementDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean ajouter(SalleEquipement salleEquipement) {
        String query = "INSERT INTO salle_equipement (salle_id, equipement_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salleEquipement.getSalleId());
            pstmt.setInt(2, salleEquipement.getEquipementId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimer(int salleId, int equipementId) {
        String query = "DELETE FROM salle_equipement WHERE salle_id = ? AND equipement_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salleId);
            pstmt.setInt(2, equipementId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Equipement> getEquipementsBySalle(int salleId) {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT e.* FROM equipement e JOIN salle_equipement se ON e.id = se.equipement_id WHERE se.salle_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Equipement equipement = new Equipement();
                equipement.setId(rs.getInt("id"));
                equipement.setNom(rs.getString("nom"));
                equipement.setDescription(rs.getString("description"));
                equipements.add(equipement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    public List<SalleEquipement> getAll() {
        List<SalleEquipement> list = new ArrayList<>();
        String query = "SELECT * FROM salle_equipement";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                SalleEquipement se = new SalleEquipement();
                se.setSalleId(rs.getInt("salle_id"));
                se.setEquipementId(rs.getInt("equipement_id"));
                list.add(se);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
