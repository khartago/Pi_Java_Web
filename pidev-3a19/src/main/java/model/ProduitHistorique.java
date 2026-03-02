package model;

import java.time.LocalDateTime;

/**
 * Represents a traceability event for a product:
 * creation, update, stock movement, consultation, etc.
 */
public class ProduitHistorique {

    private int idHistorique;
    private int idProduit;
    private String typeEvenement;
    private Integer quantiteAvant;
    private Integer quantiteApres;
    private LocalDateTime dateEvenement;
    private String commentaire;

    public int getIdHistorique() {
        return idHistorique;
    }

    public void setIdHistorique(int idHistorique) {
        this.idHistorique = idHistorique;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(String typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public Integer getQuantiteAvant() {
        return quantiteAvant;
    }

    public void setQuantiteAvant(Integer quantiteAvant) {
        this.quantiteAvant = quantiteAvant;
    }

    public Integer getQuantiteApres() {
        return quantiteApres;
    }

    public void setQuantiteApres(Integer quantiteApres) {
        this.quantiteApres = quantiteApres;
    }

    public LocalDateTime getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(LocalDateTime dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
