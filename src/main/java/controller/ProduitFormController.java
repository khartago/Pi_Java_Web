package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.ProduitHistorique;
import model.ProduitHistoriqueDAO;
import service.QrCodeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ProduitFormController {

    @FXML private TextField nomField;
    @FXML private TextField quantiteField;
    @FXML private TextField uniteField;
    @FXML private DatePicker dateExpirationPicker;
    @FXML private TextField imagePathField;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ProduitDAO produitDAO;
    private Produit produit;
    private final ProduitHistoriqueDAO historiqueDAO = new ProduitHistoriqueDAO();

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

            // ✅ NOUVEAU : afficher l'image en mode modification
            imagePathField.setText(produit.getImagePath());
        } else {
            imagePathField.clear();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        boolean isNew = (produit.getIdProduit() == 0);
        Integer quantiteAvant = isNew ? null : produit.getQuantite();

        produit.setNom(nomField.getText().trim());
        produit.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
        produit.setUnite(uniteField.getText().trim());
        produit.setDateExpiration(dateExpirationPicker.getValue());

        // ✅ NOUVEAU : sauvegarder imagePath
        produit.setImagePath(imagePathField.getText() == null ? null : imagePathField.getText().trim());

        boolean success = isNew
                ? produitDAO.insert(produit)
                : produitDAO.update(produit);

        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'enregistrer le produit");
            alert.setContentText("Une erreur s'est produite lors de l'enregistrement.");
            alert.showAndWait();
            return;
        }

        // ✅ Génération du QR code (best effort)
        try {
            Path qrPath = QrCodeService.generateProduitQr(produit.getIdProduit());
            System.out.println("QR code généré pour le produit " + produit.getIdProduit() + " : " + qrPath);
        } catch (IOException e) {
            e.printStackTrace();
            // On ne bloque pas l'enregistrement pour une erreur de QR
        }

        // ✅ Traçabilité : enregistrer l'événement
        ProduitHistorique h = new ProduitHistorique();
        h.setIdProduit(produit.getIdProduit());
        h.setTypeEvenement(isNew ? "CREATION" : "MISE_A_JOUR");
        h.setQuantiteAvant(quantiteAvant);
        h.setQuantiteApres(produit.getQuantite());
        h.setCommentaire(isNew ? "Création du produit" : "Mise à jour du produit");
        historiqueDAO.insertEvent(h);

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // ✅ NOUVEAU : bouton "Choisir" (FileChooser)
    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(saveButton.getScene().getWindow());
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
        }
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

        // (optionnel) validation image : si rempli, vérifier extension
        String img = imagePathField.getText();
        if (img != null && !img.trim().isEmpty()) {
            String lower = img.trim().toLowerCase();
            if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
                errors.append("- L'image doit être au format png/jpg/jpeg.\n");
            }
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