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
import model.Materiel;
import model.MaterielDAO;
import model.Produit;
import model.ProduitDAO;
import java.io.IOException;
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
    private javafx.scene.control.Label pageTitleLabel;
    @FXML
    private javafx.scene.control.Label productFilterHint;

    private final MaterielDAO materielDAO = new MaterielDAO();
    private ObservableList<Materiel> materiels;
    private Produit produit;

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (pageTitleLabel != null) {
            pageTitleLabel.setText(produit != null ? "Matériels de " + produit.getNom() : "Gestion des Matériels");
        }
        if (productFilterHint != null) {
            productFilterHint.setVisible(produit == null);
            productFilterHint.setText(produit == null ? "Sélectionnez un produit dans la liste Produits pour voir ses matériels associés." : "");
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
        if (productFilterHint != null && produit == null) {
            productFilterHint.setVisible(true);
            productFilterHint.setText("Sélectionnez un produit dans la liste Produits pour voir ses matériels associés.");
        }
    }

    private void loadMateriels() {
        if (produit != null) {
            materiels = FXCollections.observableArrayList(materielDAO.getAllByProduit(produit.getIdProduit()));
            materielTable.setItems(materiels);
        } else if (materielTable != null) {
            materielTable.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void handleAjouter() {
        openMaterielForm(null);
    }

    @FXML
    private void handleModifier() {
        Materiel selected = materielTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMaterielForm(selected);
        }
    }

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

    @FXML
    private void handleVoirProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

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
