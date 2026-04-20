<?php

namespace App\Controller\Api;

use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;
use App\Service\CriticalAlertNotifier;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/alerts', name: 'api_alerts_')]
final class AlertController extends AbstractController
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly CriticalAlertNotifier $notifier,
        private readonly string $alertFromEmail,
        private readonly string $alertToEmail,
        private readonly int $stockThreshold,
    ) {
    }

    #[Route('/summary', name: 'summary', methods: ['GET'])]
    public function summary(): JsonResponse
    {
        return $this->json([
            'threshold' => $this->stockThreshold,
            'low_stock_count' => count($this->produitRepository->findLowStock($this->stockThreshold)),
            'broken_count' => count($this->materielRepository->findByEtatWithProduit('panne')),
        ]);
    }

    #[Route('/send', name: 'send', methods: ['POST'])]
    public function send(): JsonResponse
    {
        $result = $this->notifier->sendCriticalAlerts($this->alertFromEmail, $this->alertToEmail, $this->stockThreshold);
        return $this->json(['sent' => $result['sent'], 'low_stock_count' => $result['low_stock_count'], 'broken_count' => $result['panne_count']]);
    }
}
