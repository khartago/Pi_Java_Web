package controller;

import Services.PromotionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import model.Promotion;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PromotionController {

    // ------------------------------------------------------------------ //
    //  FXML bindings
    // ------------------------------------------------------------------ //

    @FXML private TableView<Promotion>           tablePromotions;
    @FXML private TableColumn<Promotion, String> colNom;
    @FXML private TableColumn<Promotion, String> colType;
    @FXML private TableColumn<Promotion, String> colValeur;
    @FXML private TableColumn<Promotion, String> colQuantiteMin;
    @FXML private TableColumn<Promotion, String> colDateDebut;
    @FXML private TableColumn<Promotion, String> colDateFin;
    @FXML private TableColumn<Promotion, String> colProduit;
    @FXML private TableColumn<Promotion, String> colStatut;
    @FXML private TableColumn<Promotion, Void>   colActions;
    @FXML private Label                          labelTotal;

    private final PromotionService promotionService = new PromotionService();
    private final ProduitDAO       produitDAO       = new ProduitDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //

    @FXML
    private void initialize() {
        setupColumns();
        reloadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(c -> c.getValue().nomProperty());

        colType.setCellValueFactory(c -> {
            String t = c.getValue().getTypeReduction();
            String label = Promotion.TYPE_MONTANT_FIXE.equals(t) ? "Montant fixe" : "Pourcentage";
            return new SimpleStringProperty(label);
        });

        colValeur.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLabel()));

        colQuantiteMin.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getQuantiteMin())));

        colDateDebut.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getDateDebut();
            return new SimpleStringProperty(d == null ? "—" : d.format(DATE_FMT));
        });

        colDateFin.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getDateFin();
            return new SimpleStringProperty(d == null ? "—" : d.format(DATE_FMT));
        });

        colProduit.setCellValueFactory(c -> {
            int id = c.getValue().getIdProduit();
            if (id == 0) return new SimpleStringProperty("Tous les produits");
            Produit p = produitDAO.getById(id);
            return new SimpleStringProperty(p == null ? "id=" + id : p.getNom());
        });

        colStatut.setCellValueFactory(c -> {
            Promotion promo = c.getValue();
            String statut;
            if (!promo.isActif()) {
                statut = "⏸ Inactive";
            } else if (promo.isActiveNow()) {
                statut = "✅ Active";
            } else {
                statut = "⏳ Planifiée";
            }
            return new SimpleStringProperty(statut);
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("primary-button");
                deleteBtn.getStyleClass().add("danger-button");
                editBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 12px;");
                deleteBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 12px;");

                editBtn.setOnAction(e -> openForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void reloadData() {
        List<Promotion> list = promotionService.getAllPromotions();
        tablePromotions.setItems(FXCollections.observableArrayList(list));
        if (labelTotal != null)
            labelTotal.setText("Total : " + list.size() + " promotion(s)");
    }

    // ------------------------------------------------------------------ //
    //  Actions FXML
    // ------------------------------------------------------------------ //

    @FXML private void handleNew()  { openForm(null); }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/produit_list.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) tablePromotions.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Produits et Matériels");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void openForm(Promotion promotion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/promotion_form.fxml"));
            Parent root = loader.load();
            PromotionFormController ctrl = loader.getController();
            ctrl.setPromotion(promotion);
            ctrl.setOnSaved(this::reloadData);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(promotion == null ? "Nouvelle promotion" : "Modifier promotion");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(Promotion promotion) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la promotion");
        confirm.setContentText("Supprimer : " + promotion.getNom() + " ?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (promotionService.delete(promotion.getIdPromotion())) reloadData();
            else new Alert(Alert.AlertType.ERROR, "Suppression impossible.").showAndWait();
        }
    }
}
