package controller;

import Services.PromotionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.Promotion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulaire création / modification d'une promotion.
 * Structure calquée sur la table Symfony.
 */
public class PromotionFormController {

    // ------------------------------------------------------------------ //
    //  FXML bindings
    // ------------------------------------------------------------------ //

    @FXML private TextField             fieldNom;
    @FXML private TextArea              fieldDescription;
    @FXML private ComboBox<String>      comboType;
    @FXML private TextField             fieldValeur;
    @FXML private Spinner<Integer>      spinnerQuantiteMin;
    @FXML private DatePicker            dateDebut;
    @FXML private DatePicker            dateFin;
    @FXML private CheckBox              checkCumulable;
    @FXML private CheckBox              checkActif;
    @FXML private ComboBox<ProduitItem> comboProduit;
    @FXML private Label                 labelValeur;
    @FXML private Label                 labelError;

    // ------------------------------------------------------------------ //
    //  State
    // ------------------------------------------------------------------ //

    private Promotion promotion;
    private Runnable  onSaved;

    private final PromotionService promotionService = new PromotionService();
    private final ProduitDAO       produitDAO       = new ProduitDAO();

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //

    @FXML
    private void initialize() {
        // Types — valeurs exactes BDD Symfony
        comboType.setItems(FXCollections.observableArrayList(
                "pourcentage", "montant_fixe"));
        comboType.setValue("pourcentage");
        comboType.setOnAction(e -> updateLabelValeur());

        // Spinner quantité min
        spinnerQuantiteMin.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        spinnerQuantiteMin.setEditable(true);

        // CheckBox actif coché par défaut
        if (checkActif != null) checkActif.setSelected(true);

        // Produits
        List<ProduitItem> items = new ArrayList<>();
        items.add(new ProduitItem(0, "Tous les produits (global)"));
        for (Produit p : produitDAO.getAll()) {
            items.add(new ProduitItem(p.getIdProduit(), p.getNom()));
        }
        comboProduit.setItems(FXCollections.observableArrayList(items));
        comboProduit.setValue(items.get(0));

        // Masquer erreur
        hideError();
        updateLabelValeur();
    }

    private void updateLabelValeur() {
        if (labelValeur == null) return;
        labelValeur.setText("montant_fixe".equals(comboType.getValue())
                ? "Montant fixe (TND) *" : "Remise (%) *");
        if (fieldValeur != null)
            fieldValeur.setPromptText("montant_fixe".equals(comboType.getValue())
                    ? "ex. 5.00" : "ex. 20");
    }

    // ------------------------------------------------------------------ //
    //  API publique
    // ------------------------------------------------------------------ //

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
        if (promotion != null) populate();
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // ------------------------------------------------------------------ //
    //  Actions FXML
    // ------------------------------------------------------------------ //

    @FXML
    private void handleSave() {
        hideError();
        if (!validate()) return;

        Promotion p = (promotion != null) ? promotion : new Promotion();
        p.setNom(fieldNom.getText().trim());
        p.setDescription(fieldDescription != null ? fieldDescription.getText() : null);
        p.setTypeReduction(comboType.getValue());
        p.setValeurReduction(parseDouble(fieldValeur.getText()));
        p.setQuantiteMin(safeSpinnerValue(spinnerQuantiteMin, 1));
        p.setDateDebut(dateDebut.getValue());
        p.setDateFin(dateFin.getValue());
        p.setCumulable(checkCumulable != null && checkCumulable.isSelected());
        p.setActif(checkActif == null || checkActif.isSelected());
        p.setIdProduit(comboProduit.getValue() == null ? 0 : comboProduit.getValue().id());

        String erreur = promotionService.saveWithError(p);
        if (erreur == null) {
            if (onSaved != null) onSaved.run();
            close();
        } else {
            showError("Erreur BDD : " + erreur);
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void populate() {
        fieldNom.setText(nvl(promotion.getNom()));
        if (fieldDescription != null)
            fieldDescription.setText(nvl(promotion.getDescription()));

        String type = promotion.getTypeReduction();
        comboType.setValue(type != null ? type : Promotion.TYPE_POURCENTAGE);

        fieldValeur.setText(promotion.getValeurReduction() > 0
                ? String.valueOf(promotion.getValeurReduction()) : "");

        spinnerQuantiteMin.getValueFactory().setValue(
                Math.max(1, promotion.getQuantiteMin()));

        dateDebut.setValue(promotion.getDateDebut());
        dateFin.setValue(promotion.getDateFin());

        if (checkCumulable != null) checkCumulable.setSelected(promotion.isCumulable());
        if (checkActif != null)     checkActif.setSelected(promotion.isActif());

        int idProduit = promotion.getIdProduit();
        comboProduit.getItems().stream()
                .filter(item -> item.id() == idProduit)
                .findFirst()
                .ifPresent(comboProduit::setValue);

        updateLabelValeur();
    }

    private boolean validate() {
        if (fieldNom.getText() == null || fieldNom.getText().isBlank()) {
            showError("Le nom est obligatoire.");
            return false;
        }
        String valText = fieldValeur.getText();
        if (valText == null || valText.isBlank()) {
            showError("La valeur de la remise est obligatoire.");
            return false;
        }
        try {
            double v = Double.parseDouble(valText.trim());
            if (v <= 0) { showError("La valeur doit être > 0."); return false; }
            if ("pourcentage".equals(comboType.getValue()) && v > 100) {
                showError("Un pourcentage ne peut pas dépasser 100.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("La valeur doit être un nombre (ex. 20 ou 5.50).");
            return false;
        }
        LocalDate debut = dateDebut.getValue();
        LocalDate fin   = dateFin.getValue();
        if (debut != null && fin != null && fin.isBefore(debut)) {
            showError("La date de fin doit être après la date de début.");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        if (labelError != null) {
            labelError.setText(msg);
            labelError.setVisible(true);
            labelError.setManaged(true);
        } else {
            new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
        }
    }

    private void hideError() {
        if (labelError != null) {
            labelError.setVisible(false);
            labelError.setManaged(false);
        }
    }

    private void close() {
        ((Stage) fieldNom.getScene().getWindow()).close();
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static int safeSpinnerValue(Spinner<Integer> sp, int def) {
        try {
            String t = sp.getEditor().getText();
            return (t != null && !t.isBlank()) ? Integer.parseInt(t.trim())
                    : (sp.getValue() != null ? sp.getValue() : def);
        } catch (NumberFormatException e) { return def; }
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    // ------------------------------------------------------------------ //
    //  Record interne
    // ------------------------------------------------------------------ //

    public record ProduitItem(int id, String nom) {
        @Override public String toString() { return nom; }
    }
}
