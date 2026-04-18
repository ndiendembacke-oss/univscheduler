package ui.util;

import dao.DatabaseConnection;
import java.sql.*;

public class RepartirEtudiants {
    
    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== RÉPARTITION DES ÉTUDIANTS DANS LES CLASSES ===\n");
            
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Vérifier les classes
            System.out.println("📚 Vérification des classes...");
            int nbClasses = 0;
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM classe");
            if (rs.next()) nbClasses = rs.getInt(1);
            
            if (nbClasses == 0) {
                System.out.println("❌ Aucune classe trouvée! Création de 10 classes...");
                for (int i = 1; i <= 10; i++) {
                    conn.createStatement().executeUpdate("INSERT INTO classe (nom) VALUES ('Classe " + i + "')");
                }
                nbClasses = 10;
                System.out.println("✅ 10 classes créées");
            } else {
                System.out.println("✅ " + nbClasses + " classes trouvées");
            }
            
            // 2. Compter les étudiants
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM utilisateur WHERE role = 'etudiant'");
            int nbEtudiants = 0;
            if (rs.next()) nbEtudiants = rs.getInt(1);
            System.out.println("👥 " + nbEtudiants + " étudiants trouvés");
            
            if (nbEtudiants == 0) {
                System.out.println("❌ Aucun étudiant dans la table utilisateur!");
                return;
            }
            
            // 3. Vider la table etudiant (optionnel)
            System.out.println("🗑️ Nettoyage de la table etudiant...");
            conn.createStatement().executeUpdate("DELETE FROM etudiant");
            
            // 4. Répartir les étudiants
            System.out.println("📊 Répartition en cours...");
            String query = "INSERT INTO etudiant (id, classe_id) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            
            int etudiantsParClasse = (int) Math.ceil((double) nbEtudiants / nbClasses);
            int classeCourante = 1;
            int compteur = 0;
            
            rs = conn.createStatement().executeQuery(
                "SELECT id FROM utilisateur WHERE role = 'etudiant' ORDER BY id");
            
            while (rs.next()) {
                int etudiantId = rs.getInt("id");
                
                stmt.setInt(1, etudiantId);
                stmt.setInt(2, classeCourante);
                stmt.executeUpdate();
                
                compteur++;
                if (compteur >= etudiantsParClasse && classeCourante < nbClasses) {
                    classeCourante++;
                    compteur = 0;
                }
            }
            
            conn.commit();
            System.out.println("✅ Répartition terminée avec succès!");
            
            // 5. Afficher le résultat
            System.out.println("\n📋 RÉPARTITION FINALE:");
            rs = conn.createStatement().executeQuery(
                "SELECT c.nom as classe, COUNT(e.id) as nb FROM classe c " +
                "LEFT JOIN etudiant e ON c.id = e.classe_id " +
                "GROUP BY c.id, c.nom ORDER BY c.id");
            
            while (rs.next()) {
                System.out.println("   " + rs.getString("classe") + ": " + rs.getInt("nb") + " étudiants");
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
        }
    }
}