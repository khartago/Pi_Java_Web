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
    @FXML
    public void initialize() {
        System.out.println("Game Mode Loaded 🚀");
    }
    @FXML
    private void openNewsAgricole() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/news_agricole.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("News Agricole");

            Scene scene = new Scene(root, 1000, 700); // 🔥 set size
            stage.setScene(scene);

            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGame(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/game.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root, 1200, 800); // 👈 FORCE SIZE
            stage.setScene(scene);
            stage.centerOnScreen(); // 👈 CENTER WINDOW
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
