package model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les favoris.
 * Crée la table automatiquement si elle n'existe pas.
 */
public class FavorisDAO {

    // ------------------------------------------------------------------ //
    //  Auto-création de la table
    // ------------------------------------------------------------------ //

    private static volatile boolean tableEnsured = false;

    public static void ensureTableExists() {
        if (tableEnsured) return;
        synchronized (FavorisDAO.class) {
            if (tableEnsured) return;
            String sql = """
                    CREATE TABLE IF NOT EXISTS favoris (
                        idFavoris  INT AUTO_INCREMENT PRIMARY KEY,
                        idProduit  INT NOT NULL,
                        dateAjout  DATETIME DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uq_favoris_produit (idProduit),
                        CONSTRAINT fk_favoris_produit
                            FOREIGN KEY (idProduit) REFERENCES produit(idProduit)
                            ON DELETE CASCADE
                    )
                    """;
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                tableEnsured = true;
            } catch (SQLException e) {
                System.err.println("[FavorisDAO] Erreur init table : " + e.getMessage());
                tableEnsured = true;
            }
        }
    }

    public FavorisDAO() {
        ensureTableExists();
    }

    // ------------------------------------------------------------------ //
    //  CRUD
    // ------------------------------------------------------------------ //

    public boolean addFavoris(int idProduit) {
        String sql = "INSERT INTO favoris (idProduit, dateAjout) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE dateAjout = VALUES(dateAjout)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.addFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFavoris(int idProduit) {
        String sql = "DELETE FROM favoris WHERE idProduit = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.removeFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFavoris(int idProduit) {
        String sql = "SELECT 1 FROM favoris WHERE idProduit = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.isFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retourne tous les produits favoris avec toutes leurs colonnes
     * (imagePath, prixUnitaire inclus si présents).
     */
    public List<Produit> getAllFavoris() {
        List<Produit> favoris = new ArrayList<>();

        // Détection dynamique des colonnes optionnelles
        boolean hasImagePath    = false;
        boolean hasPrixUnitaire = false;
        try (Connection conn = DBConnection.getConnection()) {
            hasImagePath    = columnExists(conn, "produit", "imagePath");
            hasPrixUnitaire = columnExists(conn, "produit", "prixUnitaire");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringBuilder cols = new StringBuilder(
                "p.idProduit, p.nom, p.quantite, p.unite, p.dateExpiration");
        if (hasImagePath)    cols.append(", p.imagePath");
        if (hasPrixUnitaire) cols.append(", p.prixUnitaire");

        String sql = "SELECT " + cols + " FROM produit p "
                + "INNER JOIN favoris f ON p.idProduit = f.idProduit "
                + "ORDER BY f.dateAjout DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produit p = new Produit();
                p.setIdProduit(rs.getInt("idProduit"));
                p.setNom(rs.getString("nom"));
                p.setQuantite(rs.getInt("quantite"));
                p.setUnite(rs.getString("unite"));

                Date dateExp = rs.getDate("dateExpiration");
                if (dateExp != null) p.setDateExpiration(dateExp.toLocalDate());

                if (hasImagePath) {
                    try { p.setImagePath(rs.getString("imagePath")); }
                    catch (SQLException ignored) {}
                }
                if (hasPrixUnitaire) {
                    try { p.setPrixUnitaire(rs.getDouble("prixUnitaire")); }
                    catch (SQLException ignored) {}
                }

                favoris.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.getAllFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return favoris;
    }

    public int countFavoris() {
        String sql = "SELECT COUNT(*) FROM favoris";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.countFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public boolean clearAllFavoris() {
        String sql = "DELETE FROM favoris";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[FavorisDAO.clearAllFavoris] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ------------------------------------------------------------------ //
    //  Helper
    // ------------------------------------------------------------------ //

    private static boolean columnExists(Connection conn, String table, String column)
            throws SQLException {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
