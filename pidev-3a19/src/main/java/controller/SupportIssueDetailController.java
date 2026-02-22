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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

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

        // Pour le fermier : n'afficher que le diagnostic approuvé par l'admin
        Diagnostique diagnostique = diagnostiqueService.afficherDiagnostiqueParProblemeApprouve(probleme.getId());
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
