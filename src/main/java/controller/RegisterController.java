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

import java.util.regex.Pattern;

public class RegisterController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_PASSWORD_LENGTH = 100;

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

    @FXML
    private void initialize() {
        applyMaxLength(nomField, MAX_NOM_LENGTH);
        applyMaxLength(emailField, MAX_EMAIL_LENGTH);
        applyMaxLength(passwordField, MAX_PASSWORD_LENGTH);
        applyMaxLength(confirmPasswordField, MAX_PASSWORD_LENGTH);
    }

    private void applyMaxLength(TextField field, int max) {
        field.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    private void applyMaxLength(PasswordField field, int max) {
        field.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= max) return change;
            return null;
        }));
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        errorLabel.setVisible(false);
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        if (nom.length() > MAX_NOM_LENGTH) {
            errorLabel.setText("Le nom ne doit pas dépasser " + MAX_NOM_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("Veuillez saisir une adresse email valide");
            errorLabel.setVisible(true);
            return;
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            errorLabel.setText("L'email ne doit pas dépasser " + MAX_EMAIL_LENGTH + " caractères");
            errorLabel.setVisible(true);
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            errorLabel.setText("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
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
