package controller;

import Services.PromotionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.FavorisDAO;
import model.Produit;
import model.Promotion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class MesFavorisController {

    @FXML private FlowPane cardsPane;
    @FXML private Label    titleLabel;
    @FXML private Label    emptyLabel;
    @FXML private Button   backButton;
    @FXML private Button   clearButton;

    private final FavorisDAO       favorisDAO       = new FavorisDAO();
    private final PromotionService promotionService = new PromotionService();

    @FXML
    private void initialize() {
        loadFavoris();
    }

    // ------------------------------------------------------------------ //
    //  Chargement
    // ------------------------------------------------------------------ //

    private void loadFavoris() {
        List<Produit> favoris = favorisDAO.getAllFavoris();

        if (titleLabel != null)
            titleLabel.setText("❤️ Mes Favoris (" + favoris.size() + ")");

        if (favoris.isEmpty()) {
            if (emptyLabel != null) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                emptyLabel.setText("Aucun favori pour le moment.\nExplorez le Marketplace et ajoutez des produits !");
            }
            if (cardsPane != null) { cardsPane.setVisible(false); cardsPane.setManaged(false); }
            if (clearButton != null) clearButton.setDisable(true);
        } else {
            if (emptyLabel != null) { emptyLabel.setVisible(false); emptyLabel.setManaged(false); }
            if (cardsPane != null) {
                cardsPane.setVisible(true);
                cardsPane.setManaged(true);
                renderCards(favoris);
            }
            if (clearButton != null) clearButton.setDisable(false);
        }
    }

    private void renderCards(List<Produit> produits) {
        cardsPane.getChildren().clear();
        for (Produit p : produits) {
            cardsPane.getChildren().add(createCard(p));
        }
    }

    // ------------------------------------------------------------------ //
    //  Carte — style identique marketplace
    // ------------------------------------------------------------------ //

    private VBox createCard(Produit p) {
        Promotion bestPromo = promotionService.getBestPromotionForProduct(p);

        VBox card = new VBox(0);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.getStyleClass().add("mk-card");

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadProductImage(p.getImagePath()));

        // Catégorie
        Label catLabel = new Label(resolveCategory(p).toUpperCase());
        catLabel.getStyleClass().add("mk-category");

        // Nom
        Label nomLabel = new Label(p.getNom());
        nomLabel.getStyleClass().add("mk-product-name");
        nomLabel.setMaxWidth(196);
        nomLabel.setWrapText(true);

        // Quantité
        Label qteLabel = new Label(p.getQuantite() + " "
                + (p.getUnite() != null ? p.getUnite() : ""));
        qteLabel.getStyleClass().add("mk-meta");

        // Expiration
        Label expLabel = new Label();
        if (p.getDateExpiration() != null) {
            LocalDate today = LocalDate.now();
            if (p.getDateExpiration().isBefore(today)) {
                expLabel.setText("⛔ Expiré");
                expLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-padding: 1 12;");
            } else if (!p.getDateExpiration().isAfter(today.plusDays(7))) {
                expLabel.setText("⚠ Expire bientôt");
                expLabel.setStyle("-fx-text-fill: #D97706; -fx-font-size: 12px; -fx-padding: 1 12;");
            }
        }

        // Prix + promo
        VBox prixBox = buildPrixBox(p, bestPromo);

        // Boutons actions
        Button ficheBtn = new Button("Voir fiche produit");
        ficheBtn.getStyleClass().add("mk-btn-detail");
        ficheBtn.setMaxWidth(Double.MAX_VALUE);
        ficheBtn.setOnAction(e -> openFiche(p, bestPromo));

        Button removeBtn = new Button("❤ Retirer des favoris");
        removeBtn.getStyleClass().add("mk-btn-fav-active");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setOnAction(e -> handleRemove(p));

        card.getChildren().addAll(imageView, catLabel, nomLabel, qteLabel);
        if (expLabel.getText() != null && !expLabel.getText().isBlank())
            card.getChildren().add(expLabel);
        card.getChildren().addAll(prixBox, ficheBtn, removeBtn);
        return card;
    }

    private VBox buildPrixBox(Produit p, Promotion promo) {
        VBox box = new VBox(2);
        box.setStyle("-fx-padding: 4 12 4 12;");
        if (p.getPrixUnitaire() > 0) {
            if (promo != null) {
                double promoPrice = promo.applyTo(p.getPrixUnitaire(), 1);
                Label original = new Label(String.format("%.2f TND", p.getPrixUnitaire()));
                original.getStyleClass().add("mk-price-original");
                Label discounted = new Label(String.format("%.2f TND", promoPrice));
                discounted.getStyleClass().add("mk-price-promo");
                Label badge = new Label("PROMO CHOISIE: " + promo.getLabel());
                badge.getStyleClass().add("mk-promo-badge");
                box.getChildren().addAll(original, discounted, badge);
            } else {
                Label price = new Label(String.format("%.2f TND", p.getPrixUnitaire()));
                price.getStyleClass().add("mk-price");
                box.getChildren().add(price);
            }
        }
        return box;
    }

    // ------------------------------------------------------------------ //
    //  Actions
    // ------------------------------------------------------------------ //

    private void openFiche(Produit p, Promotion bestPromotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace_detail.fxml"));
            Parent root = loader.load();
            MarketplaceDetailController ctrl = loader.getController();
            ctrl.setProduit(p, bestPromotion);

            Stage dialog = new Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.initOwner(cardsPane.getScene().getWindow());
            dialog.setTitle("Fiche produit — " + p.getNom() + " - FARMTECH");
            dialog.setScene(new Scene(root, 700, 600));
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fiche produit.");
        }
    }

    private void handleRemove(Produit p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Retirer des favoris");
        confirm.setHeaderText("Retirer « " + p.getNom() + " » ?");
        confirm.setContentText("Cette action est réversible depuis le Marketplace.");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            favorisDAO.removeFavoris(p.getIdProduit());
            loadFavoris();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/marketplace.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/marketplace.css").toExternalForm());
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Marketplace — Catalogue");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner au Marketplace.");
        }
    }

    @FXML
    private void handleClearAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider tous les favoris");
        confirm.setHeaderText("Supprimer TOUS les favoris ?");
        confirm.setContentText("Cette action est irréversible.");
        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            favorisDAO.clearAllFavoris();
            loadFavoris();
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private String resolveCategory(Produit p) {
        if (p == null) return "Autres";
        String name = p.getNom() != null ? p.getNom().toLowerCase() : "";
        String unit = p.getUnite() != null ? p.getUnite().toLowerCase() : "";
        if (name.matches(".*\\b(semence|graine|grain|plant|fourrage).*")) return "Semences";
        if (name.matches(".*\\b(engrais|fertili|compost|amendement).*"))  return "Fertilisants";
        if (unit.equals("l") || name.matches(".*\\b(lait|huile|sirop|jus|liquide).*")) return "Liquides";
        if (unit.equals("piece") || unit.equals("pièce") || unit.equals("unité") || unit.equals("unite")) return "Equipements";
        if (unit.equals("kg")) return "Intrants solides";
        return "Autres";
    }

    private Image loadProductImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) return new Image(file.toURI().toString(), true);
                if (path.startsWith("http://") || path.startsWith("https://"))
                    return new Image(path, true);
                if (path.startsWith("/"))
                    return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            }
        } catch (Exception ignored) {}
        InputStream def = getClass().getResourceAsStream("/images/products/default.png");
        return def != null ? new Image(def) : null;
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(header); a.setContentText(content); a.showAndWait();
    }
}
