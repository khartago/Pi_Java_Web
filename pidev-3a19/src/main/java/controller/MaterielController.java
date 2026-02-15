package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Materiel;
import model.MaterielDAO;
import model.Produit;
import model.ProduitDAO;

import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the material list view. Displays all equipment belonging to
 * a selected product and allows CRUD operations.
 */
public class MaterielController {
    @FXML
    private TableView<Materiel> materielTable;
    @FXML
    private TableColumn<Materiel, Integer> idColumn;
    @FXML
    private TableColumn<Materiel, String> nomColumn;
    @FXML
    private TableColumn<Materiel, String> etatColumn;
    @FXML
    private TableColumn<Materiel, String> dateAchatColumn;
    @FXML
    private TableColumn<Materiel, Double> coutColumn;
    @FXML
    private ComboBox<Produit> produitCombo;

    private final MaterielDAO materielDAO = new MaterielDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();
    private ObservableList<Materiel> materiels;
    private Produit produit;

    /**
     * Injects the product whose materials should be displayed. Must be called
     * before the window is shown.
     *
     * @param produit product to set
     */
    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produitCombo != null && produit != null) {
            produitCombo.getSelectionModel().select(produit);
        }
        loadMateriels();
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idMateriel"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        dateAchatColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateAchat() != null) {
                String formatted = cellData.getValue().getDateAchat().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        coutColumn.setCellValueFactory(new PropertyValueFactory<>("cout"));

        if (produitCombo != null) {
            produitCombo.setCellFactory(lv -> new ListCell<Produit>() {
                @Override
                protected void updateItem(Produit item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
            produitCombo.setButtonCell(new ListCell<Produit>() {
                @Override
                protected void updateItem(Produit item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
            List<Produit> produits = produitDAO.getAll();
            produitCombo.setItems(FXCollections.observableArrayList(produits));
            produitCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    setProduit(newVal);
                }
            });
            if (produit == null && !produits.isEmpty()) {
                produitCombo.getSelectionModel().selectFirst();
                setProduit(produits.get(0));
            }
        }
    }

    /**
     * Loads the materials for the current product and displays them in the TableView.
     */
    private void loadMateriels() {
        if (produit != null) {
            materiels = FXCollections.observableArrayList(materielDAO.getAllByProduit(produit.getIdProduit()));
            materielTable.setItems(materiels);
        }
    }

    /**
     * Opens the add material form.
     */
    @FXML
    private void handleAjouter() {
        openMaterielForm(null);
    }

    /**
     * Opens the edit material form for the selected material.
     */
    @FXML
    private void handleModifier() {
        Materiel selected = materielTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMaterielForm(selected);
        }
    }

    /**
     * Deletes the selected material after confirmation.
     */
    @FXML
    private void handleSupprimer() {
        Materiel selected = materielTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer le matériel");
            confirm.setContentText("Voulez-vous vraiment supprimer le matériel " + selected.getNom() + " ?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (materielDAO.delete(selected.getIdMateriel())) {
                    loadMateriels();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText("Erreur lors de la suppression du matériel");
                    error.setContentText("Une erreur s'est produite lors de la suppression.");
                    error.showAndWait();
                }
            }
        }
    }

    /**
     * Navigates back to the products list view.
     */
    @FXML
    private void handleVoirProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
            Parent root = loader.load();

            // Ajouter explicitement le CSS si nécessaire
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) materielTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion des Produits et Matériels");
        } catch (IOException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur");
            error.setHeaderText("Erreur de navigation");
            error.setContentText("Impossible de charger la vue des produits.");
            error.showAndWait();
        }
    }

    /**
     * Opens the materiel form in a modal window. Passes along the selected
     * material (or null for creation) and the current product id.
     *
     * @param materiel material to edit or null
     */
    private void openMaterielForm(Materiel materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/materiel_form.fxml"));
            Parent root = loader.load();
            MaterielFormController controller = loader.getController();
            controller.setMaterielDAO(materielDAO);
            controller.setProduit(produit);
            controller.setMateriel(materiel);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(materiel == null ? "Ajouter un matériel" : "Modifier le matériel");
            stage.showAndWait();
            loadMateriels();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}