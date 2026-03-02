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

    // ===== GAME SYSTEM FIELDS =====
    private int stage;
    private int waterCount;
    private long lastWaterTime;
    private String status;
    private double growthSpeed;
    private int slotIndex;   // 🔥 IMPORTANT

    // ===== EMPTY CONSTRUCTOR =====
    public Production() {
    }

    // ===== CONSTRUCTOR FOR BACK-OFFICE (no slot) =====
    public Production(String nomPlant, String variete, int quantite,
                      Date datePlante, String saison) {
        this(nomPlant, variete, quantite, datePlante, saison, 0);
    }

    // ===== CONSTRUCTOR WHEN ADDING NEW PLANT (with slot for game) =====
    public Production(String nomPlant, String variete, int quantite,
                      Date datePlante, String saison, int slotIndex) {

        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = "EN_ATTENTE";

        // Game defaults
        this.stage = 1;
        this.waterCount = 0;
        this.status = "ALIVE";
        this.growthSpeed = 1.0;
        this.lastWaterTime = System.currentTimeMillis();
        this.slotIndex = slotIndex;
    }

    // ===== FULL CONSTRUCTOR (LOAD FROM DATABASE) =====
    public Production(int id, String nomPlant, String variete, int quantite,
                      Date datePlante, String saison, String etat,
                      int stage, int waterCount, long lastWaterTime,
                      String status, double growthSpeed, int slotIndex) {

        this.id = id;
        this.nomPlant = nomPlant;
        this.variete = variete;
        this.quantite = quantite;
        this.datePlante = datePlante;
        this.saison = saison;
        this.etat = etat;

        this.stage = stage;
        this.waterCount = waterCount;
        this.lastWaterTime = lastWaterTime;
        this.status = status;
        this.growthSpeed = growthSpeed;
        this.slotIndex = slotIndex;
    }

    // ===== CONSTRUCTOR FOR BACK-OFFICE MODIFIER (no game fields) =====
    public Production(int id, String nomPlant, String variete, int quantite,
                      Date datePlante, String saison, String etat) {
        this(id, nomPlant, variete, quantite, datePlante, saison, etat,
             1, 0, System.currentTimeMillis(), "ALIVE", 1.0, 0);
    }

    // ===== GETTERS & SETTERS =====

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

    public int getSlotIndex() { return slotIndex; }
    public void setSlotIndex(int slotIndex) { this.slotIndex = slotIndex; }

    // runtime only (not saved in DB)
    private long nextWaterDeadline;   // time when 60s ends
    private boolean waitingForWater = false;
    public long getNextWaterDeadline() { return nextWaterDeadline; }
    public void setNextWaterDeadline(long nextWaterDeadline) { this.nextWaterDeadline = nextWaterDeadline; }

    public boolean isWaitingForWater() { return waitingForWater; }
    public void setWaitingForWater(boolean waitingForWater) { this.waitingForWater = waitingForWater; }

}