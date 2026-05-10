<?php

namespace App\Controller;

use App\Entity\Utilisateur;
use App\Repository\ArticleRepository;
use App\Repository\BlogRepository;
use App\Service\WebNewsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class NewsController extends AbstractController
{
    #[Route('/news', name: 'app_news', methods: ['GET'])]
    public function index(
        WebNewsService $newsService,
        BlogRepository $blogRepository,
        ArticleRepository $articleRepository,
    ): Response {
        $news = $newsService->getNews();
        $externalArticles = $news['articles'] ?? [];

        $blogs = [];
        $articlesGroupedByBlog = [];
        if ($this->isGranted('IS_AUTHENTICATED_FULLY')) {
            $user = $this->getUser();
            if ($user instanceof Utilisateur) {
                $blogs = $blogRepository->findByUser($user);
                $blogIds = array_map(static fn ($b) => $b->getIdBlog(), $blogs);
                if ([] !== $blogIds) {
                    $articles = $articleRepository->findByBlogIds($blogIds);
                    foreach ($articles as $article) {
                        $articlesGroupedByBlog[$article->getBlogId()][] = $article;
                    }
                }
            }
        }

        return $this->render('news/index.html.twig', [
            'articles' => $externalArticles,
            'blogs' => $blogs,
            'articles_by_blog' => $articlesGroupedByBlog,
        ]);
    }
}
