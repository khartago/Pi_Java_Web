<?php

namespace App\Controller;

use App\Repository\ProblemeRepository;
use App\Util\StatsPresentation;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class HomeController extends AbstractController
{
    #[Route('/home', name: 'app_home')]
    public function index(ProblemeRepository $problemeRepository): Response
    {
        if ($this->isGranted('ROLE_ADMIN')) {
            return $this->redirectToRoute('admin_dashboard');
        }

        $user = $this->getUser();
        \assert($user instanceof \App\Entity\Utilisateur);
        $stats = $problemeRepository->getStats($user);
        $etatDistribution = StatsPresentation::distribution($stats['byEtat']);
        $graviteDistribution = StatsPresentation::distribution($stats['byGravite']);

        return $this->render('front/home.html.twig', [
            'stats' => $stats,
            'etatDistribution' => $etatDistribution,
            'graviteDistribution' => $graviteDistribution,
        ]);
    }
}