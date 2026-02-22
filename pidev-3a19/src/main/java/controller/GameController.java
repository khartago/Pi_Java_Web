package controller;

import Services.ProductionService;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Production;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    @FXML
    private GridPane plantGrid;

    private final ProductionService productionService = new ProductionService();

    private enum Tool { NONE, WATER, MANURE, SHOVEL }

    private Tool selectedTool = Tool.NONE;
    private Button selectedButton;

    // Map slotIndex → StackPane
    private final Map<Integer, StackPane> slotMap = new HashMap<>();

    @FXML
    public void initialize() {
        System.out.println("Game Mode Loaded 🚀");
        createBoard();
        loadPlantsFromDatabase();
    }

    // ================= BOARD =================
    private void createBoard() {

        int rows = 3;
        int cols = 4;
        int slotIndex = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                StackPane slot = createPlantSlot(slotIndex);
                plantGrid.add(slot, col, row);
                slotMap.put(slotIndex, slot);
                slotIndex++;
            }
        }
    }

    private StackPane createPlantSlot(int slotIndex) {

        StackPane slot = new StackPane();
        slot.setPrefSize(150, 150);
        slot.getStyleClass().add("plant-slot");

        slot.setOnMouseClicked(e -> handlePlantClick(slotIndex));

        return slot;
    }


    private void loadPlantsFromDatabase() {

        try {

            // 🔥 CLEAR ALL SLOTS FIRST
            for (StackPane slot : slotMap.values()) {
                slot.getChildren().clear();
            }

            List<Production> plants = productionService.afficher();

            for (Production plant : plants) {

                if ("ALIVE".equalsIgnoreCase(plant.getStatus())) {

                    StackPane slot = slotMap.get(plant.getSlotIndex());

                    if (slot != null) {
                        updateSlotImage(slot, plant);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error loading plants: " + e.getMessage());
        }
    }

    // ================= IMAGE LOGIC =================
    private Image getPlantImage(Production plant) {

        String name = plant.getNomPlant().toLowerCase();
        int stage = plant.getStage();

        String imagePath = "/images/" + name + "." + stage + ".png";

        try {
            return new Image(getClass().getResource(imagePath).toExternalForm());
        } catch (Exception e) {
            System.out.println("Image not found: " + imagePath);
            return null;
        }
    }

    private void updateSlotImage(StackPane slot, Production plant) {

        slot.getChildren().clear();

        ImageView imageView = new ImageView(getPlantImage(plant));
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        slot.getChildren().add(imageView);
    }

    // ================= SLOT CLICK =================
    private void handlePlantClick(int slotIndex) {

        if (selectedTool == Tool.NONE) {
            System.out.println("⚠ Select a tool first");
            return;
        }

        try {

            Production plant = productionService.getBySlotIndex(slotIndex);
            if (plant == null) return;

            switch (selectedTool) {

                case WATER:

                    plant.setWaterCount(plant.getWaterCount() + 1);

                    if (plant.getWaterCount() >= 3 && plant.getStage() < 3) {
                        plant.setStage(plant.getStage() + 1);
                        plant.setWaterCount(0);
                    }

                    productionService.updateGameData(plant);
                    updateSlotImage(slotMap.get(slotIndex), plant);
                    break;

                case SHOVEL:

                    productionService.supprimer(plant.getId());
                    slotMap.get(slotIndex).getChildren().clear();
                    break;

                case MANURE:

                    plant.setGrowthSpeed(plant.getGrowthSpeed() + 0.5);
                    productionService.updateGameData(plant);
                    break;
            }

        } catch (Exception e) {
            System.err.println("Erreur handlePlantClick: " + e.getMessage());
        }
    }

    // ================= TOOL SELECTION =================
    @FXML
    private void selectWater(ActionEvent e) {
        selectedTool = Tool.WATER;
        highlightButton((Button) e.getSource());
    }

    @FXML
    private void selectManure(ActionEvent e) {
        selectedTool = Tool.MANURE;
        highlightButton((Button) e.getSource());
    }

    @FXML
    private void selectShovel(ActionEvent e) {
        selectedTool = Tool.SHOVEL;
        highlightButton((Button) e.getSource());
    }

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

    // ================= BACK =================
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