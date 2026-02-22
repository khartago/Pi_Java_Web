package controller;

import model.Diagnostique;
import model.DiagnosticSuggestion;
import model.Probleme;
import Services.DiagnostiqueService;
import Services.DiagnosticAIService;
import Services.ProblemeService;
import Utils.ImageUploadHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class SupportDiagnosticFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea causeArea;

    @FXML
    private TextArea solutionArea;

    @FXML
    private TextArea medicamentArea;

    @FXML
    private ComboBox<String> resultatCombo;

    @FXML
    private Button generateAIButton;

    @FXML
    private ProgressIndicator aiProgressIndicator;

    @FXML
    private Label aiStatusLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private FlowPane photosFlowPane;

    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private ProblemeService problemeService = new ProblemeService();
    private DiagnosticAIService diagnosticAIService = new DiagnosticAIService();
    private int problemeId = -1;
    private Diagnostique currentDiagnostique = null;

    private static final int MAX_CAUSE_SOLUTION_LENGTH = 2000;

    @FXML
    private void initialize() {
        resultatCombo.getItems().addAll("Résolu", "En cours", "Échec", "En attente");
        resultatCombo.setValue("En attente");
        applyMaxLength(causeArea, MAX_CAUSE_SOLUTION_LENGTH);
        applyMaxLength(solutionArea, MAX_CAUSE_SOLUTION_LENGTH);
    }

    private void applyMaxLength(TextArea area, int max) {
        area.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    public void setProblemeId(int id) {
        this.problemeId = id;
        refreshPhotos();
    }

    public void setDiagnostique(Diagnostique diagnostique) {
        this.currentDiagnostique = diagnostique;
        this.problemeId = diagnostique.getIdProbleme();
        titleLabel.setText("Modifier diagnostic");
        causeArea.setText(diagnostique.getCause());
        solutionArea.setText(diagnostique.getSolutionProposee());
        medicamentArea.setText(diagnostique.getMedicament() != null ? diagnostique.getMedicament() : "");
        resultatCombo.setValue(diagnostique.getResultat());
        refreshPhotos();
    }

    /** Affiche les photos du problème dans le formulaire (pour l'admin). */
    private void refreshPhotos() {
        if (photosFlowPane == null) return;
        photosFlowPane.getChildren().clear();
        if (problemeId == -1) return;
        Probleme p = problemeService.getProblemeById(problemeId);
        if (p == null || p.getPhotos() == null || p.getPhotos().isEmpty()) return;
        Path uploadsBase = ImageUploadHelper.getBaseUploadDir().getParent();
        for (String rel : p.getPhotos().split(";")) {
            String trimmed = rel.trim();
            if (trimmed.isEmpty()) continue;
            Path full = uploadsBase.resolve(trimmed);
            File f = full.toFile();
            if (f.exists()) {
                try {
                    // Chargement en pleine résolution pour une photo plus claire (pas de réduction à l'import)
                    Image img = new Image(f.toURI().toURL().toString());
                    ImageView iv = new ImageView(img);
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(220);
                    iv.setFitHeight(220);
                    iv.setSmooth(true);
                    iv.setStyle("-fx-border-color: #E5EDE5; -fx-border-width: 1px; -fx-border-radius: 8px;");
                    photosFlowPane.getChildren().add(iv);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleGenerateWithAI(ActionEvent event) {
        if (problemeId == -1) {
            errorLabel.setText("Aucun problème sélectionné");
            errorLabel.setVisible(true);
            return;
        }
        errorLabel.setVisible(false);
        generateAIButton.setDisable(true);
        aiProgressIndicator.setVisible(true);
        aiStatusLabel.setVisible(true);

        CompletableFuture.supplyAsync(() -> {
            Probleme probleme = problemeService.getProblemeById(problemeId);
            if (probleme == null) throw new RuntimeException("Problème introuvable.");
            return diagnosticAIService.generateFromProbleme(probleme);
        }).thenAccept(suggestion -> Platform.runLater(() -> {
            causeArea.setText(suggestion.getCause() != null ? suggestion.getCause() : "");
            solutionArea.setText(suggestion.getSolutionProposee() != null ? suggestion.getSolutionProposee() : "");
            medicamentArea.setText(suggestion.getMedicament() != null ? suggestion.getMedicament() : "");
            resultatCombo.setValue("En attente");
            generateAIButton.setDisable(false);
            aiProgressIndicator.setVisible(false);
            aiStatusLabel.setVisible(false);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                errorLabel.setText(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                errorLabel.setVisible(true);
                generateAIButton.setDisable(false);
                aiProgressIndicator.setVisible(false);
                aiStatusLabel.setVisible(false);
            });
            return null;
        });
    }

    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setVisible(false);
        String cause = causeArea.getText().trim();
        String solution = solutionArea.getText().trim();
        String medicament = medicamentArea.getText().trim();
        String resultat = resultatCombo.getValue();

        if (cause.isEmpty() || solution.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        if (cause.length() > MAX_CAUSE_SOLUTION_LENGTH || solution.length() > MAX_CAUSE_SOLUTION_LENGTH) {
            errorLabel.setText("La cause et la solution ne doivent pas dépasser " + MAX_CAUSE_SOLUTION_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (resultat == null || resultat.isEmpty()) {
            errorLabel.setText("Veuillez sélectionner un résultat");
            errorLabel.setVisible(true);
            return;
        }

        if (problemeId == -1) {
            errorLabel.setText("Aucun problème sélectionné");
            errorLabel.setVisible(true);
            return;
        }

        try {
            if (currentDiagnostique == null) {
                Diagnostique newDiagnostique = new Diagnostique(
                        problemeId,
                        cause,
                        solution,
                        LocalDateTime.now(),
                        resultat,
                        medicament.isEmpty() ? null : medicament
                );
                diagnostiqueService.ajouterDiagnostique(newDiagnostique);
            } else {
                currentDiagnostique.setCause(cause);
                currentDiagnostique.setSolutionProposee(solution);
                currentDiagnostique.setMedicament(medicament.isEmpty() ? null : medicament);
                currentDiagnostique.setResultat(resultat);
                currentDiagnostique.setDateDiagnostique(LocalDateTime.now());
                diagnostiqueService.modifierDiagnostique(currentDiagnostique);
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
