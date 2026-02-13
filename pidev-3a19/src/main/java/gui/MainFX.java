package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Utils.Mydatabase;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Mydatabase.getInstance();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/home.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("FARMTECH - Application de gestion agricole");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
