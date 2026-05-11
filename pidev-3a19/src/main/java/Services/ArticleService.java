package Services;

import model.ArticleBase;
import Utils.Mydatabase;
import Iservices.IArticleService;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistance des articles alignée sur la table Doctrine {@code article}
 * (voir {@code App\Entity\Article} et migration {@code Version20260510120000}).
 */
public class ArticleService implements IArticleService {

    private final Connection con;

    public ArticleService() {
        con = Mydatabase.getInstance().getConnextion();
    }

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
                "Table blog vide : créez au moins un blog depuis le web avant d'ajouter un article depuis JavaFX.");
    }

    @Override
    public ArticleBase ajouterArticle(ArticleBase a) {
        String req = "INSERT INTO article (Titre, texte, Likes, Dislikes, edited, BlogID, CreationDate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int blogId = resolveDefaultBlogId(con);
            try (PreparedStatement ste = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
                ste.setString(1, a.getTitle());
                ste.setString(2, a.getTexte());
                ste.setObject(3, a.getLike(), Types.INTEGER);
                ste.setObject(4, a.getDislike(), Types.INTEGER);
                ste.setBoolean(5, false);
                ste.setInt(6, blogId);
                ste.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                ste.executeUpdate();
                try (ResultSet keys = ste.getGeneratedKeys()) {
                    if (keys.next()) {
                        a.setId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    @Override
    public void modifierArticle(ArticleBase a) {
        String req = "UPDATE article SET Titre = ?, texte = ?, Likes = ?, Dislikes = ?, edited = ? WHERE ArticleID = ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setString(1, a.getTitle());
            ste.setString(2, a.getTexte());
            ste.setInt(3, a.getLike());
            ste.setInt(4, a.getDislike());
            ste.setBoolean(5, true);
            ste.setInt(6, a.getId());
            ste.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supprimerArticle(int id) {
        String req = "DELETE FROM article WHERE ArticleID = ?";
        try (PreparedStatement ste = con.prepareStatement(req)) {
            ste.setInt(1, id);
            return ste.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ArticleBase> afficherArticle() {
        List<ArticleBase> out = new ArrayList<>();
        String req = "SELECT a.ArticleID, a.Titre, a.texte, a.Likes, a.Dislikes, a.edited, a.BlogID, a.CreationDate, "
                + "u.nom AS authorName FROM article a "
                + "LEFT JOIN blog b ON a.BlogID = b.idBlog "
                + "LEFT JOIN utilisateur u ON b.idutilisateur = u.id "
                + "ORDER BY a.CreationDate DESC";
        try (Statement ste = con.createStatement();
             ResultSet rs = ste.executeQuery(req)) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private static ArticleBase mapRow(ResultSet rs) throws SQLException {
        ArticleBase a = new ArticleBase();
        a.setId(rs.getInt("ArticleID"));
        a.setTitle(rs.getString("Titre"));
        a.setTexte(rs.getString("texte"));
        int likes = rs.getInt("Likes");
        if (rs.wasNull()) {
            likes = 0;
        }
        int dislikes = rs.getInt("Dislikes");
        if (rs.wasNull()) {
            dislikes = 0;
        }
        a.setLike(likes);
        a.setDislike(dislikes);
        Timestamp ts = rs.getTimestamp("CreationDate");
        if (ts != null) {
            a.setCreationDate(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        String author = rs.getString("authorName");
        a.setAuthorName(author != null ? author : "");
        return a;
    }
}
