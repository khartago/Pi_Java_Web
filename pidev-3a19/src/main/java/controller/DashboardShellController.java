package controller;

import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.PropertyResourceBundle;

/**
 * Single post-login layout: sidebar menu + content area.
 * Navigation switches only the center content (no full-scene replacement).
 */
public class DashboardShellController {

    @FXML private StackPane contentArea;
    @FXML private Label headerTitle;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnSupport;
    @FXML private Button btnSupportFarmer;
    @FXML private Button btnProduits;
    @FXML private Button btnMateriels;
    @FXML private Button btnProduction;

    private User user;
    private Node adminUsersContent;
    private Node adminSupportContent;
    private Node farmerContent;
    public static Locale currentLocale = new Locale("fr");
    /**
     * Called by LoginController after loading the shell.
     * Only two roles: ADMIN and FARMER. Produits & Matériels are backoffice-only (admin).
     */
    public void initUser(User user) {
        this.user = user;
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        if (headerTitle != null) {
            headerTitle.setText(isAdmin ? "Backoffice Admin" : "Mon espace");
        }
        btnUtilisateurs.setVisible(isAdmin);
        btnUtilisateurs.setManaged(isAdmin);
        btnSupport.setVisible(isAdmin);
        btnSupport.setManaged(isAdmin);
        btnSupportFarmer.setVisible(!isAdmin);
        btnSupportFarmer.setManaged(!isAdmin);
        btnProduits.setVisible(isAdmin);
        btnProduits.setManaged(isAdmin);
        btnMateriels.setVisible(isAdmin);
        btnMateriels.setManaged(isAdmin);
        btnProduction.setVisible(!isAdmin);
        btnProduction.setManaged(!isAdmin);

        if (isAdmin) {
            showUtilisateurs();
        } else {
            showSupportFarmer();
        }
    }

    private Node getAdminUsersContent() throws IOException {
        if (adminUsersContent == null) {
            adminUsersContent = FXMLLoader.load(getClass().getResource("/view/admin_users.fxml"));
        }
        return adminUsersContent;
    }

    private Node getAdminSupportContent() throws IOException {
        if (adminSupportContent == null) {
            adminSupportContent = FXMLLoader.load(getClass().getResource("/view/admin_support.fxml"));
        }
        return adminSupportContent;
    }

    private Node getFarmerContent() throws IOException {
        if (farmerContent == null) {
            farmerContent = FXMLLoader.load(getClass().getResource("/view/farmer_content.fxml"));
        }
        return farmerContent;
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    @FXML
    private void showUtilisateurs() {
        try {
            setContent(getAdminUsersContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showSupport() {
        try {
            setContent(getAdminSupportContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showSupportFarmer() {
        try {
            setContent(getFarmerContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/produit_list.fxml"));
            Parent root = loader.load();
            Node content = (root instanceof BorderPane) ? ((BorderPane) root).getCenter() : root;
            setContent(content != null ? content : root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMateriels() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/materiel_list.fxml"));
            Parent root = loader.load();
            Node content = (root instanceof BorderPane) ? ((BorderPane) root).getCenter() : root;
            setContent(content != null ? content : root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showProduction() {
        try {

            // Use global selected language
            Locale locale = currentLocale;

            ResourceBundle bundle = ResourceBundle.getBundle(
                    "translation.messages",
                    locale,
                    new ResourceBundle.Control() {
                        @Override
                        public ResourceBundle newBundle(String baseName, Locale locale,
                                                        String format, ClassLoader loader,
                                                        boolean reload)
                                throws IllegalAccessException, InstantiationException, IOException {

                            String bundleName = toBundleName(baseName, locale);
                            String resourceName = toResourceName(bundleName, "properties");

                            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                                if (stream == null) return null;
                                return new PropertyResourceBundle(
                                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                                );
                            }
                        }
                    }
            );

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Production.fxml"),
                    bundle
            );

            Parent root = loader.load();

            // RTL support for Arabic
            if (locale.getLanguage().equals("ar")) {
                root.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            }

            setContent(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/home.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("FARMTECH - Application de gestion agricole");
            stage.setMaximized(true);
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
