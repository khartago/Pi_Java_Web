package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Produit;
import model.FavorisDAO;
import model.ProduitDAO;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class MesFavorisController {

    @FXML private FlowPane cardsPane;
    @FXML private Label titleLabel;
    @FXML private Label emptyLabel;
    @FXML private Button backButton;
    @FXML private Button clearButton;

    private final FavorisDAO favorisDAO = new FavorisDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();

    @FXML
    private void initialize() {
        loadFavoris();
    }

    private void loadFavoris() {
        List<Produit> favoris = favorisDAO.getAllFavoris();
        if (titleLabel != null) titleLabel.setText("❤️ Mes Favoris (" + favoris.size() + ")");
        if (favoris.isEmpty()) {
            if (emptyLabel != null) {
                emptyLabel.setVisible(true);
                emptyLabel.setText("Aucun favori pour le moment.\nExplorez le Marketplace et ajoutez des produits à vos favoris!");
            }
            if (cardsPane != null) cardsPane.setVisible(false);
            if (clearButton != null) clearButton.setDisable(true);
        } else {
            if (emptyLabel != null) emptyLabel.setVisible(false);
            if (cardsPane != null) {
                cardsPane.setVisible(true);
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

    private VBox createCard(Produit p) {
        VBox card = new VBox(8);
        card.setPrefWidth(260);
        card.getStyleClass().add("product-card");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadProductImage(p.getImagePath()));
        imageView.getStyleClass().add("product-card-image");

        Label title = new Label(p.getNom());
        title.getStyleClass().add("product-title");
        title.setWrapText(true);

        String metaText = "Stock: " + p.getQuantite() + " " + p.getUnite();
        Label meta = new Label(metaText);
        meta.getStyleClass().add("product-meta");

        Label expirationLabel = new Label();
        if (p.getDateExpiration() != null) {
            expirationLabel.setText("Expire: " + p.getDateExpiration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            if (p.getDateExpiration().minusDays(7).isBefore(java.time.LocalDate.now())) {
                expirationLabel.getStyleClass().add("badge-soon");
            } else {
                expirationLabel.getStyleClass().add("product-meta");
            }
        }

        VBox actions = new VBox(6);
        actions.setStyle("-fx-padding: 8 0 0 0;");
        Button detailsButton = new Button("Détails");
        detailsButton.setPrefWidth(240);
        detailsButton.getStyleClass().add("secondary-button");
        detailsButton.setOnAction(e -> openDetails(p));
        Button removeButton = new Button("Retirer des favoris");
        removeButton.setPrefWidth(240);
        removeButton.getStyleClass().add("danger-button");
        removeButton.setOnAction(e -> removeFavoris(p));
        actions.getChildren().addAll(detailsButton, removeButton);

        card.getChildren().addAll(imageView, title, meta);
        if (p.getDateExpiration() != null) card.getChildren().add(expirationLabel);
        card.getChildren().add(actions);
        return card;
    }

    private void openDetails(Produit p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace_detail.fxml"));
            Parent root = loader.load();
            MarketplaceDetailController controller = loader.getController();
            controller.setProduit(p);
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Détails - " + p.getNom());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les détails du produit.");
        }
    }

    private void removeFavoris(Produit p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Retirer des favoris");
        confirm.setHeaderText("Êtes-vous sûr?");
        confirm.setContentText("Voulez-vous retirer \"" + p.getNom() + "\" de vos favoris?");
        if (confirm.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            if (favorisDAO.removeFavoris(p.getIdProduit())) {
                showInfo("Succès", "Produit retiré de vos favoris.");
                loadFavoris();
            } else {
                showError("Erreur", "Impossible de retirer le produit des favoris.");
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Marketplace - Catalogue");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner au Marketplace.");
        }
    }

    @FXML
    private void handleClearAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider tous les favoris");
        confirm.setHeaderText("Êtes-vous sûr?");
        confirm.setContentText("Voulez-vous supprimer TOUS vos favoris? Cette action est irréversible.");
        if (confirm.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            if (favorisDAO.clearAllFavoris()) {
                showInfo("Succès", "Tous les favoris ont été supprimés.");
                loadFavoris();
            } else {
                showError("Erreur", "Impossible de vider les favoris.");
            }
        }
    }

    private Image loadProductImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) return new Image(file.toURI().toString());
                if (path.startsWith("http://") || path.startsWith("https://")) return new Image(path);
                if (path.startsWith("/")) {
                    var is = getClass().getResourceAsStream(path);
                    if (is != null) return new Image(is);
                }
            }
        } catch (Exception ignored) {}
        InputStream def = getClass().getResourceAsStream("/images/products/default.png");
        return def != null ? new Image(def) : null;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
