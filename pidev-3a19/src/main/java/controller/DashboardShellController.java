package controller;

import model.Produit;
import model.User;
import Utils.UserContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.WeatherInfo;
import Services.WeatherService;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Single post-login layout: sidebar menu + content area.
 * Navigation switches only the center content (no full-scene replacement).
 */
public class DashboardShellController {

    @FXML private StackPane contentArea;
    @FXML private Label headerTitle;
    @FXML private VBox sidebar;
    @FXML private VBox sidebarContent;
    @FXML private HBox sidebarHeader;
    @FXML private Label sidebarBrandLabel;
    @FXML private Button btnCollapseSidebar;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnSupport;
    @FXML private Button btnDiagnosticAnalytics;
    @FXML private Button btnSupportFarmer;
    @FXML private Button btnProduits;
    @FXML private Button btnMateriels;
    @FXML private Button btnProduction;
    @FXML private Button btnGame;
    @FXML private Button btnNews;
    @FXML private Button btnMarketplace;
    @FXML private Button btnMesFavoris;
    @FXML private Button btnAssistant;
    @FXML private Button btnQrScan;
    @FXML private Button btnStatistics;
    @FXML private Button btnTraceabilite;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherDescLabel;

    public static Locale currentLocale = new Locale("fr");

    private User user;
    private Node adminUsersContent;
    private Node adminSupportContent;
    private Node farmerContent;

    /**
     * Called by LoginController after loading the shell.
     * Only two roles: ADMIN and FARMER. Produits & Matériels are backoffice-only (admin).
     */
    public void initUser(User user) {
        this.user = user;
        UserContext.setCurrentUser(user);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        if (headerTitle != null) {
            headerTitle.setText(isAdmin ? "Backoffice Admin" : "Mon espace");
        }
        btnUtilisateurs.setVisible(isAdmin);
        btnUtilisateurs.setManaged(isAdmin);
        btnSupport.setVisible(isAdmin);
        btnSupport.setManaged(isAdmin);
        btnDiagnosticAnalytics.setVisible(isAdmin);
        btnDiagnosticAnalytics.setManaged(isAdmin);
        btnSupportFarmer.setVisible(!isAdmin);
        btnSupportFarmer.setManaged(!isAdmin);
        btnProduits.setVisible(isAdmin);
        btnProduits.setManaged(isAdmin);
        btnMateriels.setVisible(isAdmin);
        btnMateriels.setManaged(isAdmin);
        btnProduction.setVisible(isAdmin);
        btnProduction.setManaged(isAdmin);
        btnGame.setVisible(!isAdmin);
        btnGame.setManaged(!isAdmin);
        btnNews.setVisible(true);
        btnNews.setManaged(true);
        // Farmer-only: Marketplace, Favoris, Assistant, QR, Statistics, Traceabilité (not in backoffice)
        btnMarketplace.setVisible(!isAdmin);
        btnMarketplace.setManaged(!isAdmin);
        btnMesFavoris.setVisible(!isAdmin);
        btnMesFavoris.setManaged(!isAdmin);
        btnAssistant.setVisible(!isAdmin);
        btnAssistant.setManaged(!isAdmin);
        btnQrScan.setVisible(!isAdmin);
        btnQrScan.setManaged(!isAdmin);
        btnStatistics.setVisible(!isAdmin);
        btnStatistics.setManaged(!isAdmin);
        btnTraceabilite.setVisible(!isAdmin);
        btnTraceabilite.setManaged(!isAdmin);

        loadWeatherAsync();

        if (isAdmin) {
            showUtilisateurs();
        } else {
            showSupportFarmer();
        }
    }

    private void loadWeatherAsync() {
        if (weatherTempLabel == null && weatherDescLabel == null) return;
        CompletableFuture.supplyAsync(() -> {
            try {
                return new WeatherService().getCurrentWeather();
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(info -> {
            Platform.runLater(() -> {
                if (weatherTempLabel != null) {
                    weatherTempLabel.setText(info != null ? String.format("%.0f °C", info.getTemperature()) : "—");
                }
                if (weatherDescLabel != null) {
                    weatherDescLabel.setText(info != null ? info.getDescription() : "Météo indisponible");
                }
            });
        });
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
    private void showDiagnosticAnalytics() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/diagnostic_analytics.fxml"));
            setContent(root);
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
            MaterielController controller = loader.getController();
            // Pass the selected product from navigation context if available
            Produit selectedProduit = NavigationContext.getInstance().getSelectedProduit();
            if (selectedProduit != null) {
                controller.setProduit(selectedProduit);
            }
            Node content = (root instanceof BorderPane) ? ((BorderPane) root).getCenter() : root;
            setContent(content != null ? content : root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showProduction() {
        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("translation.messages", currentLocale);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/Production.fxml"), bundle);
            Parent root = loader.load();
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showGame() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/game.fxml"));
            Stage gameStage = new Stage();
            gameStage.setScene(new Scene(root, 1200, 800));
            gameStage.setTitle("FARMTECH - Jeu plantation");
            gameStage.setResizable(true);
            gameStage.centerOnScreen();
            gameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showNews() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/news_agricole.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMarketplace() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/marketplace.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMesFavoris() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/mes_favoris.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAssistant() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/assistant.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showQrScan() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/qr_scan.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStatistics() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/statistiques.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTraceabilite() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/traceabilite.fxml"));
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean sidebarCollapsed = false;

    @FXML
    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        if (sidebar != null) {
            if (sidebarCollapsed) {
                sidebar.setPrefWidth(56);
                sidebar.setMinWidth(56);
                sidebar.setMaxWidth(56);
                if (sidebarContent != null) {
                    sidebarContent.setVisible(false);
                    sidebarContent.setManaged(false);
                }
                if (sidebarBrandLabel != null) {
                    sidebarBrandLabel.setVisible(false);
                    sidebarBrandLabel.setManaged(false);
                }
                if (btnCollapseSidebar != null) btnCollapseSidebar.setText("▶");
            } else {
                sidebar.setPrefWidth(220);
                sidebar.setMinWidth(220);
                sidebar.setMaxWidth(220);
                if (sidebarContent != null) {
                    sidebarContent.setVisible(true);
                    sidebarContent.setManaged(true);
                }
                if (sidebarBrandLabel != null) {
                    sidebarBrandLabel.setVisible(true);
                    sidebarBrandLabel.setManaged(true);
                }
                if (btnCollapseSidebar != null) btnCollapseSidebar.setText("≡");
            }
        }
    }

    @FXML
    private void logout() {
        UserContext.setCurrentUser(null);
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
