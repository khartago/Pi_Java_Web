package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import service.PdfService;
import service.StatisticsService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour la page de statistiques
 * Affiche les KPIs, graphiques et permet l'export PDF
 */
public class StatisticsController {

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label totalStockLabel;

    @FXML
    private Label averageStockLabel;

    @FXML
    private Label healthScoreLabel;

    @FXML
    private Label expiredLabel;

    @FXML
    private Label expiringLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private BarChart<String, Number> stockChart;

    @FXML
    private BarChart<String, Number> unitsChart;

    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button backButton;

    private ProduitDAO produitDAO;
    private StatisticsService statisticsService;
    private PdfService pdfService;

    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        statisticsService = new StatisticsService(produitDAO);
        pdfService = new PdfService(produitDAO);

        setupCharts();
        loadStatistics();

        refreshButton.setOnAction(e -> loadStatistics());
        exportButton.setOnAction(e -> exportToPDF());
        backButton.setOnAction(e -> goBack());
    }

    /**
     * Charge et affiche toutes les statistiques
     */
    private void loadStatistics() {
        // Mise à jour des KPIs
        totalProductsLabel.setText(String.valueOf(statisticsService.getTotalProducts()));
        totalStockLabel.setText(String.valueOf(statisticsService.getTotalStock()));
        averageStockLabel.setText(String.format("%.1f", statisticsService.getAverageStock()));
        healthScoreLabel.setText(String.format("%.1f%%", statisticsService.getHealthScore()));

        // Mise à jour des indicateurs à risque
        expiredLabel.setText(String.valueOf(statisticsService.getExpiredProductCount()));
        expiringLabel.setText(String.valueOf(statisticsService.getExpiringProductCount()));
        lowStockLabel.setText(String.valueOf(statisticsService.getLowStockProductCount()));
        totalValueLabel.setText(String.format("%.2f€", statisticsService.getTotalStockValue()));

        // Mise à jour des graphiques
        updateStockChart();
        updateUnitsChart();
    }

    /**
     * Configure les graphiques
     */
    private void setupCharts() {
        // Stock Chart
        if (stockChart != null) {
            stockChart.setTitle("Top 10 Produits par Quantité");
            stockChart.setAnimated(true);
        }

        // Units Chart
        if (unitsChart != null) {
            unitsChart.setTitle("Distribution par Unité");
            unitsChart.setAnimated(true);
        }
    }

    /**
     * Miseà jour du graphique des stocks
     */
    private void updateStockChart() {
        if (stockChart == null) return;

        try {
            stockChart.getData().clear();

            List<Produit> sorted = statisticsService.getProductsSortedByQuantity(true);
            // Limiter à top 10
            List<Produit> top10 = sorted.stream().limit(10).toList();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Quantités");

            for (Produit p : top10) {
                series.getData().add(new XYChart.Data<>(p.getNom(), p.getQuantite()));
            }

            stockChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mise à jour du graphique des unités
     */
    private void updateUnitsChart() {
        if (unitsChart == null) return;

        try {
            unitsChart.getData().clear();

            var unitMap = statisticsService.getProductsByUnit();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de produits");

            unitMap.forEach((unit, count) -> {
                series.getData().add(new XYChart.Data<>(unit, count));
            });

            unitsChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exporte les statistiques en PDF
     */
    @FXML
    private void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("rapport_statistiques.pdf");

        File selectedFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                pdfService.generateComprehensiveReport(selectedFile.getAbsolutePath());
                showInfo("Succès", "Rapport PDF généré avec succès:\n" + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Erreur lors de la génération du PDF: " + e.getMessage());
            }
        }
    }

    /**
     * Retour à la page précédente
     */
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Gestion Produits");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à la page précédente.");
        }
    }

    // ===== Helpers =====

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

