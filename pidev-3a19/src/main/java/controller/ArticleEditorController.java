package controller;

import java.util.ResourceBundle;

import javafx.stage.Stage;
import model.ArticleBase;
import Services.ArticleService;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;






    public class ArticleEditorController implements Initializable {

        @FXML private TextField titleField;

        @FXML private TextArea contentArea;



        private final ArticleService articleService = new ArticleService();
        private ArticleBase currentArticle;
        private int authorId = 1;
        private boolean isNewArticle = true;

        private static final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        @Override
        public void initialize(URL location, ResourceBundle resources) {
        }







        public void setArticle(ArticleBase article) {
            this.currentArticle = article;
            this.isNewArticle = false;

            titleField.setText(article.getTitle());

            contentArea.setText(article.getTexte());



        }

        public void setAuthorId(int authorId) {
            this.authorId = authorId;
        }

        private ArticleBase getArticleFromForm() {
            ArticleBase article = new ArticleBase();

            if (!isNewArticle) {
                article.setId((currentArticle.getId()));
            }

            article.setTitle(titleField.getText().trim());
            article.setTexte(contentArea.getText());


            return article;
        }

        private boolean validateForm() {
            if (titleField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Title is required");
                titleField.requestFocus();
                return false;
            }

            if (contentArea.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Content is required");
                contentArea.requestFocus();
                return false;
            }

            return true;
        }



@FXML
        private void saveArticle() {
            ArticleBase article = getArticleFromForm();
                ArticleBase created = articleService.ajouterArticle(article);

        }

        // Toolbar actions (simplified - inserts markdown)
        @FXML private void handleBold() { insertText("**", "**"); }
        @FXML private void handleItalic() { insertText("*", "*"); }
        @FXML private void handleUnderline() { insertText("<u>", "</u>"); }
        @FXML private void handleHeading1() { insertAtLineStart("# "); }
        @FXML private void handleHeading2() { insertAtLineStart("## "); }
        @FXML private void handleUnorderedList() { insertAtLineStart("- "); }
        @FXML private void handleOrderedList() { insertAtLineStart("1. "); }

        @FXML private void handleLink() {
            TextInputDialog dialog = new TextInputDialog("https://");
            dialog.setTitle("Insert Link");
            dialog.setHeaderText("Enter the URL");
            dialog.setContentText("URL:");

            dialog.showAndWait().ifPresent(url -> {
                insertText("[link text](", url + ")");
            });
        }

        @FXML private void handleImage() {
            TextInputDialog dialog = new TextInputDialog("https://");
            dialog.setTitle("Insert Image");
            dialog.setHeaderText("Enter the image URL");
            dialog.setContentText("Image URL:");

            dialog.showAndWait().ifPresent(url -> {
                insertText("![alt text](", url + ")");
            });
        }

        @FXML private void handleCode() { insertText("`", "`"); }

        private void insertText(String before, String after) {
            TextArea area = contentArea;
            int start = area.getSelection().getStart();
            int end = area.getSelection().getEnd();
            String selected = area.getSelectedText();

            area.replaceText(start, end, before + selected + after);
            area.positionCaret(start + before.length() + selected.length());
            area.requestFocus();
        }

        private void insertAtLineStart(String prefix) {
            TextArea area = contentArea;
            int pos = area.getCaretPosition();
            String text = area.getText();

            // Find start of line
            int lineStart = text.lastIndexOf('\n', pos - 1) + 1;

            area.insertText(lineStart, prefix);
            area.positionCaret(lineStart + prefix.length());
            area.requestFocus();
        }

        @FXML
        private void handleBack() {
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
        }

        private void showAlert(Alert.AlertType type, String title, String message) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

