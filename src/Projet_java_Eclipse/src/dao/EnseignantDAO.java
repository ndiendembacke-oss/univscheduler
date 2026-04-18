package dao;

import model.Enseignant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnseignantDAO {

    private Connection connection;

    public EnseignantDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean ajouter(Enseignant enseignant) {
        String query = "INSERT INTO enseignant (utilisateur_id, matiere_principale, bureau) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, enseignant.getUtilisateurId());
            pstmt.setString(2, enseignant.getMatierePrincipale());
            pstmt.setString(3, enseignant.getBureau());
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    enseignant.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Enseignant getById(int id) {
        String query = "SELECT * FROM enseignant e JOIN utilisateur u ON e.utilisateur_id = u.id WHERE e.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEnseignant(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Enseignant> getAll() {
        List<Enseignant> enseignants = new ArrayList<>();
        String query = "SELECT * FROM enseignant e JOIN utilisateur u ON e.utilisateur_id = u.id ORDER BY u.nom";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                enseignants.add(mapResultSetToEnseignant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enseignants;
    }

    public boolean modifier(Enseignant enseignant) {
        String query = "UPDATE enseignant SET utilisateur_id = ?, matiere_principale = ?, bureau = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, enseignant.getUtilisateurId());
            pstmt.setString(2, enseignant.getMatierePrincipale());
            pstmt.setString(3, enseignant.getBureau());
            pstmt.setInt(4, enseignant.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimer(int id) {
        String query = "DELETE FROM enseignant WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Enseignant mapResultSetToEnseignant(ResultSet rs) throws SQLException {
        Enseignant enseignant = new Enseignant();
        enseignant.setId(rs.getInt("e.id"));
        enseignant.setUtilisateurId(rs.getInt("utilisateur_id"));
        enseignant.setMatierePrincipale(rs.getString("matiere_principale"));
        enseignant.setBureau(rs.getString("bureau"));
        // Utilisateur details from join if needed
        return enseignant;
    }
}
