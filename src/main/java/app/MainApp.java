package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of the JavaFX application. Loads the product list view and
 * displays it in the primary stage. This class is referenced by the
 * javafx-maven-plugin as the main class.
 */
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        // Apply custom stylesheet if available
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (NullPointerException ignored) {
            // stylesheet missing: no custom styles applied
        }
        primaryStage.setTitle("Gestion des Produits et Matériels");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}