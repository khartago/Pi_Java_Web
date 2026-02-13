package gui;

import Entites.Probleme;
import Services.ProblemeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FarmerDashboardController implements Initializable {

    @FXML
    private TableView<Probleme> problemeTable;

    @FXML
    private TableColumn<Probleme, String> typeColumn;

    @FXML
    private TableColumn<Probleme, String> graviteColumn;

    @FXML
    private TableColumn<Probleme, String> dateColumn;

    @FXML
    private TableColumn<Probleme, String> etatColumn;

    private ProblemeService problemeService = new ProblemeService();
    private ObservableList<Probleme> problemeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        graviteColumn.setCellValueFactory(new PropertyValueFactory<>("gravite"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDateDetection();
            if (date != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        
        loadProblemes();
    }

    private void loadProblemes() {
        problemeList.clear();
        problemeList.addAll(problemeService.afficherProblemes());
        problemeTable.setItems(problemeList);
    }

    @FXML
    private void handleNewProbleme() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/gui/probleme_form.fxml"));
            Scene scene = new Scene(root, 600, 700);
            stage.setTitle("Nouveau problème - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(500);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
            stage.setOnHidden(e -> loadProblemes());
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenDetail() {
        Probleme selectedProbleme = problemeTable.getSelectionModel().getSelectedItem();
        if (selectedProbleme == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un problème pour voir les détails.");
            alert.showAndWait();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/probleme_detail.fxml"));
            Parent root = loader.load();
            ProblemeDetailController controller = loader.getController();
            controller.setProbleme(selectedProbleme);
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 700, 600);
            stage.setTitle("Détail du problème - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/gui/home.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        handleHome(event);
    }
}
