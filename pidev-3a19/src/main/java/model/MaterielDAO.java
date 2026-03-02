package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for {@link Materiel} instances. Provides CRUD operations
 * relating to equipment objects tied to a product. All database interaction
 * occurs through this class and the {@link DBConnection} utility.
 */
public class MaterielDAO {

    public List<Materiel> getAllByProduit(int produitId) {
        List<Materiel> materiels = new ArrayList<>();
        String sql = "SELECT idMateriel, nom, etat, dateAchat, cout, idProduit FROM materiel WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produitId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Materiel m = new Materiel();
                    m.setIdMateriel(rs.getInt("idMateriel"));
                    m.setNom(rs.getString("nom"));
                    m.setEtat(rs.getString("etat"));
                    Date achat = rs.getDate("dateAchat");
                    m.setDateAchat(achat != null ? achat.toLocalDate() : null);
                    m.setCout(rs.getDouble("cout"));
                    m.setIdProduit(rs.getInt("idProduit"));
                    materiels.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiels;
    }

    public boolean insert(Materiel m) {
        String sql = "INSERT INTO materiel (nom, etat, dateAchat, cout, idProduit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, m.getNom());
            stmt.setString(2, m.getEtat());
            if (m.getDateAchat() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(m.getDateAchat()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            stmt.setDouble(4, m.getCout());
            stmt.setInt(5, m.getIdProduit());
            int affected = stmt.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        m.setIdMateriel(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Materiel m) {
        String sql = "UPDATE materiel SET nom = ?, etat = ?, dateAchat = ?, cout = ?, idProduit = ? WHERE idMateriel = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNom());
            stmt.setString(2, m.getEtat());
            if (m.getDateAchat() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(m.getDateAchat()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            stmt.setDouble(4, m.getCout());
            stmt.setInt(5, m.getIdProduit());
            stmt.setInt(6, m.getIdMateriel());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM materiel WHERE idMateriel = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Materiel getById(int id) {
        String sql = "SELECT idMateriel, nom, etat, dateAchat, cout, idProduit FROM materiel WHERE idMateriel = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Materiel m = new Materiel();
                    m.setIdMateriel(rs.getInt("idMateriel"));
                    m.setNom(rs.getString("nom"));
                    m.setEtat(rs.getString("etat"));
                    Date achat = rs.getDate("dateAchat");
                    m.setDateAchat(achat != null ? achat.toLocalDate() : null);
                    m.setCout(rs.getDouble("cout"));
                    m.setIdProduit(rs.getInt("idProduit"));
                    return m;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
