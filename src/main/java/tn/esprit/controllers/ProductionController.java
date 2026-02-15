package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import tn.esprit.entities.Production;
import tn.esprit.entities.ProductionPlante;
import tn.esprit.services.ProductionPlanteService;
import tn.esprit.services.ProductionService;

import java.sql.Date;

public class ProductionController {

    // ===== TABPANE =====
    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane productionPlantContainer;

    // ===== FORM =====
    @FXML
    private TextField tfNomPlant, tfVariete, tfQuantite, tfSaison;

    @FXML
    private DatePicker dpDatePlante;

    // ===== TABLE =====
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

    // ✅ ETAT
    @FXML
    private TableColumn<Production, String> colEtat;

    // ✅ ACTION BUTTON
    @FXML
    private TableColumn<Production, Void> colAction;

    // ===== SERVICES =====
    private final ProductionService productionService = new ProductionService();
    private final ProductionPlanteService productionPlanteService = new ProductionPlanteService();

    private final ObservableList<Production> list = FXCollections.observableArrayList();

    // ===================== INIT =====================
    @FXML
    public void initialize() {

        // Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomPlant.setCellValueFactory(new PropertyValueFactory<>("nomPlant"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colDatePlante.setCellValueFactory(new PropertyValueFactory<>("datePlante"));
        colSaison.setCellValueFactory(new PropertyValueFactory<>("saison"));

        // Etat
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

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

        // Load tab2 content
        loadProductionPlantTab();

        // Add accepter button
        addAccepterButtonToTable();

        refreshTable();
    }

    // ===================== LOAD TAB 2 =====================
    private void loadProductionPlantTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gui/ProductionPlante.fxml"));
            Parent root = loader.load();

            productionPlantContainer.getChildren().setAll(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (Exception e) {
            System.out.println("❌ Erreur chargement ProductionPlante.fxml");
            e.printStackTrace();
        }
    }

    // ===================== BUTTON COLUMN =====================
    private void addAccepterButtonToTable() {

        Callback<TableColumn<Production, Void>, TableCell<Production, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Production, Void> call(final TableColumn<Production, Void> param) {

                return new TableCell<>() {

                    private final Button btn = new Button("Accepter");

                    {
                        btn.setStyle("-fx-background-color: #0f3d24; -fx-text-fill: white; -fx-font-weight: bold;");

                        btn.setOnAction(event -> {
                            Production selected = getTableView().getItems().get(getIndex());

                            // Already complete
                            if (selected.getEtat() != null && selected.getEtat().equalsIgnoreCase("COMPLETE")) {
                                showAlert(Alert.AlertType.WARNING, "Déjà accepté", "Cette plantation est déjà COMPLETE !");
                                return;
                            }

                            // Confirmation
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                            confirm.setTitle("Confirmation");
                            confirm.setHeaderText(null);
                            confirm.setContentText("Voulez-vous accepter cette plantation ?");

                            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                                return;
                            }

                            try {
                                // 1) UPDATE plantation.etat -> COMPLETE
                                productionService.updateEtat(selected.getId(), "COMPLETE");

                                // 2) INSERT into production (ProductionPlant)
                                ProductionPlante prodPlant = new ProductionPlante(
                                        selected.getQuantite(),                 // quantiteProduite
                                        new Date(System.currentTimeMillis()),   // dateRecolte = today
                                        "Bonne",                                // qualite default
                                        "Récoltée"                              // etat default
                                );

                                productionPlanteService.ajouter(prodPlant);

                                showAlert(Alert.AlertType.INFORMATION, "Succès",
                                        "Plantation acceptée ✅\nAjout automatique dans Production Plant.");

                                refreshTable();

                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            Production p = getTableView().getItems().get(getIndex());

                            if (p.getEtat() != null && p.getEtat().equalsIgnoreCase("COMPLETE")) {
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
                };
            }
        };

        colAction.setCellFactory(cellFactory);
    }

    // ===================== ADD =====================
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

            // etat auto EN_ATTENTE
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

    // ===================== UPDATE =====================
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

            // garder l'etat actuel
            Production p = new Production(
                    selected.getId(),
                    tfNomPlant.getText(),
                    tfVariete.getText(),
                    quantite,
                    datePlante,
                    tfSaison.getText(),
                    selected.getEtat()
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

    // ===================== DELETE =====================
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

    // ===================== REFRESH =====================
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

    // ===================== CLEAR FORM =====================
    private void clearForm() {
        tfNomPlant.clear();
        tfVariete.clear();
        tfQuantite.clear();
        tfSaison.clear();
        dpDatePlante.setValue(null);
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
