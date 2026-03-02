package model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    private volatile Boolean imagePathSupportedCache = null;

    private boolean isImagePathSupported(Connection conn) throws SQLException {
        Boolean cached = imagePathSupportedCache;
        if (cached != null) return cached;

        // Vérifie la présence de la colonne via INFORMATION_SCHEMA (compatible MySQL)
        String sql = """
                SELECT 1
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                LIMIT 1
                """;
        boolean supported;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "produit");
            stmt.setString(2, "imagePath");
            try (ResultSet rs = stmt.executeQuery()) {
                supported = rs.next();
            }
        }

        imagePathSupportedCache = supported;
        return supported;
    }

    private Produit mapRow(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setIdProduit(rs.getInt("idProduit"));
        p.setNom(rs.getString("nom"));
        p.setQuantite(rs.getInt("quantite"));
        p.setUnite(rs.getString("unite"));

        Date exp = rs.getDate("dateExpiration");
        p.setDateExpiration(exp != null ? exp.toLocalDate() : null);

        // ✅ imagePath (si disponible dans la requête)
        try {
            p.setImagePath(rs.getString("imagePath"));
        } catch (SQLException ignored) {
            p.setImagePath(null);
        }

        return p;
    }

    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = hasImagePath
                    ? "SELECT idProduit, nom, quantite, unite, dateExpiration, imagePath FROM produit"
                    : "SELECT idProduit, nom, quantite, unite, dateExpiration FROM produit";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) produits.add(mapRow(rs));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    public boolean insert(Produit p) {
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = hasImagePath
                    ? "INSERT INTO produit (nom, quantite, unite, dateExpiration, imagePath) VALUES (?, ?, ?, ?, ?)"
                    : "INSERT INTO produit (nom, quantite, unite, dateExpiration) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, p.getNom());
                stmt.setInt(2, p.getQuantite());
                stmt.setString(3, p.getUnite());

                if (p.getDateExpiration() != null) stmt.setDate(4, Date.valueOf(p.getDateExpiration()));
                else stmt.setNull(4, Types.DATE);

                if (hasImagePath) {
                    // ✅ imagePath peut être null
                    if (p.getImagePath() != null && !p.getImagePath().trim().isEmpty())
                        stmt.setString(5, p.getImagePath().trim());
                    else
                        stmt.setNull(5, Types.VARCHAR);
                }

                int affected = stmt.executeUpdate();
                if (affected == 1) {
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) p.setIdProduit(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Produit p) {
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = hasImagePath
                    ? "UPDATE produit SET nom = ?, quantite = ?, unite = ?, dateExpiration = ?, imagePath = ? WHERE idProduit = ?"
                    : "UPDATE produit SET nom = ?, quantite = ?, unite = ?, dateExpiration = ? WHERE idProduit = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, p.getNom());
                stmt.setInt(2, p.getQuantite());
                stmt.setString(3, p.getUnite());

                if (p.getDateExpiration() != null) stmt.setDate(4, Date.valueOf(p.getDateExpiration()));
                else stmt.setNull(4, Types.DATE);

                int idIndex;
                if (hasImagePath) {
                    if (p.getImagePath() != null && !p.getImagePath().trim().isEmpty())
                        stmt.setString(5, p.getImagePath().trim());
                    else
                        stmt.setNull(5, Types.VARCHAR);
                    idIndex = 6;
                } else {
                    idIndex = 5;
                }

                stmt.setInt(idIndex, p.getIdProduit());

                return stmt.executeUpdate() == 1;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public Produit getById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = hasImagePath
                    ? "SELECT idProduit, nom, quantite, unite, dateExpiration, imagePath FROM produit WHERE idProduit = ?"
                    : "SELECT idProduit, nom, quantite, unite, dateExpiration FROM produit WHERE idProduit = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========= EXPIRATION =========

    public List<Produit> getExpiringBetween(LocalDate start, LocalDate end) {
        List<Produit> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = (hasImagePath
                    ? "SELECT idProduit, nom, quantite, unite, dateExpiration, imagePath "
                    : "SELECT idProduit, nom, quantite, unite, dateExpiration ")
                    + "FROM produit WHERE dateExpiration IS NOT NULL AND dateExpiration BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Produit> getExpiredBefore(LocalDate date) {
        List<Produit> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = (hasImagePath
                    ? "SELECT idProduit, nom, quantite, unite, dateExpiration, imagePath "
                    : "SELECT idProduit, nom, quantite, unite, dateExpiration ")
                    + "FROM produit WHERE dateExpiration IS NOT NULL AND dateExpiration < ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Produit> searchByNameLike(String query, int limit) {
        List<Produit> produits = new ArrayList<>();
        if (query == null) return produits;
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return produits;

        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImagePath = isImagePathSupported(conn);
            String sql = (hasImagePath
                    ? "SELECT idProduit, nom, quantite, unite, dateExpiration, imagePath "
                    : "SELECT idProduit, nom, quantite, unite, dateExpiration ")
                    + "FROM produit WHERE LOWER(nom) LIKE ? ORDER BY nom LIMIT ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + q + "%");
                stmt.setInt(2, Math.max(1, limit));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) produits.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }
}