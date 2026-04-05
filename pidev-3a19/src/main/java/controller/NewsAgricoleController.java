package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewsAgricoleController {

    @FXML
    private VBox newsContainer;

    private final String API_KEY = "cd19adf2ff41469893ff8260d9a448a8";

    @FXML
    public void initialize() {
        loadNews();
    }

    private void loadNews() {

        new Thread(() -> {

            try {

                String urlString =
                        "https://newsapi.org/v2/everything?q=agriculture&language=fr&sortBy=publishedAt&apiKey="
                                + API_KEY;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                        );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray articles = json.getJSONArray("articles");

                Platform.runLater(() -> {

                    newsContainer.getChildren().clear();

                    for (int i = 0; i < Math.min(6, articles.length()); i++) {

                        JSONObject article = articles.getJSONObject(i);

                        String title = article.optString("title", "No title");
                        String description = article.optString("description", "No description available.");
                        String articleUrl = article.optString("url");
                        String imageUrl = article.optString("urlToImage");

                        VBox card = createNewsCard(title, description, articleUrl, imageUrl);

                        newsContainer.getChildren().add(card);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private VBox createNewsCard(String title, String description, String url, String imageUrl) {

        VBox outerBox = new VBox();
        outerBox.setPadding(new Insets(15));
        outerBox.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;" +
                        "-fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.1), 10,0,0,5);"
        );

        HBox contentBox = new HBox(20); // space between image and text

        // 🖼 IMAGE (LEFT)
        if (imageUrl != null && !imageUrl.isEmpty() && !"null".equals(imageUrl)) {
            try {
                Image image = new Image(imageUrl, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(220); // 🔥 smaller image
                imageView.setPreserveRatio(true);
                contentBox.getChildren().add(imageView);
            } catch (Exception ignored) {}
        }

        // 📄 TEXT AREA (RIGHT)
        VBox textBox = new VBox(10);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill:#555555;");

        textBox.getChildren().addAll(titleLabel, descLabel);

        contentBox.getChildren().add(textBox);

        outerBox.getChildren().add(contentBox);

        outerBox.setCursor(Cursor.HAND);
        outerBox.setOnMouseClicked(e -> {
            try {
                if (url != null && !url.isEmpty()) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return outerBox;
    }
}