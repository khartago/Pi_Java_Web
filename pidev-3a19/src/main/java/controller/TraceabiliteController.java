package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitHistorique;
import model.ProduitHistoriqueDAO;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Displays product details and traceability history.
 */
public class TraceabiliteController {

    @FXML private Label nomProduitLabel;
    @FXML private Label idProduitLabel;
    @FXML private Label stockLabel;
    @FXML private Label uniteLabel;
    @FXML private Label expirationLabel;

    @FXML private TableView<ProduitHistorique> historiqueTable;
    @FXML private TableColumn<ProduitHistorique, String> typeCol;
    @FXML private TableColumn<ProduitHistorique, Integer> qAvantCol;
    @FXML private TableColumn<ProduitHistorique, Integer> qApresCol;
    @FXML private TableColumn<ProduitHistorique, String> dateCol;
    @FXML private TableColumn<ProduitHistorique, String> commentaireCol;

    private final ProduitHistoriqueDAO historiqueDAO = new ProduitHistoriqueDAO();
    private Produit produit;

    @FXML
    private void initialize() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        qAvantCol.setCellValueFactory(new PropertyValueFactory<>("quantiteAvant"));
        qApresCol.setCellValueFactory(new PropertyValueFactory<>("quantiteApres"));
        commentaireCol.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        dateCol.setCellValueFactory(cell -> {
            if (cell.getValue().getDateEvenement() == null) return null;
            return new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getDateEvenement().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
        });
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        refreshView();
    }

    private void refreshView() {
        if (produit == null) return;
        idProduitLabel.setText(String.valueOf(produit.getIdProduit()));
        nomProduitLabel.setText(produit.getNom());
        stockLabel.setText(String.valueOf(produit.getQuantite()));
        uniteLabel.setText(produit.getUnite());
        expirationLabel.setText(
                produit.getDateExpiration() == null ? "-" :
                        produit.getDateExpiration().format(DateTimeFormatter.ISO_DATE)
        );

        historiqueTable.setItems(FXCollections.observableArrayList(
                historiqueDAO.getByProduit(produit.getIdProduit())
        ));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) historiqueTable.getScene().getWindow();
        stage.close();
    }

    public static void openForProduit(Produit produit) throws IOException {
        FXMLLoader loader = new FXMLLoader(TraceabiliteController.class.getResource("/view/traceabilite.fxml"));
        Parent root = loader.load();

        TraceabiliteController controller = loader.getController();
        controller.setProduit(produit);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Traçabilité - " + produit.getNom());
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}
