<?php

namespace App\Service;

use App\Entity\Produit;
use App\Repository\MaterielRepository;
use App\Repository\RecommandationRepository;

final class MaterielRecommendationService
{
    public function __construct(
        private readonly MaterielRepository $materielRepository,
        private readonly RecommandationRepository $recommandationRepository,
    ) {
    }

    /**
     * @return list<array{materiel: \App\Entity\Materiel, score: int}>
     */
    public function recommend(Produit $produit, int $limit = 4): array
    {
        $scored = [];
        foreach ($this->materielRepository->findAllWithProduit() as $materiel) {
            if ($materiel->getProduit()?->getIdProduit() === $produit->getIdProduit()) {
                continue;
            }
            if ($materiel->getEtat() === 'panne') {
                continue;
            }
            $score = $materiel->getEtat() === 'neuf' ? 80 : ($materiel->getEtat() === 'bon' ? 65 : 40);
            $scored[] = ['materiel' => $materiel, 'score' => $score];
        }

        usort($scored, static fn (array $a, array $b): int => $b['score'] <=> $a['score']);
        return array_slice($scored, 0, $limit);
    }
}
