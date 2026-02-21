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

    // 🔥 NEW FIELDS (Game System)
    private int stage = 1;
    private int waterCount = 0;
    private long lastWaterTime;
    private String status = "ALIVE";
    private double growthSpeed = 1.0;

    public Production() {}

    public Production(String nomPlant, String variete, int quantite, Date datePlante, String saison) {
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = "EN_ATTENTE";

        // default game values
        this.stage = 1;
        this.waterCount = 0;
        this.status = "ALIVE";
        this.growthSpeed = 1.0;
        this.lastWaterTime = System.currentTimeMillis();
    }

    public Production(int id, String nomPlant, String variete, int quantite,
                      Date datePlante, String saison, String etat) {

        this.id = id;
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = etat;
    }

    // ===== EXISTING GETTERS =====

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

    // ===== NEW GAME GETTERS =====

    public int getStage() { return stage; }
    public void setStage(int stage) { this.stage = stage; }

    public int getWaterCount() { return waterCount; }
    public void setWaterCount(int waterCount) { this.waterCount = waterCount; }

    public long getLastWaterTime() { return lastWaterTime; }
    public void setLastWaterTime(long lastWaterTime) { this.lastWaterTime = lastWaterTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getGrowthSpeed() { return growthSpeed; }
    public void setGrowthSpeed(double growthSpeed) { this.growthSpeed = growthSpeed; }
}