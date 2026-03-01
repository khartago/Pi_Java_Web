package model;

import java.sql.Date;

 /* Base d'Article, Blog, Commentaires*/

public class ArticleBase {

        private String id;
        private String title;
        private String texte;
        private Date creationDate;
        private int like;
        private int dislike;
        private boolean edit;


    public ArticleBase(String id, String title, String texte, Date creationDate, int like, int dislike, boolean edit) {
        this.id = id;
        this.title = title;
        this.texte = texte;
        this.creationDate = creationDate;
        this.like = 0;
        this.dislike = 0;
        this.edit = false;

    }

    public String getId() {
            return id;
    }
    public void setId(String id) {
            this.id = id;
    }
    public String getTitle() {
            return title;
    }
    public void setTitle(String title) {
            this.title = title;
    }
    public String getTexte() {
            return texte;
    }
    public void setTexte(String texte) {
            this.texte = texte;
    }
    public Date getCreationDate() {
            return creationDate;
    }
    public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
    }
    public int getLike() {
            return like;
    }
    public void setLike(int like) {
            this.like = like;
    }
    public int getDislike() {
            return dislike;
    }
    public void setDislike(int dislike) {
            this.dislike = dislike;
    }
    public void setEdit() {
            this.edit=edit;
    }
    public boolean getEdit() {
            return edit;
    }

}
