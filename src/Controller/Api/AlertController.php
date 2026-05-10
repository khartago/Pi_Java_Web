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
        $lowStockProducts = $this->produitRepository->findLowStock($this->stockThreshold);
        $brokenMateriels = $this->materielRepository->findByEtatWithProduit('panne');
        $lowStockData = array_map(static fn ($p) => [
            'id' => $p->getIdProduit(),
            'nom' => $p->getNom(),
            'quantite' => $p->getQuantite(),
            'unite' => $p->getUnite(),
        ], $lowStockProducts);
        $brokenData = array_map(static fn ($m) => [
            'id' => $m->getIdMateriel(),
            'nom' => $m->getNom(),
            'etat' => $m->getEtat(),
            'produit' => $m->getProduit()?->getNom(),
        ], $brokenMateriels);

        return $this->json([
            'threshold' => $this->stockThreshold,
            'low_stock_count' => count($lowStockData),
            'broken_count' => count($brokenData),
            'low_stock_products' => $lowStockData,
            'broken_materiels' => $brokenData,
        ]);
    }

    #[Route('/send', name: 'send', methods: ['POST'])]
    public function send(): JsonResponse
    {
        try {
            $result = $this->notifier->sendCriticalAlerts($this->alertFromEmail, $this->alertToEmail, $this->stockThreshold);
        } catch (\Throwable $e) {
            return $this->json([
                'sent' => false,
                'message' => 'Echec envoi: '.$e->getMessage(),
            ], JsonResponse::HTTP_INTERNAL_SERVER_ERROR);
        }

        return $this->json([
            'sent' => $result['sent'],
            'low_stock_count' => $result['low_stock_count'],
            'broken_count' => $result['panne_count'],
            'message' => $result['sent']
                ? sprintf('Alertes envoyees vers %s', $this->alertToEmail)
                : 'Aucune alerte critique a envoyer',
        ]);
    }
}
