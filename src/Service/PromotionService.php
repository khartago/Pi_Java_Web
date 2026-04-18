<?php

namespace App\Service;

use App\Entity\Produit;
use App\Entity\Promotion;
use App\Repository\PromotionRepository;

class PromotionService
{
    public function __construct(
        private readonly PromotionRepository $promotionRepository,
    ) {
    }

    public function getBestPromotionForProduct(Produit $produit, int $quantity = 1): ?Promotion
    {
        if ($produit->getPrix() === null) {
            return null;
        }

        $promotions = $this->promotionRepository->findActiveForProduct($produit);

        if ($promotions === []) {
            return null;
        }

        $basePrice = (float) $produit->getPrix();
        $best = null;
        $bestSavings = -1.0;

        foreach ($promotions as $promotion) {
            if ($quantity < $promotion->getQuantiteMin()) {
                continue;
            }

            $finalPrice = $promotion->applyTo($basePrice, $quantity);
            $savings = $basePrice - $finalPrice;

            if ($savings > $bestSavings) {
                $bestSavings = $savings;
                $best = $promotion;
            }
        }

        return $best;
    }

    /**
     * @return array{original: float, final: float, promotion: ?Promotion, savings: float}
     */
    public function calculateDiscountedPrice(Produit $produit, int $quantity = 1): array
    {
        $original = (float) ($produit->getPrix() ?? 0.0);
        $promotion = $this->getBestPromotionForProduct($produit, $quantity);

        if ($promotion === null) {
            return [
                'original' => $original,
                'final' => $original,
                'promotion' => null,
                'savings' => 0.0,
            ];
        }

        $final = $promotion->applyTo($original, $quantity);

        return [
            'original' => $original,
            'final' => $final,
            'promotion' => $promotion,
            'savings' => max(0.0, $original - $final),
        ];
    }
}
