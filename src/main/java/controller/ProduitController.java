package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Produit;
import model.ProduitDAO;
import service.EmailService;
import service.ExpirationNotifierService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Optional;

public class ProduitController {

    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, Integer> idColumn;
    @FXML private TableColumn<Produit, String> nomColumn;
    @FXML private TableColumn<Produit, Integer> quantiteColumn;
    @FXML private TableColumn<Produit, String> uniteColumn;
    @FXML private TableColumn<Produit, String> dateExpColumn;

    private final ProduitDAO produitDAO = new ProduitDAO();
    private ObservableList<Produit> produits;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));

        dateExpColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateExpiration() != null) {
                String formatted = cellData.getValue().getDateExpiration()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return new SimpleStringProperty(formatted);
            } else {
                return new SimpleStringProperty("");
            }
        });

        loadProduits();
    }

    private void loadProduits() {
        produits = FXCollections.observableArrayList(produitDAO.getAll());
        produitTable.setItems(produits);
    }

    // ================== CRUD ==================

    @FXML
    private void handleAjouter() {
        openProduitForm(null);
    }

    @FXML
    private void handleModifier() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openProduitForm(selected);
        } else {
            showWarning("Sélection requise", "Veuillez sélectionner un produit à modifier.");
        }
    }

    @FXML
    private void handleSupprimer() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer le produit");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le produit " + selected.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (produitDAO.delete(selected.getIdProduit())) {
                loadProduits();
            } else {
                showError("Erreur lors de la suppression",
                        "Impossible de supprimer ce produit. Il est peut-être référencé par un matériel.");
            }
        }
    }

    // ================== NAVIGATION ==================

    @FXML
    private void handleVoirMateriels() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un produit pour voir ses matériels.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/materiel_list.fxml"));
            Parent root = loader.load();

            MaterielController controller = loader.getController();
            controller.setProduit(selected);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Matériels de " + selected.getNom());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProduits();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la liste des matériels.");
        }
    }

    @FXML
    private void handleOpenMarketplace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/marketplace.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) produitTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Marketplace - Catalogue");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la Marketplace.");
        }
    }

    @FXML
    private void handleOpenAssistant() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/assistant.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) produitTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Assistant IA - Détails produit");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir l'assistant IA.");
        }
    }

    @FXML
    private void handleTraceabilite() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Veuillez sélectionner un produit pour voir sa traçabilité.");
            return;
        }

        try {
            TraceabiliteController.openForProduit(selected);
            loadProduits();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la traçabilité.");
        }
    }

    @FXML
    private void handleScanQr() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/qr_scan.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Scanner QR Code");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProduits();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le scanner QR.");
        }
    }

    // ================== EMAIL EXPIRATION ==================

    @FXML
    private void handleNotifierExpiration() {
        try {
            // Option 1: Utiliser une boîte de dialogue pour entrer l'email
            TextInputDialog dialog = new TextInputDialog("votre.email@example.com");
            dialog.setTitle("Notification d'expiration");
            dialog.setHeaderText("Entrez l'email destinataire");
            dialog.setContentText("Email:");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return;

            String recipient = result.get().trim();
            if (recipient.isEmpty()) {
                showWarning("Email requis", "Veuillez entrer une adresse email.");
                return;
            }

            // Configuration email (À personnaliser)
            String sender = "amnafati94@gmail.com";  // ✅ Votre email d'envoi
            String appPassword = "jcdo lljy fgug omgr";  // ✅ Votre mot de passe d'application

            // ⚠️ MEILLEURE PRATIQUE: Utiliser des variables d'environnement au lieu de coder en dur
            String envPassword = System.getenv("GMAIL_APP_PASSWORD");
            if (envPassword != null && !envPassword.isBlank()) {
                appPassword = envPassword;
            }

            int daysBefore = 7;

            // Créer les services
            EmailService emailService = new EmailService(sender, appPassword);
            ExpirationNotifierService notifier = new ExpirationNotifierService(new ProduitDAO(), emailService);

            // Envoyer l'email
            int total = notifier.notifyByEmail(recipient, daysBefore);

            // Afficher le résultat
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notification expiration");
            alert.setHeaderText("Résultat envoi email");
            alert.setContentText(total == 0
                    ? "Aucune expiration à signaler."
                    : "Email envoyé à " + recipient + ".\nProduits concernés: " + total);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Envoi email échoué", e.getMessage());
        }
    }

    // ================== FORM ==================

    private void openProduitForm(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_form.fxml"));
            Parent root = loader.load();

            ProduitFormController controller = loader.getController();
            controller.setProduitDAO(produitDAO);
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(produit == null ? "Ajouter un produit" : "Modifier le produit");
            stage.showAndWait();

            loadProduits();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire produit.");
        }
    }

    // ================== ALERTS ==================

    private void showWarning(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
