package controller;

import model.Probleme;
import model.User;
import Services.ProblemeService;
import Utils.ImageUploadHelper;
import Utils.UserContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Button browsePhotosButton;

    @FXML
    private FlowPane photosFlowPane;

    @FXML
    private Label photosErrorLabel;

    @FXML
    private Label errorLabel;

    private ProblemeService problemeService = new ProblemeService();

    private final List<File> selectedPhotoFiles = new ArrayList<>();

    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_GRAVITE_LENGTH = 50;

    @FXML
    private void initialize() {
        graviteCombo.getItems().addAll("Faible", "Moyenne", "Élevée", "Critique");
        graviteCombo.setValue("Moyenne");
        applyMaxLength(typeField, MAX_TYPE_LENGTH);
        applyMaxLength(descriptionArea, MAX_DESCRIPTION_LENGTH);
    }

    @FXML
    private void handleBrowsePhotos(ActionEvent event) {
        photosErrorLabel.setVisible(false);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner des photos");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"));
        List<File> files = chooser.showOpenMultipleDialog(getStage(event));
        if (files == null || files.isEmpty()) return;

        long maxSize = ImageUploadHelper.getMaxFileSizeBytes();
        int maxImages = ImageUploadHelper.getMaxImages();

        for (File f : files) {
            if (selectedPhotoFiles.size() >= maxImages) {
                photosErrorLabel.setText("Maximum " + maxImages + " images autorisées.");
                photosErrorLabel.setVisible(true);
                break;
            }
            if (!ImageUploadHelper.isValidExtension(f.getName())) {
                photosErrorLabel.setText("Format non accepté : " + f.getName() + ". Utilisez JPG ou PNG.");
                photosErrorLabel.setVisible(true);
                continue;
            }
            if (!ImageUploadHelper.isValidSize(f.length())) {
                photosErrorLabel.setText("Fichier trop volumineux : " + f.getName() + " (max 5 Mo).");
                photosErrorLabel.setVisible(true);
                continue;
            }
            selectedPhotoFiles.add(f);
        }
        refreshPhotosFlowPane();
    }

    private void refreshPhotosFlowPane() {
        photosFlowPane.getChildren().clear();
        for (int i = 0; i < selectedPhotoFiles.size(); i++) {
            File file = selectedPhotoFiles.get(i);
            VBox cell = new VBox(4);
            cell.setStyle("-fx-padding: 8; -fx-border-color: #E5EDE5; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-color: white;");
            try {
                Image img = new Image(file.toURI().toString(), 80, 80, true, true);
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                cell.getChildren().add(iv);
            } catch (Exception e) {
                Label nameLabel = new Label(file.getName());
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(100);
                cell.getChildren().add(nameLabel);
            }
            Label nameLabel = new Label(file.getName());
            nameLabel.setMaxWidth(100);
            nameLabel.setWrapText(true);
            nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");
            cell.getChildren().add(nameLabel);
            Button removeBtn = new Button("Retirer");
            removeBtn.setStyle("-fx-font-size: 11px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                selectedPhotoFiles.remove(file);
                refreshPhotosFlowPane();
            });
            cell.getChildren().add(removeBtn);
            photosFlowPane.getChildren().add(cell);
        }
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
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

    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setVisible(false);
        photosErrorLabel.setVisible(false);
        String type = typeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String gravite = graviteCombo.getValue();

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
            User currentUser = UserContext.getCurrentUser();
            LocalDateTime now = LocalDateTime.now();
            Probleme probleme = new Probleme(type, description, gravite, now, "EN_ATTENTE", null);
            if (currentUser != null) {
                probleme.setIdUtilisateur(currentUser.getId());
            }
            int id = problemeService.ajouterProbleme(probleme);

            String photosValue = null;
            if (!selectedPhotoFiles.isEmpty()) {
                List<String> paths = new ArrayList<>();
                for (int i = 0; i < selectedPhotoFiles.size(); i++) {
                    Path source = selectedPhotoFiles.get(i).toPath();
                    String relativePath = ImageUploadHelper.copyWithUniqueName(source, id, i);
                    paths.add(relativePath);
                }
                photosValue = String.join(";", paths);
                Probleme toUpdate = new Probleme(id, currentUser != null ? currentUser.getId() : null, type, description, gravite, now, "EN_ATTENTE", photosValue);
                problemeService.modifierProbleme(toUpdate);
            }

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
