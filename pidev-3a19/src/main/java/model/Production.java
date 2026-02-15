package model;

import java.sql.Date;

/** Entite plantation (production en attente ou completee). */
public class Production {

    private int id;
    private String nomPlant;
    private String variete;
    private int quantite;
    private Date datePlante;
    private String saison;
    private String etat;

    public Production() {}

    public Production(String nomPlant, String variete, int quantite, Date datePlante, String saison) {
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = "EN_ATTENTE";
    }

    public Production(int id, String nomPlant, String variete, int quantite, Date datePlante, String saison, String etat) {
        this.id = id;
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = etat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomPlant() { return nomPlant; }
    public void setNomPlant(String nomPlant) { this.nomPlant = nomPlant; }
    public String getVariete() { return variete; }
    public void setVariete(String variete) { this.variete = variete; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public Date getDatePlante() { return datePlante; }
    public void setDatePlante(Date datePlante) { this.datePlante = datePlante; }
    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}
