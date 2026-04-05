package model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for product traceability events.
 */
public class ProduitHistoriqueDAO {

    public void insertEvent(ProduitHistorique h) {
        String sql = "INSERT INTO produit_historique " +
                "(idProduit, typeEvenement, quantiteAvant, quantiteApres, dateEvenement, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, h.getIdProduit());
            stmt.setString(2, h.getTypeEvenement());

            if (h.getQuantiteAvant() != null) stmt.setInt(3, h.getQuantiteAvant());
            else stmt.setNull(3, Types.INTEGER);

            if (h.getQuantiteApres() != null) stmt.setInt(4, h.getQuantiteApres());
            else stmt.setNull(4, Types.INTEGER);

            if (h.getDateEvenement() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(h.getDateEvenement()));
            } else {
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }

            if (h.getCommentaire() != null) stmt.setString(6, h.getCommentaire());
            else stmt.setNull(6, Types.VARCHAR);

            int affected = stmt.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        h.setIdHistorique(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ProduitHistorique> getByProduit(int idProduit) {
        List<ProduitHistorique> list = new ArrayList<>();
        String sql = "SELECT idHistorique, idProduit, typeEvenement, quantiteAvant, quantiteApres, " +
                "dateEvenement, commentaire FROM produit_historique WHERE idProduit = ? " +
                "ORDER BY dateEvenement DESC, idHistorique DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProduitHistorique h = new ProduitHistorique();
                    h.setIdHistorique(rs.getInt("idHistorique"));
                    h.setIdProduit(rs.getInt("idProduit"));
                    h.setTypeEvenement(rs.getString("typeEvenement"));
                    int qa = rs.getInt("quantiteAvant");
                    h.setQuantiteAvant(rs.wasNull() ? null : qa);
                    int qap = rs.getInt("quantiteApres");
                    h.setQuantiteApres(rs.wasNull() ? null : qap);
                    Timestamp ts = rs.getTimestamp("dateEvenement");
                    h.setDateEvenement(ts != null ? ts.toLocalDateTime() : null);
                    h.setCommentaire(rs.getString("commentaire"));
                    list.add(h);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
