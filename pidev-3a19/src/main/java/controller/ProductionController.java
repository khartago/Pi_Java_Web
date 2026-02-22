package controller;

import model.Production;
import model.ProductionPlante;
import Services.ProductionService;
import Services.ProductionPlanteService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.sql.Date;

public class ProductionController {

    @FXML private TabPane tabPane;
    @FXML private AnchorPane productionPlantContainer;

    @FXML private TextField tfNomPlant, tfVariete, tfQuantite, tfSaison;
    @FXML private DatePicker dpDatePlante;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortColumnBox;
    @FXML private ComboBox<String> sortOrderBox;
    @FXML private TableView<Production> tableProduction;
    @FXML private TableColumn<Production, Integer> colId;
    @FXML private TableColumn<Production, String> colNomPlant, colVariete, colSaison;
    @FXML private TableColumn<Production, Integer> colQuantite;
    @FXML private TableColumn<Production, Date> colDatePlante;
    @FXML private TableColumn<Production, String> colEtat;
    @FXML private TableColumn<Production, Void> colAction;

    private final ProductionService productionService = new ProductionService();
    private final ProductionPlanteService productionPlanteService = new ProductionPlanteService();

    private final ObservableList<Production> list = FXCollections.observableArrayList();
    private FilteredList<Production> filteredData;
    private SortedList<Production> sortedData;

    @FXML
    public void initialize() {

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomPlant.setCellValueFactory(new PropertyValueFactory<>("nomPlant"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colDatePlante.setCellValueFactory(new PropertyValueFactory<>("datePlante"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
// 🔽 Sorting options
        sortColumnBox.getItems().addAll(
                "Nom",
                "Variete",
                "Quantite",
                "Date",
                "Saison",
                "Etat"
        );

        sortOrderBox.getItems().addAll("ASC", "DESC");
        // Selection listener
        tableProduction.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfNomPlant.setText(newV.getNomPlant());
                tfVariete.setText(newV.getVariete());
                tfQuantite.setText(String.valueOf(newV.getQuantite()));
                tfSaison.setText(newV.getSaison());

                if (newV.getDatePlante() != null) {
                    dpDatePlante.setValue(newV.getDatePlante().toLocalDate());
                } else {
                    dpDatePlante.setValue(null);
                }
            }
        });

        loadProductionPlantTab();
        addAccepterButtonToTable();

        // 🔎 SEARCH + SORT (Safe Null Handling)
        filteredData = new FilteredList<>(list, p -> true);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {

                filteredData.setPredicate(production -> {

                    if (newValue == null || newValue.trim().isEmpty()) {
                        return true;
                    }

                    String filter = newValue.toLowerCase();

                    if (production.getNomPlant() != null &&
                            production.getNomPlant().toLowerCase().contains(filter)) return true;

                    if (production.getVariete() != null &&
                            production.getVariete().toLowerCase().contains(filter)) return true;

                    if (production.getSaison() != null &&
                            production.getSaison().toLowerCase().contains(filter)) return true;

                    if (String.valueOf(production.getQuantite()).contains(filter)) return true;

                    if (production.getEtat() != null &&
                            production.getEtat().toLowerCase().contains(filter)) return true;

                    return false;
                });
            });
        }

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProduction.comparatorProperty());

        tableProduction.setItems(sortedData);

        refreshTable();
    }

    private void loadProductionPlantTab() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/ProductionPlante.fxml"));
            productionPlantContainer.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAccepterButtonToTable() {

        colAction.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("Accepter");

            {
                btn.setStyle("-fx-background-color: #0f3d24; -fx-text-fill: white; -fx-font-weight: bold;");

                btn.setOnAction(event -> {

                    Production selected = getTableView().getItems().get(getIndex());

                    if (selected.getEtat() != null &&
                            "COMPLETE".equalsIgnoreCase(selected.getEtat())) {

                        showAlert(Alert.AlertType.WARNING,
                                "Deja accepte",
                                "Plantation deja COMPLETE.");
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setContentText("Accepter cette plantation ?");

                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
                        return;

                    try {

                        productionService.updateEtat(selected.getId(), "COMPLETE");

                        productionPlanteService.ajouter(
                                new ProductionPlante(
                                        selected.getId(), // 🔥 THIS IS THE FIX
                                        (float) selected.getQuantite(),
                                        new Date(System.currentTimeMillis()),
                                        "Bonne",
                                        "Recoltee"
                                )
                        );

                        showAlert(Alert.AlertType.INFORMATION,
                                "Succes",
                                "Plantation acceptee.");

                        refreshTable();

                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR,
                                "Erreur",
                                e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {

                    Production p = getTableView().getItems().get(getIndex());

                    if (p.getEtat() != null &&
                            "COMPLETE".equalsIgnoreCase(p.getEtat())) {

                        btn.setDisable(true);
                        btn.setText("Complete");
                        btn.setStyle("-fx-background-color: gray; -fx-text-fill: white;");

                    } else {

                        btn.setDisable(false);
                        btn.setText("Accepter");
                        btn.setStyle("-fx-background-color: #0f3d24; -fx-text-fill: white; -fx-font-weight: bold;");
                    }

                    setGraphic(btn);
                }
            }
        });
    }

    @FXML
    public void ajouterProduction() {

        try {

            if (tfNomPlant.getText().isEmpty() ||
                    tfVariete.getText().isEmpty() ||
                    tfQuantite.getText().isEmpty() ||
                    dpDatePlante.getValue() == null ||
                    tfSaison.getText().isEmpty()) {

                showAlert(Alert.AlertType.WARNING,
                        "Champs vides",
                        "Remplir tous les champs.");
                return;
            }

            Production p = new Production();

            p.setNomPlant(tfNomPlant.getText());
            p.setVariete(tfVariete.getText());
            p.setQuantite(Integer.parseInt(tfQuantite.getText()));
            p.setDatePlante(Date.valueOf(dpDatePlante.getValue()));
            p.setSaison(tfSaison.getText());
            p.setEtat("EN_ATTENTE");

            // 🔥 Game defaults
            p.setStage(1);
            p.setWaterCount(0);
            p.setLastWaterTime(System.currentTimeMillis());
            p.setStatus("ALIVE");
            p.setGrowthSpeed(1.0);
            p.setSlotIndex(0); // you can improve later

            productionService.ajouter(p);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succes",
                    "Production ajoutee.");

            clearForm();
            refreshTable();

        } catch (NumberFormatException e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Quantite invalide.");

        } catch (Exception e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    @FXML
    public void modifierProduction() {

        try {

            Production selected = tableProduction.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert(Alert.AlertType.WARNING,
                        "Selection",
                        "Selectionner une production.");
                return;
            }

            selected.setNomPlant(tfNomPlant.getText());
            selected.setVariete(tfVariete.getText());
            selected.setQuantite(Integer.parseInt(tfQuantite.getText()));
            selected.setDatePlante(Date.valueOf(dpDatePlante.getValue()));
            selected.setSaison(tfSaison.getText());

            // 🔥 KEEP GAME VALUES (do NOT reset)
            // stage, waterCount, status etc stay unchanged

            productionService.modifier(selected);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succes",
                    "Production modifiee.");

            clearForm();
            refreshTable();

        } catch (Exception e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    @FXML
    public void supprimerProduction() {

        try {

            Production selected = tableProduction.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert(Alert.AlertType.WARNING,
                        "Selection",
                        "Selectionner une production.");
                return;
            }

            productionService.supprimer(selected.getId());

            showAlert(Alert.AlertType.INFORMATION,
                    "Succes",
                    "Production supprimee.");

            clearForm();
            refreshTable();

        } catch (Exception e) {

            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }
    @FXML
    public void applySorting() {

        String column = sortColumnBox.getValue();
        String order = sortOrderBox.getValue();

        if (column == null || order == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Tri",
                    "Choisir colonne et ordre.");
            return;
        }

        // 🔥 UNBIND before setting comparator
        sortedData.comparatorProperty().unbind();

        sortedData.setComparator((p1, p2) -> {

            int result = 0;

            switch (column) {

                case "Nom":
                    result = p1.getNomPlant().compareToIgnoreCase(p2.getNomPlant());
                    break;

                case "Variete":
                    result = p1.getVariete().compareToIgnoreCase(p2.getVariete());
                    break;

                case "Quantite":
                    result = Integer.compare(p1.getQuantite(), p2.getQuantite());
                    break;

                case "Date":
                    result = p1.getDatePlante().compareTo(p2.getDatePlante());
                    break;

                case "Saison":
                    result = p1.getSaison().compareToIgnoreCase(p2.getSaison());
                    break;

                case "Etat":
                    if (p1.getEtat() != null && p2.getEtat() != null)
                        result = p1.getEtat().compareToIgnoreCase(p2.getEtat());
                    break;
            }

            return order.equals("ASC") ? result : -result;
        });

        tableProduction.setItems(sortedData);
    }
    @FXML
    public void refreshTable() {

        try {
            list.clear();
            list.addAll(productionService.afficher());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    e.getMessage());
        }
    }

    private void clearForm() {
        tfNomPlant.clear();
        tfVariete.clear();
        tfQuantite.clear();
        tfSaison.clear();
        dpDatePlante.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}