package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code promotion} — structure identique à Symfony.
 *
 * Colonnes : idPromotion, nom, description, typeReduction,
 *            valeurReduction, dateDebut, dateFin, quantiteMin,
 *            cumulable, actif, idProduit
 *
 * La table est créée/mise à jour automatiquement au premier accès.
 */
public class PromotionDAO {

    // ------------------------------------------------------------------ //
    //  Auto-création / migration de la table
    // ------------------------------------------------------------------ //

    private static volatile boolean tableEnsured = false;

    public static void ensureTableExists() {
        if (tableEnsured) return;
        synchronized (PromotionDAO.class) {
            if (tableEnsured) return;
            // Crée la table avec la structure exacte Symfony + idProduit pour Java
            String createSql = """
                    CREATE TABLE IF NOT EXISTS promotion (
                        idPromotion     INT AUTO_INCREMENT PRIMARY KEY,
                        nom             VARCHAR(255) NOT NULL,
                        description     TEXT         DEFAULT NULL,
                        typeReduction   VARCHAR(30)  NOT NULL DEFAULT 'pourcentage',
                        valeurReduction DOUBLE       NOT NULL DEFAULT 0,
                        dateDebut       DATE         DEFAULT NULL,
                        dateFin         DATE         DEFAULT NULL,
                        quantiteMin     INT          NOT NULL DEFAULT 1,
                        cumulable       TINYINT(1)   NOT NULL DEFAULT 0,
                        actif           TINYINT(1)   NOT NULL DEFAULT 1,
                        idProduit       INT          DEFAULT NULL
                    )
                    """;
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createSql);
                // Ajoute idProduit si la table existait déjà sans cette colonne
                addColumnIfMissing(conn, "promotion", "idProduit",
                        "INT DEFAULT NULL");
                // Ajoute description si manquante (table créée par Symfony sans cette col)
                addColumnIfMissing(conn, "promotion", "description",
                        "TEXT DEFAULT NULL");
                // Ajoute cumulable si manquante
                addColumnIfMissing(conn, "promotion", "cumulable",
                        "TINYINT(1) NOT NULL DEFAULT 0");
                // Ajoute actif si manquante
                addColumnIfMissing(conn, "promotion", "actif",
                        "TINYINT(1) NOT NULL DEFAULT 1");
                System.out.println("[PromotionDAO] Table 'promotion' prête.");
                tableEnsured = true;
            } catch (SQLException e) {
                System.err.println("[PromotionDAO] Erreur init table : " + e.getMessage());
                tableEnsured = true;
            }
        }
    }

    private static void addColumnIfMissing(Connection conn, String table,
                                            String column, String definition) {
        String check = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    String alter = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate(alter);
                        System.out.println("[PromotionDAO] Colonne ajoutée : " + column);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO] addColumnIfMissing(" + column + ") : " + e.getMessage());
        }
    }

    public PromotionDAO() {
        ensureTableExists();
    }

    // ------------------------------------------------------------------ //
    //  Mapping ResultSet → Promotion
    // ------------------------------------------------------------------ //

    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setIdPromotion(rs.getInt("idPromotion"));
        p.setNom(rs.getString("nom"));

        // description peut être NULL
        p.setDescription(rs.getString("description"));

        // typeReduction : "pourcentage" ou "montant_fixe"
        String type = rs.getString("typeReduction");
        p.setTypeReduction(type != null ? type.toLowerCase() : Promotion.TYPE_POURCENTAGE);

        p.setValeurReduction(rs.getDouble("valeurReduction"));
        p.setQuantiteMin(rs.getInt("quantiteMin"));

        Date debut = rs.getDate("dateDebut");
        p.setDateDebut(debut != null ? debut.toLocalDate() : null);

        Date fin = rs.getDate("dateFin");
        p.setDateFin(fin != null ? fin.toLocalDate() : null);

        p.setCumulable(rs.getInt("cumulable") == 1);
        p.setActif(rs.getInt("actif") == 1);

        // idProduit peut être NULL (promo globale)
        int idProduit = rs.getInt("idProduit");
        p.setIdProduit(rs.wasNull() ? 0 : idProduit);

        return p;
    }

    // ------------------------------------------------------------------ //
    //  Lecture
    // ------------------------------------------------------------------ //

    public List<Promotion> getAll() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion ORDER BY idPromotion DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.getAll] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Promotions actives pour un produit donné.
     * Inclut les promos globales (idProduit IS NULL).
     */
    public List<Promotion> findActiveForProduct(int idProduit) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion"
                + " WHERE actif = 1"
                + " AND (idProduit = ? OR idProduit IS NULL)"
                + " AND (dateDebut IS NULL OR dateDebut <= CURDATE())"
                + " AND (dateFin   IS NULL OR dateFin   >= CURDATE())"
                + " AND quantiteMin <= 1"
                + " ORDER BY valeurReduction DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.findActiveForProduct] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Promotions actives pour un produit et une quantité donnée.
     */
    public List<Promotion> findActiveForProduct(int idProduit, int quantity) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion"
                + " WHERE actif = 1"
                + " AND (idProduit = ? OR idProduit IS NULL)"
                + " AND (dateDebut IS NULL OR dateDebut <= CURDATE())"
                + " AND (dateFin   IS NULL OR dateFin   >= CURDATE())"
                + " AND quantiteMin <= ?"
                + " ORDER BY valeurReduction DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setInt(2, quantity);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.findActiveForProduct(qty)] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Promotion getById(int id) {
        String sql = "SELECT * FROM promotion WHERE idPromotion = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.getById] " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    //  Écriture
    // ------------------------------------------------------------------ //

    /** @return null si succès, message d'erreur sinon */
    public String insertWithError(Promotion p) {
        String sql = "INSERT INTO promotion"
                + " (nom, description, typeReduction, valeurReduction,"
                + "  dateDebut, dateFin, quantiteMin, cumulable, actif, idProduit)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, p);
            int affected = stmt.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) p.setIdPromotion(keys.getInt(1));
                }
                return null;
            }
            return "Aucune ligne insérée.";
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.insert] SQLState=" + e.getSQLState()
                    + " Code=" + e.getErrorCode() + " : " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean insert(Promotion p) {
        return insertWithError(p) == null;
    }

    /** @return null si succès, message d'erreur sinon */
    public String updateWithError(Promotion p) {
        String sql = "UPDATE promotion SET"
                + " nom = ?, description = ?, typeReduction = ?, valeurReduction = ?,"
                + " dateDebut = ?, dateFin = ?, quantiteMin = ?, cumulable = ?, actif = ?, idProduit = ?"
                + " WHERE idPromotion = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, p);
            stmt.setInt(11, p.getIdPromotion());
            int affected = stmt.executeUpdate();
            if (affected == 1) return null;
            return "Aucune ligne mise à jour (id=" + p.getIdPromotion() + ").";
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.update] " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean update(Promotion p) {
        return updateWithError(p) == null;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM promotion WHERE idPromotion = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.delete] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void bindParams(PreparedStatement stmt, Promotion p) throws SQLException {
        stmt.setString(1, p.getNom());

        if (p.getDescription() != null && !p.getDescription().isBlank())
            stmt.setString(2, p.getDescription());
        else stmt.setNull(2, Types.VARCHAR);

        stmt.setString(3, p.getTypeReduction() != null
                ? p.getTypeReduction() : Promotion.TYPE_POURCENTAGE);
        stmt.setDouble(4, p.getValeurReduction());

        if (p.getDateDebut() != null) stmt.setDate(5, Date.valueOf(p.getDateDebut()));
        else stmt.setNull(5, Types.DATE);

        if (p.getDateFin() != null) stmt.setDate(6, Date.valueOf(p.getDateFin()));
        else stmt.setNull(6, Types.DATE);

        stmt.setInt(7, Math.max(1, p.getQuantiteMin()));
        stmt.setInt(8, p.isCumulable() ? 1 : 0);
        stmt.setInt(9, p.isActif() ? 1 : 0);

        // 0 = global → NULL en BDD
        if (p.getIdProduit() == 0) stmt.setNull(10, Types.INTEGER);
        else stmt.setInt(10, p.getIdProduit());
    }
}
