package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object providing CRUD operations for {@link Produit} instances. All
 * database interaction goes through this class. It uses prepared statements to
 * protect against SQL injection and ensures resources are closed via
 * try‑with‑resources.
 */
public class ProduitDAO {

    /**
     * Fetches all products from the database.
     *
     * @return list of all products
     */
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT idProduit, nom, quantite, unite, dateExpiration FROM produit";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produit p = new Produit();
                p.setIdProduit(rs.getInt("idProduit"));
                p.setNom(rs.getString("nom"));
                p.setQuantite(rs.getInt("quantite"));
                p.setUnite(rs.getString("unite"));
                Date exp = rs.getDate("dateExpiration");
                p.setDateExpiration(exp != null ? exp.toLocalDate() : null);
                produits.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    /**
     * Inserts a new product into the database.
     *
     * @param p product to insert
     * @return {@code true} if insertion succeeded
     */
    public boolean insert(Produit p) {
        String sql = "INSERT INTO produit (nom, quantite, unite, dateExpiration) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNom());
            stmt.setInt(2, p.getQuantite());
            stmt.setString(3, p.getUnite());
            if (p.getDateExpiration() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(p.getDateExpiration()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            int affected = stmt.executeUpdate();
            if (affected == 1) {
                // set generated id on model
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setIdProduit(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates an existing product in the database.
     *
     * @param p product with new values
     * @return {@code true} if update succeeded
     */
    public boolean update(Produit p) {
        String sql = "UPDATE produit SET nom = ?, quantite = ?, unite = ?, dateExpiration = ? WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNom());
            stmt.setInt(2, p.getQuantite());
            stmt.setString(3, p.getUnite());
            if (p.getDateExpiration() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(p.getDateExpiration()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            stmt.setInt(5, p.getIdProduit());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a product by its identifier. Note that this method does not perform
     * referential integrity checks on associated materials – the foreign key
     * constraint defined on the {@code materiel} table will take care of
     * preventing deletions if materials still reference the product.
     *
     * @param id id of the product to delete
     * @return {@code true} if deletion succeeded
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM produit WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves a single product by its identifier.
     *
     * @param id product id
     * @return the matching product or {@code null} if none found
     */
    public Produit getById(int id) {
        String sql = "SELECT idProduit, nom, quantite, unite, dateExpiration FROM produit WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produit p = new Produit();
                    p.setIdProduit(rs.getInt("idProduit"));
                    p.setNom(rs.getString("nom"));
                    p.setQuantite(rs.getInt("quantite"));
                    p.setUnite(rs.getString("unite"));
                    Date exp = rs.getDate("dateExpiration");
                    p.setDateExpiration(exp != null ? exp.toLocalDate() : null);
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}