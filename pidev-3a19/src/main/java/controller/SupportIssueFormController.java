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

public class SupportIssueFormController {

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

    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_PHOTOS_LENGTH = 500;
    private static final int MAX_GRAVITE_LENGTH = 50;

    @FXML
    private void initialize() {
        graviteCombo.getItems().addAll("Faible", "Moyenne", "Élevée", "Critique");
        graviteCombo.setValue("Moyenne");
        applyMaxLength(typeField, MAX_TYPE_LENGTH);
        applyMaxLength(descriptionArea, MAX_DESCRIPTION_LENGTH);
        applyMaxLength(photosField, MAX_PHOTOS_LENGTH);
    }

    private void applyMaxLength(TextField field, int max) {
        field.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    private void applyMaxLength(TextArea area, int max) {
        area.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setVisible(false);
        String type = typeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String gravite = graviteCombo.getValue();
        String photos = photosField.getText().trim();

        if (type.isEmpty() || description.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs obligatoires");
            errorLabel.setVisible(true);
            return;
        }

        if (type.length() > MAX_TYPE_LENGTH) {
            errorLabel.setText("Le type ne doit pas dépasser " + MAX_TYPE_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            errorLabel.setText("La description ne doit pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (!photos.isEmpty() && photos.length() > MAX_PHOTOS_LENGTH) {
            errorLabel.setText("Le champ photos ne doit pas dépasser " + MAX_PHOTOS_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (gravite == null || gravite.isEmpty()) {
            errorLabel.setText("Veuillez sélectionner une gravité");
            errorLabel.setVisible(true);
            return;
        }

        if (gravite.length() > MAX_GRAVITE_LENGTH) {
            errorLabel.setText("La gravité ne doit pas dépasser " + MAX_GRAVITE_LENGTH + " caractères");
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
