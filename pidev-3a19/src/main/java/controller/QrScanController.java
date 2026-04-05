package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.ProduitHistorique;
import model.ProduitHistoriqueDAO;
import Services.QrCodeService;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

public class QrScanController {

    @FXML private Label selectedFileLabel;
    @FXML private Label resultLabel;

    @FXML private Label idProduitLabel;
    @FXML private Label nomProduitLabel;
    @FXML private Label expirationLabel;
    @FXML private ImageView produitImageView;

    @FXML private TableView<ProduitHistorique> historiqueTable;
    @FXML private TableColumn<ProduitHistorique, String> typeCol;
    @FXML private TableColumn<ProduitHistorique, String> dateCol;

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final ProduitHistoriqueDAO historiqueDAO = new ProduitHistoriqueDAO();

    @FXML
    private void initialize() {
        typeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("typeEvenement"));
        dateCol.setCellValueFactory(cell -> {
            if (cell.getValue().getDateEvenement() == null) return null;
            return new javafx.beans.property.SimpleStringProperty(cell.getValue().getDateEvenement().toString());
        });
        selectedFileLabel.setText("Aucun fichier sélectionné");
        resultLabel.setText("");
        produitImageView.setImage(loadProductImage(null));
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image de QR code");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File file = fc.showOpenDialog(selectedFileLabel.getScene().getWindow());
        if (file == null) return;

        selectedFileLabel.setText(file.getAbsolutePath());
        try {
            String text = QrCodeService.decodeFromFile(file);
            if (text == null || !text.startsWith("PROD:")) {
                resultLabel.setText("QR non reconnu ou format invalide. Attendu: PROD:{idProduit}");
                clearProduit();
                return;
            }
            String idStr = text.substring("PROD:".length()).trim();
            int id = Integer.parseInt(idStr);

            Produit p = produitDAO.getById(id);
            if (p == null) {
                resultLabel.setText("Aucun produit trouvé pour l'ID " + id);
                clearProduit();
                return;
            }
            resultLabel.setText("QR valide pour le produit ID " + id);
            fillProduit(p);
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Erreur lors du décodage du QR code.");
            clearProduit();
        }
    }

    @FXML
    private void handleOpenQrFolder() {
        try {
            File dir = QrCodeService.getQrDirectory().toFile();
            if (!dir.exists()) dir.mkdirs();
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(dir);
            } else {
                resultLabel.setText("Ouverture du dossier non supportée sur ce système.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Impossible d'ouvrir le dossier des QR codes.");
        }
    }

    private void fillProduit(Produit p) {
        idProduitLabel.setText(String.valueOf(p.getIdProduit()));
        nomProduitLabel.setText(p.getNom());
        expirationLabel.setText(p.getDateExpiration() == null ? "-" : p.getDateExpiration().toString());
        produitImageView.setImage(loadProductImage(p.getImagePath()));
        historiqueTable.setItems(FXCollections.observableArrayList(historiqueDAO.getByProduit(p.getIdProduit())));
    }

    private void clearProduit() {
        idProduitLabel.setText("-");
        nomProduitLabel.setText("-");
        expirationLabel.setText("-");
        produitImageView.setImage(loadProductImage(null));
        historiqueTable.setItems(FXCollections.observableArrayList());
    }

    private Image loadProductImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) return new Image(file.toURI().toString(), true);
                if (path.startsWith("http://") || path.startsWith("https://")) return new Image(path, true);
                if (path.startsWith("/")) {
                    var is = getClass().getResourceAsStream(path);
                    if (is != null) return new Image(is);
                }
            }
        } catch (Exception ignored) {}
        InputStream def = getClass().getResourceAsStream("/images/products/default.png");
        return def != null ? new Image(def) : null;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) selectedFileLabel.getScene().getWindow();
        stage.close();
    }
}
