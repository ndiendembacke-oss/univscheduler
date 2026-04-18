package dao;

import model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    private Connection connection;
    
    public ReservationDAO() {
        this.connection = DatabaseConnection.getConnection();
        System.out.println("✅ ReservationDAO initialisé");
    }
    
    public int compterTotal() {
        String query = "SELECT COUNT(*) FROM reservation";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur compterTotal: " + e.getMessage());
        }
        return 0;
    }
    
    public int compterParStatut(String statut) {
        String query = "SELECT COUNT(*) FROM reservation WHERE statut = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, statut);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur compterParStatut: " + e.getMessage());
        }
        return 0;
    }
}