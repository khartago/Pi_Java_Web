package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Production;
import tn.esprit.services.ProductionService;

import java.sql.Date;

public class ProductionController {

    @FXML
    private TextField tfNomPlant, tfVariete, tfQuantite, tfSaison;

    @FXML
    private DatePicker dpDatePlante;

    @FXML
    private TableView<Production> tableProduction;

    @FXML
    private TableColumn<Production, Integer> colId;

    @FXML
    private TableColumn<Production, String> colNomPlant, colVariete, colSaison;

    @FXML
    private TableColumn<Production, Integer> colQuantite;

    @FXML
    private TableColumn<Production, Date> colDatePlante;

    private final ProductionService productionService = new ProductionService();

    private ObservableList<Production> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomPlant.setCellValueFactory(new PropertyValueFactory<>("nomPlant"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colDatePlante.setCellValueFactory(new PropertyValueFactory<>("datePlante"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison"));

        // Click on row -> fill form
        tableProduction.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfNomPlant.setText(newV.getNomPlant());
                tfVariete.setText(newV.getVariete());
                tfQuantite.setText(String.valueOf(newV.getQuantite()));
                tfSaison.setText(newV.getSaison());

                if (newV.getDatePlante() != null) {
                    dpDatePlante.setValue(newV.getDatePlante().toLocalDate());
                }
            }
        });

        refreshTable();
    }

    // ✅ ADD
    @FXML
    public void ajouterProduction() {
        try {
            if (tfNomPlant.getText().isEmpty() || tfVariete.getText().isEmpty()
                    || tfQuantite.getText().isEmpty() || dpDatePlante.getValue() == null
                    || tfSaison.getText().isEmpty()) {

                showAlert(Alert.AlertType.WARNING, "Champs vides", "Remplir tous les champs !");
                return;
            }

            int quantite = Integer.parseInt(tfQuantite.getText());
            Date datePlante = Date.valueOf(dpDatePlante.getValue());

            Production p = new Production(
                    tfNomPlant.getText(),
                    tfVariete.getText(),
                    quantite,
                    datePlante,
                    tfSaison.getText()
            );

            productionService.ajouter(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Production ajoutée !");
            clearForm();
            refreshTable();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité doit être un nombre !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ✅ UPDATE
    @FXML
    public void modifierProduction() {
        try {
            Production selected = tableProduction.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Sélectionner une production !");
                return;
            }

            int quantite = Integer.parseInt(tfQuantite.getText());
            Date datePlante = Date.valueOf(dpDatePlante.getValue());

            Production p = new Production(
                    selected.getId(),
                    tfNomPlant.getText(),
                    tfVariete.getText(),
                    quantite,
                    datePlante,
                    tfSaison.getText()
            );

            productionService.modifier(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Production modifiée !");
            clearForm();
            refreshTable();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité doit être un nombre !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ✅ DELETE
    @FXML
    public void supprimerProduction() {
        try {
            Production selected = tableProduction.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Sélectionner une production !");
                return;
            }

            productionService.supprimer(selected.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Production supprimée !");
            clearForm();
            refreshTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ✅ REFRESH
    @FXML
    public void refreshTable() {
        try {
            list.clear();
            list.addAll(productionService.afficher());
            tableProduction.setItems(list);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // Clear form
    private void clearForm() {
        tfNomPlant.clear();
        tfVariete.clear();
        tfQuantite.clear();
        tfSaison.clear();
        dpDatePlante.setValue(null);
    }

    // Alert helper
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
