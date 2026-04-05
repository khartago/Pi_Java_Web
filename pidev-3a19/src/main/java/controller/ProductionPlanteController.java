package controller;

import model.ProductionPlante;
import Services.ProductionPlanteService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;

public class ProductionPlanteController {

    @FXML private TextField tfQuantiteProduite, tfQualite, tfEtat;
    @FXML private DatePicker dpDateRecolte;

    @FXML private TextField searchFieldPlant;
    @FXML private ComboBox<String> sortColumnBoxPlant;
    @FXML private ComboBox<String> sortOrderBoxPlant;

    @FXML private TableView<ProductionPlante> tableProductionPlante;
    @FXML private TableColumn<ProductionPlante, Integer> colIdProduction;
    @FXML private TableColumn<ProductionPlante, Float> colQuantiteProduite;
    @FXML private TableColumn<ProductionPlante, Date> colDateRecolte;
    @FXML private TableColumn<ProductionPlante, String> colQualite;
    @FXML private TableColumn<ProductionPlante, String> colEtat;

    private final ProductionPlanteService productionPlanteService = new ProductionPlanteService();
    private final ObservableList<ProductionPlante> list = FXCollections.observableArrayList();

    private FilteredList<ProductionPlante> filteredData;
    private SortedList<ProductionPlante> sortedData;

    @FXML
    public void initialize() {

        colIdProduction.setCellValueFactory(new PropertyValueFactory<>("idProduction"));
        colQuantiteProduite.setCellValueFactory(new PropertyValueFactory<>("quantiteProduite"));
        colDateRecolte.setCellValueFactory(new PropertyValueFactory<>("dateRecolte"));
        colQualite.setCellValueFactory(new PropertyValueFactory<>("qualite"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        tableProductionPlante.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfQuantiteProduite.setText(String.valueOf(newV.getQuantiteProduite()));
                tfQualite.setText(newV.getQualite());
                tfEtat.setText(newV.getEtat());

                if (newV.getDateRecolte() != null)
                    dpDateRecolte.setValue(newV.getDateRecolte().toLocalDate());
                else
                    dpDateRecolte.setValue(null);
            }
        });

        // 🔎 SEARCH
        filteredData = new FilteredList<>(list, p -> true);

        if (searchFieldPlant != null) {
            searchFieldPlant.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(p -> {

                    if (newVal == null || newVal.trim().isEmpty())
                        return true;

                    String filter = newVal.toLowerCase();

                    if (String.valueOf(p.getIdProduction()).contains(filter)) return true;
                    if (String.valueOf(p.getQuantiteProduite()).contains(filter)) return true;
                    if (p.getQualite() != null && p.getQualite().toLowerCase().contains(filter)) return true;
                    if (p.getEtat() != null && p.getEtat().toLowerCase().contains(filter)) return true;
                    if (p.getDateRecolte() != null &&
                            p.getDateRecolte().toString().contains(filter)) return true;

                    return false;
                });
            });
        }

        // 🔽 SORT
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProductionPlante.comparatorProperty());
        tableProductionPlante.setItems(sortedData);

        if (sortColumnBoxPlant != null) {
            sortColumnBoxPlant.getItems().addAll(
                    "ID", "Quantite", "Date", "Qualite", "Etat"
            );
        }

        if (sortOrderBoxPlant != null) {
            sortOrderBoxPlant.getItems().addAll("ASC", "DESC");
        }

        refreshTable();
    }

    @FXML
    public void applySortingPlant() {

        if (sortColumnBoxPlant == null || sortOrderBoxPlant == null)
            return;

        String column = sortColumnBoxPlant.getValue();
        String order = sortOrderBoxPlant.getValue();

        if (column == null || order == null) {
            showAlert(Alert.AlertType.WARNING, "Tri", "Choisir colonne et ordre.");
            return;
        }

        sortedData.comparatorProperty().unbind();

        sortedData.setComparator((p1, p2) -> {

            int result = 0;

            switch (column) {
                case "ID":
                    result = Integer.compare(p1.getIdProduction(), p2.getIdProduction());
                    break;
                case "Quantite":
                    result = Float.compare(p1.getQuantiteProduite(), p2.getQuantiteProduite());
                    break;
                case "Date":
                    result = p1.getDateRecolte().compareTo(p2.getDateRecolte());
                    break;
                case "Qualite":
                    result = p1.getQualite().compareToIgnoreCase(p2.getQualite());
                    break;
                case "Etat":
                    result = p1.getEtat().compareToIgnoreCase(p2.getEtat());
                    break;
            }

            return order.equals("ASC") ? result : -result;
        });

        tableProductionPlante.setItems(sortedData);
    }

    @FXML
    public void refreshTable() {
        try {
            list.clear();
            list.addAll(productionPlanteService.afficher());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void modifierProductionPlante() {
        try {
            ProductionPlante selected = tableProductionPlante.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Selectionner une production plant.");
                return;
            }

            ProductionPlante p = new ProductionPlante(
                    selected.getIdProduction(),
                    Float.parseFloat(tfQuantiteProduite.getText()),
                    Date.valueOf(dpDateRecolte.getValue()),
                    tfQualite.getText(),
                    tfEtat.getText()
            );

            productionPlanteService.modifier(p);

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Production plant modifiee.");
            clearForm();
            refreshTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void supprimerProductionPlante() {
        try {
            ProductionPlante selected = tableProductionPlante.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Selection", "Selectionner une production plant.");
                return;
            }

            productionPlanteService.supprimer(selected.getIdProduction());

            showAlert(Alert.AlertType.INFORMATION, "Succes", "Production plant supprimee.");
            clearForm();
            refreshTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void clearForm() {
        tfQuantiteProduite.clear();
        tfQualite.clear();
        tfEtat.clear();
        dpDateRecolte.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}