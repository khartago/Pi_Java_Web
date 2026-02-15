package controller;

import model.User;
import Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    private Stage getStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        User user = userService.authentifier(email, password);
        if (user != null) {
            try {
                Stage stage = getStage(event);
                java.net.URL shellUrl = getClass().getResource("/view/dashboard_shell.fxml");
                if (shellUrl == null) {
                    showError("Fichier introuvable: /view/dashboard_shell.fxml");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(shellUrl);
                Parent root = loader.load();
                DashboardShellController shell = loader.getController();
                if (shell == null) {
                    showError("Controller introuvable pour dashboard_shell.fxml");
                    return;
                }
                shell.initUser(user);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("FARMTECH - Tableau de bord");
                stage.setMaximized(true);
                javafx.application.Platform.runLater(() -> stage.setMaximized(true));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur après connexion: " + e.getMessage());
            }
        } else {
            errorLabel.setText("Email ou mot de passe incorrect");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Stage stage = getStage(event);
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
    private void handleHome(ActionEvent event) {
        try {
            Stage stage = getStage(event);
            Parent root = FXMLLoader.load(getClass().getResource("/view/home.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
