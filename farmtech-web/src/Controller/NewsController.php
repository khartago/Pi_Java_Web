<?php

namespace App\Controller;

use App\Repository\ArticleRepository;
use App\Repository\BlogRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/news')]
class NewsController extends AbstractController
{
    #[Route('', name: 'app_news', methods: ['GET'])]
    #[IsGranted('IS_AUTHENTICATED_FULLY')]
    public function index(BlogRepository $blogRepository, ArticleRepository $articleRepository): Response
    {
        $user = $this->getUser();

        // 1. Get all blogs created by this user
        $blogs = $blogRepository->findByUser($user);

        // 2. Extract just the IDs from those blogs
        $blogIds = array_map(fn($b) => $b->getIdBlog(), $blogs);

        // 3. Fetch all articles that belong to these blog IDs
        $articlesGroupedByBlog = [];
        if (!empty($blogIds)) {
            $articles = $articleRepository->findBy(['blogId' => $blogIds]);

            // 4. Group the articles by their blogId so Twig can loop through them easily
            foreach ($articles as $article) {
                $articlesGroupedByBlog[$article->getBlogId()][] = $article;
            }
        }

        return $this->render('news/index.html.twig', [
            'blogs' => $blogs,
            'articles_by_blog' => $articlesGroupedByBlog, // Send this grouped array to Twig
        ]);
    }
}