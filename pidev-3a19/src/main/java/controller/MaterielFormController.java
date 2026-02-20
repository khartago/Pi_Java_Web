package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Materiel;
import model.MaterielDAO;
import model.Produit;

import java.time.LocalDate;

public class MaterielFormController {

    @FXML private TextField nomField;
    @FXML private TextField etatField;
    @FXML private DatePicker dateAchatPicker;
    @FXML private TextField coutField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private MaterielDAO materielDAO;
    private Materiel materiel;
    private Produit produit;

    // ================= INITIALIZE =================
    @FXML
    private void initialize() {

        // Bloquer toutes les dates passées (création + modification)
        dateAchatPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    // ================= SETTERS =================
    public void setMaterielDAO(MaterielDAO dao) {
        this.materielDAO = dao;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public void setMateriel(Materiel materiel) {
        if (materiel != null) {
            this.materiel = materiel;
            nomField.setText(materiel.getNom());
            etatField.setText(materiel.getEtat());
            if (materiel.getDateAchat() != null) {
                dateAchatPicker.setValue(materiel.getDateAchat());
            }
            coutField.setText(String.valueOf(materiel.getCout()));
        } else {
            this.materiel = new Materiel();
        }
    }

    // ================= SAVE =================
    @FXML
    private void handleSave() {

        if (produit == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Produit requis");
            alert.setHeaderText(null);
            alert.setContentText("Aucun produit n'est associé à cette vue. Revenez à la liste des produits, sélectionnez un produit puis ouvrez ses matériels.");
            alert.showAndWait();
            return;
        }

        if (!validateInput()) return;

        materiel.setNom(nomField.getText().trim());
        materiel.setEtat(etatField.getText().trim());
        materiel.setDateAchat(dateAchatPicker.getValue());
        materiel.setCout(Double.parseDouble(coutField.getText().trim()));
        materiel.setIdProduit(produit.getIdProduit());

        boolean success = (materiel.getIdMateriel() == 0)
                ? materielDAO.insert(materiel)
                : materielDAO.update(materiel);

        if (!success) {
            showError("Impossible d'enregistrer le matériel.");
        } else {
            closeWindow();
        }
    }

    // ================= CANCEL =================
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // ================= VALIDATION =================
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty())
            errors.append("- Le nom du matériel est requis.\n");

        if (etatField.getText() == null || etatField.getText().trim().isEmpty())
            errors.append("- L'état du matériel est requis.\n");

        LocalDate selectedDate = dateAchatPicker.getValue();
        if (selectedDate == null) {
            errors.append("- La date d'achat est requise.\n");
        } else if (selectedDate.isBefore(LocalDate.now())) {
            errors.append("- La date d'achat doit être aujourd’hui ou future.\n");
        }

        if (coutField.getText() == null || coutField.getText().trim().isEmpty()) {
            errors.append("- Le coût est requis.\n");
        } else {
            try {
                double cost = Double.parseDouble(coutField.getText().trim());
                if (cost < 0) errors.append("- Le coût doit être positif.\n");
            } catch (NumberFormatException e) {
                errors.append("- Le coût doit être un nombre valide.\n");
            }
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    // ================= UTIL =================
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
