package model;

import java.sql.Date;

public class CommentBase {
    private int id;
    private int idArticle;
    private Date dateComment;
    private int idUser;
    private String Text;

    public CommentBase() {
    }

    public CommentBase(int idArticle, Date dateComment, int idUser, String Text) {}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getIdArticle() {
        return idArticle;
    }
    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }
    public Date getDateComment() {
        return dateComment;
    }
    public void setDateComment(Date dateComment) {
        this.dateComment = dateComment;
    }
    public int getIdUser() {
        return idUser;
    }
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    public String getText() {
        return Text;
    }
    public void setText(String text) {
        Text = text;
    }

}
