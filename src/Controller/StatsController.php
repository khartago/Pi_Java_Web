<?php

namespace App\Controller;

use App\Service\ProduitStatsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class StatsController extends AbstractController
{
    #[Route('/admin/statistiques/produits', name: 'app_stats_produits', methods: ['GET'])]
    public function produits(ProduitStatsService $statsService): Response
    {
        return $this->render('stats/produits.html.twig', [
            'stats' => $statsService->compute(),
        ]);
    }
}
