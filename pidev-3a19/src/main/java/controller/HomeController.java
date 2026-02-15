package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private void handleHome() {
        // Déjà sur la page d'accueil
    }

    @FXML
    private void handleAbout() {
        // TODO: Implémenter la page À propos
        System.out.println("À propos");
    }

    @FXML
    private void handleContact() {
        // TODO: Implémenter la page Contact
        System.out.println("Contact");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
