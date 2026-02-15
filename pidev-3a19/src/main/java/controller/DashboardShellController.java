package controller;

import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Single post-login layout: sidebar menu + content area.
 * Navigation switches only the center content (no full-scene replacement).
 */
public class DashboardShellController {

    @FXML private StackPane contentArea;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnProblemes;
    @FXML private Button btnMesProblemes;
    @FXML private Button btnProduitsMateriels;

    private User user;
    private Node adminContent;        // TabPane (Utilisateurs | Problèmes & Diagnostics)
    private Node farmerContent;       // VBox (Mes problèmes)
    private Node produitsMaterielsContent;  // Produits & Matériels module (produits list + matériels)

    /**
     * Called by LoginController after loading the shell.
     * Only two roles: ADMIN and FARMER. Produits & Matériels is one module for both.
     */
    public void initUser(User user) {
        this.user = user;
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        btnUtilisateurs.setVisible(isAdmin);
        btnUtilisateurs.setManaged(isAdmin);
        btnProblemes.setVisible(isAdmin);
        btnProblemes.setManaged(isAdmin);
        btnMesProblemes.setVisible(!isAdmin);
        btnMesProblemes.setManaged(!isAdmin);
        btnProduitsMateriels.setVisible(true);
        btnProduitsMateriels.setManaged(true);

        if (isAdmin) {
            showUtilisateurs();
        } else {
            showMesProblemes();
        }
    }

    private Node getAdminContent() throws IOException {
        if (adminContent == null) {
            adminContent = FXMLLoader.load(getClass().getResource("/view/admin_content.fxml"));
        }
        return adminContent;
    }

    private Node getFarmerContent() throws IOException {
        if (farmerContent == null) {
            farmerContent = FXMLLoader.load(getClass().getResource("/view/farmer_content.fxml"));
        }
        return farmerContent;
    }

    private Node getProduitsMaterielsContent() throws IOException {
        if (produitsMaterielsContent == null) {
            produitsMaterielsContent = FXMLLoader.load(getClass().getResource("/view/produit_content.fxml"));
        }
        return produitsMaterielsContent;
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    @FXML
    private void showUtilisateurs() {
        try {
            Node content = getAdminContent();
            setContent(content);
            if (content instanceof TabPane) {
                ((TabPane) content).getSelectionModel().selectFirst();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProblemes() {
        try {
            Node content = getAdminContent();
            setContent(content);
            if (content instanceof TabPane) {
                ((TabPane) content).getSelectionModel().selectLast();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMesProblemes() {
        try {
            setContent(getFarmerContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProduitsMateriels() {
        try {
            setContent(getProduitsMaterielsContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/home.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FARMTECH - Application de gestion agricole");
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
