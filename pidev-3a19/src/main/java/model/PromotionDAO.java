package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO {@code promotion} + table de liaison {@code promotion_produit} — aligné sur Doctrine
 * ({@code App\Entity\Promotion}, JoinTable {@code promotion_produit} avec {@code promotion_id}, {@code produit_id}).
 */
public class PromotionDAO {

    private static volatile boolean schemaChecked = false;

    /** Vérifie une fois que les tables existent (créées par les migrations Symfony). */
    public static void ensureTableExists() {
        if (schemaChecked) {
            return;
        }
        synchronized (PromotionDAO.class) {
            if (schemaChecked) {
                return;
            }
            try (Connection conn = DBConnection.getConnection();
                 Statement st = conn.createStatement()) {
                st.executeQuery("SELECT 1 FROM promotion LIMIT 1");
            } catch (SQLException e) {
                System.err.println("[PromotionDAO] Table promotion introuvable. Exécutez : php bin/console doctrine:migrations:migrate");
            }
            schemaChecked = true;
        }
    }

    public PromotionDAO() {
        ensureTableExists();
    }

    private Promotion mapRow(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setIdPromotion(rs.getInt("idPromotion"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
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

        int linked;
        try {
            linked = rs.getInt("java_primary_produit");
            p.setIdProduit(rs.wasNull() ? 0 : linked);
        } catch (SQLException ex) {
            p.setIdProduit(0);
        }
        return p;
    }

    private static String selectPromotionSql() {
        return "SELECT p.*, "
                + "(SELECT pp.produit_id FROM promotion_produit pp WHERE pp.promotion_id = p.idPromotion LIMIT 1) "
                + "AS java_primary_produit FROM promotion p";
    }

    private static String activeForProductWhere() {
        return " WHERE p.actif = 1"
                + " AND (p.dateDebut IS NULL OR p.dateDebut <= CURDATE())"
                + " AND (p.dateFin IS NULL OR p.dateFin >= CURDATE())"
                + " AND ("
                + " NOT EXISTS (SELECT 1 FROM promotion_produit pp0 WHERE pp0.promotion_id = p.idPromotion)"
                + " OR EXISTS (SELECT 1 FROM promotion_produit pp1 WHERE pp1.promotion_id = p.idPromotion AND pp1.produit_id = ?)"
                + ")";
    }

    public List<Promotion> getAll() {
        List<Promotion> list = new ArrayList<>();
        String sql = selectPromotionSql() + " ORDER BY p.idPromotion DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.getAll] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Promotion> findActiveForProduct(int idProduit) {
        return findActiveForProduct(idProduit, 1);
    }

    public List<Promotion> findActiveForProduct(int idProduit, int quantity) {
        List<Promotion> list = new ArrayList<>();
        String sql = selectPromotionSql() + activeForProductWhere() + " AND p.quantiteMin <= ? ORDER BY p.valeurReduction DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setInt(2, Math.max(1, quantity));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.findActiveForProduct] " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Promotion getById(int id) {
        String sql = selectPromotionSql() + " WHERE p.idPromotion = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.getById] " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void replacePromotionProduits(Connection conn, int promotionId, int idProduit) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM promotion_produit WHERE promotion_id = ?")) {
            del.setInt(1, promotionId);
            del.executeUpdate();
        }
        if (idProduit > 0) {
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO promotion_produit (promotion_id, produit_id) VALUES (?, ?)")) {
                ins.setInt(1, promotionId);
                ins.setInt(2, idProduit);
                ins.executeUpdate();
            }
        }
    }

    private void bindPromotionColumns(PreparedStatement stmt, Promotion p) throws SQLException {
        stmt.setString(1, p.getNom());
        if (p.getDescription() != null && !p.getDescription().isBlank()) {
            stmt.setString(2, p.getDescription());
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }
        stmt.setString(3, p.getTypeReduction() != null ? p.getTypeReduction() : Promotion.TYPE_POURCENTAGE);
        stmt.setDouble(4, p.getValeurReduction());
        if (p.getDateDebut() != null) {
            stmt.setDate(5, Date.valueOf(p.getDateDebut()));
        } else {
            stmt.setNull(5, Types.DATE);
        }
        if (p.getDateFin() != null) {
            stmt.setDate(6, Date.valueOf(p.getDateFin()));
        } else {
            stmt.setNull(6, Types.DATE);
        }
        stmt.setInt(7, Math.max(1, p.getQuantiteMin()));
        stmt.setInt(8, p.isCumulable() ? 1 : 0);
        stmt.setInt(9, p.isActif() ? 1 : 0);
    }

    public String insertWithError(Promotion p) {
        String sql = "INSERT INTO promotion (nom, description, typeReduction, valeurReduction, dateDebut, dateFin, quantiteMin, cumulable, actif) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindPromotionColumns(stmt, p);
                int affected = stmt.executeUpdate();
                if (affected != 1) {
                    conn.rollback();
                    return "Aucune ligne insérée.";
                }
                int newId;
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return "Pas d'ID généré.";
                    }
                    newId = keys.getInt(1);
                    p.setIdPromotion(newId);
                }
                replacePromotionProduits(conn, newId, p.getIdProduit());
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.insert] " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public boolean insert(Promotion p) {
        return insertWithError(p) == null;
    }

    public String updateWithError(Promotion p) {
        String sql = "UPDATE promotion SET nom = ?, description = ?, typeReduction = ?, valeurReduction = ?, "
                + "dateDebut = ?, dateFin = ?, quantiteMin = ?, cumulable = ?, actif = ? WHERE idPromotion = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                bindPromotionColumns(stmt, p);
                stmt.setInt(10, p.getIdPromotion());
                if (stmt.executeUpdate() != 1) {
                    conn.rollback();
                    return "Aucune ligne mise à jour (id=" + p.getIdPromotion() + ").";
                }
                replacePromotionProduits(conn, p.getIdPromotion(), p.getIdProduit());
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
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
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement delLinks = conn.prepareStatement(
                    "DELETE FROM promotion_produit WHERE promotion_id = ?")) {
                delLinks.setInt(1, id);
                delLinks.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM promotion WHERE idPromotion = ?")) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDAO.delete] " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
