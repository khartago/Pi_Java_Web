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
        $today    = new \DateTimeImmutable('today');
        $in30     = $today->modify('+30 days');
        $in90     = $today->modify('+90 days');

        // ── Overview KPIs
        $totalProducts  = count($produits);
        $totalQuantity  = 0;
        $totalValue     = 0.0;
        $withPrice      = 0;
        $withoutPrice   = 0;
        $withPromo      = 0;

        // ── Categories (pie)
        $byCategory = [];
        $valueByCategory = [];

        // ── Units (bar)
        $byUnit = [];

        // ── Stock health
        $outOfStock  = 0;
        $lowStock    = 0;
        $healthyStock = 0;
        $overstock   = 0;

        // ── Expirations
        $expired     = 0;
        $expSoon     = 0;
        $expOk       = 0;
        $noExpiry    = 0;

        // ── Top by quantity / value
        $topQty = [];
        $topValue = [];

        foreach ($produits as $p) {
            $qty   = (int) $p->getQuantite();
            $prix  = $p->getPrix();
            $unite = $p->getUnite();
            $cat   = $this->categorize($p);
            $expir = $p->getDateExpiration();

            // Totals
            $totalQuantity += $qty;
            if ($prix !== null) {
                $withPrice++;
                $lineValue = $prix * $qty;
                $totalValue += $lineValue;
                $valueByCategory[$cat] = ($valueByCategory[$cat] ?? 0) + $lineValue;

                $topValue[] = ['produit' => $p, 'value' => $lineValue];
            } else {
                $withoutPrice++;
            }

            // Category count
            $byCategory[$cat] = ($byCategory[$cat] ?? 0) + 1;

            // Unit count
            $byUnit[$unite] = ($byUnit[$unite] ?? 0) + 1;

            // Stock health
            if ($qty === 0) {
                $outOfStock++;
            } elseif ($qty <= $this->stockThreshold) {
                $lowStock++;
            } elseif ($qty > $this->stockThreshold * 3) {
                $overstock++;
            } else {
                $healthyStock++;
            }

            // Expirations
            if ($expir === null) {
                $noExpiry++;
            } elseif ($expir < $today) {
                $expired++;
            } elseif ($expir <= $in30) {
                $expSoon++;
            } else {
                $expOk++;
            }

            // Promotion
            if (!empty($this->promotionRepository->findActiveForProduct($p))) {
                $withPromo++;
            }

            $topQty[] = ['produit' => $p, 'quantite' => $qty, 'unite' => $unite];
        }

        // Sort tops
        usort($topQty, static fn ($a, $b) => $b['quantite'] <=> $a['quantite']);
        usort($topValue, static fn ($a, $b) => $b['value'] <=> $a['value']);
        $topQty = array_slice($topQty, 0, 10);
        $topValue = array_slice($topValue, 0, 5);

        // Sort categories alphabetically for stable rendering
        ksort($byCategory);
        ksort($valueByCategory);
        ksort($byUnit);

        // Cost per materiels aggregated
        $materielCosts = $this->materielRepository->sumCostByProduit();
        $totalMaterielCost = array_sum(array_map(static fn ($r) => (float) $r['total_cout'], $materielCosts));
        $totalMateriels = array_sum(array_map(static fn ($r) => (int) $r['count'], $materielCosts));

        return [
            'overview' => [
                'total_products'     => $totalProducts,
                'total_quantity'     => $totalQuantity,
                'total_value'        => $totalValue,
                'with_price'         => $withPrice,
                'without_price'      => $withoutPrice,
                'with_promo'         => $withPromo,
                'total_materiels'    => $totalMateriels,
                'total_mat_cost'     => $totalMaterielCost,
                'avg_qty_per_prod'   => $totalProducts > 0 ? round($totalQuantity / $totalProducts, 1) : 0,
            ],
            'by_category'      => $this->toChartData($byCategory),
            'value_by_category' => $this->toChartData($valueByCategory),
            'by_unit'          => $this->toChartData($byUnit),
            'stock_health'     => [
                'out'     => $outOfStock,
                'low'     => $lowStock,
                'healthy' => $healthyStock,
                'over'    => $overstock,
            ],
            'expirations' => [
                'expired'  => $expired,
                'soon'     => $expSoon,
                'ok'       => $expOk,
                'none'     => $noExpiry,
            ],
            'top_qty'   => $topQty,
            'top_value' => $topValue,
            'threshold' => $this->stockThreshold,
        ];
    }

    /**
     * Converts [name => count] to a sorted array with percentage.
     *
     * @param array<string, int|float> $data
     * @return list<array{name: string, count: int|float, percent: float}>
     */
    private function toChartData(array $data): array
    {
        $total = array_sum($data);
        $result = [];
        foreach ($data as $name => $count) {
            $result[] = [
                'name'    => $name,
                'count'   => $count,
                'percent' => $total > 0 ? round(($count / $total) * 100, 1) : 0,
            ];
        }
        usort($result, static fn ($a, $b) => $b['count'] <=> $a['count']);
        return $result;
    }

    private function categorize(Produit $p): string
    {
        $name = mb_strtolower($p->getNom());
        $unit = $p->getUnite();

        if (preg_match('/semence|graine|grain|plant|fourrage/u', $name) === 1) return 'Semences';
        if (preg_match('/engrais|fertili|compost|amendement/u', $name) === 1) return 'Fertilisants';
        if ($unit === 'l' || preg_match('/lait|huile|sirop|jus|liquide/u', $name) === 1) return 'Liquides';
        if ($unit === 'piece') return 'Equipements';
        if ($unit === 'kg') return 'Intrants solides';
        return 'Autres';
    }
}
