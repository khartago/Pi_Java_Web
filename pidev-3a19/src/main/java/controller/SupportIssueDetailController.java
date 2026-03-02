package controller;

import model.Diagnostique;
import model.Probleme;
import Services.DiagnostiqueService;
import Services.PdfExportService;
import Utils.ImageUploadHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class SupportIssueDetailController {

    @FXML
    private Label typeLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label graviteLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label etatLabel;

    @FXML
    private Label diagnosticStatusLabel;

    @FXML
    private Label causeLabel;

    @FXML
    private Label solutionLabel;

    @FXML
    private Label resultatLabel;

    @FXML
    private Label medicamentLabel;

    @FXML
    private FlowPane photosFlowPane;

    @FXML
    private Button exportPdfButton;

    @FXML
    private TitledPane feedbackPane;

    @FXML
    private TextArea feedbackCommentArea;

    @FXML
    private Button marquerResoluButton;

    @FXML
    private Button marquerNonResoluButton;

    @FXML
    private TitledPane meteoPane;

    @FXML
    private Label meteoLabel;

    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private Probleme currentProbleme;
    private Diagnostique currentDiagnostique;

    public void setProbleme(Probleme probleme) {
        this.currentProbleme = probleme;
        typeLabel.setText(probleme.getType());
        descriptionLabel.setText(probleme.getDescription());
        graviteLabel.setText(probleme.getGravite());
        dateLabel.setText(probleme.getDateDetection().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        etatLabel.setText(probleme.getEtat());

        // Pour le fermier : n'afficher que le dernier diagnostic approuvé (révision active)
        Diagnostique diagnostique = diagnostiqueService.getDiagnostiqueActif(probleme.getId());
        this.currentDiagnostique = diagnostique;
        if (diagnostique != null) {
            diagnosticStatusLabel.setText("Diagnostic disponible");
            causeLabel.setText(diagnostique.getCause());
            solutionLabel.setText(diagnostique.getSolutionProposee());
            resultatLabel.setText(diagnostique.getResultat());
            String med = diagnostique.getMedicament();
            medicamentLabel.setText(med != null && !med.isEmpty() ? med : "-");
        } else {
            diagnosticStatusLabel.setText("Aucun diagnostic disponible");
            causeLabel.setText("-");
            solutionLabel.setText("-");
            resultatLabel.setText("-");
            medicamentLabel.setText("-");
        }

        // Afficher le feedback uniquement si diagnostic approuvé et pas encore de feedback
        boolean showFeedback = diagnostique != null && diagnostique.isApprouve()
                && (diagnostique.getFeedbackFermier() == null || diagnostique.getFeedbackFermier().isEmpty());
        if (feedbackPane != null) {
            feedbackPane.setVisible(showFeedback);
            feedbackPane.setManaged(showFeedback);
        }
        if (feedbackCommentArea != null) feedbackCommentArea.clear();
        if (marquerResoluButton != null) marquerResoluButton.setDisable(!showFeedback);
        if (marquerNonResoluButton != null) marquerNonResoluButton.setDisable(!showFeedback);

        String meteo = probleme.getMeteoSnapshot();
        if (meteoPane != null && meteoLabel != null) {
            boolean hasMeteo = meteo != null && !meteo.isEmpty();
            meteoPane.setVisible(hasMeteo);
            meteoPane.setManaged(hasMeteo);
            if (hasMeteo) {
                meteoLabel.setText(formatMeteoSnapshot(meteo));
            }
        }

        photosFlowPane.getChildren().clear();
        String photos = probleme.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            Path uploadsBase = ImageUploadHelper.getBaseUploadDir().getParent();
            for (String rel : photos.split(";")) {
                String trimmed = rel.trim();
                if (trimmed.isEmpty()) continue;
                Path full = uploadsBase.resolve(trimmed);
                File f = full.toFile();
                if (f.exists()) {
                    try {
                        Image img = new Image(f.toURI().toURL().toString());
                        ImageView iv = new ImageView(img);
                        iv.setPreserveRatio(true);
                        iv.setFitWidth(200);
                        iv.setFitHeight(200);
                        iv.setSmooth(true);
                        iv.setStyle("-fx-border-color: #E5EDE5; -fx-border-width: 1px; -fx-border-radius: 8px;");
                        photosFlowPane.getChildren().add(iv);
                    } catch (Exception e) {
                        // skip invalid image
                    }
                }
            }
        }
    }

    @FXML
    private void marquerResolu() {
        doFeedback(DiagnostiqueService.FEEDBACK_RESOLU);
    }

    @FXML
    private void marquerNonResolu() {
        doFeedback(DiagnostiqueService.FEEDBACK_NON_RESOLU);
    }

    private void doFeedback(String resolu) {
        if (currentDiagnostique == null) return;
        String commentaire = feedbackCommentArea != null ? feedbackCommentArea.getText() : null;
        if (commentaire != null) commentaire = commentaire.trim();
        diagnostiqueService.enregistrerFeedback(currentDiagnostique.getId(), resolu, commentaire);
        currentDiagnostique.setFeedbackFermier(resolu);
        currentDiagnostique.setFeedbackCommentaire(commentaire);
        currentDiagnostique.setDateFeedback(java.time.LocalDateTime.now());
        if (currentProbleme != null) {
            currentProbleme.setEtat("RESOLU".equals(resolu) ? "CLOTURE" : "REOUVERT");
        }
        if (feedbackPane != null) {
            feedbackPane.setVisible(false);
            feedbackPane.setManaged(false);
        }
        if (marquerResoluButton != null) marquerResoluButton.setDisable(true);
        if (marquerNonResoluButton != null) marquerNonResoluButton.setDisable(true);
        new Alert(Alert.AlertType.INFORMATION, "Merci pour votre retour.").showAndWait();
    }

    private String formatMeteoSnapshot(String json) {
        try {
            JsonObject o = new Gson().fromJson(json, JsonObject.class);
            if (o == null) return json;
            StringBuilder sb = new StringBuilder();
            if (o.has("temp")) sb.append("Température: ").append(o.get("temp").getAsDouble()).append(" °C\n");
            if (o.has("description")) sb.append("Conditions: ").append(o.get("description").getAsString()).append("\n");
            if (o.has("humidity") && o.get("humidity").getAsInt() > 0) sb.append("Humidité: ").append(o.get("humidity").getAsInt()).append("%");
            return sb.length() > 0 ? sb.toString() : json;
        } catch (Exception e) {
            return json;
        }
    }

    @FXML
    private void exportToPdf() {
        if (currentProbleme == null) {
            new Alert(Alert.AlertType.WARNING, "Aucun problème sélectionné.").showAndWait();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le rapport PDF");
        chooser.setInitialFileName("rapport-probleme-" + currentProbleme.getId() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = chooser.showSaveDialog(exportPdfButton != null ? exportPdfButton.getScene().getWindow() : null);
        if (file == null) return;
        byte[] pdf = new PdfExportService().generateReportPdf(currentProbleme, currentDiagnostique);
        if (pdf == null || pdf.length == 0) {
            new Alert(Alert.AlertType.ERROR, "Impossible de générer le PDF (réseau ou API indisponible).").showAndWait();
            return;
        }
        try {
            Files.write(file.toPath(), pdf);
            new Alert(Alert.AlertType.INFORMATION, "Rapport exporté : " + file.getAbsolutePath()).showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'écriture du fichier : " + e.getMessage()).showAndWait();
        }
    }
}
