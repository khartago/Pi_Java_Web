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
        $byUnit = [];
        $stockHealth = ['out' => 0, 'low' => 0, 'healthy' => 0, 'over' => 0];
        $expirations = ['expired' => 0, 'soon' => 0, 'ok' => 0, 'none' => 0];
        $today = new \DateTimeImmutable('today');
        $soonLimit = $today->modify('+30 days');

        foreach ($produits as $p) {
            $quantity = (int) $p->getQuantite();
            $totalQuantity += $quantity;
            if (!empty($this->promotionRepository->findActiveForProduct($p))) {
                $withPromo++;
            }

            $unit = trim((string) $p->getUnite());
            if ($unit === '') {
                $unit = 'N/A';
            }
            $byUnit[$unit] = ($byUnit[$unit] ?? 0) + 1;

            if ($quantity <= 0) {
                $stockHealth['out']++;
            } elseif ($quantity <= $this->stockThreshold) {
                $stockHealth['low']++;
            } elseif ($quantity >= ($this->stockThreshold * 5)) {
                $stockHealth['over']++;
            } else {
                $stockHealth['healthy']++;
            }

            $expiration = $p->getDateExpiration();
            if (null === $expiration) {
                $expirations['none']++;
            } elseif ($expiration < $today) {
                $expirations['expired']++;
            } elseif ($expiration <= $soonLimit) {
                $expirations['soon']++;
            } else {
                $expirations['ok']++;
            }
        }

        usort($produits, static fn (Produit $a, Produit $b): int => (int) $b->getQuantite() <=> (int) $a->getQuantite());
        arsort($byUnit);

        $topQty = array_slice(array_map(static fn (Produit $p): array => ['produit' => $p, 'quantite' => (int) $p->getQuantite()], $produits), 0, 10);
        $topQtyChart = [
            'labels' => array_map(static fn (array $item): string => (string) $item['produit']->getNom(), $topQty),
            'values' => array_map(static fn (array $item): int => (int) $item['quantite'], $topQty),
        ];

        return [
            'overview' => [
                'total_products' => count($produits),
                'total_quantity' => $totalQuantity,
                'with_promo' => $withPromo,
            ],
            'threshold' => $this->stockThreshold,
            'top_qty' => $topQty,
            'top_qty_chart' => $topQtyChart,
            'top_value' => [],
            'by_category' => [],
            'value_by_category' => [],
            'by_unit' => $byUnit,
            'stock_health' => $stockHealth,
            'expirations' => $expirations,
        ];
    }
}
