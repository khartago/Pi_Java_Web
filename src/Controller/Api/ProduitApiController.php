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
        $ids = array_values(array_filter(array_map(
            static fn (string $v): int => (int) trim($v),
            explode(',', $idsParam),
        ), static fn (int $id): bool => $id > 0));

        if (empty($ids)) {
            return $this->json([
                'items'     => [],
                'threshold' => $this->stockThreshold,
            ]);
        }

        $threshold = $this->stockThreshold;
        $today     = new \DateTimeImmutable('today');
        $soon      = $today->modify('+30 days');

        $produits = $this->produitRepository->findBy(['idProduit' => $ids]);
        $items = array_map(static function (Produit $p) use ($threshold, $today, $soon): array {
            $quantite = (int) $p->getQuantite();
            $expiry   = $p->getDateExpiration();
            return [
                'id'              => $p->getIdProduit(),
                'nom'             => $p->getNom(),
                'quantite'        => $quantite,
                'unite'           => $p->getUnite(),
                'dateExpiration'  => $p->getDateExpirationLabel(),
                'imagePath'       => $p->getImageAssetPath(),
                'materielCount'   => $p->getMateriels()->count(),
                'isLowStock'      => $quantite <= $threshold,
                'isOutOfStock'    => $quantite === 0,
                'isExpiringSoon'  => $expiry !== null && $expiry <= $soon && $expiry >= $today,
                'isExpired'       => $expiry !== null && $expiry < $today,
            ];
        }, $produits);

        return $this->json([
            'items'     => $items,
            'threshold' => $threshold,
        ]);
    }
}
