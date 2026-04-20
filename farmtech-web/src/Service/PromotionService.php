<?php

namespace App\Service;

use App\Entity\Produit;
use App\Entity\Promotion;
use App\Repository\PromotionRepository;

class PromotionService
{
    public function __construct(private readonly PromotionRepository $promotionRepository)
    {
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
            $savings = $basePrice - $promotion->applyTo($basePrice, $quantity);
            if ($savings > $bestSavings) {
                $bestSavings = $savings;
                $best = $promotion;
            }
        }

        return $best;
    }
}
