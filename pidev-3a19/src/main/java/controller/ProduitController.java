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
import Services.EmailService;
import Services.ExpirationNotifierService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProduitController {

    @FXML private TableView<Produit>             produitTable;
    @FXML private TableColumn<Produit, Integer>  idColumn;
    @FXML private TableColumn<Produit, String>   nomColumn;
    @FXML private TableColumn<Produit, Integer>  quantiteColumn;
    @FXML private TableColumn<Produit, String>   uniteColumn;
    @FXML private TableColumn<Produit, String>   prixColumn;
    @FXML private TableColumn<Produit, String>   dateExpColumn;
    @FXML private TableColumn<Produit, String>   statutColumn;
    @FXML private Label                          labelCount;

    private final ProduitDAO            produitDAO = new ProduitDAO();
    private ObservableList<Produit>     produits;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------ //
    //  Init
    // ------------------------------------------------------------------ //

    @FXML
    private void initialize() {
        setupColumns();
        loadProduits();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));

        prixColumn.setCellValueFactory(c -> {
            double prix = c.getValue().getPrixUnitaire();
            return new SimpleStringProperty(prix > 0 ? String.format("%.2f", prix) : "—");
        });

        dateExpColumn.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getDateExpiration();
            return new SimpleStringProperty(d == null ? "—" : d.format(DATE_FMT));
        });

        // Colonne statut avec badge coloré
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                    return;
                }
                Produit p = (Produit) getTableRow().getItem();
                LocalDate exp = p.getDateExpiration();
                LocalDate today = LocalDate.now();

                if (exp == null) {
                    setText("—");
                    setStyle("-fx-text-fill: #aaa; -fx-alignment: CENTER;");
                } else if (exp.isBefore(today)) {
                    setText("⛔ Expiré");
                    setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else if (!exp.isAfter(today.plusDays(7))) {
                    setText("⚠ Bientôt");
                    setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else {
                    setText("✅ Valide");
                    setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        // Coloration des lignes expirées
        produitTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produit p, boolean empty) {
                super.updateItem(p, empty);
                if (p == null || empty) {
                    setStyle("");
                } else if (p.getDateExpiration() != null
                        && p.getDateExpiration().isBefore(LocalDate.now())) {
                    setStyle("-fx-background-color: #FFF5F5;");
                } else if (p.getDateExpiration() != null
                        && !p.getDateExpiration().isAfter(LocalDate.now().plusDays(7))) {
                    setStyle("-fx-background-color: #FFFBEB;");
                } else {
                    setStyle("");
                }
            }
        });

        // Sélection → NavigationContext
        produitTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> NavigationContext.getInstance().setSelectedProduit(n));
    }

    private void loadProduits() {
        produits = FXCollections.observableArrayList(produitDAO.getAll());
        produitTable.setItems(produits);
        updateCount();
    }

    private void updateCount() {
        if (labelCount != null) {
            long expires = produits.stream()
                    .filter(p -> p.getDateExpiration() != null
                            && !p.getDateExpiration().isAfter(LocalDate.now().plusDays(7)))
                    .count();
            String txt = produits.size() + " produit(s)";
            if (expires > 0) txt += "  •  ⚠ " + expires + " expirant bientôt";
            labelCount.setText(txt);
        }
    }

    // ------------------------------------------------------------------ //
    //  Actions CRUD
    // ------------------------------------------------------------------ //

    @FXML
    private void handleAjouter() { openProduitForm(null); }

    @FXML
    private void handleModifier() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected != null) openProduitForm(selected);
        else showWarning("Sélection requise", "Sélectionnez un produit à modifier.");
    }

    @FXML
    private void handleSupprimer() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Sélection requise", "Sélectionnez un produit à supprimer."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer « " + selected.getNom() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (produitDAO.delete(selected.getIdProduit())) loadProduits();
            else showError("Suppression impossible",
                    "Ce produit est peut-être référencé par un matériel.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Navigation vers autres vues
    // ------------------------------------------------------------------ //

    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) produitTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("FarmTech — Connexion");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page de connexion.");
        }
    }

    @FXML
    private void handleVoirMateriels() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Sélection requise", "Sélectionnez un produit."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/materiel_list.fxml"));
            Parent root = loader.load();
            MaterielController ctrl = loader.getController();
            ctrl.setProduit(selected);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Matériels — " + selected.getNom());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadProduits();
        } catch (IOException e) { e.printStackTrace(); showError("Erreur", "Impossible d'ouvrir les matériels."); }
    }

    @FXML
    private void handleOpenAssistant() {
        navigateTo("/view/assistant.fxml", "Assistant IA");
    }

    @FXML
    private void handleOpenStatistics() {
        navigateTo("/view/statistiques.fxml", "Statistiques et Rapports");
    }

    @FXML
    private void handleOpenMarketplace() {
        navigateTo("/view/marketplace.fxml", "Marketplace — Catalogue");
    }

    @FXML
    private void handleOpenPromotions() {
        navigateTo("/view/promotion_list.fxml", "🏷️ Gestion des Promotions");
    }

    @FXML
    private void handleTraceabilite() {
        Produit selected = produitTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Sélection requise", "Sélectionnez un produit."); return; }
        try {
            TraceabiliteController.openForProduit(selected);
            loadProduits();
        } catch (IOException e) { e.printStackTrace(); showError("Erreur", "Impossible d'ouvrir la traçabilité."); }
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
        } catch (IOException e) { e.printStackTrace(); showError("Erreur", "Impossible d'ouvrir le scanner QR."); }
    }

    @FXML
    private void handleNotifierExpiration() {
        try {
            TextInputDialog dialog = new TextInputDialog("votre.email@example.com");
            dialog.setTitle("Notification d'expiration");
            dialog.setHeaderText("Email destinataire");
            dialog.setContentText("Email :");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return;
            String recipient = result.get().trim();
            if (recipient.isEmpty()) { showWarning("Email requis", "Entrez une adresse email."); return; }

            String sender      = "amnafati94@gmail.com";
            String appPassword = "jcdo lljy fgug omgr";
            String envPwd      = System.getenv("GMAIL_APP_PASSWORD");
            if (envPwd != null && !envPwd.isBlank()) appPassword = envPwd;

            EmailService emailService = new EmailService(sender, appPassword);
            ExpirationNotifierService notifier = new ExpirationNotifierService(new ProduitDAO(), emailService);
            int total = notifier.notifyByEmail(recipient, 7);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notification expiration");
            alert.setHeaderText("Résultat");
            alert.setContentText(total == 0
                    ? "Aucune expiration à signaler dans les 7 prochains jours."
                    : "Email envoyé à " + recipient + "\nProduits concernés : " + total);
            alert.showAndWait();
        } catch (Exception e) { e.printStackTrace(); showError("Envoi échoué", e.getMessage()); }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void openProduitForm(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_form.fxml"));
            Parent root = loader.load();
            ProduitFormController ctrl = loader.getController();
            ctrl.setProduitDAO(produitDAO);
            ctrl.setProduit(produit);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(produit == null ? "Ajouter un produit" : "Modifier — " + produit.getNom());
            stage.showAndWait();
            loadProduits();
        } catch (IOException e) { e.printStackTrace(); showError("Erreur", "Impossible d'ouvrir le formulaire."); }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            if (getClass().getResource("/css/style.css") != null)
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) produitTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) { e.printStackTrace(); showError("Erreur", "Impossible d'ouvrir : " + title); }
    }

    private void showWarning(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention"); a.setHeaderText(header); a.setContentText(content); a.showAndWait();
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(header); a.setContentText(content); a.showAndWait();
    }
}
