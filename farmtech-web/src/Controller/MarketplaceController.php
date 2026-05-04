<?php

namespace App\Controller;

use App\Entity\Produit;
use App\Entity\Promotion;
use App\Repository\ProduitRepository;
use App\Repository\RecommandationRepository;
use App\Service\MaterielRecommendationService;
use App\Service\PromotionService;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class MarketplaceController extends AbstractController
{
    private const MARKETPLACE_PER_PAGE = 8;

    #[Route('/marketplace', name: 'app_marketplace_index', methods: ['GET'])]
    public function index(Request $request, ProduitRepository $produitRepository, PromotionService $promotionService): Response
    {
        $recherche = trim((string) $request->query->get('recherche', ''));
        $unite = trim((string) $request->query->get('unite', ''));
        $categorie = trim((string) $request->query->get('categorie', ''));
        $page = max(1, (int) $request->query->get('page', 1));
        $favoriteIds = $this->getFavoriteIds($request->getSession());

        $products = $produitRepository->findForMarketplace($recherche, $unite);
        $cards = array_map(fn (Produit $p): array => $this->buildCard($p, $promotionService, $favoriteIds), $products);
        $categoryFacets = $this->buildCategoryFacets($cards);
        if ($categorie !== '') {
            $cards = array_values(array_filter($cards, static fn (array $card): bool => $card['category'] === $categorie));
        }
        $totalMatching = count($cards);
        $totalPages = max(1, (int) ceil($totalMatching / self::MARKETPLACE_PER_PAGE));
        $page = min($page, $totalPages);
        $cards = array_slice($cards, ($page - 1) * self::MARKETPLACE_PER_PAGE, self::MARKETPLACE_PER_PAGE);

        return $this->render('marketplace/index.html.twig', [
            'cards' => $cards,
            'recherche' => $recherche,
            'unite' => $unite,
            'categorie' => $categorie,
            'unites' => $produitRepository->findDistinctUnites(),
            'category_facets' => $categoryFacets,
            'total_products' => count($products),
            'total_matching' => $totalMatching,
            'current_page' => $page,
            'total_pages' => $totalPages,
            'materiel_total' => array_sum(array_column($cards, 'materielCount')),
            'favorites_count' => count($favoriteIds),
        ]);
    }

    #[Route('/marketplace/produit/{idProduit<\d+>}', name: 'app_marketplace_show', methods: ['GET'])]
    public function show(
        int $idProduit,
        ProduitRepository $produitRepository,
        PromotionService $promotionService,
        MaterielRecommendationService $materielRecommendationService,
        RecommandationRepository $recommandationRepository,
        Request $request
    ): Response
    {
        $produit = $produitRepository->findOneForDetail($idProduit);
        if (!$produit) {
            throw $this->createNotFoundException('Produit introuvable.');
        }
        $bestPromotion = $promotionService->getBestPromotionForProduct($produit);
        $linkedPromotion = $this->resolveLinkedPromotion($produit);
        $basePrice = $produit->getPrix();
        $promoPrice = null;
        if (null !== $bestPromotion && null !== $basePrice) {
            $promoPrice = $bestPromotion->applyTo((float) $basePrice, 1);
        }
        $recommendations = [];
        foreach ($recommandationRepository->findActiveForProduit($produit) as $item) {
            $materiel = $item->getMateriel();
            if (null === $materiel) {
                continue;
            }
            $recommendations[] = [
                'materiel' => $materiel,
                'score' => 100,
                'curated' => true,
                'priorityLabel' => $item->getPrioriteLabel(),
                'reason' => $item->getRaison(),
            ];
        }
        if ($recommendations === []) {
            foreach ($materielRecommendationService->recommend($produit, 4) as $item) {
                $recommendations[] = [
                    'materiel' => $item['materiel'],
                    'score' => (int) $item['score'],
                    'curated' => false,
                    'priorityLabel' => null,
                    'reason' => null,
                ];
            }
        }

        return $this->render('marketplace/show.html.twig', [
            'produit' => $produit,
            'category' => $this->resolveCategory($produit),
            'is_favorite' => in_array($idProduit, $this->getFavoriteIds($request->getSession()), true),
            'best_promotion' => $bestPromotion,
            'linked_promotion' => $linkedPromotion,
            'promo_price' => $promoPrice,
            'recommendations' => $recommendations,
        ]);
    }

    /**
     * @param list<int> $favoriteIds
     *
     * @return array<string, mixed>
     */
    private function buildCard(Produit $produit, PromotionService $promotionService, array $favoriteIds): array
    {
        $materielNames = [];
        foreach ($produit->getMateriels() as $materiel) {
            $materielNames[] = $materiel->getNom();
        }
        sort($materielNames, SORT_NATURAL | SORT_FLAG_CASE);
        $bestPromotion = $promotionService->getBestPromotionForProduct($produit);
        $linkedPromotion = $this->resolveLinkedPromotion($produit);
        $basePrice = $produit->getPrix();
        $promoPrice = null;
        if (null !== $bestPromotion && null !== $basePrice) {
            $promoPrice = $bestPromotion->applyTo((float) $basePrice, 1);
        }

        return [
            'produit' => $produit,
            'category' => $this->resolveCategory($produit),
            'materielCount' => count($materielNames),
            'materielPreview' => array_slice($materielNames, 0, 3),
            'isFavorite' => in_array($produit->getIdProduit(), $favoriteIds, true),
            'bestPromotion' => $bestPromotion,
            'linkedPromotion' => $linkedPromotion,
            'promoPrice' => $promoPrice,
        ];
    }

    /**
     * @param list<array{category: string}> $cards
     * @return list<array{name: string, count: int}>
     */
    private function buildCategoryFacets(array $cards): array
    {
        $counts = [];
        foreach ($cards as $card) {
            $counts[$card['category']] = ($counts[$card['category']] ?? 0) + 1;
        }
        ksort($counts, SORT_NATURAL | SORT_FLAG_CASE);

        $facets = [];
        foreach ($counts as $name => $count) {
            $facets[] = ['name' => $name, 'count' => $count];
        }

        return $facets;
    }

    private function resolveCategory(Produit $produit): string
    {
        $name = mb_strtolower($produit->getNom());
        $unit = $produit->getUnite();
        if (preg_match('/semence|graine|grain|plant|fourrage/u', $name) === 1) {
            return 'Semences';
        }
        if (preg_match('/engrais|fertili|compost|amendement/u', $name) === 1) {
            return 'Fertilisants';
        }
        if ($unit === 'l' || preg_match('/lait|huile|sirop|jus|liquide/u', $name) === 1) {
            return 'Liquides';
        }
        if ($unit === 'piece') {
            return 'Equipements';
        }
        if ($unit === 'kg') {
            return 'Intrants solides';
        }
        return 'Autres';
    }

    private function resolveLinkedPromotion(Produit $produit): ?Promotion
    {
        foreach ($produit->getPromotions() as $promotion) {
            return $promotion;
        }

        return null;
    }

    /**
     * @return list<int>
     */
    private function getFavoriteIds(SessionInterface $session): array
    {
        $raw = $session->get('marketplace_favorites', []);
        if (!is_array($raw)) {
            return [];
        }
        $normalized = array_values(array_unique(array_map(static fn (mixed $value): int => (int) $value, $raw)));

        return array_values(array_filter($normalized, static fn (int $id): bool => $id > 0));
    }
}
