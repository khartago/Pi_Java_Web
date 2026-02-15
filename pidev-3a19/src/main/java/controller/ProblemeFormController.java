package controller;

import model.Probleme;
import Services.ProblemeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class ProblemeFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField typeField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> graviteCombo;

    @FXML
    private TextField photosField;

    @FXML
    private Label errorLabel;

    private ProblemeService problemeService = new ProblemeService();

    @FXML
    private void initialize() {
        graviteCombo.getItems().addAll("Faible", "Moyenne", "Élevée", "Critique");
        graviteCombo.setValue("Moyenne");
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String type = typeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String gravite = graviteCombo.getValue();
        String photos = photosField.getText().trim();

        if (type.isEmpty() || description.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs obligatoires");
            errorLabel.setVisible(true);
            return;
        }

        if (gravite == null || gravite.isEmpty()) {
            errorLabel.setText("Veuillez sélectionner une gravité");
            errorLabel.setVisible(true);
            return;
        }

        try {
            Probleme probleme = new Probleme(
                    type,
                    description,
                    gravite,
                    LocalDateTime.now(),
                    "EN_ATTENTE",
                    photos.isEmpty() ? null : photos
            );

            problemeService.ajouterProbleme(probleme);
            
            Stage stage = getStage(event);
            stage.close();
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de la sauvegarde: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = getStage(event);
        stage.close();
    }
}
