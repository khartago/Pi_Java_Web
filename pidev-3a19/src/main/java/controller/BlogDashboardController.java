package controller;

import model.ArticleBase;
import Services.ArticleService;
/*import com.blog.dashboard.service.CommentService;*/
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BlogDashboardController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<ArticleBase> articlesTable;

    @FXML private TableColumn<ArticleBase, String> titleColumn;
    @FXML private TableColumn<ArticleBase, String> authorColumn;



    @FXML private TableColumn<ArticleBase, String> dateColumn;


    @FXML private Label publishedCountLabel;
    @FXML private Label draftCountLabel;
    @FXML private Label pendingCommentsLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label statusLabel;

    @FXML private ToggleButton filterAll;
    @FXML private ToggleButton filterPublished;
    @FXML private ToggleButton filterDraft;
    @FXML private ToggleButton filterArchived;

    private final ArticleService articleService = new ArticleService();
    /*private final CommentService commentService = new CommentService();*/
    private final ObservableList<ArticleBase> articlesList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private String currentFilter = "all";
    private static final int AUTHOR_ID = 1; // Default author ID

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        /*setupFilters();*/
        loadDashboard();
    }

    private void setupTable() {

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreationDate().format(dateFormatter)));



        articlesTable.setItems(articlesList);

        // Double click to edit
        articlesTable.setRowFactory(tv -> {
            TableRow<ArticleBase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditArticle(row.getItem());
                }
            });
            return row;
        });
    }

    /*private void setupFilters() {
        ToggleGroup filterGroup = new ToggleGroup();
        filterAll.setToggleGroup(filterGroup);
        filterPublished.setToggleGroup(filterGroup);
        filterDraft.setToggleGroup(filterGroup);
        filterArchived.setToggleGroup(filterGroup);
    }*/

    private void loadDashboard() {
        try {
            // Load stats
           /* int published = articleService.getPublishedCount();
            int drafts = articleService.getDraftCount();
            int pendingComments = commentService.getPendingCount();
            int total = published + drafts;

            publishedCountLabel.setText(String.valueOf(published));
            draftCountLabel.setText(String.valueOf(drafts));
            pendingCommentsLabel.setText(String.valueOf(pendingComments));
            totalArticlesLabel.setText(String.valueOf(total));*/

            // Load articles
            loadArticles();


        } catch (SQLException e) {

            showError("Database Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    private void loadArticles() throws SQLException {
        List<ArticleBase> articles = List.of();

       /* switch (currentFilter) {
            case "published":
                articles = articleService.getPublishedArticles();
                break;
            case "draft":
                articles = articleService.getDraftArticles();
                break;
            case "archived":
                articles = articleService.findByStatus("archived");
                break;
            default:
                articles = articleService.getAllArticles();
        }*/

        articlesList.setAll(articles);
        /*statusLabel.setText("Showing " + articles.size() + " articles");*/
    }

    @FXML
    private void handleNewArticle() {
        openArticleEditor(null);
    }

    @FXML
    private void handleEditArticle(ArticleBase article) {
        openArticleEditor(article);
    }

    private void openArticleEditor(ArticleBase article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/view/article_editor.fxml"));
            Parent root = loader.load();

            ArticleEditorController controller = loader.getController();
           /* if (article != null) {
                controller.setArticle(article);
            }*/

            Stage stage = new Stage();
            stage.setTitle(article != null ? "Edit Article" : "New Article");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh after closing
            loadDashboard();

        } catch (IOException e) {
            showError("Error", "Failed to open article editor: " + e.getMessage());
        }
    }

   /* @FXML
    private void handleViewComments(ArticleBase article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/fxml/comment_section.fxml"));
            Parent root = loader.load();

            CommentSectionController controller = loader.getController();
            controller.setArticle(article);

            Stage stage = new Stage();
            stage.setTitle("Comments - " + article.getTitle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showError("Error", "Failed to open comments: " + e.getMessage());
        }
    }
*/
    @FXML
    private void handleDeleteArticle(ArticleBase article) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Article");
        alert.setHeaderText("Are you sure you want to delete this article?");
        alert.setContentText("This will also delete all associated comments.");

        Optional<ButtonType> result = alert.showAndWait();

                if (articleService.supprimerArticle(article.getId())) {
                    articlesList.remove(article);
                    loadDashboard();
                    statusLabel.setText("Article deleted successfully");
                }

        }


   /* @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            try {
                loadArticles();
            } catch (SQLException e) {
                showError("Error", "Search failed: " + e.getMessage());
            }
            return;
        }

        try {
            List<Article> results = articleService.searchArticles(query);
            articlesList.setAll(results);
            statusLabel.setText("Found " + results.size() + " articles for '" + query + "'");
        } catch (SQLException e) {
            showError("Error", "Search failed: " + e.getMessage());
        }
    }*/

    /*@FXML
    private void handleFilterChange() {
        if (filterAll.isSelected()) currentFilter = "all";
        else if (filterPublished.isSelected()) currentFilter = "published";
        else if (filterDraft.isSelected()) currentFilter = "draft";
        else if (filterArchived.isSelected()) currentFilter = "archived";

        try {
            loadArticles();
        } catch (SQLException e) {
            showError("Error", "Failed to filter articles: " + e.getMessage());
        }
    }*/

    /*@FXML
    private void handleShowAll() {
        filterAll.setSelected(true);
        handleFilterChange();
    }

    @FXML
    private void handleShowPublished() {
        filterPublished.setSelected(true);
        handleFilterChange();
    }

    @FXML
    private void handleShowDrafts() {
        filterDraft.setSelected(true);
        handleFilterChange();
    }

    @FXML
    private void handleShowPendingComments() {
        statusLabel.setText("Pending comments: View from article comments section");
    }*/

    @FXML
    private void handleExit() {
        Stage stage = (Stage) articlesTable.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}