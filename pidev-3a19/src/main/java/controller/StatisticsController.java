package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import Services.PdfService;
import Services.StatisticsService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StatisticsController {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalStockLabel;
    @FXML private Label averageStockLabel;
    @FXML private Label healthScoreLabel;
    @FXML private Label expiredLabel;
    @FXML private Label expiringLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label totalValueLabel;
    @FXML private VBox stockChartPane;
    @FXML private VBox unitsChartPane;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;

    private ProduitDAO produitDAO;
    private StatisticsService statisticsService;
    private PdfService pdfService;
    private BarChart<String, Number> stockChart;
    private BarChart<String, Number> unitsChart;

    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        statisticsService = new StatisticsService(produitDAO);
        pdfService = new PdfService(produitDAO);

        CategoryAxis stockX = new CategoryAxis();
        NumberAxis stockY = new NumberAxis();
        stockChart = new BarChart<>(stockX, stockY);
        stockChart.setTitle("Top 10 Produits par Quantité");
        stockChart.setAnimated(true);
        stockX.setLabel("Produit");
        stockY.setLabel("Quantité");
        if (stockChartPane != null) stockChartPane.getChildren().add(stockChart);

        CategoryAxis unitsX = new CategoryAxis();
        NumberAxis unitsY = new NumberAxis();
        unitsChart = new BarChart<>(unitsX, unitsY);
        unitsChart.setTitle("Distribution par Unité");
        unitsChart.setAnimated(true);
        unitsX.setLabel("Unité");
        unitsY.setLabel("Nombre");
        if (unitsChartPane != null) unitsChartPane.getChildren().add(unitsChart);

        loadStatistics();
    }

    @FXML
    public void handleRefresh() {
        loadStatistics();
    }

    private void loadStatistics() {
        totalProductsLabel.setText(String.valueOf(statisticsService.getTotalProducts()));
        totalStockLabel.setText(String.valueOf(statisticsService.getTotalStock()));
        averageStockLabel.setText(String.format("%.1f", statisticsService.getAverageStock()));
        healthScoreLabel.setText(String.format("%.1f%%", statisticsService.getHealthScore()));
        expiredLabel.setText(String.valueOf(statisticsService.getExpiredProductCount()));
        expiringLabel.setText(String.valueOf(statisticsService.getExpiringProductCount()));
        lowStockLabel.setText(String.valueOf(statisticsService.getLowStockProductCount()));
        totalValueLabel.setText(String.format("%.2f €", statisticsService.getTotalStockValue()));
        updateStockChart();
        updateUnitsChart();
    }

    private void setupCharts() {
        // charts created in initialize
    }

    private void updateStockChart() {
        if (stockChart == null) return;
        try {
            stockChart.getData().clear();
            List<Produit> top10 = statisticsService.getProductsSortedByQuantity(true).stream().limit(10).toList();
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

    private void updateUnitsChart() {
        if (unitsChart == null) return;
        try {
            unitsChart.getData().clear();
            var unitMap = statisticsService.getProductsByUnit();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de produits");
            unitMap.forEach((unit, count) -> series.getData().add(new XYChart.Data<>(unit, count)));
            unitsChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
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
