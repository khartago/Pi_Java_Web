package controller;

import model.User;
import Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.regex.Pattern;

public class UserFormController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_PASSWORD_LENGTH = 100;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label errorLabel;

    private UserService userService = new UserService();
    private User currentUser = null;

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll("FERMIER", "ADMIN");
        roleCombo.setValue("FERMIER");
        applyMaxLength(nomField, MAX_NOM_LENGTH);
        applyMaxLength(emailField, MAX_EMAIL_LENGTH);
        applyMaxLength(passwordField, MAX_PASSWORD_LENGTH);
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

    public void setUser(User user) {
        this.currentUser = user;
        titleLabel.setText("Modifier utilisateur");
        nomField.setText(user.getNom());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getMotDePasse());
        roleCombo.setValue(user.getRole());
    }

    private Stage getStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        return (Stage) source.getScene().getWindow();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        errorLabel.setVisible(false);
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (nom.isEmpty()) {
            showError("Veuillez saisir le nom.");
            return;
        }
        if (nom.length() > MAX_NOM_LENGTH) {
            showError("Le nom ne doit pas dépasser " + MAX_NOM_LENGTH + " caractères.");
            return;
        }

        if (email.isEmpty()) {
            showError("Veuillez saisir l'email.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Veuillez saisir une adresse email valide.");
            return;
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            showError("L'email ne doit pas dépasser " + MAX_EMAIL_LENGTH + " caractères.");
            return;
        }

        if (role == null || role.isEmpty()) {
            showError("Veuillez sélectionner un rôle.");
            return;
        }

        boolean isEdit = currentUser != null;
        if (isEdit && password.isEmpty()) {
            // Modification : mot de passe vide = garder l'actuel
        } else {
            if (password.isEmpty()) {
                showError("Veuillez saisir un mot de passe.");
                return;
            }
            if (password.length() < MIN_PASSWORD_LENGTH) {
                showError("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
                return;
            }
        }

        try {
            if (currentUser == null) {
                if (userService.trouverParEmail(email) != null) {
                    showError("Cet email est déjà utilisé.");
                    return;
                }
                User newUser = new User(nom, email, password, role);
                userService.ajouterUser(newUser);
            } else {
                currentUser.setNom(nom);
                currentUser.setEmail(email);
                if (!password.isEmpty()) {
                    currentUser.setMotDePasse(password);
                }
                currentUser.setRole(role);
                userService.modifierUser(currentUser);
            }
            Stage stage = getStage(event);
            stage.close();
        } catch (Exception e) {
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = getStage(event);
        stage.close();
    }
}
