package Iservices;

import model.ArticleBase;

import java.time.LocalDate;
import java.util.List;

public interface IArticleService {

void ajouterArticle(ArticleBase a);

void modifierArticle(ArticleBase a);

void supprimerArticle(int id);

List<ArticleBase> afficherArticle();








}