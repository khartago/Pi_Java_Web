package tn.esprit.entities;

import java.sql.Date;

public class Production {

    private int id;
    private String nomPlant;
    private String variete;
    private int quantite;
    private Date datePlante;
    private String saison;

    // ✅ Constructor vide
    public Production() {
    }

    // ✅ Constructor sans id (pour INSERT)
    public Production(String nomPlant, String variete, int quantite, Date datePlante, String saison) {
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
    }

    // ✅ Constructor avec id (pour UPDATE / SELECT)
    public Production(int id, String nomPlant, String variete, int quantite, Date datePlante, String saison) {
        this.id = id;
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
    }

    // ✅ Getters / Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomPlant() {
        return nomPlant;
    }

    public void setNomPlant(String nomPlant) {
        this.nomPlant = nomPlant;
    }

    public String getVariete() {
        return variete;
    }

    public void setVariete(String variete) {
        this.variete = variete;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Date getDatePlante() {
        return datePlante;
    }

    public void setDatePlante(Date datePlante) {
        this.datePlante = datePlante;
    }

    public String getSaison() {
        return saison;
    }

    public void setSaison(String saison) {
        this.saison = saison;
    }

    @Override
    public String toString() {
        return "Production{" +
                "id=" + id +
                ", nomPlant='" + nomPlant + '\'' +
                ", variete='" + variete + '\'' +
                ", quantite=" + quantite +
                ", datePlante=" + datePlante +
                ", saison='" + saison + '\'' +
                '}';
    }
}
