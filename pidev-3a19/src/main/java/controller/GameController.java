package controller;
import Services.WeatherService;
import Services.ProductionService;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Production;
import Services.EmailService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    @FXML
    private GridPane plantGrid;
    private Label weatherLabel;
    private final ProductionService productionService = new ProductionService();

    private enum Tool { NONE, WATER, MANURE, SHOVEL }
    private int rows = 3;
    private Tool selectedTool = Tool.NONE;
    private Button selectedButton;
    private final EmailService emailService = new EmailService();
    private final Map<Integer, StackPane> slotMap = new HashMap<>();
    private final Map<Integer, Label> timerLabels = new HashMap<>();
    private List<Production> plants;

    private AnimationTimer globalTimer;
    private static final long BASE_DEAD_LIMIT = 60000;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        try {

            createBoard();

            reviveAllPlantsOnStart();

            plants = productionService.afficher();  // reload after revive

            System.out.println("Plants loaded: " + plants.size());

            loadPlantsFromDatabase();

            startGlobalTimer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REVIVE ALL =================
    private void reviveAllPlantsOnStart() {

        try {

            List<Production> tempPlants = productionService.afficher();

            for (Production plant : tempPlants) {

                plant.setStage(1);                 // ✅ START AT LEVEL 1
                plant.setWaterCount(0);            // reset water
                plant.setStatus("ALIVE");          // alive
                plant.setLastWaterTime(System.currentTimeMillis()); // reset timer

                productionService.updateGameData(plant);
            }

            System.out.println("All plants reset to stage 1 🌱");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createBoard() {

        int cols = 4;
        int slotIndex = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                StackPane slot = new StackPane();
                slot.setPrefSize(150, 150);
                slot.getStyleClass().add("plant-slot");

                int finalIndex = slotIndex;
                slot.setOnMouseClicked(e -> handlePlantClick(finalIndex));

                plantGrid.add(slot, col, row);
                slotMap.put(slotIndex, slot);

                slotIndex++;
            }
        }

        // ✅ WEATHER TEST HERE (inside method)
        double temp = WeatherService.getTemperature("Tunis");
        System.out.println("Temperature: " + temp);
    }
    // ================= BOARD =================


    // ================= LOAD =================
    private void loadPlantsFromDatabase() {

        for (StackPane slot : slotMap.values()) {
            slot.getChildren().clear();
        }

        for (Production plant : plants) {

            System.out.println("Loading slot: " + plant.getSlotIndex());

            StackPane slot = slotMap.get(plant.getSlotIndex());

            if (slot == null) {
                System.out.println("Slot not found for index: " + plant.getSlotIndex());
                continue;
            }

            updateSlotImage(slot, plant);
        }
    }

    // ================= UPDATE ALIVE =================
    private void updateSlotImage(StackPane slot, Production plant) {

        slot.getChildren().clear();

        Image image = getPlantImage(plant);
        if (image == null) return;

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Label timerLabel = new Label();
        timerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        timerLabels.put(plant.getSlotIndex(), timerLabel);

        VBox box = new VBox(5, imageView, timerLabel);
        box.setAlignment(Pos.CENTER);
        box.setMouseTransparent(true);

        slot.getChildren().add(box);
    }

    // ================= UPDATE DEAD =================
    private void updateSlotToDead(int slotIndex) {

        StackPane slot = slotMap.get(slotIndex);
        if (slot == null) return;

        slot.getChildren().clear();

        ImageView dead = new ImageView(
                new Image(getClass().getResource("/images/dead.png").toExternalForm())
        );

        dead.setFitWidth(100);
        dead.setFitHeight(100);

        slot.getChildren().add(dead);
    }

    // ================= IMAGE =================
    private Image getPlantImage(Production plant) {

        String name = plant.getNomPlant().toLowerCase();
        int stage = plant.getStage();
        String path = "/images/" + name + "." + stage + ".png";

        try {
            return new Image(getClass().getResource(path).toExternalForm());
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
            return null;
        }
    }

    // ================= TIMER =================
    private void startGlobalTimer() {

        globalTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                long current = System.currentTimeMillis();

                for (Production plant : plants) {

                    long limit = (long) (BASE_DEAD_LIMIT * plant.getGrowthSpeed());
                    long diff = current - plant.getLastWaterTime();
                    long remaining = (limit - diff) / 1000;

                    Label label = timerLabels.get(plant.getSlotIndex());

                    // ================= IF PLANT IS ALIVE =================
                    if ("ALIVE".equalsIgnoreCase(plant.getStatus())) {

                        if (remaining > 0) {

                            if (label != null) {
                                label.setText("⏳ " + remaining + "s");
                            }

                        } else {

                            // 🔥 PLANT DIES HERE
                            plant.setStatus("DEAD");
                            productionService.updateGameData(plant);

                            updateSlotToDead(plant.getSlotIndex());

                            // 🔥 SEND EMAIL ONLY ONCE
                            emailService.sendPlantDeathMail(
                                    plant.getNomPlant(),
                                    plant.getSlotIndex()
                            );
                        }
                    }
                }
            }
        };

        globalTimer.start();
    }

    // ================= CLICK =================
    private void handlePlantClick(int slotIndex) {

        if (selectedTool == Tool.NONE) return;

        try {

            Production plant = plants.stream()
                    .filter(p -> p.getSlotIndex() == slotIndex)
                    .findFirst()
                    .orElse(null);

            if (plant == null) return;

            if (selectedTool == Tool.WATER) {

                plant.setStatus("ALIVE");
                plant.setWaterCount(plant.getWaterCount() + 1);
                plant.setLastWaterTime(System.currentTimeMillis());

                if (plant.getWaterCount() >= 3 && plant.getStage() < 3) {
                    plant.setStage(plant.getStage() + 1);
                    plant.setWaterCount(0);
                }

                productionService.updateGameData(plant);
                updateSlotImage(slotMap.get(slotIndex), plant);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TOOL =================
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

        if (selectedButton != null)
            selectedButton.setStyle("");

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
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}