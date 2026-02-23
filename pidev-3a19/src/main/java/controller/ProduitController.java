package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Controller for the product list view. Handles display of all products and
 * operations such as create, update, delete and navigating to the material
 * management view.
 */
public class ProduitController {
    @FXML
    private TableView<Produit> produitTable;
    @FXML
    private TableColumn<Produit, Integer> idColumn;
    @FXML
    private TableColumn<Produit, String> nomColumn;
    @FXML
    private TableColumn<Produit, Integer> quantiteColumn;
    @FXML
    private TableColumn<Produit, String> uniteColumn;
    @FXML
    private TableColumn<Produit, String> dateExpColumn;


    private final ProduitDAO produitDAO = new ProduitDAO();
    private ObservableList<Produit> produits;

    @FXML
    private void initialize() {
        // initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));
        // convert LocalDate to formatted string for display
        dateExpColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateExpiration() != null) {
                String formatted = cellData.getValue().getDateExpiration().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        loadProduits();
    }

    /**
     * Loads all products from the database into the TableView.
     */
    private void loadProduits() {
        produits = FXCollections.observableArrayList(produitDAO.getAll());
        produitTable.setItems(produits);
    }

    /**
     * Opens the add product form.
     */
    @FXML
    private void handleAjouter() {
        openProduitForm(null);
    }

    /**
     * Opens the edit product form for the selected product.
     */
    @FXML
    private void handleModifier() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openProduitForm(selected);
        }
    }

    /**
     * Deletes the selected product after user confirmation.
     */
    @FXML
    private void handleSupprimer() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer le produit");
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer le produit " + selected.getNom() + " ?\nCela supprimera également ses matériels associés.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (produitDAO.delete(selected.getIdProduit())) {
                    loadProduits();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText("Erreur lors de la suppression");
                    error.setContentText("Impossible de supprimer ce produit. Assurez-vous qu'il n'est pas référencé ailleurs.");
                    error.showAndWait();
                }
            }
        }
    }

    /**
     * Opens the material management view for the selected product.
     */
    @FXML
    private void handleVoirMateriels() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/materiel_list.fxml"));
                Parent root = loader.load();
                MaterielController controller = loader.getController();
                controller.setProduit(selected);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Matériels de " + selected.getNom());
                stage.setScene(new Scene(root));
                stage.showAndWait();
                // refresh after closing materiel list (e.g. if user changed product quantity)
                loadProduits();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens the product form in a modal window. If {@code produit} is null then
     * the form will create a new product; otherwise it will edit the existing
     * product.
     *
     * @param produit product to edit or null
     */
    private void openProduitForm(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_form.fxml"));
            Parent root = loader.load();
            ProduitFormController controller = loader.getController();
            controller.setProduitDAO(produitDAO);
            controller.setProduit(produit);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(produit == null ? "Ajouter un produit" : "Modifier le produit");
            stage.showAndWait();
            loadProduits();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Retour à la page d'accueil FARMTECH. */
    @FXML
    private void handleRetourAccueil() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/home.fxml"));
            Stage stage = (Stage) produitTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("FARMTECH - Application de gestion agricole");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}