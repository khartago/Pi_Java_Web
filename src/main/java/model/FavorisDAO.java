package model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les favoris/wishlist des utilisateurs.
 * Stocke les produits que les utilisateurs ont marqué comme favoris.
 */
public class FavorisDAO {

    /**
     * Ajoute un produit aux favoris.
     */
    public boolean addFavoris(int idProduit) {
        String sql = "INSERT INTO favoris (idProduit, dateAjout) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE dateAjout = VALUES(dateAjout)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retire un produit des favoris.
     */
    public boolean removeFavoris(int idProduit) {
        String sql = "DELETE FROM favoris WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifie si un produit est dans les favoris.
     */
    public boolean isFavoris(int idProduit) {
        String sql = "SELECT COUNT(*) as count FROM favoris WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupère tous les produits marqués comme favoris.
     */
    public List<Produit> getAllFavoris() {
        List<Produit> favoris = new ArrayList<>();
        String sql = "SELECT p.* FROM produit p " +
                     "INNER JOIN favoris f ON p.idProduit = f.idProduit " +
                     "ORDER BY f.dateAjout DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produit p = new Produit();
                    p.setIdProduit(rs.getInt("idProduit"));
                    p.setNom(rs.getString("nom"));
                    p.setQuantite(rs.getInt("quantite"));
                    p.setUnite(rs.getString("unite"));

                    Date dateExp = rs.getDate("dateExpiration");
                    if (dateExp != null) {
                        p.setDateExpiration(dateExp.toLocalDate());
                    }

                    p.setImagePath(rs.getString("imagePath"));
                    p.setPrixUnitaire(rs.getDouble("prixUnitaire"));

                    favoris.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
    }

    /**
     * Compte le nombre de favoris.
     */
    public int countFavoris() {
        String sql = "SELECT COUNT(*) as count FROM favoris";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Vide tous les favoris.
     */
    public boolean clearAllFavoris() {
        String sql = "DELETE FROM favoris";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

