package dao;

import model.EmploiDuTemps;
import model.Cours;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmploiDuTempsDAO {

    private Connection connection;

    public EmploiDuTempsDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    // ==================== CRUD ====================

    public boolean ajouter(EmploiDuTemps edt) {
        String query = "INSERT INTO emploi_du_temps (nom, periode, date_debut, date_fin, classe_id, est_actif, description) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, edt.getNom());
            pstmt.setString(2, edt.getPeriode());
            pstmt.setString(3, edt.getDateDebut().toString());
            pstmt.setString(4, edt.getDateFin().toString());
            pstmt.setInt(5, edt.getClasseId());
            pstmt.setBoolean(6, true); // est_actif par défaut
            pstmt.setString(7, edt.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    edt.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public EmploiDuTemps getById(int id) {
        String query = "SELECT * FROM emploi_du_temps WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                EmploiDuTemps edt = mapResultSetToEmploiDuTemps(rs);
                // Charger les cours associés
                edt.setCoursList(getCoursForEmploiDuTemps(id));
                return edt;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EmploiDuTemps> getAll() {
        List<EmploiDuTemps> edtList = new ArrayList<>();
        String query = "SELECT * FROM emploi_du_temps ORDER BY date_debut DESC";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                EmploiDuTemps edt = mapResultSetToEmploiDuTemps(rs);
                edtList.add(edt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edtList;
    }

    public List<EmploiDuTemps> getByClasse(int classeId) {
        List<EmploiDuTemps> edtList = new ArrayList<>();
        String query = "SELECT * FROM emploi_du_temps WHERE classe_id = ? ORDER BY date_debut DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, classeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                EmploiDuTemps edt = mapResultSetToEmploiDuTemps(rs);
                edtList.add(edt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edtList;
    }

    public List<EmploiDuTemps> getActifs() {
        List<EmploiDuTemps> edtList = new ArrayList<>();
        String query = "SELECT * FROM emploi_du_temps WHERE est_actif = TRUE ORDER BY date_debut DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, true);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                EmploiDuTemps edt = mapResultSetToEmploiDuTemps(rs);
                edtList.add(edt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edtList;
    }

    public boolean modifier(EmploiDuTemps edt) {
        String query = "UPDATE emploi_du_temps SET nom = ?, periode = ?, date_debut = ?, " +
                "date_fin = ?, classe_id = ?, est_actif = ?, description = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, edt.getNom());
            pstmt.setString(2, edt.getPeriode());
            pstmt.setString(3, edt.getDateDebut().toString());
            pstmt.setString(4, edt.getDateFin().toString());
            pstmt.setInt(5, edt.getClasseId());
            pstmt.setBoolean(6, true);
            pstmt.setString(7, edt.getDescription());
            pstmt.setInt(8, edt.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimer(int id) {
        String query = "DELETE FROM emploi_du_temps WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== GESTION DES COURS DE L'EDT ====================

    public boolean ajouterCours(int edtId, int coursId) {
        String query = "INSERT INTO emploi_du_temps_cours (emploi_du_temps_id, cours_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, edtId);
            pstmt.setInt(2, coursId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean retirerCours(int edtId, int coursId) {
        String query = "DELETE FROM emploi_du_temps_cours WHERE emploi_du_temps_id = ? AND cours_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, edtId);
            pstmt.setInt(2, coursId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Cours> getCoursForEmploiDuTemps(int edtId) {
        List<Cours> coursList = new ArrayList<>();
        String query = "SELECT c.* FROM cours c " +
                "JOIN emploi_du_temps_cours etc ON c.id = etc.cours_id " +
                "WHERE etc.emploi_du_temps_id = ? ORDER BY c.date, c.creneau_id";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, edtId);
            ResultSet rs = pstmt.executeQuery();

            CoursDAO coursDAO = new CoursDAO();
            while (rs.next()) {
                // Note: il faudrait avoir une méthode pour mapper sans créer un nouveau DAO
                Cours cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setMatiere(rs.getString("matiere"));
                coursList.add(cours);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coursList;
    }

    private EmploiDuTemps mapResultSetToEmploiDuTemps(ResultSet rs) throws SQLException {
        EmploiDuTemps edt = new EmploiDuTemps();
        edt.setId(rs.getInt("id"));
        edt.setNom(rs.getString("nom"));
        edt.setPeriode(rs.getString("periode"));
        edt.setDateDebut(LocalDate.parse(rs.getString("date_debut")));
        edt.setDateFin(LocalDate.parse(rs.getString("date_fin")));
        edt.setClasseId(rs.getInt("classe_id"));
        edt.setDescription(rs.getString("description"));
        return edt;
    }
}