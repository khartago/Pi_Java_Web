package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès JDBC à la table {@code article} (même schéma que {@code App\Entity\Article}).
 * Préférer {@link Services.ArticleService} pour le code métier ; cette classe reste pour compatibilité.
 */
public class ArticleDAO {

    private static int resolveDefaultBlogId(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT MIN(idBlog) AS m FROM blog")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("m");
                if (!rs.wasNull()) {
                    return id;
                }
            }
        }
        throw new SQLException(
                "Table blog vide : créez au moins un blog depuis le web avant d'ajouter un article.");
    }

    private static ArticleBase mapRow(ResultSet rs) throws SQLException {
        ArticleBase article = new ArticleBase();
        article.setId(rs.getInt("ArticleID"));
        article.setTitle(rs.getString("Titre"));
        article.setTexte(rs.getString("texte"));
        int lk = rs.getInt("Likes");
        if (rs.wasNull()) {
            lk = 0;
        }
        int dk = rs.getInt("Dislikes");
        if (rs.wasNull()) {
            dk = 0;
        }
        article.setLike(lk);
        article.setDislike(dk);
        Timestamp ts = rs.getTimestamp("CreationDate");
        if (ts != null) {
            article.setCreationDate(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        try {
            String an = rs.getString("authorName");
            article.setAuthorName(an != null ? an : "");
        } catch (SQLException ignored) {
            article.setAuthorName("");
        }
        return article;
    }

    public boolean createArticle(ArticleBase article) {
        String sql = "INSERT INTO article (Titre, texte, Likes, Dislikes, edited, BlogID, CreationDate) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int blogId = resolveDefaultBlogId(conn);
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getTexte());
            stmt.setInt(3, article.getLike());
            stmt.setInt(4, article.getDislike());
            stmt.setBoolean(5, false);
            stmt.setInt(6, blogId);
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    article.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ArticleBase> getAllArticles() throws SQLException {
        List<ArticleBase> articles = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT a.ArticleID, a.Titre, a.texte, a.Likes, a.Dislikes, a.edited, a.BlogID, a.CreationDate, "
                    + "u.nom AS authorName FROM article a "
                    + "LEFT JOIN blog b ON a.BlogID = b.idBlog "
                    + "LEFT JOIN utilisateur u ON b.idutilisateur = u.id "
                    + "ORDER BY a.CreationDate DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    articles.add(mapRow(rs));
                }
            }
        }
        return articles;
    }

    public boolean updateArticle(ArticleBase article) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE article SET Titre = ?, texte = ?, Likes = ?, Dislikes = ?, edited = ? WHERE ArticleID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, article.getTitle());
                stmt.setString(2, article.getTexte());
                stmt.setInt(3, article.getLike());
                stmt.setInt(4, article.getDislike());
                stmt.setBoolean(5, true);
                stmt.setInt(6, article.getId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean supprimerArticle(int id) {
        String req = "DELETE FROM article WHERE ArticleID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(req)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
