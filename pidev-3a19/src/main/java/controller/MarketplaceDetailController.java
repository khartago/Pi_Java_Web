package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Produit;

import java.io.IOException;
import java.time.LocalDate;

public class MarketplaceDetailController {

    @FXML private Label nomLabel;
    @FXML private Label stockLabel;
    @FXML private Label uniteLabel;
    @FXML private Label expLabel;
    @FXML private Label badgeLabel;

    private Produit produit;

    public void setProduit(Produit produit) {
        this.produit = produit;
        refresh();
    }

    private void refresh() {
        nomLabel.setText(produit.getNom());
        stockLabel.setText("Quantité : " + produit.getQuantite());
        uniteLabel.setText("Unité : " + produit.getUnite());
        expLabel.setText("Expiration : " + (produit.getDateExpiration() == null ? "-" : produit.getDateExpiration().toString()));

        badgeLabel.setText("");
        badgeLabel.setStyle("-fx-background-color: transparent;");

        if (produit.getDateExpiration() != null) {
            LocalDate today = LocalDate.now();
            if (!produit.getDateExpiration().isBefore(today) && !produit.getDateExpiration().isAfter(today.plusDays(7))) {
                badgeLabel.setText("Expire bientôt");
                badgeLabel.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 14;");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/marketplace.fxml"));
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Marketplace - Catalogue");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
