package model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Entité Promotion — structure identique à la table Symfony.
 *
 * Colonnes : idPromotion, nom, description, typeReduction,
 *            valeurReduction, dateDebut, dateFin, quantiteMin,
 *            cumulable, actif, idProduit (NULL = global)
 *
 * typeReduction : "pourcentage" | "montant_fixe"
 */
public class Promotion {

    // Valeurs exactes stockées en BDD (minuscules, comme Symfony)
    public static final String TYPE_POURCENTAGE  = "pourcentage";
    public static final String TYPE_MONTANT_FIXE = "montant_fixe";

    // ------------------------------------------------------------------ //
    //  Propriétés JavaFX
    // ------------------------------------------------------------------ //

    private final IntegerProperty idPromotion     = new SimpleIntegerProperty();
    private final StringProperty  nom             = new SimpleStringProperty();
    private final StringProperty  description     = new SimpleStringProperty();
    private final StringProperty  typeReduction   = new SimpleStringProperty(TYPE_POURCENTAGE);
    private final DoubleProperty  valeurReduction = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> dateDebut = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateFin   = new SimpleObjectProperty<>();
    private final IntegerProperty quantiteMin     = new SimpleIntegerProperty(1);
    private final BooleanProperty cumulable       = new SimpleBooleanProperty(false);
    private final BooleanProperty actif           = new SimpleBooleanProperty(true);
    // NULL en BDD = 0 ici (global, tous les produits)
    private final IntegerProperty idProduit       = new SimpleIntegerProperty(0);

    // ------------------------------------------------------------------ //
    //  Constructeurs
    // ------------------------------------------------------------------ //

    public Promotion() {}

    // ------------------------------------------------------------------ //
    //  Logique métier — applyTo() miroir du service Symfony
    // ------------------------------------------------------------------ //

    /**
     * Calcule le prix unitaire après application de la promotion.
     *
     * @param basePrice  prix unitaire de base
     * @param quantity   quantité commandée
     * @return prix unitaire effectif (jamais négatif)
     */
    public double applyTo(double basePrice, int quantity) {
        if (basePrice <= 0 || quantity <= 0) return basePrice;
        if (!isActif()) return basePrice;

        return switch (getTypeReduction()) {
            case TYPE_POURCENTAGE -> {
                double pct = Math.min(100.0, Math.max(0.0, getValeurReduction()));
                yield Math.max(0.0, basePrice * (1.0 - pct / 100.0));
            }
            case TYPE_MONTANT_FIXE -> Math.max(0.0, basePrice - getValeurReduction());
            default -> basePrice;
        };
    }

    /** Vérifie si la promotion est active à la date donnée. */
    public boolean isActiveOn(LocalDate date) {
        if (date == null || !isActif()) return false;
        LocalDate debut = getDateDebut();
        LocalDate fin   = getDateFin();
        if (debut != null && date.isBefore(debut)) return false;
        if (fin   != null && date.isAfter(fin))    return false;
        return true;
    }

    /** Active aujourd'hui ? */
    public boolean isActiveNow() {
        return isActiveOn(LocalDate.now());
    }

    /**
     * Libellé lisible pour l'UI.
     * Ex. : "−20 %", "−5.00 TND"
     */
    public String getLabel() {
        if (TYPE_MONTANT_FIXE.equals(getTypeReduction())) {
            return String.format("−%.2f TND", getValeurReduction());
        }
        return String.format("−%.0f %%", getValeurReduction());
    }

    // ------------------------------------------------------------------ //
    //  Getters / Setters JavaFX
    // ------------------------------------------------------------------ //

    public int getIdPromotion()                       { return idPromotion.get(); }
    public IntegerProperty idPromotionProperty()      { return idPromotion; }
    public void setIdPromotion(int v)                 { idPromotion.set(v); }

    public String getNom()                            { return nom.get(); }
    public StringProperty nomProperty()               { return nom; }
    public void setNom(String v)                      { nom.set(v); }

    public String getDescription()                    { return description.get(); }
    public StringProperty descriptionProperty()       { return description; }
    public void setDescription(String v)              { description.set(v); }

    public String getTypeReduction()                  { return typeReduction.get(); }
    public StringProperty typeReductionProperty()     { return typeReduction; }
    public void setTypeReduction(String v)            { typeReduction.set(v != null ? v : TYPE_POURCENTAGE); }

    public double getValeurReduction()                { return valeurReduction.get(); }
    public DoubleProperty valeurReductionProperty()   { return valeurReduction; }
    public void setValeurReduction(double v)          { valeurReduction.set(v); }

    public LocalDate getDateDebut()                   { return dateDebut.get(); }
    public ObjectProperty<LocalDate> dateDebutProperty() { return dateDebut; }
    public void setDateDebut(LocalDate v)             { dateDebut.set(v); }

    public LocalDate getDateFin()                     { return dateFin.get(); }
    public ObjectProperty<LocalDate> dateFinProperty() { return dateFin; }
    public void setDateFin(LocalDate v)               { dateFin.set(v); }

    public int getQuantiteMin()                       { return quantiteMin.get(); }
    public IntegerProperty quantiteMinProperty()      { return quantiteMin; }
    public void setQuantiteMin(int v)                 { quantiteMin.set(Math.max(1, v)); }

    public boolean isCumulable()                      { return cumulable.get(); }
    public BooleanProperty cumulableProperty()        { return cumulable; }
    public void setCumulable(boolean v)               { cumulable.set(v); }

    public boolean isActif()                          { return actif.get(); }
    public BooleanProperty actifProperty()            { return actif; }
    public void setActif(boolean v)                   { actif.set(v); }

    public int getIdProduit()                         { return idProduit.get(); }
    public IntegerProperty idProduitProperty()        { return idProduit; }
    public void setIdProduit(int v)                   { idProduit.set(v); }

    @Override
    public String toString() {
        return getNom() + " (" + getLabel() + ")";
    }
}
