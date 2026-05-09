package model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Represents a piece of equipment (materiel) that is associated with a product. Each
 * materiel belongs to exactly one product, identified by {@code idProduit}. The
 * materiel is described by a name, its state (e.g. new, good, used), the purchase
 * date and its cost.
 */
public class Materiel {
    private final IntegerProperty idMateriel = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty etat = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateAchat = new SimpleObjectProperty<>();
    private final DoubleProperty cout = new SimpleDoubleProperty();
    private final IntegerProperty idProduit = new SimpleIntegerProperty();

    public Materiel() {
    }

    public Materiel(int idMateriel, String nom, String etat, LocalDate dateAchat, double cout, int idProduit) {
        this.idMateriel.set(idMateriel);
        this.nom.set(nom);
        this.etat.set(etat);
        this.dateAchat.set(dateAchat);
        this.cout.set(cout);
        this.idProduit.set(idProduit);
    }

    public int getIdMateriel() {
        return idMateriel.get();
    }

    public IntegerProperty idMaterielProperty() {
        return idMateriel;
    }

    public void setIdMateriel(int idMateriel) {
        this.idMateriel.set(idMateriel);
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

    public String getEtat() {
        return etat.get();
    }

    public StringProperty etatProperty() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat.set(etat);
    }

    public LocalDate getDateAchat() {
        return dateAchat.get();
    }

    public ObjectProperty<LocalDate> dateAchatProperty() {
        return dateAchat;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat.set(dateAchat);
    }

    public double getCout() {
        return cout.get();
    }

    public DoubleProperty coutProperty() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout.set(cout);
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

    @Override
    public String toString() {
        return nom.get();
    }
}
