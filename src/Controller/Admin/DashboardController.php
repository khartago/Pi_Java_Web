<?php

namespace App\Controller\Admin;

use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Repository\UtilisateurRepository;
use App\Util\StatsPresentation;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin')]
class DashboardController extends AbstractController
{
    #[Route('', name: 'admin_dashboard')]
    public function index(
        ProblemeRepository $problemeRepository,
        DiagnostiqueRepository $diagnostiqueRepository,
        UtilisateurRepository $utilisateurRepository,
    ): Response {
        $pStats = $problemeRepository->getStats(null);
        $dStats = $diagnostiqueRepository->getStats();
        $uStats = $utilisateurRepository->getStats();

        $problemeByEtat = StatsPresentation::distribution($pStats['byEtat']);
        $usersByRole = StatsPresentation::distribution($uStats['byRole']);
        if ($dStats['total'] > 0) {
            $dStats['pctApproved'] = round(100 * $dStats['approved'] / $dStats['total'], 1);
            $dStats['pctPending'] = round(100 * $dStats['pendingApproval'] / $dStats['total'], 1);
        } else {
            $dStats['pctApproved'] = 0.0;
            $dStats['pctPending'] = 0.0;
        }

        return $this->render('admin/dashboard.html.twig', [
            'problemeStats' => $pStats,
            'problemeByEtat' => $problemeByEtat,
            'diagnosticStats' => $dStats,
            'userStats' => $uStats,
            'usersByRole' => $usersByRole,
        ]);
    }
}
