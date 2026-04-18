<?php

namespace App\Controller;

use App\Entity\Recommandation;
use App\Form\RecommandationType;
use App\Repository\RecommandationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/recommandations', name: 'app_recommandation_')]
final class RecommandationController extends AbstractController
{
    public function __construct(
        private readonly EntityManagerInterface $em,
        private readonly RecommandationRepository $repo,
    ) {
    }

    #[Route('', name: 'index', methods: ['GET'])]
    public function index(): Response
    {
        $all = $this->repo->findAllOrdered();

        // Group by product for nicer display
        $grouped = [];
        foreach ($all as $reco) {
            $pid = $reco->getProduit()?->getIdProduit();
            if ($pid === null) continue;
            $grouped[$pid]['produit'] = $reco->getProduit();
            $grouped[$pid]['recos'][] = $reco;
        }

        return $this->render('recommandation/index.html.twig', [
            'grouped'    => array_values($grouped),
            'totalCount' => count($all),
            'activeCount' => count(array_filter($all, static fn (Recommandation $r) => $r->isActif())),
        ]);
    }

    #[Route('/nouvelle', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $reco = new Recommandation();
        $form = $this->createForm(RecommandationType::class, $reco);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Prevent duplicate pair
            $existing = $this->repo->findOnePair($reco->getProduit(), $reco->getMateriel());
            if ($existing !== null) {
                $this->addFlash('error', 'Cette recommandation existe déjà pour ce couple produit/matériel.');
                return $this->redirectToRoute('app_recommandation_index');
            }

            $this->em->persist($reco);
            $this->em->flush();
            $this->addFlash('success', 'Recommandation créée.');
            return $this->redirectToRoute('app_recommandation_index');
        }

        return $this->render('recommandation/form.html.twig', [
            'form' => $form->createView(),
            'mode' => 'new',
        ]);
    }

    #[Route('/{id<\d+>}/modifier', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Recommandation $reco): Response
    {
        $form = $this->createForm(RecommandationType::class, $reco);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->em->flush();
            $this->addFlash('success', 'Recommandation mise à jour.');
            return $this->redirectToRoute('app_recommandation_index');
        }

        return $this->render('recommandation/form.html.twig', [
            'form' => $form->createView(),
            'mode' => 'edit',
            'reco' => $reco,
        ]);
    }

    #[Route('/{id<\d+>}/toggle', name: 'toggle', methods: ['POST'])]
    public function toggle(Request $request, Recommandation $reco): Response
    {
        if (!$this->isCsrfTokenValid('toggle-reco-'.$reco->getIdRecommandation(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Token CSRF invalide.');
        }
        $reco->setActif(!$reco->isActif());
        $this->em->flush();
        $this->addFlash('success', $reco->isActif() ? 'Recommandation activée.' : 'Recommandation désactivée.');
        return $this->redirectToRoute('app_recommandation_index');
    }

    #[Route('/{id<\d+>}/supprimer', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, Recommandation $reco): Response
    {
        if (!$this->isCsrfTokenValid('delete-reco-'.$reco->getIdRecommandation(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Token CSRF invalide.');
        }
        $this->em->remove($reco);
        $this->em->flush();
        $this->addFlash('success', 'Recommandation supprimée.');
        return $this->redirectToRoute('app_recommandation_index');
    }
}
