<?php

namespace App\Controller;

use App\Repository\ProduitRepository;
use App\Service\PromotionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Attribute\Route;

final class FavorisController extends AbstractController
{
    #[Route('/marketplace/favoris', name: 'app_favoris_index', methods: ['GET'])]
    public function index(Request $request, ProduitRepository $produitRepository, PromotionService $promotionService): Response
    {
        $favoriteIds = $this->getFavoriteIds($request->getSession());
        $products = $favoriteIds === []
            ? []
            : $produitRepository->findBy(['idProduit' => $favoriteIds]);
        usort($products, static fn ($a, $b): int => strcasecmp($a->getNom(), $b->getNom()));

        $cards = array_map(function ($produit) use ($promotionService): array {
            $bestPromotion = $promotionService->getBestPromotionForProduct($produit);
            $basePrice = $produit->getPrix();
            $promoPrice = null;
            if (null !== $bestPromotion && null !== $basePrice) {
                $promoPrice = $bestPromotion->applyTo((float) $basePrice, 1);
            }

            return [
                'produit' => $produit,
                'bestPromotion' => $bestPromotion,
                'promoPrice' => $promoPrice,
            ];
        }, $products);

        return $this->render('marketplace/favoris.html.twig', ['cards' => $cards]);
    }

    #[Route('/marketplace/favoris/toggle/{idProduit<\d+>}', name: 'app_favoris_toggle', methods: ['POST'])]
    public function toggle(int $idProduit, Request $request, ProduitRepository $produitRepository): RedirectResponse
    {
        if (!$this->isCsrfTokenValid('toggle_favorite_'.$idProduit, (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Token CSRF invalide.');
        }
        if (null === $produitRepository->find($idProduit)) {
            throw $this->createNotFoundException('Produit introuvable.');
        }

        $session = $request->getSession();
        $favoriteIds = $this->getFavoriteIds($session);
        if (in_array($idProduit, $favoriteIds, true)) {
            $favoriteIds = array_values(array_filter($favoriteIds, static fn (int $favId): bool => $favId !== $idProduit));
            $this->addFlash('info', 'Produit retire des favoris.');
        } else {
            $favoriteIds[] = $idProduit;
            $favoriteIds = array_values(array_unique($favoriteIds));
            $this->addFlash('success', 'Produit ajoute aux favoris.');
        }
        $session->set('marketplace_favorites', $favoriteIds);

        $redirect = trim((string) $request->request->get('_redirect'));
        if ('' !== $redirect && str_starts_with($redirect, '/')) {
            return $this->redirect($redirect);
        }

        return $this->redirectToRoute('app_marketplace_index');
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
