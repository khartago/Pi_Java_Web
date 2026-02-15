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

public class UserFormController {

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

    @FXML
    private void handleSave(ActionEvent event) {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }

        if (role == null || role.isEmpty()) {
            errorLabel.setText("Veuillez sélectionner un rôle");
            errorLabel.setVisible(true);
            return;
        }

        try {
            if (currentUser == null) {
                if (userService.trouverParEmail(email) != null) {
                    errorLabel.setText("Cet email est déjà utilisé");
                    errorLabel.setVisible(true);
                    return;
                }
                User newUser = new User(nom, email, password, role);
                userService.ajouterUser(newUser);
            } else {
                currentUser.setNom(nom);
                currentUser.setEmail(email);
                currentUser.setMotDePasse(password);
                currentUser.setRole(role);
                userService.modifierUser(currentUser);
            }
            
            Stage stage = getStage(event);
            stage.close();
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de la sauvegarde: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = getStage(event);
        stage.close();
    }
}
