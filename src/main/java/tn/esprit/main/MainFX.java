package tn.esprit.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/views/gui/Production.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestion Production");
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.getStylesheets().add(
                getClass().getResource("/views/gui/farmtech-theme.css").toExternalForm()
        );

    }

    public static void main(String[] args) {
        launch(args);
    }
}
