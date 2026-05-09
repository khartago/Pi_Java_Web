package controller;

import model.Probleme;
import model.User;
import Services.ProblemeService;
import Utils.UserContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FarmerDashboardController implements Initializable {

    @FXML
    private FlowPane problemeCardsContainer;

    private ProblemeService problemeService = new ProblemeService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProblemes();
    }

    private void loadProblemes() {
        problemeCardsContainer.getChildren().clear();
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        for (Probleme p : problemeService.afficherProblemesParUtilisateur(currentUser.getId())) {
            VBox card = buildProblemeCard(p);
            problemeCardsContainer.getChildren().add(card);
        }
    }

    private VBox buildProblemeCard(Probleme p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("probleme-card");
        card.setPrefWidth(280);
        card.setMinWidth(260);
        card.setMaxWidth(320);
        card.setPadding(new Insets(16));

        Label typeLabel = new Label(p.getType() != null ? p.getType() : "—");
        typeLabel.getStyleClass().add("probleme-card-title");
        typeLabel.setWrapText(true);

        String dateStr = p.getDateDetection() != null
            ? p.getDateDetection().format(DATE_FORMAT)
            : "—";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("probleme-card-date");

        HBox badges = new HBox(8);
        badges.getStyleClass().add("probleme-card-badges");
        Label graviteBadge = new Label(p.getGravite() != null ? p.getGravite() : "—");
        graviteBadge.getStyleClass().add("probleme-card-badge");
        Label etatBadge = new Label(p.getEtat() != null ? p.getEtat() : "—");
        etatBadge.getStyleClass().add("probleme-card-badge");
        etatBadge.getStyleClass().add("probleme-card-etat");
        badges.getChildren().addAll(graviteBadge, etatBadge);

        Button voirDetail = new Button("Voir détail");
        voirDetail.getStyleClass().add("secondary-button");
        voirDetail.setOnAction(e -> openDetailFor(p));

        card.getChildren().addAll(typeLabel, dateLabel, badges, voirDetail);
        return card;
    }

    private void openDetailFor(Probleme p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/support_issue_detail.fxml"));
            Parent root = loader.load();
            SupportIssueDetailController controller = loader.getController();
            controller.setProbleme(p);

            Stage stage = new Stage();
            Scene scene = new Scene(root, 700, 600);
            stage.setTitle("Détail du problème - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewProbleme() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/view/support_issue_form.fxml"));
            Scene scene = new Scene(root, 600, 700);
            stage.setTitle("Nouveau problème - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(500);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            
            stage.setOnHidden(e -> loadProblemes());
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome(javafx.event.ActionEvent event) {
        navigateTo(event, "/view/home.fxml", "FARMTECH - Application de gestion agricole");
    }

    @FXML
    private void handleProduitsMateriels(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/produit_list.fxml"));
            Scene scene = new Scene(root);
            try {
                String css = getClass().getResource("/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (NullPointerException ignored) {}
            stage.setScene(scene);
            stage.setTitle("FARMTECH - Produits & Matériels");
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        handleHome(event);
    }

    private void navigateTo(javafx.event.ActionEvent event, String fxml, String title) {
        try {
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
