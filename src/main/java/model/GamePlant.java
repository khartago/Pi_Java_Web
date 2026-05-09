package model;

/**
 * Game entity for the front-office plant game.
 * Separate from Production (back-office plantation tracking).
 */
public class GamePlant {

    private int id;
    private String nomPlant;
    private int slotIndex;
    private int stage;
    private int waterCount;
    private String status;
    private double growthSpeed;
    private long lastWaterTime;

    public GamePlant() {
        this.stage = 1;
        this.waterCount = 0;
        this.status = "ALIVE";
        this.growthSpeed = 1.0;
        this.lastWaterTime = System.currentTimeMillis();
    }

    public GamePlant(String nomPlant, int slotIndex) {
        this();
        this.nomPlant = nomPlant;
        this.slotIndex = slotIndex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomPlant() { return nomPlant; }
    public void setNomPlant(String nomPlant) { this.nomPlant = nomPlant; }
    public int getSlotIndex() { return slotIndex; }
    public void setSlotIndex(int slotIndex) { this.slotIndex = slotIndex; }
    public int getStage() { return stage; }
    public void setStage(int stage) { this.stage = stage; }
    public int getWaterCount() { return waterCount; }
    public void setWaterCount(int waterCount) { this.waterCount = waterCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getGrowthSpeed() { return growthSpeed; }
    public void setGrowthSpeed(double growthSpeed) { this.growthSpeed = growthSpeed; }
    public long getLastWaterTime() { return lastWaterTime; }
    public void setLastWaterTime(long lastWaterTime) { this.lastWaterTime = lastWaterTime; }
}
