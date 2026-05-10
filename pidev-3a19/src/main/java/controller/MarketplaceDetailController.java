package controller;

import Services.MaterielRecommendationService;
import Services.MaterielRecommendationService.RecommendationItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.FavorisDAO;
import model.Materiel;
import model.MaterielDAO;
import model.Produit;
import model.Promotion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Fiche produit — même style que SupportIssueDetailController.
 * Utilise farmtech-theme.css, GridPane, TitledPane, Separator.
 */
public class MarketplaceDetailController {

    // ------------------------------------------------------------------ //
    //  FXML bindings — correspondance exacte avec marketplace_detail.fxml
    // ------------------------------------------------------------------ //

    @FXML private ImageView  imageView;
    @FXML private Label      nomLabel;
    @FXML private Label      categorieLabel;
    @FXML private Label      statutExpLabel;

    // GridPane infos
    @FXML private Label      quantiteLabel;
    @FXML private Label      uniteLabel;
    @FXML private Label      expirationLabel;
    @FXML private Label      prixLabel;

    // TitledPane promotion
    @FXML private TitledPane promoPane;
    @FXML private Label      promoNomLabel;
    @FXML private Label      promoPrixOriginalLabel;
    @FXML private Label      promoPrixFinalLabel;
    @FXML private Label      promoBadgeLabel;

    // Favoris
    @FXML private Button     btnFavoris;

    // TitledPane matériels associés
    @FXML private TitledPane materielsAssociesPane;
    @FXML private VBox       materielsAssociesList;

    // TitledPane recommandations IA
    @FXML private TitledPane recommandationsPane;
    @FXML private VBox       recommandationsList;

    // ------------------------------------------------------------------ //
    //  State
    // ------------------------------------------------------------------ //

    private Produit   produit;
    private Promotion bestPromotion;

    private final MaterielDAO                   materielDAO  = new MaterielDAO();
    private final MaterielRecommendationService recoService  = new MaterielRecommendationService();
    private final FavorisDAO                    favorisDAO   = new FavorisDAO();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------ //
    //  API publique
    // ------------------------------------------------------------------ //

    public void setProduit(Produit produit, Promotion bestPromotion) {
        this.produit       = produit;
        this.bestPromotion = bestPromotion;
        refresh();
    }

    public void setProduit(Produit produit) {
        setProduit(produit, null);
    }

    // ------------------------------------------------------------------ //
    //  Rendu
    // ------------------------------------------------------------------ //

    private void refresh() {
        renderHeader();
        renderInfoGrid();
        renderPromoPane();
        renderFavorisBtn();
        renderMaterielsAssocies();
        renderRecommandations();
    }

    /** Image + nom + catégorie + statut expiration */
    private void renderHeader() {
        if (imageView != null)
            imageView.setImage(loadImage(produit.getImagePath()));

        if (nomLabel != null)
            nomLabel.setText(produit.getNom());

        if (categorieLabel != null)
            categorieLabel.setText("Categorie: " + resolveCategory(produit));

        // Statut expiration
        if (statutExpLabel != null) {
            LocalDate exp   = produit.getDateExpiration();
            LocalDate today = LocalDate.now();
            if (exp == null) {
                statutExpLabel.setText("");
            } else if (exp.isBefore(today)) {
                statutExpLabel.setText("⛔ Produit expiré");
                statutExpLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px; -fx-font-weight: 600;");
            } else if (!exp.isAfter(today.plusDays(7))) {
                statutExpLabel.setText("⚠ Expire bientôt");
                statutExpLabel.setStyle("-fx-text-fill: #D97706; -fx-font-size: 13px; -fx-font-weight: 600;");
            } else {
                statutExpLabel.setText("✅ Valide");
                statutExpLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 13px; -fx-font-weight: 600;");
            }
        }
    }

    /** GridPane : Quantité / Unité / Expiration / Prix */
    private void renderInfoGrid() {
        if (quantiteLabel != null)
            quantiteLabel.setText(String.valueOf(produit.getQuantite()));

        if (uniteLabel != null)
            uniteLabel.setText(produit.getUnite() != null ? produit.getUnite() : "—");

        if (expirationLabel != null) {
            LocalDate exp = produit.getDateExpiration();
            expirationLabel.setText(exp == null ? "—" : exp.format(DATE_FMT));
        }

        if (prixLabel != null) {
            double prix = produit.getPrixUnitaire();
            if (bestPromotion != null && prix > 0) {
                double promoPrice = bestPromotion.applyTo(prix, 1);
                prixLabel.setText(String.format("%.2f TND", promoPrice));
                prixLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A4D2E;");
            } else {
                prixLabel.setText(prix > 0 ? String.format("%.2f TND", prix) : "—");
            }
        }
    }

    /** TitledPane promotion — visible seulement si promo active */
    private void renderPromoPane() {
        if (promoPane == null) return;

        if (bestPromotion != null && produit.getPrixUnitaire() > 0) {
            double prix      = produit.getPrixUnitaire();
            double promoPrice = bestPromotion.applyTo(prix, 1);

            promoPane.setVisible(true);
            promoPane.setManaged(true);

            if (promoNomLabel != null)
                promoNomLabel.setText(bestPromotion.getNom());
            if (promoPrixOriginalLabel != null)
                promoPrixOriginalLabel.setText(String.format("%.2f TND", prix));
            if (promoPrixFinalLabel != null)
                promoPrixFinalLabel.setText(String.format("%.2f TND", promoPrice));
            if (promoBadgeLabel != null)
                promoBadgeLabel.setText(bestPromotion.getLabel());
        } else {
            promoPane.setVisible(false);
            promoPane.setManaged(false);
        }
    }

    /** Bouton Ajouter / Retirer des favoris */
    private void renderFavorisBtn() {
        if (btnFavoris == null) return;
        updateFavBtn(favorisDAO.isFavoris(produit.getIdProduit()));
        btnFavoris.setOnAction(e -> {
            boolean nowFav = favorisDAO.isFavoris(produit.getIdProduit());
            if (nowFav) favorisDAO.removeFavoris(produit.getIdProduit());
            else        favorisDAO.addFavoris(produit.getIdProduit());
            updateFavBtn(!nowFav);
        });
    }

    private void updateFavBtn(boolean isFav) {
        if (btnFavoris == null) return;
        if (isFav) {
            btnFavoris.setText("❤ Retirer des favoris");
            btnFavoris.getStyleClass().setAll("danger-button");
        } else {
            btnFavoris.setText("Ajouter aux favoris");
            btnFavoris.getStyleClass().setAll("primary-button");
        }
    }

    /** TitledPane "Matériels associés" */
    private void renderMaterielsAssocies() {
        if (materielsAssociesList == null) return;
        materielsAssociesList.getChildren().clear();

        List<Materiel> associes = materielDAO.getAllByProduit(produit.getIdProduit());

        if (materielsAssociesPane != null) {
            materielsAssociesPane.setVisible(!associes.isEmpty());
            materielsAssociesPane.setManaged(!associes.isEmpty());
        }

        for (Materiel m : associes) {
            Label row = new Label("• " + m.getNom() + " — " + capitalise(m.getEtat()));
            row.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
            materielsAssociesList.getChildren().add(row);
        }
    }

    /** TitledPane "Matériels recommandés" — miroir Symfony */
    private void renderRecommandations() {
        if (recommandationsList == null) return;
        recommandationsList.getChildren().clear();

        List<RecommendationItem> recos = recoService.recommend(produit.getIdProduit(), 4);

        if (recommandationsPane != null) {
            recommandationsPane.setVisible(!recos.isEmpty());
            recommandationsPane.setManaged(!recos.isEmpty());
        }

        for (RecommendationItem item : recos) {
            Materiel m = item.materiel();

            Label nomEtat = new Label("• " + m.getNom() + " — etat: " + item.etatLabel());
            nomEtat.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

            // Badge beige style Symfony "MATCH 65%"
            Label badge = new Label(item.badgeLabel());
            badge.setStyle("-fx-background-color: #E8E0C8; -fx-text-fill: #5C4A1E;"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-padding: 3 8; -fx-background-radius: 10;");

            HBox row = new HBox(10, nomEtat, badge);
            row.setAlignment(Pos.CENTER_LEFT);
            recommandationsList.getChildren().add(row);
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

    private static String capitalise(String s) {
        if (s == null || s.isBlank()) return "—";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private Image loadImage(String path) {
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

    // ------------------------------------------------------------------ //
    //  Navigation — fenêtre modale, fermeture via le X de la fenêtre
    // ------------------------------------------------------------------ //
    // Pas de handleBack() — la fenêtre modale se ferme avec le bouton X

}
