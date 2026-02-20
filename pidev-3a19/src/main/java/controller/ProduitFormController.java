package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;

import java.time.LocalDate;

/**
 * Controller for the product form. Handles both creation and update of
 * {@link Produit} instances. The DAO is injected by the caller.
 */
public class ProduitFormController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextField uniteField;
    @FXML
    private DatePicker dateExpirationPicker;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private ProduitDAO produitDAO;
    private Produit produit;

    public void setProduitDAO(ProduitDAO dao) {
        this.produitDAO = dao;
    }

    public void setProduit(Produit produit) {
        this.produit = (produit != null) ? produit : new Produit();
        if (produit != null) {
            nomField.setText(produit.getNom());
            quantiteField.setText(String.valueOf(produit.getQuantite()));
            uniteField.setText(produit.getUnite());
            if (produit.getDateExpiration() != null) {
                dateExpirationPicker.setValue(produit.getDateExpiration());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        produit.setNom(nomField.getText().trim());
        produit.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
        produit.setUnite(uniteField.getText().trim());
        produit.setDateExpiration(dateExpirationPicker.getValue());

        boolean success = (produit.getIdProduit() == 0) ?
                produitDAO.insert(produit) : produitDAO.update(produit);

        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'enregistrer le produit");
            alert.setContentText("Une erreur s'est produite lors de l'enregistrement.");
            alert.showAndWait();
        } else {
            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // ====== Nom ======
        String nom = nomField.getText();
        if (nom == null || nom.trim().isEmpty()) {
            errors.append("- Le nom du produit est requis.\n");
        } else {
            nom = nom.trim();
            if (nom.length() < 2 || nom.length() > 50) {
                errors.append("- Le nom doit contenir entre 2 et 50 caractères.\n");
            }
            if (!nom.matches("[a-zA-ZÀ-ÿ0-9 ]+")) {
                errors.append("- Le nom ne doit contenir que des lettres, chiffres et espaces.\n");
            }
        }

        // ====== Quantité ======
        String quantiteText = quantiteField.getText();
        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            errors.append("- La quantité est requise.\n");
        } else {
            try {
                int qty = Integer.parseInt(quantiteText.trim());
                if (qty < 0) errors.append("- La quantité doit être positive.\n");
            } catch (NumberFormatException e) {
                errors.append("- La quantité doit être un nombre entier.\n");
            }
        }

        // ====== Unité ======
        String unite = uniteField.getText();
        if (unite == null || unite.trim().isEmpty()) {
            errors.append("- L'unité est requise.\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
