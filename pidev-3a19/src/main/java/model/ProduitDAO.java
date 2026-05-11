package model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    // ------------------------------------------------------------------ //
    //  Cache de détection des colonnes optionnelles
    // ------------------------------------------------------------------ //

    private volatile Boolean imagePathSupportedCache    = null;
    private volatile Boolean prixUnitaireSupportedCache = null;

    private boolean isColumnSupported(Connection conn, String table, String column) throws SQLException {
        String sql = """
                SELECT 1
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME   = ?
                  AND COLUMN_NAME  = ?
                LIMIT 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table);
            stmt.setString(2, column);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isImagePathSupported(Connection conn) throws SQLException {
        if (imagePathSupportedCache == null)
            imagePathSupportedCache = isColumnSupported(conn, "produit", "imagePath");
        return imagePathSupportedCache;
    }

    private boolean isPrixUnitaireSupported(Connection conn) throws SQLException {
        if (prixUnitaireSupportedCache == null)
            prixUnitaireSupportedCache = isColumnSupported(conn, "produit", "prixUnitaire");
        return prixUnitaireSupportedCache;
    }

    // ------------------------------------------------------------------ //
    //  Mapping ResultSet → Produit
    // ------------------------------------------------------------------ //

    private Produit mapRow(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setIdProduit(rs.getInt("idProduit"));
        p.setNom(rs.getString("nom"));
        p.setQuantite(rs.getInt("quantite"));
        p.setUnite(rs.getString("unite"));

        Date exp = rs.getDate("dateExpiration");
        p.setDateExpiration(exp != null ? exp.toLocalDate() : null);

        try { p.setImagePath(rs.getString("imagePath")); }
        catch (SQLException ignored) { p.setImagePath(null); }

        try { p.setPrixUnitaire(rs.getDouble("prixUnitaire")); }
        catch (SQLException ignored) { p.setPrixUnitaire(0.0); }

        return p;
    }

    // ------------------------------------------------------------------ //
    //  Construction dynamique des colonnes SELECT
    // ------------------------------------------------------------------ //

    private String buildSelectCols(boolean hasImage, boolean hasPrix) {
        StringBuilder sb = new StringBuilder(
                "idProduit, nom, quantite, unite, dateExpiration");
        if (hasImage) sb.append(", imagePath");
        if (hasPrix)  sb.append(", prixUnitaire");
        return sb.toString();
    }

    // ------------------------------------------------------------------ //
    //  CRUD
    // ------------------------------------------------------------------ //

    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);
            String sql = "SELECT " + buildSelectCols(hasImg, hasPrix) + " FROM produit";
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
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);

            StringBuilder cols = new StringBuilder("nom, quantite, unite, dateExpiration");
            StringBuilder vals = new StringBuilder("?, ?, ?, ?");
            if (hasImg)  { cols.append(", imagePath");    vals.append(", ?"); }
            if (hasPrix) { cols.append(", prixUnitaire"); vals.append(", ?"); }

            String sql = "INSERT INTO produit (" + cols + ") VALUES (" + vals + ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int idx = 1;
                stmt.setString(idx++, p.getNom());
                stmt.setInt(idx++, p.getQuantite());
                stmt.setString(idx++, p.getUnite());
                if (p.getDateExpiration() != null) stmt.setDate(idx++, Date.valueOf(p.getDateExpiration()));
                else stmt.setNull(idx++, Types.DATE);
                if (hasImg) {
                    if (p.getImagePath() != null && !p.getImagePath().isBlank())
                        stmt.setString(idx++, p.getImagePath().trim());
                    else stmt.setNull(idx++, Types.VARCHAR);
                }
                if (hasPrix) stmt.setDouble(idx, p.getPrixUnitaire());

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
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);

            StringBuilder set = new StringBuilder(
                    "nom = ?, quantite = ?, unite = ?, dateExpiration = ?");
            if (hasImg)  set.append(", imagePath = ?");
            if (hasPrix) set.append(", prixUnitaire = ?");

            String sql = "UPDATE produit SET " + set + " WHERE idProduit = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int idx = 1;
                stmt.setString(idx++, p.getNom());
                stmt.setInt(idx++, p.getQuantite());
                stmt.setString(idx++, p.getUnite());
                if (p.getDateExpiration() != null) stmt.setDate(idx++, Date.valueOf(p.getDateExpiration()));
                else stmt.setNull(idx++, Types.DATE);
                if (hasImg) {
                    if (p.getImagePath() != null && !p.getImagePath().isBlank())
                        stmt.setString(idx++, p.getImagePath().trim());
                    else stmt.setNull(idx++, Types.VARCHAR);
                }
                if (hasPrix) stmt.setDouble(idx++, p.getPrixUnitaire());
                stmt.setInt(idx, p.getIdProduit());
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
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);
            String sql = "SELECT " + buildSelectCols(hasImg, hasPrix)
                    + " FROM produit WHERE idProduit = ?";
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

    public List<Produit> getExpiringBetween(LocalDate start, LocalDate end) {
        List<Produit> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);
            String sql = "SELECT " + buildSelectCols(hasImg, hasPrix)
                    + " FROM produit WHERE dateExpiration IS NOT NULL AND dateExpiration BETWEEN ? AND ?";
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
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);
            String sql = "SELECT " + buildSelectCols(hasImg, hasPrix)
                    + " FROM produit WHERE dateExpiration IS NOT NULL AND dateExpiration < ?";
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
            boolean hasImg  = isImagePathSupported(conn);
            boolean hasPrix = isPrixUnitaireSupported(conn);
            String sql = "SELECT " + buildSelectCols(hasImg, hasPrix)
                    + " FROM produit WHERE LOWER(nom) LIKE ? ORDER BY nom LIMIT ?";
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
