package model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Produit {
    private final IntegerProperty idProduit = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final StringProperty unite = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateExpiration = new SimpleObjectProperty<>();

    // ✅ NOUVEAU : chemin image (local, resource ou URL)
    private final StringProperty imagePath = new SimpleStringProperty();
    private final DoubleProperty prixUnitaire = new SimpleDoubleProperty();



    public Produit() {}

    // ✅ constructeur complet (ajout imagePath)
    public Produit(int idProduit, String nom, int quantite, String unite, LocalDate dateExpiration, String imagePath) {
        this.idProduit.set(idProduit);
        this.nom.set(nom);
        this.quantite.set(quantite);
        this.unite.set(unite);
        this.dateExpiration.set(dateExpiration);
        this.imagePath.set(imagePath);
    }

    public int getIdProduit() {
        return idProduit.get();
    }

    public IntegerProperty idProduitProperty() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit.set(idProduit);
    }

    public String getNom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public int getQuantite() {
        return quantite.get();
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
    }

    public String getUnite() {
        return unite.get();
    }

    public StringProperty uniteProperty() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite.set(unite);
    }

    public LocalDate getDateExpiration() {
        return dateExpiration.get();
    }

    public ObjectProperty<LocalDate> dateExpirationProperty() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration.set(dateExpiration);
    }

    // ✅ GET/SET imagePath
    public String getImagePath() {
        return imagePath.get();
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    // ✅ GET/SET prixUnitaire
    public double getPrixUnitaire() {
        return prixUnitaire.get();
    }

    public DoubleProperty prixUnitaireProperty() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire.set(prixUnitaire);
    }

    @Override
    public String toString() {
        return nom.get();
    }
}