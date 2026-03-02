package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.FavorisDAO;
import javafx.scene.layout.Region;
import service.OpenAiChatService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MarketplaceController {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> uniteFilter;
    @FXML private CheckBox expiringSoonCheck;

    // Chatbot UI
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Button chatSendButton;
    @FXML private ProgressIndicator chatLoading;
    @FXML private Label chatHintLabel;

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final FavorisDAO favorisDAO = new FavorisDAO();
    private List<Produit> allProduits;

    private final List<OpenAiChatService.Msg> chatHistory = new ArrayList<>();

    @FXML
    private void initialize() {
        reloadData();
        initChat();
    }

    private void initChat() {
        // Message d'accueil (local)
        if (chatArea != null) {
            chatArea.setText("""
                    Assistant: Bonjour ! Posez-moi une question sur les produits (stock, expiration, etc.).
                    """.trim());
        }

        // Hint si la cl├® n'est pas configur├®e
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            if (chatHintLabel != null) {
                chatHintLabel.setText("Note: configurez OPENAI_API_KEY pour activer les r├®ponses IA.");
            }
        } else {
            if (chatHintLabel != null) chatHintLabel.setText("");
        }
    }

    private void reloadData() {
        allProduits = produitDAO.getAll();

        Set<String> unites = allProduits.stream()
                .map(Produit::getUnite)
                .filter(u -> u != null && !u.isBlank())
                .collect(Collectors.toSet());

        uniteFilter.getItems().clear();
        uniteFilter.getItems().add("Toutes");
        uniteFilter.getItems().addAll(unites);
        uniteFilter.setValue("Toutes");

        renderCards(allProduits);
    }

    @FXML
    private void handleFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String unite = uniteFilter.getValue();
        boolean soon = expiringSoonCheck.isSelected();

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(7);

        List<Produit> filtered = allProduits.stream()
                .filter(p -> q.isEmpty() || (p.getNom() != null && p.getNom().toLowerCase().contains(q)))
                .filter(p -> unite == null || unite.equals("Toutes") || unite.equals(p.getUnite()))
                .filter(p -> {
                    if (!soon) return true;
                    if (p.getDateExpiration() == null) return false;
                    return !p.getDateExpiration().isBefore(today) && !p.getDateExpiration().isAfter(limit);
                })
                .toList();

        renderCards(filtered);
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        uniteFilter.setValue("Toutes");
        expiringSoonCheck.setSelected(false);
        renderCards(allProduits);
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

        // Ô£à IMPORTANT : plus de setStyle() -> on utilise le CSS
        card.getStyleClass().add("product-card");

        // Ô£à IMAGE
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setImage(loadProductImage(p.getImagePath()));
        imageView.getStyleClass().add("product-card-image");

        // Ô£à TITRE + META + EMOJI FAVORIS EN HAUT
        Label title = new Label(p.getNom());
        title.getStyleClass().add("product-title");

        // ÔØñ´©Å EMOJI FAVORIS - SIMPLE ET CLIQUABLE
        Button favorisEmoji = new Button();
        favorisEmoji.setPrefWidth(40);
        favorisEmoji.setPrefHeight(40);
        favorisEmoji.setStyle("-fx-font-size: 24px; -fx-padding: 0; -fx-background-color: transparent;");
        updateFavorisEmoji(favorisEmoji, p);
        favorisEmoji.setOnAction(e -> toggleFavoriSimple(favorisEmoji, p));

        HBox titleBar = new HBox(8);
        titleBar.setPrefWidth(240);
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().addAll(title, spacer, favorisEmoji);

        Label stock = new Label("Stock: " + p.getQuantite() + " " + p.getUnite());
        stock.getStyleClass().add("product-meta");

        Label exp = new Label("Expiration: " + (p.getDateExpiration() == null ? "-" : p.getDateExpiration()));
        exp.getStyleClass().add("product-meta");

        // Ô£à BADGE
        Label badge = new Label();
        badge.setText("");

        if (p.getDateExpiration() != null) {
            LocalDate today = LocalDate.now();
            if (!p.getDateExpiration().isBefore(today) && !p.getDateExpiration().isAfter(today.plusDays(7))) {
                badge.setText("Expire bient├┤t");
                badge.getStyleClass().add("badge-soon");
            }
        }

        // Ô£à BOUTONS
        Button detailsBtn = new Button("D├®tails");
        detailsBtn.getStyleClass().add("secondary-button");
        detailsBtn.setOnAction(e -> openDetails(p));

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("primary-button");
        editBtn.setOnAction(e -> openEditForm(p));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> handleDelete(p));

        HBox actions = new HBox(8, detailsBtn, editBtn, deleteBtn);
        actions.getStyleClass().add("product-actions");

        // Ô£à Optionnel : pousser les boutons en bas si tu veux "card bien align├®e"
        // card.getChildren().addAll(imageView, titleBar, badge, stock, exp, new Region(), actions);

        card.getChildren().addAll(imageView, titleBar, badge, stock, exp, actions);
        return card;
    }

    /**
     * Met ├á jour l'emoji favoris (ÔØñ´©Å = favori, ­ƒñì = non favori)
     */
    private void updateFavorisEmoji(Button btn, Produit p) {
        if (favorisDAO.isFavoris(p.getIdProduit())) {
            btn.setText("ÔØñ´©Å");
        } else {
            btn.setText("­ƒñì");
        }
    }

    /**
     * Toggle simple - ajoute/retire des favoris avec changement emoji instantan├®
     */
    private void toggleFavoriSimple(Button btn, Produit p) {
        if (favorisDAO.isFavoris(p.getIdProduit())) {
            favorisDAO.removeFavoris(p.getIdProduit());
        } else {
            favorisDAO.addFavoris(p.getIdProduit());
        }
        updateFavorisEmoji(btn, p);
    }

    private Image loadProductImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                File file = new File(path);
                if (file.exists()) {
                    return new Image(file.toURI().toString(), true);
                }

                if (path.startsWith("http://") || path.startsWith("https://")) {
                    return new Image(path, true);
                }

                if (path.startsWith("/")) {
                    return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                }
            }
        } catch (Exception ignored) {}

        return new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/products/default.png")
        ));
    }

    private void openDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace_detail.fxml"));
            Parent root = loader.load();

            MarketplaceDetailController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("D├®tails - " + produit.getNom());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatSend() {
        if (chatInput == null) return;
        String userText = chatInput.getText() == null ? "" : chatInput.getText().trim();
        if (userText.isEmpty()) return;

        appendChat("Vous", userText);
        chatInput.clear();

        // Si pas de cl├®, on r├®pond localement
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            appendChat("Assistant", "Veuillez configurer la variable dÔÇÖenvironnement OPENAI_API_KEY pour activer le chatbot IA.");
            return;
        }

        setChatBusy(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                List<OpenAiChatService.Msg> msgs = buildPrompt(userText);
                return OpenAiChatService.chat(msgs);
            }
        };

        task.setOnSucceeded(ev -> {
            setChatBusy(false);
            String reply = task.getValue();
            appendChat("Assistant", reply);
        });

        task.setOnFailed(ev -> {
            setChatBusy(false);
            Throwable ex = task.getException();
            appendChat("Assistant", "Erreur IA: " + (ex == null ? "inconnue" : ex.getMessage()));
        });

        Thread t = new Thread(task, "openai-chat");
        t.setDaemon(true);
        t.start();
    }

    private List<OpenAiChatService.Msg> buildPrompt(String userText) {
        // Contexte "produits pertinents" (sans changer wording UI)
        String q = userText.toLowerCase();
        List<Produit> matches = allProduits == null ? List.of() : allProduits.stream()
                .filter(p -> p.getNom() != null && p.getNom().toLowerCase().contains(q))
                .limit(5)
                .toList();

        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        StringBuilder ctx = new StringBuilder();
        ctx.append("Contexte produits (extraits du catalogue):\n");
        if (matches.isEmpty()) {
            ctx.append("- Aucun match direct sur le nom. Si n├®cessaire, pose une question de clarification.\n");
        } else {
            for (Produit p : matches) {
                ctx.append("- id=").append(p.getIdProduit())
                        .append(", nom=").append(p.getNom())
                        .append(", stock=").append(p.getQuantite()).append(" ").append(p.getUnite())
                        .append(", expiration=").append(p.getDateExpiration() == null ? "-" : p.getDateExpiration().format(df))
                        .append("\n");
            }
        }

        List<OpenAiChatService.Msg> msgs = new ArrayList<>();
        msgs.add(new OpenAiChatService.Msg("system",
                "Tu es un assistant de marketplace pour une application de gestion de produits agricoles. " +
                        "R├®ponds en fran├ºais, de fa├ºon concise et utile. " +
                        "Si l'information n'existe pas dans le contexte fourni, dis-le clairement et propose une question courte."));
        msgs.add(new OpenAiChatService.Msg("system", ctx.toString()));

        // Historique (dernier N ├®changes)
        int keep = 8;
        int start = Math.max(0, chatHistory.size() - keep);
        for (int i = start; i < chatHistory.size(); i++) {
            msgs.add(chatHistory.get(i));
        }

        msgs.add(new OpenAiChatService.Msg("user", userText));
        // On ajoute le user msg ├á l'historique (et on compl├¿tera assistant quand on re├ºoit la r├®ponse)
        chatHistory.add(new OpenAiChatService.Msg("user", userText));

        return msgs;
    }

    private void appendChat(String who, String text) {
        if (chatArea == null) return;
        String current = chatArea.getText() == null ? "" : chatArea.getText();
        String next = (current.isBlank() ? "" : current + "\n\n") + who + ": " + text;
        chatArea.setText(next);
        chatArea.positionCaret(next.length());

        // Maintenir l'historique assistant aussi
        if ("Assistant".equals(who)) {
            chatHistory.add(new OpenAiChatService.Msg("assistant", text));
        }
    }

    private void setChatBusy(boolean busy) {
        if (chatLoading != null) {
            chatLoading.setVisible(busy);
            chatLoading.setManaged(busy);
        }
        if (chatSendButton != null) chatSendButton.setDisable(busy);
        if (chatInput != null) chatInput.setDisable(busy);
    }
    @FXML
    private void handleBackToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/produit_list.fxml"));

            Scene scene = new Scene(root);

            // Ô£à ajouter le CSS global
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Produits et Mat├®riels");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditForm(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_form.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.setProduitDAO(produitDAO);
            controller.setProduit(produit);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Modifier produit");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            allProduits = produitDAO.getAll();
            handleFilter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Produit produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Voulez-vous supprimer : " + produit.getNom() + " ?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = produitDAO.delete(produit.getIdProduit());
            if (ok) {
                allProduits = produitDAO.getAll();
                handleFilter();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erreur");
                err.setHeaderText("Suppression impossible");
                err.setContentText("Le produit ne peut pas ├¬tre supprim├® (peut-├¬tre r├®f├®renc├® par un mat├®riel).");
                err.showAndWait();
            }
        }
    }

    @FXML
    private void handleOpenFavoris() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mes_favoris.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ÔØñ´©Å Mes Favoris");
        } catch (IOException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Erreur");
            err.setHeaderText("Erreur");
            err.setContentText("Impossible d'ouvrir la page des favoris: " + e.getMessage());
            err.showAndWait();
        }
    }
}
