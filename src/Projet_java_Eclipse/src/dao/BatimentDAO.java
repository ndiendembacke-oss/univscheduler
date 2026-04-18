package dao;

import model.Batiment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatimentDAO {

    private Connection connection;

    public BatimentDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    // CREATE
    public boolean ajouter(Batiment batiment) {
        String query = "INSERT INTO batiment (nom, localisation, nombre_etages) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, batiment.getNom());
            pstmt.setString(2, batiment.getLocalisation());
            pstmt.setInt(3, batiment.getNombreEtages());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    batiment.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ - par ID
    public Batiment getById(int id) {
        String query = "SELECT * FROM batiment WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBatiment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ - tous
    public List<Batiment> getAll() {
        List<Batiment> batiments = new ArrayList<>();
        String query = "SELECT * FROM batiment ORDER BY nom";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                batiments.add(mapResultSetToBatiment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batiments;
    }

    // UPDATE
    public boolean modifier(Batiment batiment) {
        String query = "UPDATE batiment SET nom = ?, localisation = ?, nombre_etages = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, batiment.getNom());
            pstmt.setString(2, batiment.getLocalisation());
            pstmt.setInt(3, batiment.getNombreEtages());
            pstmt.setInt(4, batiment.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE
    public boolean supprimer(int id) {
        String query = "DELETE FROM batiment WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Recherche par nom
    public List<Batiment> rechercherParNom(String nom) {
        List<Batiment> batiments = new ArrayList<>();
        String query = "SELECT * FROM batiment WHERE nom LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + nom + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                batiments.add(mapResultSetToBatiment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batiments;
    }

    // Compter le nombre de salles par bâtiment
    public int getNombreSalles(int batimentId) {
        String query = "SELECT COUNT(*) FROM salle WHERE batiment_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, batimentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Batiment mapResultSetToBatiment(ResultSet rs) throws SQLException {
        Batiment batiment = new Batiment();
        batiment.setId(rs.getInt("id"));
        batiment.setNom(rs.getString("nom"));
        batiment.setLocalisation(rs.getString("localisation"));
        batiment.setNombreEtages(rs.getInt("nombre_etages"));
        return batiment;
    }
}