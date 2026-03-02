package controller;

import model.Diagnostique;
import model.Probleme;
import model.User;
import Services.DiagnostiqueService;
import Services.ProblemeService;
import Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSupportController implements Initializable {

    @FXML
    private TableView<Probleme> problemeTable;

    @FXML
    private TableColumn<Probleme, Integer> idColumn;

    @FXML
    private TableColumn<Probleme, String> typeColumn;

    @FXML
    private TableColumn<Probleme, String> descriptionColumn;

    @FXML
    private TableColumn<Probleme, String> graviteColumn;

    @FXML
    private TableColumn<Probleme, String> etatColumn;

    @FXML
    private TableColumn<Probleme, String> assigneeColumn;

    @FXML
    private TableColumn<Probleme, String> fermierColumn;

    @FXML
    private ComboBox<User> assignerCombo;

    @FXML
    private ComboBox<String> typeFilterCombo;

    @FXML
    private ComboBox<String> graviteFilterCombo;

    @FXML
    private ComboBox<String> etatFilterCombo;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private ComboBox<String> triCombo;

    @FXML
    private Label emptyMessageLabel;

    @FXML
    private Button accepterDiagnosticButton;

    private ProblemeService problemeService = new ProblemeService();
    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private UserService userService = new UserService();
    private ObservableList<Probleme> filteredList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(cellData -> {
            Probleme p = cellData.getValue();
            String desc = p != null ? p.getDescription() : null;
            return new SimpleStringProperty(desc != null && !desc.isEmpty() ? desc : "—");
        });
        graviteColumn.setCellValueFactory(new PropertyValueFactory<>("gravite"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        assigneeColumn.setCellValueFactory(cellData -> {
            Probleme p = cellData.getValue();
            if (p == null || p.getIdAdminAssignee() == null) return new SimpleStringProperty("—");
            String nom = userService.getNomById(p.getIdAdminAssignee());
            return new SimpleStringProperty(nom != null ? nom : "—");
        });
        fermierColumn.setCellValueFactory(cellData -> {
            Probleme p = cellData.getValue();
            if (p == null || p.getIdUtilisateur() == null) return new SimpleStringProperty("—");
            String nom = userService.getNomById(p.getIdUtilisateur());
            return new SimpleStringProperty(nom != null ? nom : "—");
        });

        assignerCombo.getItems().addAll(userService.getAdmins());
        assignerCombo.setConverter(new javafx.util.StringConverter<User>() {
            @Override public String toString(User u) { return u != null ? u.getNom() : ""; }
            @Override public User fromString(String s) { return null; }
        });

        typeFilterCombo.getItems().add("Tous");
        typeFilterCombo.setValue("Tous");
        graviteFilterCombo.getItems().addAll("Tous", "Faible", "Moyenne", "Élevée", "Critique");
        graviteFilterCombo.setValue("Tous");
        etatFilterCombo.getItems().addAll("Tous", "EN_ATTENTE", "DIAGNOSTIQUE_DISPONIBLE", "REOUVERT", "CLOTURE");
        etatFilterCombo.setValue("Tous");

        triCombo.getItems().addAll("Date (plus récent)", "Date (plus ancien)", "Gravité", "Type");
        triCombo.setValue("Date (plus récent)");

        problemeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateAccepterButtonVisibility());
        problemeTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && problemeTable.getSelectionModel().getSelectedItem() != null) {
                handleEditDiagnostique();
            }
        });
        loadProblemes();
    }

    private void updateAccepterButtonVisibility() {
        Probleme p = problemeTable.getSelectionModel().getSelectedItem();
        if (p == null) {
            accepterDiagnosticButton.setVisible(false);
            return;
        }
        Diagnostique d = diagnostiqueService.afficherDiagnostiqueParProbleme(p.getId());
        accepterDiagnosticButton.setVisible(d != null && !d.isApprouve());
    }

    private void loadProblemes() {
        List<String> types = problemeService.getTypesDistincts();
        String currentType = typeFilterCombo.getValue();
        typeFilterCombo.getItems().clear();
        typeFilterCombo.getItems().add("Tous");
        typeFilterCombo.getItems().addAll(types);
        if (currentType != null && typeFilterCombo.getItems().contains(currentType)) {
            typeFilterCombo.setValue(currentType);
        } else {
            typeFilterCombo.setValue("Tous");
        }
        applyFiltersAndSort();
    }

    @FXML
    private void applyFiltersAndSort() {
        String type = typeFilterCombo.getValue();
        String gravite = graviteFilterCombo.getValue();
        String etat = etatFilterCombo.getValue();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String triValue = triCombo.getValue();
        String ordreTri = "date_desc";
        if (triValue != null) {
            switch (triValue) {
                case "Date (plus ancien)": ordreTri = "date_asc"; break;
                case "Gravité": ordreTri = "gravite_desc"; break;
                case "Type": ordreTri = "type_asc"; break;
                default: ordreTri = "date_desc"; break;
            }
        }
        filteredList.clear();
        filteredList.addAll(problemeService.afficherProblemesFiltresEtTries(type, gravite, etat, dateDebut, dateFin, ordreTri));
        problemeTable.setItems(filteredList);
        emptyMessageLabel.setVisible(filteredList.isEmpty());
        updateAccepterButtonVisibility();
    }

    @FXML
    private void handleAccepterDiagnostic() {
        Probleme selectedProbleme = problemeTable.getSelectionModel().getSelectedItem();
        if (selectedProbleme == null) {
            return;
        }
        Diagnostique d = diagnostiqueService.afficherDiagnostiqueParProbleme(selectedProbleme.getId());
        if (d == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun diagnostic");
            alert.setHeaderText(null);
            alert.setContentText("Ce problème n'a pas encore de diagnostic. Éditez-le d'abord pour en créer un.");
            alert.showAndWait();
            return;
        }
        if (d.isApprouve()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Déjà accepté");
            alert.setHeaderText(null);
            alert.setContentText("Ce diagnostic est déjà visible par le fermier.");
            alert.showAndWait();
            return;
        }
        diagnostiqueService.approuverDiagnostique(d.getId());
        applyFiltersAndSort();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Diagnostic accepté");
        alert.setHeaderText(null);
        alert.setContentText("Le diagnostic est maintenant visible par le fermier. Le statut du problème a été mis à jour.");
        alert.showAndWait();
        updateAccepterButtonVisibility();
    }

    @FXML
    private void handleAssigner() {
        Probleme p = problemeTable.getSelectionModel().getSelectedItem();
        User admin = assignerCombo.getValue();
        if (p == null || admin == null) return;
        problemeService.assignerProbleme(p.getId(), admin.getId());
        p.setIdAdminAssignee(admin.getId());
        applyFiltersAndSort();
        assignerCombo.setValue(null);
    }

    @FXML
    private void handleResetFilters() {
        typeFilterCombo.setValue("Tous");
        graviteFilterCombo.setValue("Tous");
        etatFilterCombo.setValue("Tous");
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        triCombo.setValue("Date (plus récent)");
        applyFiltersAndSort();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/support_diagnostic_form.fxml"));
            Parent root = loader.load();
            SupportDiagnosticFormController controller = loader.getController();
            controller.setProblemeId(selectedProbleme.getId());

            boolean isReouvert = "REOUVERT".equals(selectedProbleme.getEtat());
            if (isReouvert) {
                controller.setRevisionMode(true);
                controller.setDiagnostiquesPrecedents(diagnostiqueService.getDiagnostiquesParProbleme(selectedProbleme.getId()));
            } else {
                Diagnostique existingDiagnostique = diagnostiqueService.afficherDiagnostiqueParProbleme(selectedProbleme.getId());
                if (existingDiagnostique != null) {
                    controller.setDiagnostique(existingDiagnostique);
                }
            }

            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setTitle("Diagnostic - FARMTECH");
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
