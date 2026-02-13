package gui;

import Entites.Diagnostique;
import Services.DiagnostiqueService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class DiagnostiqueFormController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea causeArea;

    @FXML
    private TextArea solutionArea;

    @FXML
    private ComboBox<String> resultatCombo;

    @FXML
    private Label errorLabel;

    private DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
    private int problemeId = -1;
    private Diagnostique currentDiagnostique = null;

    @FXML
    private void initialize() {
        resultatCombo.getItems().addAll("Résolu", "En cours", "Échec", "En attente");
        resultatCombo.setValue("En attente");
    }

    public void setProblemeId(int id) {
        this.problemeId = id;
    }

    public void setDiagnostique(Diagnostique diagnostique) {
        this.currentDiagnostique = diagnostique;
        this.problemeId = diagnostique.getIdProbleme();
        titleLabel.setText("Modifier diagnostic");
        causeArea.setText(diagnostique.getCause());
        solutionArea.setText(diagnostique.getSolutionProposee());
        resultatCombo.setValue(diagnostique.getResultat());
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String cause = causeArea.getText().trim();
        String solution = solutionArea.getText().trim();
        String resultat = resultatCombo.getValue();

        if (cause.isEmpty() || solution.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
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
                        resultat
                );
                diagnostiqueService.ajouterDiagnostique(newDiagnostique);
            } else {
                currentDiagnostique.setCause(cause);
                currentDiagnostique.setSolutionProposee(solution);
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
