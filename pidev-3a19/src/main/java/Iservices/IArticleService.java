package Iservices;

import model.ArticleBase;

import java.util.List;

public interface IArticleService {

ArticleBase ajouterArticle(ArticleBase a);

void modifierArticle(ArticleBase a);

boolean supprimerArticle(int id);

List<ArticleBase> afficherArticle();








}