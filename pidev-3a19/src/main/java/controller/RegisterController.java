package controller;

import model.User;
import Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Les mots de passe ne correspondent pas");
            errorLabel.setVisible(true);
            return;
        }

        if (userService.trouverParEmail(email) != null) {
            errorLabel.setText("Cet email est déjà utilisé");
            errorLabel.setVisible(true);
            return;
        }

        User newUser = new User(nom, email, password, "FERMIER");
        userService.ajouterUser(newUser);

        try {
            Stage stage = getStage(event);
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
    private void handleCancel(ActionEvent event) {
        handleHome(event);
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
