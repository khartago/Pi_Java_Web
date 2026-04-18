<?php

namespace App\Service;

use App\Entity\Materiel;
use App\Entity\Produit;
use App\Repository\MaterielRepository;
use App\Repository\RecommandationRepository;

/**
 * Multi-criteria scoring engine that recommends matériels for a given produit.
 *
 * Scoring (max 100):
 *  - Same category as the source produit ............ 35 pts
 *  - Compatible unit (kg/l/piece match) ............. 15 pts
 *  - Keyword overlap in product name ................ 10 pts
 *  - Equipment state quality (neuf/bon/usagé/panne) . 0–25 pts
 *  - Available (no active assignment) ............... 10 pts
 *  - Recent purchase (≤ 3 years) .................... 5 pts
 */
final class MaterielRecommendationService
{
    private const KEYWORD_GROUPS = [
        'arrosage'   => ['lait', 'huile', 'sirop', 'jus', 'liquide', 'eau'],
        'plantation' => ['semence', 'graine', 'plant', 'fourrage'],
        'fertilisation' => ['engrais', 'fertili', 'compost', 'amendement'],
    ];

    public function __construct(
        private readonly MaterielRepository $materielRepository,
        private readonly RecommandationRepository $recommandationRepository,
    ) {
    }

    /**
     * @return list<array{materiel: Materiel, score: int, matchPercent: int, reasons: list<array{icon: string, label: string}>, curated: bool, priority: ?int, priorityLabel: ?string, adminReason: ?string}>
     */
    public function recommend(Produit $produit, int $limit = 4): array
    {
        // 1) Admin-curated recommendations (priorité 1–5 → bonus 20–50 pts)
        $curatedByMaterielId = [];
        foreach ($this->recommandationRepository->findActiveForProduit($produit) as $reco) {
            $m = $reco->getMateriel();
            if ($m === null) continue;
            $curatedByMaterielId[$m->getIdMateriel()] = $reco;
        }
        $sourceCategory = $this->categorize($produit);
        $sourceUnit     = $produit->getUnite();
        $sourceKeywords = $this->extractKeywords($produit->getNom());
        $thisYear       = (int) date('Y');

        $scored = [];

        foreach ($this->materielRepository->findAllWithProduit() as $materiel) {
            // Skip materials already linked to THIS product
            if ($materiel->getProduit()?->getIdProduit() === $produit->getIdProduit()) {
                continue;
            }

            $score   = 0;
            $reasons = [];
            $parent  = $materiel->getProduit();

            // 1. Same category (35 pts) — strongest signal
            if ($parent !== null) {
                $matCategory = $this->categorize($parent);
                if ($matCategory === $sourceCategory) {
                    $score += 35;
                    $reasons[] = ['icon' => '🎯', 'label' => "Catégorie « {$sourceCategory} »"];
                }
            }

            // 2. Compatible unit (15 pts)
            if ($parent !== null && $parent->getUnite() === $sourceUnit) {
                $score += 15;
                $reasons[] = ['icon' => '📐', 'label' => "Compatible {$sourceUnit}"];
            }

            // 3. Keyword overlap (10 pts)
            $matKeywords = $this->extractKeywords(($parent?->getNom() ?? '').' '.$materiel->getNom());
            $overlap = array_intersect($sourceKeywords, $matKeywords);
            if (!empty($overlap)) {
                $score += 10;
                $reasons[] = ['icon' => '🔗', 'label' => 'Usage proche : '.implode(', ', $overlap)];
            }

            // 4. Equipment state (0-25 pts, "panne" disqualifies entirely)
            switch ($materiel->getEtat()) {
                case 'neuf':
                    $score += 25;
                    $reasons[] = ['icon' => '✨', 'label' => 'Matériel neuf'];
                    break;
                case 'bon':
                    $score += 18;
                    $reasons[] = ['icon' => '✓', 'label' => 'Bon état'];
                    break;
                case 'usagé':
                    $score += 5;
                    break;
                case 'panne':
                    continue 2; // skip entirely — never recommend broken equipment
            }

            // 5. Available (10 pts)
            $hasActive = method_exists($materiel, 'getAffectationActive')
                ? $materiel->getAffectationActive() !== null
                : false;
            if (!$hasActive) {
                $score += 10;
                $reasons[] = ['icon' => '🟢', 'label' => 'Disponible immédiatement'];
            }

            // 6. Recent purchase (5 pts)
            $purchaseYear = (int) ($materiel->getDateAchat()?->format('Y') ?? 0);
            if ($purchaseYear >= $thisYear - 3 && $purchaseYear > 0) {
                $score += 5;
                $reasons[] = ['icon' => '📅', 'label' => "Acheté en {$purchaseYear}"];
            }

            // 7. Admin curated bonus (10 × priorité → 10–50 pts)
            $curated    = $curatedByMaterielId[$materiel->getIdMateriel()] ?? null;
            $isCurated  = $curated !== null;
            $priority   = $curated?->getPriorite();
            $adminReason = $curated?->getRaison();
            if ($isCurated) {
                $bonus  = 10 * $priority;
                $score += $bonus;
                array_unshift($reasons, [
                    'icon'  => '👨‍💼',
                    'label' => "Recommandé par le gérant ({$curated->getPrioriteLabel()})",
                ]);
            }

            if ($score <= 0) {
                continue;
            }

            $scored[] = [
                'materiel'      => $materiel,
                'score'         => $score,
                'matchPercent'  => min(100, $score),
                'reasons'       => $reasons,
                'curated'       => $isCurated,
                'priority'      => $priority,
                'priorityLabel' => $curated?->getPrioriteLabel(),
                'adminReason'   => $adminReason,
            ];

            unset($curatedByMaterielId[$materiel->getIdMateriel()]);
        }

        // Curated recos pointing to materiels NOT in the main iteration (own-product case) — keep them
        foreach ($curatedByMaterielId as $reco) {
            $m = $reco->getMateriel();
            if ($m === null) continue;
            $score = 50 + (10 * $reco->getPriorite());
            $scored[] = [
                'materiel'      => $m,
                'score'         => $score,
                'matchPercent'  => min(100, $score),
                'reasons'       => [[
                    'icon'  => '👨‍💼',
                    'label' => "Recommandé par le gérant ({$reco->getPrioriteLabel()})",
                ]],
                'curated'       => true,
                'priority'      => $reco->getPriorite(),
                'priorityLabel' => $reco->getPrioriteLabel(),
                'adminReason'   => $reco->getRaison(),
            ];
        }

        usort($scored, static fn (array $a, array $b): int => $b['score'] <=> $a['score']);

        return array_slice($scored, 0, $limit);
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

    /**
     * @return list<string>
     */
    private function extractKeywords(string $text): array
    {
        $text = mb_strtolower($text);
        $found = [];
        foreach (self::KEYWORD_GROUPS as $group => $words) {
            foreach ($words as $w) {
                if (str_contains($text, $w)) {
                    $found[] = $group;
                    break;
                }
            }
        }
        return array_values(array_unique($found));
    }
}
