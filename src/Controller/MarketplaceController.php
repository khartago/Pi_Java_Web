<?php

namespace App\Controller;

use App\Entity\Produit;
use App\Repository\ProduitRepository;
use App\Service\MaterielRecommendationService;
use App\Service\PromotionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class MarketplaceController extends AbstractController
{
    private const PER_PAGE = 9
    ;

    public function __construct(
        private readonly PromotionService $promotionService,
        private readonly MaterielRecommendationService $recommendationService,
    ) {
    }

    #[Route('/marketplace/produit/{idProduit<\d+>}', name: 'app_marketplace_show', methods: ['GET'])]
    public function show(int $idProduit, ProduitRepository $produitRepository): Response
    {
        $produit = $produitRepository->findOneForDetail($idProduit);

        if (!$produit) {
            throw $this->createNotFoundException('Produit introuvable.');
        }

        $promotion = $this->promotionService->getBestPromotionForProduct($produit);
        $discountedPrice = null;
        if ($promotion !== null && $produit->getPrix() !== null) {
            $discountedPrice = $promotion->applyTo((float) $produit->getPrix(), 1);
        }

        $recommendations = $this->recommendationService->recommend($produit, 4);

        $relatedProducts = array_filter(
            $produitRepository->findForMarketplace(),
            static fn (Produit $p): bool => $p->getIdProduit() !== $idProduit
        );
        shuffle($relatedProducts);
        $relatedProducts = array_slice($relatedProducts, 0, 4);

        return $this->render('marketplace/show.html.twig', [
            'produit'         => $produit,
            'category'        => $this->resolveCategory($produit),
            'promotion'       => $promotion,
            'discountedPrice' => $discountedPrice,
            'recommendations' => $recommendations,
            'related'         => $relatedProducts,
        ]);
    }

    #[Route('/marketplace', name: 'app_marketplace_index', methods: ['GET'])]
    public function index(Request $request, ProduitRepository $produitRepository): Response
    {
        $recherche = trim((string) $request->query->get('recherche', ''));
        $unite = trim((string) $request->query->get('unite', ''));
        $categorie = trim((string) $request->query->get('categorie', ''));
        $page = max(1, (int) $request->query->get('page', 1));

        $products = $produitRepository->findForMarketplace($recherche, $unite);
        $cards = array_map(
            fn (Produit $produit): array => $this->buildCard($produit),
            $products,
        );

        $categoryFacets = $this->buildCategoryFacets($cards);

        if ($categorie !== '') {
            $cards = array_values(array_filter(
                $cards,
                static fn (array $card): bool => $card['category'] === $categorie,
            ));
        }

        $materielTotal = array_reduce(
            $cards,
            static fn (int $carry, array $card): int => $carry + $card['materielCount'],
            0,
        );

        $totalCards = count($cards);
        $totalPages = max(1, (int) ceil($totalCards / self::PER_PAGE));
        $page = min($page, $totalPages);
        $pagedCards = array_slice($cards, ($page - 1) * self::PER_PAGE, self::PER_PAGE);

        return $this->render('marketplace/index.html.twig', [
            'cards' => $pagedCards,
            'recherche' => $recherche,
            'unite' => $unite,
            'categorie' => $categorie,
            'unites' => $produitRepository->findDistinctUnites(),
            'category_facets' => $categoryFacets,
            'total_products' => count($products),
            'materiel_total' => $materielTotal,
            'pagination' => [
                'page'        => $page,
                'total_pages' => $totalPages,
                'total_items' => $totalCards,
                'per_page'    => self::PER_PAGE,
                'from'        => $totalCards === 0 ? 0 : ($page - 1) * self::PER_PAGE + 1,
                'to'          => min($page * self::PER_PAGE, $totalCards),
            ],
        ]);
    }

    /**
     * @return array{
     *     produit: Produit,
     *     category: string,
     *     materielCount: int,
     *     materielPreview: list<string>,
     *     promotion: ?\App\Entity\Promotion,
     *     discountedPrice: ?float
     * }
    */
    private function buildCard(Produit $produit): array
    {
        $materielNames = [];

        foreach ($produit->getMateriels() as $materiel) {
            $materielNames[] = $materiel->getNom();
        }

        sort($materielNames, SORT_NATURAL | SORT_FLAG_CASE);

        $promotion = $this->promotionService->getBestPromotionForProduct($produit);
        $discountedPrice = null;

        if ($promotion !== null && $produit->getPrix() !== null) {
            $discountedPrice = $promotion->applyTo((float) $produit->getPrix(), 1);
        }

        return [
            'produit' => $produit,
            'category' => $this->resolveCategory($produit),
            'materielCount' => count($materielNames),
            'materielPreview' => array_slice($materielNames, 0, 3),
            'promotion' => $promotion,
            'discountedPrice' => $discountedPrice,
        ];
    }

    /**
     * @param list<array{category: string}> $cards
     *
     * @return list<array{name: string, count: int}>
     */
    private function buildCategoryFacets(array $cards): array
    {
        $counts = [];

        foreach ($cards as $card) {
            $category = $card['category'];
            $counts[$category] = ($counts[$category] ?? 0) + 1;
        }

        ksort($counts, SORT_NATURAL | SORT_FLAG_CASE);

        $facets = [];

        foreach ($counts as $name => $count) {
            $facets[] = [
                'name' => $name,
                'count' => $count,
            ];
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
}
