package model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Represents a product stocked in the inventory. Each product has an identifier, a name,
 * a quantity, a unit of measurement and an expiration date. Products can have many
 * associated pieces of equipment (materiels). The relationship is handled on the
 * {@link Materiel} side via a foreign key.
 */
public class Produit {
    private final IntegerProperty idProduit = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final StringProperty unite = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateExpiration = new SimpleObjectProperty<>();

    public Produit() {
    }

    public Produit(int idProduit, String nom, int quantite, String unite, LocalDate dateExpiration) {
        this.idProduit.set(idProduit);
        this.nom.set(nom);
        this.quantite.set(quantite);
        this.unite.set(unite);
        this.dateExpiration.set(dateExpiration);
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

    @Override
    public String toString() {
        return nom.get();
    }
}