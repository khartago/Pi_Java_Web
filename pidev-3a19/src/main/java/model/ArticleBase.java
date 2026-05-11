package model;

import java.time.LocalDate;

/* Base d'Article, Blog, Commentaires*/

public class ArticleBase {

        private int id;
        private String title;
        private String texte;
        private LocalDate creationDate;
        private int like;
        private int dislike;

    /** Nom affiché (ex. auteur via blog → utilisateur) ; non persisté dans article seul. */
    private String authorName = "";

public ArticleBase() {}
    public ArticleBase(int id, String title, String texte, LocalDate creationDate, int like, int dislike) {
        this.id = id;
        this.title = title;
        this.texte = texte;
        this.creationDate = creationDate;
        this.like = like;
        this.dislike = dislike;
    }

    public int getId() {
            return id;
    }
    public void setId(int id) {
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
    public LocalDate getCreationDate() {
            return creationDate;
    }
    public void setCreationDate(LocalDate creationDate) {
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

    public String getAuthorName() {
        return authorName != null ? authorName : "";
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName != null ? authorName : "";
    }

}
