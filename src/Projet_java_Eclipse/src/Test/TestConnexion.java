package Test;

import dao.DatabaseConnection;
import java.sql.Connection;

public class TestConnexion {
    public static void main(String[] args) {
        System.out.println("🔌 Test de connexion à la base de données...");
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ CONNEXION RÉUSSIE !");
                System.out.println("Base de données : " + conn.getCatalog());
            } else {
                System.out.println("❌ ÉCHEC DE CONNEXION");
            }
        } catch (Exception e) {
            System.out.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
