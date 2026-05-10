<?php

namespace App\Controller;

use App\Service\WebNewsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class NewsController extends AbstractController
{
    #[Route('/news', name: 'app_news', methods: ['GET'])]
    public function index(WebNewsService $newsService): Response
    {
        $news = $newsService->getNews();

        return $this->render('news/index.html.twig', [
            'articles' => $news['articles'] ?? [],
        ]);
    }
}
