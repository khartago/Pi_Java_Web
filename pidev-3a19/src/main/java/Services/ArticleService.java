package Services;

import model.ArticleBase;

import Utils.Mydatabase;
import Iservices.IArticleService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService implements IArticleService {

    Connection con;
    public ArticleService() {
        con= Mydatabase.getInstance().getConnextion();
    }


    public ArticleBase ajouterArticle(ArticleBase a){
        String req="INSERT INTO articletable (title, texte) VALUES (?,?)";
        try {
            PreparedStatement ste = con.prepareStatement(req);
            ste.setString(1, a.getTitle());
            ste.setString(2, a.getTexte());
            ste.executeUpdate();

            System.out.println("Article Ajouté");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    public void modifierArticle (ArticleBase a){
        String req="UPDATE articletable SET title=?, texte=?";
        try {
            PreparedStatement ste=con.prepareStatement(req);
            ste.setString(1,a.getTitle());
            ste.setString(2,a.getTexte());
            System.out.println("Article modifié");
        } catch (SQLException e)  {
            throw new RuntimeException(e);
        }
    }

    public boolean supprimerArticle(int id) {
        String req="DELETE FROM articletable WHERE id=?";
        try {
            PreparedStatement ste=con.prepareStatement(req);
            ste.setInt(1,id);
            ste.executeUpdate();
            System.out.println("Article Supprimer");
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return false;
    }

    public List<ArticleBase> afficherArticle() {
        List<ArticleBase> ArticleBase=new ArrayList<>();
        String req="SELECT * FROM articletable";
        try {
            Statement ste=con.createStatement();
            ResultSet rs= ste.executeQuery(req);
            while (rs.next()){
                ArticleBase a=new ArticleBase(rs.getInt("id"),rs.getString("titre"),rs.getString("texte"), rs.getDate("creationDate").toLocalDate(),rs.getInt("likes"),rs.getInt("dislikes"));
                ArticleBase.add(a);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ArticleBase;
    }
}
