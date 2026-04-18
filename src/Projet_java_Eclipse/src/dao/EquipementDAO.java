package dao;

import model.Equipement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAO {

    private Connection connection;

    public EquipementDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    // ==================== CRUD ====================

    // CREATE - Ajouter un équipement
    public boolean ajouter(Equipement equipement) {
        String query = "INSERT INTO equipement (nom, description, fonctionnel) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, equipement.getNom());
            pstmt.setString(2, equipement.getDescription());
            pstmt.setBoolean(3, equipement.isFonctionnel());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    equipement.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ - Récupérer un équipement par ID
    public Equipement getById(int id) {
        String query = "SELECT * FROM equipement WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEquipement(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ - Récupérer tous les équipements
    public List<Equipement> getAll() {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT * FROM equipement ORDER BY nom";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                equipements.add(mapResultSetToEquipement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    // READ - Récupérer les équipements d'une salle
    public List<Equipement> getEquipementsBySalle(int salleId) {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT e.* FROM equipement e " +
                "JOIN salle_equipement se ON e.id = se.equipement_id " +
                "WHERE se.salle_id = ? ORDER BY e.nom";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, salleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                equipements.add(mapResultSetToEquipement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    // READ - Récupérer les équipements fonctionnels
    public List<Equipement> getFonctionnels() {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT * FROM equipement WHERE fonctionnel = TRUE ORDER BY nom";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                equipements.add(mapResultSetToEquipement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    // READ - Récupérer les équipements non fonctionnels
    public List<Equipement> getNonFonctionnels() {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT * FROM equipement WHERE fonctionnel = FALSE ORDER BY nom";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                equipements.add(mapResultSetToEquipement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    // READ - Rechercher par nom
    public List<Equipement> rechercherParNom(String recherche) {
        List<Equipement> equipements = new ArrayList<>();
        String query = "SELECT * FROM equipement WHERE nom LIKE ? ORDER BY nom";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + recherche + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                equipements.add(mapResultSetToEquipement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    // UPDATE - Modifier un équipement
    public boolean modifier(Equipement equipement) {
        String query = "UPDATE equipement SET nom = ?, description = ?, fonctionnel = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, equipement.getNom());
            pstmt.setString(2, equipement.getDescription());
            pstmt.setBoolean(3, equipement.isFonctionnel());
            pstmt.setInt(4, equipement.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Supprimer un équipement
    public boolean supprimer(int id) {
        String query = "DELETE FROM equipement WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== MÉTHODES SPÉCIFIQUES ====================

    // Marquer comme fonctionnel/non fonctionnel
    public boolean setFonctionnel(int id, boolean fonctionnel) {
        String query = "UPDATE equipement SET fonctionnel = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, fonctionnel);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Compter le nombre total d'équipements
    public int compterTotal() {
        String query = "SELECT COUNT(*) FROM equipement";
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

    // Compter par type (fonctionnel/non fonctionnel)
    public int compterParStatut(boolean fonctionnel) {
        String query = "SELECT COUNT(*) FROM equipement WHERE fonctionnel = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, fonctionnel);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Vérifier si un équipement est utilisé dans des salles
    public boolean estUtilise(int equipementId) {
        String query = "SELECT COUNT(*) FROM salle_equipement WHERE equipement_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, equipementId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtenir les statistiques
    public String getStatistiques() {
        int total = compterTotal();
        int fonctionnels = compterParStatut(true);
        int nonFonctionnels = compterParStatut(false);

        return String.format("Total: %d, Fonctionnels: %d, Non fonctionnels: %d",
                total, fonctionnels, nonFonctionnels);
    }

    private Equipement mapResultSetToEquipement(ResultSet rs) throws SQLException {
        Equipement equipement = new Equipement();
        equipement.setId(rs.getInt("id"));
        equipement.setNom(rs.getString("nom"));
        equipement.setDescription(rs.getString("description"));
        equipement.setFonctionnel(rs.getBoolean("fonctionnel"));
        return equipement;
    }
}