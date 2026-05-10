<?php

namespace App\Controller\Api;

use App\Entity\Produit;
use App\Repository\ProduitRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/produits', name: 'api_produits_')]
final class ProduitApiController extends AbstractController
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly int $stockThreshold,
    ) {
    }

    #[Route('/batch', name: 'batch', methods: ['GET'])]
    public function batch(Request $request): JsonResponse
    {
        $idsParam = (string) $request->query->get('ids', '');
        $ids = array_values(array_filter(array_map(static fn (string $v): int => (int) trim($v), explode(',', $idsParam)), static fn (int $id): bool => $id > 0));

        if ($ids === []) {
            return $this->json(['items' => [], 'threshold' => $this->stockThreshold]);
        }

        $produits = $this->produitRepository->findBy(['idProduit' => $ids]);
        $items = array_map(fn (Produit $p): array => [
            'id' => $p->getIdProduit(),
            'nom' => $p->getNom(),
            'quantite' => (int) $p->getQuantite(),
            'unite' => $p->getUnite(),
            'isLowStock' => (int) $p->getQuantite() <= $this->stockThreshold,
        ], $produits);

        return $this->json(['items' => $items, 'threshold' => $this->stockThreshold]);
    }
}
