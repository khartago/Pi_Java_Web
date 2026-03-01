package controller;

import model.ArticleBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;

public class ArticleBaseController {
    @FXML
    private TextField titleLabel;
    @FXML
    private TextField textField;
    @FXML
    private TableView<ArticleBase> tableArticleBase;
    @FXML
    private TableColumn<ArticleBase, Integer> colIdArticle;
    @FXML
    private TableColumn<ArticleBase, String> colArticleTitle;
    @FXML
    private TableColumn<ArticleBase, Date> colDateArticle;
    @FXML
    private TableColumn<ArticleBase, Integer> colLikes;
    @FXML
    private TableColumn<ArticleBase, Integer> colDislikes;
    @FXML
    private TableColumn<ArticleBase, String> colEdit;

    @FXML
    public void initialize() {
        colIdArticle.setCellValueFactory(new PropertyValueFactory<>("idArticleBase"));
        colArticleTitle.setCellValueFactory(new PropertyValueFactory<>("ArticleTitle"));
        colDateArticle.setCellValueFactory(new PropertyValueFactory<>("DateArticle"));
        colLikes.setCellValueFactory(new PropertyValueFactory<>("Likes"));
        colDislikes.setCellValueFactory(new PropertyValueFactory<>("Dislikes"));
        colEdit.setCellValueFactory(new PropertyValueFactory<>("Edit"));

        /*tableArticleBase.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (cellData.getValue().getDateCreation() != null) {
            }
            );*/
        }
    }

