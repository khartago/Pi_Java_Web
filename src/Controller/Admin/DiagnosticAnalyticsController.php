<?php

namespace App\Controller\Admin;

use App\Service\DiagnosticAnalyticsService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/analytics')]
class DiagnosticAnalyticsController extends AbstractController
{
    #[Route('/diagnostics', name: 'admin_analytics_diagnostics', methods: ['GET'])]
    public function diagnostics(DiagnosticAnalyticsService $analytics): Response
    {
        $taux = $analytics->getTauxResolution();
        $dureeH = $analytics->getDureeMoyenneDiagnosticHeures();
        $parType = $analytics->getProblemesParType();
        $causes = $analytics->getCausesFrequentes(10);
        $parPlant = $analytics->getProblemesParPlantation();
        $parProd = $analytics->getProblemesParProduit();

        return $this->render('admin/analytics/diagnostics.html.twig', [
            'tauxResolution' => $taux,
            'dureeMoyenneHeures' => $dureeH,
            'problemesParType' => $parType,
            'causesFrequentes' => $causes,
            'problemesParPlantation' => $parPlant,
            'problemesParProduit' => $parProd,
        ]);
    }
}
