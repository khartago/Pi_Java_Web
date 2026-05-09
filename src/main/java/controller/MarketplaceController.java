package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.FavorisDAO;
import model.Promotion;
import Services.OpenAiChatService;
import Services.PromotionService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MarketplaceController {

    // ------------------------------------------------------------------ //
    //  FXML bindings
    // ------------------------------------------------------------------ //

    @FXML private FlowPane            cardsPane;
    @FXML private TextField           searchField;
    @FXML private ComboBox<String>    uniteFilter;
    @FXML private ComboBox<String>    categorieFilter;
    @FXML private Label               labelResultInfo;
    @FXML private Button              btnFavoris;

    // Pagination
    @FXML private HBox   paginationBar;
    @FXML private Label  labelPageInfo;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private HBox   pageNumbersBox;

    // ------------------------------------------------------------------ //
    //  Pagination
    // ------------------------------------------------------------------ //

    private static final int PAGE_SIZE = 8;
    private int currentPage = 1;
    private int totalPages  = 1;
    private List<Produit> filteredProduits = new ArrayList<>();

    // ------------------------------------------------------------------ //
    //  Services
    // ------------------------------------------------------------------ //

    private final ProduitDAO       produitDAO       = new ProduitDAO();
    private final FavorisDAO       favorisDAO       = new FavorisDAO();
    private final PromotionService promotionService = new PromotionService();
    private List<Produit>          allProduits;

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //

    @FXML
    private void initialize() {
        reloadData();
    }

    private void reloadData() {
        allProduits = produitDAO.getAll();

        // Unités
        List<String> unites = allProduits.stream()
                .map(Produit::getUnite)
                .filter(u -> u != null && !u.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        uniteFilter.getItems().clear();
        uniteFilter.getItems().add("Toutes unites");
        uniteFilter.getItems().addAll(unites);
        uniteFilter.setValue("Toutes unites");

        // Catégories
        List<String> cats = allProduits.stream()
                .map(this::resolveCategory)
                .distinct().sorted().collect(Collectors.toList());
        categorieFilter.getItems().clear();
        categorieFilter.getItems().add("Toutes categories");
        categorieFilter.getItems().addAll(cats);
        categorieFilter.setValue("Toutes categories");

        updateFavorisBtn();
        currentPage = 1;
        applyFilterAndRender();
    }

    private void updateFavorisBtn() {
        if (btnFavoris == null) return;
        int count = favorisDAO.getAllFavoris().size();
        btnFavoris.setText("Mes favoris (" + count + ")");
    }

    // ------------------------------------------------------------------ //
    //  Filtrage + pagination
    // ------------------------------------------------------------------ //

    @FXML
    private void handleFilter() {
        currentPage = 1;
        applyFilterAndRender();
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        uniteFilter.setValue("Toutes unites");
        categorieFilter.setValue("Toutes categories");
        currentPage = 1;
        applyFilterAndRender();
    }

    private void applyFilterAndRender() {
        String q      = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String unite  = uniteFilter.getValue();
        String cat    = categorieFilter.getValue();

        filteredProduits = allProduits.stream()
                .filter(p -> q.isEmpty() || (p.getNom() != null && p.getNom().toLowerCase().contains(q)))
                .filter(p -> unite == null || unite.equals("Toutes unites") || unite.equals(p.getUnite()))
                .filter(p -> cat == null || cat.equals("Toutes categories")
                        || cat.equals(resolveCategory(p)))
                .collect(Collectors.toList());

        totalPages  = Math.max(1, (int) Math.ceil((double) filteredProduits.size() / PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages);

        // Label résultats style Symfony : "11 resultat(s) - page 1/2"
        if (labelResultInfo != null) {
            labelResultInfo.setText(filteredProduits.size() + " resultat(s) - page "
                    + currentPage + "/" + totalPages);
        }

        renderCurrentPage();
        updatePaginationBar();
    }

    private void renderCurrentPage() {
        cardsPane.getChildren().clear();
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filteredProduits.size());
        for (int i = from; i < to; i++) {
            cardsPane.getChildren().add(createCard(filteredProduits.get(i)));
        }
    }

    // ------------------------------------------------------------------ //
    //  Pagination
    // ------------------------------------------------------------------ //

    private void updatePaginationBar() {
        if (paginationBar == null) return;
        if (btnPrev != null) btnPrev.setDisable(currentPage <= 1);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages);
        if (labelPageInfo != null)
            labelPageInfo.setText("Page " + currentPage + " / " + totalPages);

        if (pageNumbersBox != null) {
            pageNumbersBox.getChildren().clear();
            int start = Math.max(1, currentPage - 2);
            int end   = Math.min(totalPages, currentPage + 2);
            if (start > 1) {
                pageNumbersBox.getChildren().add(makePageBtn(1));
                if (start > 2) pageNumbersBox.getChildren().add(makeEllipsis());
            }
            for (int i = start; i <= end; i++)
                pageNumbersBox.getChildren().add(makePageBtn(i));
            if (end < totalPages) {
                if (end < totalPages - 1) pageNumbersBox.getChildren().add(makeEllipsis());
                pageNumbersBox.getChildren().add(makePageBtn(totalPages));
            }
        }
    }

    private Button makePageBtn(int page) {
        Button btn = new Button(String.valueOf(page));
        btn.getStyleClass().add(page == currentPage ? "mk-page-btn-active" : "mk-page-btn");
        btn.setOnAction(e -> goToPage(page));
        return btn;
    }

    private Label makeEllipsis() {
        Label l = new Label("…");
        l.setStyle("-fx-padding: 0 4; -fx-text-fill: #888;");
        return l;
    }

    private void goToPage(int page) {
        currentPage = page;
        if (labelResultInfo != null)
            labelResultInfo.setText(filteredProduits.size() + " resultat(s) - page "
                    + currentPage + "/" + totalPages);
        renderCurrentPage();
        updatePaginationBar();
    }

    @FXML private void handlePrevPage() { if (currentPage > 1)         goToPage(currentPage - 1); }
    @FXML private void handleNextPage() { if (currentPage < totalPages) goToPage(currentPage + 1); }

    // ------------------------------------------------------------------ //
    //  Création des cartes — style identique Symfony
    // ------------------------------------------------------------------ //

    private VBox createCard(Produit p) {
        VBox card = new VBox(0);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.getStyleClass().add("mk-card");

        // ── Image ──
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadProductImage(p.getImagePath()));
        imageView.getStyleClass().add("mk-card-image");

        // ── Catégorie (SEMENCES, EQUIPEMENTS…) ──
        Label catLabel = new Label(resolveCategory(p).toUpperCase());
        catLabel.getStyleClass().add("mk-category");

        // ── Nom ──
        Label nomLabel = new Label(p.getNom());
        nomLabel.getStyleClass().add("mk-product-name");
        nomLabel.setMaxWidth(196);

        // ── Quantité + unité ──
        Label qteLabel = new Label(p.getQuantite() + " " + (p.getUnite() != null ? p.getUnite() : ""));
        qteLabel.getStyleClass().add("mk-meta");

        // ── Nombre de matériels (0 pour l'instant, extensible) ──
        Label matLabel = new Label("0 materiel(s)");
        matLabel.getStyleClass().add("mk-meta");

        // ── Prix + promo ──
        Promotion bestPromo = promotionService.getBestPromotionForProduct(p);
        VBox prixBox = buildPrixBox(p, bestPromo);

        // ── Bouton favoris / actions admin ──
        boolean isFav = favorisDAO.isFavoris(p.getIdProduit());
        Button favBtn = new Button(isFav ? "❤ Retirer des favoris" : "Ajouter favoris");
        favBtn.getStyleClass().add(isFav ? "mk-btn-fav-active" : "mk-btn-fav");
        favBtn.setMaxWidth(Double.MAX_VALUE);
        favBtn.setOnAction(e -> {
            if (favorisDAO.isFavoris(p.getIdProduit())) {
                favorisDAO.removeFavoris(p.getIdProduit());
                favBtn.setText("Ajouter favoris");
                favBtn.getStyleClass().setAll("mk-btn-fav");
            } else {
                favorisDAO.addFavoris(p.getIdProduit());
                favBtn.setText("❤ Retirer des favoris");
                favBtn.getStyleClass().setAll("mk-btn-fav-active");
            }
            updateFavorisBtn();
        });

        // Bouton "Voir fiche" — vert foncé, pleine largeur
        Button ficheBtn = new Button("Voir fiche produit");
        ficheBtn.getStyleClass().add("mk-btn-detail");
        ficheBtn.setMaxWidth(Double.MAX_VALUE);
        ficheBtn.setOnAction(e -> openFiche(p, bestPromo));

        // Boutons admin (Modifier / Supprimer) — petits, discrets
        Button editBtn   = new Button("✏");
        Button deleteBtn = new Button("🗑");
        editBtn.getStyleClass().add("mk-btn-edit");
        deleteBtn.getStyleClass().add("mk-btn-delete");
        editBtn.setOnAction(e -> openEditForm(p));
        deleteBtn.setOnAction(e -> handleDelete(p));
        HBox adminBtns = new HBox(6, editBtn, deleteBtn);
        adminBtns.setAlignment(Pos.CENTER_RIGHT);
        adminBtns.setStyle("-fx-padding: 6 12 4 12;");

        card.getChildren().addAll(
                imageView, catLabel, nomLabel, qteLabel, matLabel, prixBox,
                adminBtns, ficheBtn, favBtn);
        return card;
    }

    /** Bloc prix — style Symfony : prix barré + prix promo + badge vert */
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

                // Badge vert style Symfony : "PROMO CHOISIE: -40%"
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
    //  Catégorie (logique identique Symfony)
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

    // ------------------------------------------------------------------ //
    //  Image
    // ------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------ //
    //  Navigation
    // ------------------------------------------------------------------ //

    private void openFiche(Produit produit, Promotion bestPromotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace_detail.fxml"));
            Parent root = loader.load();
            MarketplaceDetailController ctrl = loader.getController();
            ctrl.setProduit(produit, bestPromotion);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(cardsPane.getScene().getWindow());
            dialog.setTitle("Fiche produit — " + produit.getNom() + " - FARMTECH");
            dialog.setScene(new Scene(root, 700, 600));
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openEditForm(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_form.fxml"));
            Parent root = loader.load();
            ProduitFormController ctrl = loader.getController();
            ctrl.setProduitDAO(produitDAO);
            ctrl.setProduit(produit);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Modifier — " + produit.getNom());
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            allProduits = produitDAO.getAll();
            applyFilterAndRender();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(Produit produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer « " + produit.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (produitDAO.delete(produit.getIdProduit())) {
                allProduits = produitDAO.getAll();
                applyFilterAndRender();
            } else {
                new Alert(Alert.AlertType.ERROR, "Suppression impossible.").showAndWait();
            }
        }
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/produit_list.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Produits");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleOpenFavoris() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/mes_favoris.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("❤️ Mes Favoris");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
