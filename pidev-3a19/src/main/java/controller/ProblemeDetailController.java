package controller;

import model.Diagnostique;
import model.Probleme;
import Services.DiagnostiqueService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class ProblemeDetailController {

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

    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();

    public void setProbleme(Probleme probleme) {
        typeLabel.setText(probleme.getType());
        descriptionLabel.setText(probleme.getDescription());
        graviteLabel.setText(probleme.getGravite());
        dateLabel.setText(probleme.getDateDetection().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        etatLabel.setText(probleme.getEtat());

        Diagnostique diagnostique = diagnostiqueService.afficherDiagnostiqueParProbleme(probleme.getId());
        if (diagnostique != null) {
            diagnosticStatusLabel.setText("Diagnostic disponible");
            causeLabel.setText(diagnostique.getCause());
            solutionLabel.setText(diagnostique.getSolutionProposee());
            resultatLabel.setText(diagnostique.getResultat());
        } else {
            diagnosticStatusLabel.setText("Aucun diagnostic disponible");
            causeLabel.setText("-");
            solutionLabel.setText("-");
            resultatLabel.setText("-");
        }
    }
}
