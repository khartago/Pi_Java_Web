<?php

namespace App\Service;

use App\Entity\Produit;
use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;
use App\Repository\PromotionRepository;

final class ProduitStatsService
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly PromotionRepository $promotionRepository,
        private readonly int $stockThreshold,
    ) {
    }

    /**
     * @return array<string, mixed>
     */
    public function compute(): array
    {
        $produits = $this->produitRepository->findAll();
        $totalQuantity = 0;
        $withPromo = 0;

        foreach ($produits as $p) {
            $totalQuantity += (int) $p->getQuantite();
            if (!empty($this->promotionRepository->findActiveForProduct($p))) {
                $withPromo++;
            }
        }

        return [
            'overview' => [
                'total_products' => count($produits),
                'total_quantity' => $totalQuantity,
                'with_promo' => $withPromo,
            ],
            'threshold' => $this->stockThreshold,
            'top_qty' => array_slice(array_map(static fn (Produit $p): array => ['produit' => $p, 'quantite' => (int) $p->getQuantite()], $produits), 0, 10),
            'top_value' => [],
            'by_category' => [],
            'value_by_category' => [],
            'by_unit' => [],
            'stock_health' => ['out' => 0, 'low' => 0, 'healthy' => 0, 'over' => 0],
            'expirations' => ['expired' => 0, 'soon' => 0, 'ok' => 0, 'none' => 0],
        ];
    }
}
