package model;

import java.sql.Date;

/**
 * Entité production (récolte issue d'une plantation).
 */
public class ProductionPlante {

    private int idProduction;
    private float quantiteProduite;
    private Date dateRecolte;
    private String qualite;
    private String etat;

    public ProductionPlante() {
    }

    public ProductionPlante(float quantiteProduite, Date dateRecolte, String qualite, String etat) {
        this.quantiteProduite = quantiteProduite;
        this.dateRecolte = dateRecolte;
        this.qualite = qualite;
        this.etat = etat;
    }

    public ProductionPlante(int idProduction, float quantiteProduite, Date dateRecolte, String qualite, String etat) {
        this.idProduction = idProduction;
        this.quantiteProduite = quantiteProduite;
        this.dateRecolte = dateRecolte;
        this.qualite = qualite;
        this.etat = etat;
    }

    public int getIdProduction() { return idProduction; }
    public void setIdProduction(int idProduction) { this.idProduction = idProduction; }
    public float getQuantiteProduite() { return quantiteProduite; }
    public void setQuantiteProduite(float quantiteProduite) { this.quantiteProduite = quantiteProduite; }
    public Date getDateRecolte() { return dateRecolte; }
    public void setDateRecolte(Date dateRecolte) { this.dateRecolte = dateRecolte; }
    public String getQualite() { return qualite; }
    public void setQualite(String qualite) { this.qualite = qualite; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}
