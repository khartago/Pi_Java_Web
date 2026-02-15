package tn.esprit.entities;

import java.sql.Date;

public class ProductionPlante {

    private int idProduction;
    private float quantiteProduite;
    private Date dateRecolte;
    private String qualite;
    private String etat;

    // ✅ Constructeur vide
    public ProductionPlante() {
    }

    // ✅ Constructeur sans id (INSERT)
    public ProductionPlante(float quantiteProduite, Date dateRecolte, String qualite, String etat) {
        this.quantiteProduite = quantiteProduite;
        this.dateRecolte = dateRecolte;
        this.qualite = qualite;
        this.etat = etat;
    }

    // ✅ Constructeur avec id (UPDATE / SELECT)
    public ProductionPlante(int idProduction, float quantiteProduite, Date dateRecolte, String qualite, String etat) {
        this.idProduction = idProduction;
        this.quantiteProduite = quantiteProduite;
        this.dateRecolte = dateRecolte;
        this.qualite = qualite;
        this.etat = etat;
    }

    // ===== GETTERS =====
    public int getIdProduction() {
        return idProduction;
    }

    public float getQuantiteProduite() {
        return quantiteProduite;
    }

    public Date getDateRecolte() {
        return dateRecolte;
    }

    public String getQualite() {
        return qualite;
    }

    public String getEtat() {
        return etat;
    }

    // ===== SETTERS =====
    public void setIdProduction(int idProduction) {
        this.idProduction = idProduction;
    }

    public void setQuantiteProduite(float quantiteProduite) {
        this.quantiteProduite = quantiteProduite;
    }

    public void setDateRecolte(Date dateRecolte) {
        this.dateRecolte = dateRecolte;
    }

    public void setQualite(String qualite) {
        this.qualite = qualite;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    @Override
    public String toString() {
        return "ProductionPlante{" +
                "idProduction=" + idProduction +
                ", quantiteProduite=" + quantiteProduite +
                ", dateRecolte=" + dateRecolte +
                ", qualite='" + qualite + '\'' +
                ", etat='" + etat + '\'' +
                '}';
    }
}
