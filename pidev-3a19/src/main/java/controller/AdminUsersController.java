package controller;

import model.User;
import Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminUsersController implements Initializable {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nomColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    private UserService userService = new UserService();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        loadUsers();
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(userService.afficherUsers());
        userTable.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/view/user_form.fxml"));
            Scene scene = new Scene(root, 500, 600);
            stage.setTitle("Nouvel utilisateur - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(450);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            
            stage.setOnHidden(e -> loadUsers());
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à modifier.");
            alert.showAndWait();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_form.fxml"));
            Parent root = loader.load();
            UserFormController controller = loader.getController();
            controller.setUser(selectedUser);
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 500, 600);
            stage.setTitle("Modifier utilisateur - FARMTECH");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(450);
            stage.setMinHeight(550);
            stage.centerOnScreen();
            
            stage.setOnHidden(e -> loadUsers());
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer.");
            alert.showAndWait();
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedUser.getNom() + " ?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                userService.supprimerUser(selectedUser.getId());
                loadUsers();
            }
        });
    }
}
