package model;
import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


public class ArticleDAO {
    public boolean createArticle(ArticleBase article) {
        String sql = "INSERT INTO articletable (Titre,Texte,CreationDate,Likes,Dislikes) VALUES (?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, article.getTitle());
            stmt.setString(2, article.getTexte());
            if (article.getCreationDate() != null) stmt.setDate(4, Date.valueOf(article.getCreationDate()));
            else stmt.setNull(4, Types.DATE);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return true;
            } ;
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public List<ArticleBase> getAllArticles() throws SQLException {
    List<ArticleBase> articles = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();) {
            String sql= "SELECT ArticleID, Titre, Texte, CreationDate, Likes, Dislikes FROM articletable";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();{
                while (rs.next()) articles.add(mapResultSetToArticles(rs));
            }return articles;
        }
}catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

private ArticleBase mapResultSetToArticles(ResultSet rs) throws SQLException {
    ArticleBase article =new ArticleBase();
    article.setId(rs.getInt("ArticleID"));
    article.setTitle(rs.getString("Titre"));
    article.setTexte(rs.getString("Texte"));
    article.setCreationDate(rs.getDate("CreationDate").toLocalDate());
    article.setLike(rs.getInt("Likes"));
    article.setDislike(rs.getInt("Dislikes"));

    return article;
}

public boolean updateArticle(ArticleBase article) {
    try(Connection conn = DBConnection.getConnection()){
        String sql ="UPDATE articletable SET  Titre=?, Texte=?, WHERE ArticleID=?";
        try (PreparedStatement stmt =conn.prepareStatement(sql)){
            stmt.setString(1,article.getTitle());
            stmt.setString(2,article.getTexte());
            return stmt.executeUpdate() >0;
        }
    }catch (SQLException e) {
    e.printStackTrace();
    }
    return false;
    }

    public boolean supprimerArticle(int id) {
        String req="DELETE FROM articletable WHERE id=?";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt=conn.prepareStatement(req);
            stmt.setInt(1,id);
            System.out.println("Article Supprimer");
            return stmt.executeUpdate()>0;
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
