package Entites;

import java.time.LocalDateTime;

public class Probleme {

    private int id;
    private String type;
    private String description;
    private String gravite;
    private LocalDateTime dateDetection;
    private String etat;
    private String photos;

    public Probleme() {
    }

    public Probleme(int id, String type, String description, String gravite, LocalDateTime dateDetection, String etat, String photos) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.gravite = gravite;
        this.dateDetection = dateDetection;
        this.etat = etat;
        this.photos = photos;
    }

    public Probleme(String type, String description, String gravite, LocalDateTime dateDetection, String etat, String photos) {
        this.type = type;
        this.description = description;
        this.gravite = gravite;
        this.dateDetection = dateDetection;
        this.etat = etat;
        this.photos = photos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGravite() {
        return gravite;
    }

    public void setGravite(String gravite) {
        this.gravite = gravite;
    }

    public LocalDateTime getDateDetection() {
        return dateDetection;
    }

    public void setDateDetection(LocalDateTime dateDetection) {
        this.dateDetection = dateDetection;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "Probleme{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", gravite='" + gravite + '\'' +
                ", dateDetection=" + dateDetection +
                ", etat='" + etat + '\'' +
                ", photos='" + photos + '\'' +
                '}';
    }
}

