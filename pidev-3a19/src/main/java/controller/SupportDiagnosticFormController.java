package controller;

import model.Diagnostique;
import model.DiagnosticSuggestion;
import model.Probleme;
import model.User;
import Services.DiagnostiqueService;
import Services.DiagnosticAIService;
import Services.ProblemeService;
import Utils.UserContext;
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
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SupportDiagnosticFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TitledPane problemeInfoPane;

    @FXML
    private Label problemeTypeLabel;

    @FXML
    private Label problemeDescriptionLabel;

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

    @FXML
    private TitledPane meteoPane;

    @FXML
    private Label meteoLabel;

    @FXML
    private TitledPane revisionsPane;

    @FXML
    private javafx.scene.layout.VBox revisionsContent;

    @FXML
    private TitledPane similairesPane;

    @FXML
    private javafx.scene.layout.VBox similairesContent;

    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private ProblemeService problemeService = new ProblemeService();
    private DiagnosticAIService diagnosticAIService = new DiagnosticAIService();
    private int problemeId = -1;
    private Diagnostique currentDiagnostique = null;
    private boolean revisionMode = false;
    private List<Diagnostique> diagnostiquesPrecedents = null;

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
        refreshProblemeInfo();
        refreshPhotos();
        refreshMeteo();
        refreshSimilaires();
    }

    public void setDiagnostique(Diagnostique diagnostique) {
        this.currentDiagnostique = diagnostique;
        this.problemeId = diagnostique.getIdProbleme();
        titleLabel.setText("Modifier diagnostic");
        causeArea.setText(diagnostique.getCause());
        solutionArea.setText(diagnostique.getSolutionProposee());
        medicamentArea.setText(diagnostique.getMedicament() != null ? diagnostique.getMedicament() : "");
        resultatCombo.setValue(diagnostique.getResultat());
        refreshProblemeInfo();
        refreshPhotos();
        refreshMeteo();
        refreshSimilaires();
    }

    public void setRevisionMode(boolean revisionMode) {
        this.revisionMode = revisionMode;
        if (titleLabel != null) {
            titleLabel.setText(revisionMode ? "Créer révision du diagnostic" : "Diagnostic du problème");
        }
        refreshRevisionsPane();
    }

    public void setDiagnostiquesPrecedents(List<Diagnostique> list) {
        this.diagnostiquesPrecedents = list;
        refreshRevisionsPane();
    }

    private void refreshProblemeInfo() {
        if (problemeInfoPane == null || problemeTypeLabel == null || problemeDescriptionLabel == null) return;
        if (problemeId == -1) return;
        Probleme p = problemeService.getProblemeById(problemeId);
        if (p == null) {
            problemeInfoPane.setVisible(false);
            problemeInfoPane.setManaged(false);
            return;
        }
        problemeInfoPane.setVisible(true);
        problemeInfoPane.setManaged(true);
        problemeTypeLabel.setText("Type: " + (p.getType() != null ? p.getType() : "—"));
        problemeDescriptionLabel.setText(p.getDescription() != null && !p.getDescription().isEmpty()
                ? p.getDescription() : "Aucune description fournie.");
    }

    private void refreshRevisionsPane() {
        if (revisionsPane == null || revisionsContent == null) return;
        revisionsPane.setVisible(revisionMode && diagnostiquesPrecedents != null && !diagnostiquesPrecedents.isEmpty());
        revisionsPane.setManaged(revisionsPane.isVisible());
        revisionsContent.getChildren().clear();
        if (diagnostiquesPrecedents == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Diagnostique d : diagnostiquesPrecedents) {
            Label label = new Label(
                "Révision " + d.getNumRevision() + " - " + d.getDateDiagnostique().format(fmt)
                + (d.isApprouve() ? " (approuvé)" : "")
                + "\nCause: " + (d.getCause() != null ? d.getCause() : "-")
                + "\nSolution: " + (d.getSolutionProposee() != null ? d.getSolutionProposee() : "-")
            );
            label.setWrapText(true);
            label.setStyle("-fx-padding: 8; -fx-background-color: #F3F4F6; -fx-background-radius: 6; -fx-font-size: 12px;");
            label.setMaxWidth(Double.MAX_VALUE);
            revisionsContent.getChildren().add(label);
        }
    }

    private void refreshMeteo() {
        if (meteoPane == null || meteoLabel == null) return;
        if (problemeId == -1) return;
        Probleme p = problemeService.getProblemeById(problemeId);
        String meteo = p != null ? p.getMeteoSnapshot() : null;
        boolean hasMeteo = meteo != null && !meteo.isEmpty();
        meteoPane.setVisible(hasMeteo);
        meteoPane.setManaged(hasMeteo);
        if (hasMeteo) {
            try {
                com.google.gson.JsonObject o = new com.google.gson.Gson().fromJson(meteo, com.google.gson.JsonObject.class);
                if (o != null) {
                    StringBuilder sb = new StringBuilder();
                    if (o.has("temp")) sb.append("Température: ").append(o.get("temp").getAsDouble()).append(" °C\n");
                    if (o.has("description")) sb.append("Conditions: ").append(o.get("description").getAsString()).append("\n");
                    if (o.has("humidity") && o.get("humidity").getAsInt() > 0) sb.append("Humidité: ").append(o.get("humidity").getAsInt()).append("%");
                    meteoLabel.setText(sb.length() > 0 ? sb.toString() : meteo);
                } else {
                    meteoLabel.setText(meteo);
                }
            } catch (Exception e) {
                meteoLabel.setText(meteo);
            }
        }
    }

    private void refreshSimilaires() {
        if (similairesPane == null || similairesContent == null) return;
        if (problemeId == -1) return;
        Probleme p = problemeService.getProblemeById(problemeId);
        if (p == null || p.getType() == null) return;
        List<Diagnostique> similaires = diagnostiqueService.getDiagnostiquesSimilaires(p.getType(), problemeId, 5);
        similairesPane.setVisible(!similaires.isEmpty());
        similairesPane.setManaged(!similaires.isEmpty());
        similairesContent.getChildren().clear();
        for (Diagnostique d : similaires) {
            javafx.scene.control.Button copyBtn = new javafx.scene.control.Button("Copier");
            copyBtn.setStyle("-fx-font-size: 11px; -fx-cursor: hand;");
            javafx.scene.control.Label label = new javafx.scene.control.Label(
                "Cause: " + (d.getCause() != null ? d.getCause() : "-") + "\n" +
                "Solution: " + (d.getSolutionProposee() != null ? d.getSolutionProposee() : "-")
            );
            label.setWrapText(true);
            label.setStyle("-fx-padding: 8; -fx-background-color: #F3F4F6; -fx-background-radius: 6; -fx-font-size: 12px;");
            label.setMaxWidth(Double.MAX_VALUE);
            copyBtn.setOnAction(e -> {
                causeArea.setText(d.getCause() != null ? d.getCause() : "");
                solutionArea.setText(d.getSolutionProposee() != null ? d.getSolutionProposee() : "");
                medicamentArea.setText(d.getMedicament() != null ? d.getMedicament() : "");
            });
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(4);
            card.getChildren().addAll(label, copyBtn);
            card.setStyle("-fx-padding: 8; -fx-border-color: #E5E7EB; -fx-border-width: 1px; -fx-border-radius: 6;");
            similairesContent.getChildren().add(card);
        }
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
            User currentUser = UserContext.getCurrentUser();
            Integer adminId = (currentUser != null && "ADMIN".equals(currentUser.getRole())) ? currentUser.getId() : null;

            if (revisionMode) {
                Diagnostique newDiagnostique = new Diagnostique(
                        problemeId,
                        cause,
                        solution,
                        LocalDateTime.now(),
                        resultat,
                        medicament.isEmpty() ? null : medicament
                );
                if (adminId != null) newDiagnostique.setIdAdminDiagnostiqueur(adminId);
                diagnostiqueService.creerRevision(problemeId, newDiagnostique);
            } else if (currentDiagnostique == null) {
                Diagnostique newDiagnostique = new Diagnostique(
                        problemeId,
                        cause,
                        solution,
                        LocalDateTime.now(),
                        resultat,
                        medicament.isEmpty() ? null : medicament
                );
                if (adminId != null) newDiagnostique.setIdAdminDiagnostiqueur(adminId);
                diagnostiqueService.ajouterDiagnostique(newDiagnostique);
            } else {
                currentDiagnostique.setCause(cause);
                currentDiagnostique.setSolutionProposee(solution);
                currentDiagnostique.setMedicament(medicament.isEmpty() ? null : medicament);
                currentDiagnostique.setResultat(resultat);
                currentDiagnostique.setDateDiagnostique(LocalDateTime.now());
                if (adminId != null) currentDiagnostique.setIdAdminDiagnostiqueur(adminId);
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
