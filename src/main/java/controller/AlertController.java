package controller;

import Services.CriticalAlertNotifier;
import Services.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import java.util.*;

/**
 * Contrôleur pour gérer l'affichage et l'envoi des alertes critiques
 */
public class AlertController {

    @FXML private TabPane alertTabPane;
    @FXML private Tab lowStockTab;
    @FXML private Tab brokenMaterielsTab;

    // Stock faible
    @FXML private TableView<Produit> lowStockTable;
    @FXML private TableColumn<Produit, Integer> idProduitColumn;
    @FXML private TableColumn<Produit, String> nomProduitColumn;
    @FXML private TableColumn<Produit, Integer> quantiteColumn;
    @FXML private TableColumn<Produit, String> uniteColumn;

    // Matériels en panne
    @FXML private TableView<Materiel> brokenMaterielsTable;
    @FXML private TableColumn<Materiel, Integer> idMaterielColumn;
    @FXML private TableColumn<Materiel, String> nomMaterielColumn;
    @FXML private TableColumn<Materiel, String> etatColumn;
    @FXML private TableColumn<Materiel, String> produitColumn;

    // Contrôles
    @FXML private Label summaryLabel;
    @FXML private Spinner<Integer> thresholdSpinner;
    @FXML private TextField emailToField;
    @FXML private Button sendAlertsButton;
    @FXML private Button refreshButton;

    private CriticalAlertNotifier alertNotifier;
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final MaterielDAO materielDAO = new MaterielDAO();

    private static final int DEFAULT_STOCK_THRESHOLD = 10;
    private static final String DEFAULT_ALERT_EMAIL = "admin@farmtech.tn";

    @FXML
    private void initialize() {
        setupUI();
        loadAlerts();
    }

    private void setupUI() {
        // Spinner pour le seuil
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, DEFAULT_STOCK_THRESHOLD);
        thresholdSpinner.setValueFactory(valueFactory);

        // Email par défaut
        emailToField.setText(DEFAULT_ALERT_EMAIL);

        // Colonnes Stock Faible
        idProduitColumn.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        nomProduitColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));

        // Colonnes Matériels en Panne
        idMaterielColumn.setCellValueFactory(new PropertyValueFactory<>("idMateriel"));
        nomMaterielColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        produitColumn.setCellValueFactory(cellData -> {
            Materiel m = cellData.getValue();
            Produit p = produitDAO.getById(m.getIdProduit());
            String produitNom = p != null ? p.getNom() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(produitNom);
        });

        // Actions des boutons
        refreshButton.setOnAction(e -> loadAlerts());
        sendAlertsButton.setOnAction(e -> handleSendAlerts());
    }

    private void loadAlerts() {
        int threshold = thresholdSpinner.getValue();

        // Initialiser le notifier
        EmailService emailService = new EmailService();
        alertNotifier = new CriticalAlertNotifier(produitDAO, materielDAO, emailService);

        // Récupérer les alertes
        Map<String, Object> summary = alertNotifier.getAlertSummary(threshold);

        // Mettre à jour le résumé
        int lowStockCount = (int) summary.get("low_stock_count");
        int brokenCount = (int) summary.get("broken_count");
        updateSummaryLabel(lowStockCount, brokenCount);

        // Remplir les tables
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lowStockData =
            (List<Map<String, Object>>) summary.get("low_stock_products");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> brokenData =
            (List<Map<String, Object>>) summary.get("broken_materiels");

        // Convertir et afficher les produits en stock faible
        ObservableList<Produit> lowStockProducts = FXCollections.observableArrayList();
        for (Map<String, Object> data : lowStockData) {
            int id = ((Number) data.get("id")).intValue();
            Produit p = produitDAO.getById(id);
            if (p != null) {
                lowStockProducts.add(p);
            }
        }
        lowStockTable.setItems(lowStockProducts);

        // Convertir et afficher les matériels en panne
        ObservableList<Materiel> brokenMateriels = FXCollections.observableArrayList();
        for (Map<String, Object> data : brokenData) {
            int id = ((Number) data.get("id")).intValue();
            Materiel m = materielDAO.getById(id);
            if (m != null) {
                brokenMateriels.add(m);
            }
        }
        brokenMaterielsTable.setItems(brokenMateriels);
    }

    @FXML
    private void handleSendAlerts() {
        int threshold = thresholdSpinner.getValue();
        String toEmail = emailToField.getText().trim();

        if (toEmail.isEmpty()) {
            showWarning("Erreur", "Veuillez entrer une adresse email.");
            return;
        }

        if (!toEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showWarning("Erreur", "Format d'email invalide.");
            return;
        }

        try {
            sendAlertsButton.setDisable(true);
            sendAlertsButton.setText("Envoi en cours...");

            String fromEmail = "amnafati94@gmail.com";
            String appPassword = "jcdo lljy fgug omgr";
            String envPassword = System.getenv("GMAIL_APP_PASSWORD");
            if (envPassword != null && !envPassword.isBlank()) {
                appPassword = envPassword;
            }

            EmailService emailService = new EmailService(fromEmail, appPassword);
            alertNotifier = new CriticalAlertNotifier(produitDAO, materielDAO, emailService);

            Map<String, Object> result = alertNotifier.sendCriticalAlerts(fromEmail, toEmail, threshold);

            sendAlertsButton.setDisable(false);
            sendAlertsButton.setText("📤 Envoyer les alertes");

            if ((boolean) result.get("sent")) {
                int lowStockCount = (int) result.get("low_stock_count");
                int brokenCount = (int) result.get("panne_count");

                if (lowStockCount == 0 && brokenCount == 0) {
                    showInfo("Aucune alerte", "Aucune alerte critique à envoyer.");
                } else {
                    String message = String.format(
                        "Alertes envoyées vers %s\n\nStock faible: %d produit(s)\nMatériels en panne: %d",
                        toEmail, lowStockCount, brokenCount
                    );
                    showInfo("Succès", message);
                }
            } else {
                showError("Erreur", "Impossible d'envoyer les alertes: " +
                    result.getOrDefault("error", "Erreur inconnue"));
            }
        } catch (Exception e) {
            sendAlertsButton.setDisable(false);
            sendAlertsButton.setText("📤 Envoyer les alertes");
            showError("Erreur", "Erreur lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateSummaryLabel(int lowStockCount, int brokenCount) {
        String text = String.format(
            "📊 Résumé des alertes\n" +
            "📦 Stock faible: %d produit(s)\n" +
            "🔧 Matériels en panne: %d",
            lowStockCount, brokenCount
        );
        summaryLabel.setText(text);
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


