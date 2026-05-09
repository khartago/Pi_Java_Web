package controller;

import Services.DiagnosticAnalyticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DiagnosticAnalyticsController implements Initializable {

    @FXML
    private Button refreshButton;

    @FXML
    private Label tauxResolutionLabel;

    @FXML
    private Label dureeMoyenneLabel;

    @FXML
    private VBox typesChartPane;

    @FXML
    private VBox causesChartPane;

    @FXML
    private VBox resolutionChartPane;

    private DiagnosticAnalyticsService analyticsService = new DiagnosticAnalyticsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refresh();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    private void refresh() {
        double taux = analyticsService.getTauxResolution();
        tauxResolutionLabel.setText(String.format("%.1f%%", taux * 100));

        double dureeH = analyticsService.getDureeMoyenneDiagnostic();
        if (dureeH <= 0) {
            dureeMoyenneLabel.setText("—");
        } else if (dureeH < 24) {
            dureeMoyenneLabel.setText(String.format("%.1f heures", dureeH));
        } else {
            dureeMoyenneLabel.setText(String.format("%.1f jours", dureeH / 24));
        }

        buildTypesChart();
        buildCausesChart();
        buildResolutionPieChart();
    }

    private void buildTypesChart() {
        typesChartPane.getChildren().clear();
        Map<String, Long> data = analyticsService.getProblemesParType();
        if (data.isEmpty()) {
            typesChartPane.getChildren().add(new Label("Aucune donnée"));
            return;
        }
        BarChart<String, Number> chart = new BarChart<>(new javafx.scene.chart.CategoryAxis(), new javafx.scene.chart.NumberAxis());
        chart.setTitle("Problèmes par type");
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart.getData().add(series);
        chart.setPrefSize(430, 250);
        typesChartPane.getChildren().add(chart);
    }

    private void buildCausesChart() {
        causesChartPane.getChildren().clear();
        Map<String, Long> data = analyticsService.getCausesFrequentes();
        if (data.isEmpty()) {
            causesChartPane.getChildren().add(new Label("Aucune donnée"));
            return;
        }
        BarChart<String, Number> chart = new BarChart<>(new javafx.scene.chart.CategoryAxis(), new javafx.scene.chart.NumberAxis());
        chart.setTitle("Causes fréquentes (top 10)");
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart.getData().add(series);
        chart.setPrefSize(430, 250);
        causesChartPane.getChildren().add(chart);
    }

    private void buildResolutionPieChart() {
        resolutionChartPane.getChildren().clear();
        double taux = analyticsService.getTauxResolution();
        double nonResolus = Math.max(0, 1.0 - taux);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Resolus", taux),
                new PieChart.Data("Non resolus", nonResolus)
        );
        PieChart chart = new PieChart(pieData);
        chart.setTitle("Taux de résolution");
        chart.setPrefSize(380, 200);
        resolutionChartPane.getChildren().add(chart);
    }
}
