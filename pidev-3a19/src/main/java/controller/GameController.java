package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GameController {

    // ===== GRID =====
    @FXML
    private GridPane plantGrid;

    // ===== TOOL SYSTEM =====
    private enum Tool {
        NONE,
        WATER,
        MANURE,
        SHOVEL
    }

    private Tool selectedTool = Tool.NONE;
    private Button selectedButton; // for glow effect

    // ===== INITIALIZE =====
    @FXML
    public void initialize() {
        System.out.println("Game Mode Loaded 🚀");
        createBoard();
    }

    // ===== CREATE 3x4 BOARD =====
    private void createBoard() {

        int rows = 3;
        int cols = 4;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                StackPane slot = createPlantSlot();
                plantGrid.add(slot, col, row);
            }
        }
    }

    // ===== CREATE ONE SLOT =====
    private StackPane createPlantSlot() {

        StackPane slot = new StackPane();
        slot.setPrefSize(150, 150);
        slot.getStyleClass().add("plant-slot");

        slot.setOnMouseClicked(e -> handlePlantClick(slot));

        return slot;
    }

    // ===== HANDLE SLOT CLICK =====
    private void handlePlantClick(StackPane slot) {

        if (selectedTool == Tool.NONE) {
            System.out.println("⚠ Select a tool first");
            return;
        }

        // Reset previous style
        slot.setStyle("");

        switch (selectedTool) {

            case WATER:
                System.out.println("💧 Water added");
                slot.setStyle("-fx-border-color: blue; -fx-border-width: 4;");
                break;

            case MANURE:
                System.out.println("🌿 Speed boosted");
                slot.setStyle("-fx-border-color: green; -fx-border-width: 4;");
                break;

            case SHOVEL:
                System.out.println("🪓 Plant removed");
                slot.getChildren().clear();
                break;
        }
    }

    // ===== TOOL SELECTION =====
    @FXML
    private void selectWater(ActionEvent e) {
        selectedTool = Tool.WATER;
        highlightButton((Button) e.getSource());
        System.out.println("Tool selected: WATER 💧");
    }

    @FXML
    private void selectManure(ActionEvent e) {
        selectedTool = Tool.MANURE;
        highlightButton((Button) e.getSource());
        System.out.println("Tool selected: MANURE 🌿");
    }

    @FXML
    private void selectShovel(ActionEvent e) {
        selectedTool = Tool.SHOVEL;
        highlightButton((Button) e.getSource());
        System.out.println("Tool selected: SHOVEL 🪓");
    }

    // ===== HIGHLIGHT SELECTED TOOL =====
    private void highlightButton(Button btn) {

        if (selectedButton != null) {
            selectedButton.setStyle("");
        }

        btn.setStyle("""
                -fx-effect: dropshadow(gaussian, gold, 20, 0.6, 0, 0);
                -fx-scale-x: 1.15;
                -fx-scale-y: 1.15;
                """);

        selectedButton = btn;
    }

    // ===== BACK BUTTON =====
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}