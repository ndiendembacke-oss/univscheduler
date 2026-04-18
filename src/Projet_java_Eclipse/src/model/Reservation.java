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
    }
    
    public int compterTotal() {
        String query = "SELECT COUNT(*) FROM reservation";
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
    
    public int compterParStatut(String statut) {
        // Version simplifiée sans utiliser le statut
        return 0;
    }
}