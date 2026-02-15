package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML
    private void handleHome(ActionEvent event) {
        navigateTo(event, "/view/home.fxml", "FARMTECH - Application de gestion agricole");
    }

    @FXML
    private void handleProduitsMateriels(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
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
    private void handleLogout(ActionEvent event) {
        handleHome(event);
    }

    private void navigateTo(ActionEvent event, String fxml, String title) {
        try {
            Node source = (Node) event.getSource();
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
