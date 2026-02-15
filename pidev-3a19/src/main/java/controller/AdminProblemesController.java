package controller;

import model.Diagnostique;
import model.Probleme;
import Services.DiagnostiqueService;
import Services.ProblemeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminProblemesController implements Initializable {

    @FXML
    private TableView<Probleme> problemeTable;

    @FXML
    private TableColumn<Probleme, Integer> idColumn;

    @FXML
    private TableColumn<Probleme, String> typeColumn;

    @FXML
    private TableColumn<Probleme, String> fermierColumn;

    @FXML
    private TableColumn<Probleme, String> etatColumn;

    @FXML
    private ComboBox<String> etatFilterCombo;

    private ProblemeService problemeService = new ProblemeService();
    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private ObservableList<Probleme> problemeList = FXCollections.observableArrayList();
    private ObservableList<Probleme> filteredList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        fermierColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> "N/A"));
        
        etatFilterCombo.getItems().addAll("Tous", "EN_ATTENTE", "EN_COURS", "RESOLU", "FERME");
        etatFilterCombo.setValue("Tous");
        etatFilterCombo.setOnAction(e -> handleFilterEtat());
        
        loadProblemes();
    }

    private void loadProblemes() {
        problemeList.clear();
        problemeList.addAll(problemeService.afficherProblemes());
        handleFilterEtat();
    }

    @FXML
    private void handleFilterEtat() {
        String selectedEtat = etatFilterCombo.getValue();
        filteredList.clear();
        
        if (selectedEtat == null || "Tous".equals(selectedEtat)) {
            filteredList.addAll(problemeList);
        } else {
            for (Probleme p : problemeList) {
                if (selectedEtat.equals(p.getEtat())) {
                    filteredList.add(p);
                }
            }
        }
        
        problemeTable.setItems(filteredList);
    }

    @FXML
    private void handleEditDiagnostique() {
        Probleme selectedProbleme = problemeTable.getSelectionModel().getSelectedItem();
        if (selectedProbleme == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un problème pour ajouter/modifier son diagnostic.");
            alert.showAndWait();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/diagnostique_form.fxml"));
            Parent root = loader.load();
            DiagnostiqueFormController controller = loader.getController();
            controller.setProblemeId(selectedProbleme.getId());
            
            Diagnostique existingDiagnostique = diagnostiqueService.afficherDiagnostiqueParProbleme(selectedProbleme.getId());
            if (existingDiagnostique != null) {
                controller.setDiagnostique(existingDiagnostique);
            }
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setTitle("Diagnostic du problème - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(500);
            stage.setMinHeight(450);
            stage.centerOnScreen();
            
            stage.setOnHidden(e -> loadProblemes());
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
