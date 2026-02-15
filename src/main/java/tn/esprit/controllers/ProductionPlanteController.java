package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.ProductionPlante;
import tn.esprit.services.ProductionPlanteService;

import java.sql.Date;

public class ProductionPlanteController {

    // ===== FORM =====
    @FXML
    private TextField tfQuantiteProduite, tfQualite, tfEtat;

    @FXML
    private DatePicker dpDateRecolte;

    // ===== TABLE =====
    @FXML
    private TableView<ProductionPlante> tableProductionPlante;

    @FXML
    private TableColumn<ProductionPlante, Integer> colIdProduction;

    @FXML
    private TableColumn<ProductionPlante, Float> colQuantiteProduite;

    @FXML
    private TableColumn<ProductionPlante, Date> colDateRecolte;

    @FXML
    private TableColumn<ProductionPlante, String> colQualite;

    @FXML
    private TableColumn<ProductionPlante, String> colEtat;

    // ===== SERVICE =====
    private final ProductionPlanteService productionPlanteService = new ProductionPlanteService();
    private final ObservableList<ProductionPlante> list = FXCollections.observableArrayList();

    // ===== INIT =====
    @FXML
    public void initialize() {

        // Columns
        colIdProduction.setCellValueFactory(new PropertyValueFactory<>("idProduction"));
        colQuantiteProduite.setCellValueFactory(new PropertyValueFactory<>("quantiteProduite"));
        colDateRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecolte"));
        colQualite.setCellValueFactory(new PropertyValueFactory<>("qualite"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        // Click row -> fill form
        tableProductionPlante.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfQuantiteProduite.setText(String.valueOf(newV.getQuantiteProduite()));
                tfQualite.setText(newV.getQualite());
                tfEtat.setText(newV.getEtat());

                if (newV.getDateRecolte() != null) {
                    dpDateRecolte.setValue(newV.getDateRecolte().toLocalDate());
                }
            }
        });

        refreshTable();
    }

    // ===================== UPDATE =====================
    @FXML
    public void modifierProductionPlante() {
        try {
            ProductionPlante selected = tableProductionPlante.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Sélectionner une production plant !");
                return;
            }

            if (tfQuantiteProduite.getText().isEmpty() || tfQualite.getText().isEmpty()
                    || tfEtat.getText().isEmpty() || dpDateRecolte.getValue() == null) {

                showAlert(Alert.AlertType.WARNING, "Champs vides", "Remplir tous les champs !");
                return;
            }

            float quantite = Float.parseFloat(tfQuantiteProduite.getText());
            Date dateRecolte = Date.valueOf(dpDateRecolte.getValue());

            ProductionPlante p = new ProductionPlante(
                    selected.getIdProduction(),
                    quantite,
                    dateRecolte,
                    tfQualite.getText(),
                    tfEtat.getText()
            );

            productionPlanteService.modifier(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Production plant modifiée !");
            clearForm();
            refreshTable();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité doit être un nombre !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ===================== DELETE =====================
    @FXML
    public void supprimerProductionPlante() {
        try {
            ProductionPlante selected = tableProductionPlante.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Sélectionner une production plant !");
                return;
            }

            productionPlanteService.supprimer(selected.getIdProduction());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Production plant supprimée !");
            clearForm();
            refreshTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ===================== REFRESH =====================
    @FXML
    public void refreshTable() {
        try {
            list.clear();
            list.addAll(productionPlanteService.afficher());
            tableProductionPlante.setItems(list);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ===================== CLEAR FORM =====================
    private void clearForm() {
        tfQuantiteProduite.clear();
        tfQualite.clear();
        tfEtat.clear();
        dpDateRecolte.setValue(null);
    }

    // ===================== ALERT =====================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
