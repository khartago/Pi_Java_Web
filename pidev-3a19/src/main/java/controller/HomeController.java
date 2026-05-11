package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private ScrollPane mainScroll;

    @FXML
    private Node missionSection;

    @FXML
    private Node featuresSection;

    @FXML
    private Node opsSection;

    @FXML
    private void handleHome() {
        scrollToVerticalRatio(0);
    }

    @FXML
    private void handleMissionNav() {
        scrollNodeIntoView(missionSection);
    }

    @FXML
    private void handleFeaturesNav() {
        scrollNodeIntoView(featuresSection);
    }

    @FXML
    private void handleOpsNav() {
        scrollNodeIntoView(opsSection);
    }

    private void scrollToVerticalRatio(double v) {
        if (mainScroll == null) {
            return;
        }
        Platform.runLater(() -> mainScroll.setVvalue(Math.max(0, Math.min(1, v))));
    }

    private void scrollNodeIntoView(Node node) {
        if (mainScroll == null || node == null) {
            return;
        }
        Platform.runLater(() -> {
            Node content = mainScroll.getContent();
            if (content == null) {
                return;
            }
            Bounds cb = content.getBoundsInLocal();
            double contentH = cb.getHeight();
            double viewportH = mainScroll.getViewportBounds().getHeight();
            double maxScroll = contentH - viewportH;
            if (maxScroll <= 1) {
                return;
            }
            Bounds nb = node.getBoundsInParent();
            double y = nb.getMinY();
            mainScroll.setVvalue(Math.max(0, Math.min(1, y / maxScroll)));
        });
    }

    @FXML
    private void handleBlog(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/view/BlogDashboard.fxml"));
            Parent root = loader.load();
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGame(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/game.fxml"));
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double w = Math.min(1200, bounds.getWidth() * 0.9);
            double h = Math.min(800, bounds.getHeight() * 0.9);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNewsAgricole() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/news_agricole.fxml"));
            Stage stage = new Stage();
            stage.setTitle("News Agricole");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
