<?php

namespace App\Controller;

use App\Repository\AffectationRepository;
use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class AdminDashboardController extends AbstractController
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly AffectationRepository $affectationRepository,
        private readonly int $stockThreshold,
    ) {
    }

    #[Route('/admin', name: 'app_admin_dashboard')]
    public function index(): Response
    {
        $lowStock     = $this->produitRepository->findLowStock($this->stockThreshold);
        $broken       = $this->materielRepository->findByEtatWithProduit('panne');
        $affectations = $this->affectationRepository->findAllActive();
        $expiring     = $this->produitRepository->findExpiringBefore(
            new \DateTimeImmutable('+30 days')
        );

        return $this->render('admin/dashboard.html.twig', [
            'total_produits'       => count($this->produitRepository->findForList()),
            'total_materiels'      => count($this->materielRepository->findAll()),
            'low_stock_count'      => count($lowStock),
            'broken_count'         => count($broken),
            'affectations_actives' => count($affectations),
            'expiring_count'       => count($expiring),
            'low_stock_products'   => array_slice($lowStock, 0, 5),
            'broken_materiels'     => array_slice($broken, 0, 5),
        ]);
    }
}
