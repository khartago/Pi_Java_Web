<?php

namespace App\Controller;

use App\Entity\Promotion;
use App\Form\PromotionType;
use App\Repository\PromotionRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/promotions')]
class PromotionController extends AbstractController
{
    #[Route('', name: 'app_promotion_index', methods: ['GET'])]
    public function index(Request $request, PromotionRepository $promotionRepository): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $promotions = $promotionRepository->findAll();

        $total = count($promotions);
        $actives = 0;
        $upcoming = 0;
        $expired = 0;

        foreach ($promotions as $promotion) {
            if ($promotion->isCurrentlyActive()) {
                $actives++;
            } elseif ($promotion->isUpcoming()) {
                $upcoming++;
            } elseif ($promotion->isExpired()) {
                $expired++;
            }
        }

        return $this->render('promotion/index.html.twig', [
            'promotions' => $promotions,
            'stats' => [
                'total' => $total,
                'actives' => $actives,
                'upcoming' => $upcoming,
                'expired' => $expired,
            ],
        ]);
    }

    #[Route('/nouvelle', name: 'app_promotion_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $promotion = new Promotion();
        $form = $this->createForm(PromotionType::class, $promotion);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($promotion);
            $em->flush();
            $this->addFlash('success', sprintf('La promotion "%s" a été créée.', $promotion->getNom()));

            return $this->redirectToRoute('app_promotion_index');
        }

        return $this->render('promotion/form.html.twig', [
            'form' => $form,
            'promotion' => $promotion,
            'mode' => 'creation',
            'submit_label' => 'Créer la promotion',
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/{id}/modifier', name: 'app_promotion_edit', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function edit(int $id, Request $request, PromotionRepository $promotionRepository, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $promotion = $promotionRepository->find($id);
        if ($promotion === null) {
            throw $this->createNotFoundException('Promotion introuvable.');
        }

        $form = $this->createForm(PromotionType::class, $promotion);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', sprintf('La promotion "%s" a été mise à jour.', $promotion->getNom()));

            return $this->redirectToRoute('app_promotion_index');
        }

        return $this->render('promotion/form.html.twig', [
            'form' => $form,
            'promotion' => $promotion,
            'mode' => 'edition',
            'submit_label' => 'Modifier la promotion',
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/{id}/toggle', name: 'app_promotion_toggle', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function toggle(int $id, Request $request, PromotionRepository $promotionRepository, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $promotion = $promotionRepository->find($id);
        if ($promotion === null) {
            throw $this->createNotFoundException('Promotion introuvable.');
        }

        if (!$this->isCsrfTokenValid('toggle-promotion-' . $id, (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $promotion->setActif(!$promotion->isActif());
        $em->flush();

        $this->addFlash('success', sprintf(
            'La promotion "%s" a été %s.',
            $promotion->getNom(),
            $promotion->isActif() ? 'activée' : 'désactivée'
        ));

        return $this->redirectToRoute('app_promotion_index');
    }

    #[Route('/{id}/supprimer', name: 'app_promotion_delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function delete(int $id, Request $request, PromotionRepository $promotionRepository, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $promotion = $promotionRepository->find($id);
        if ($promotion === null) {
            throw $this->createNotFoundException('Promotion introuvable.');
        }

        if (!$this->isCsrfTokenValid('delete-promotion-' . $id, (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $nom = $promotion->getNom();
        $em->remove($promotion);
        $em->flush();

        $this->addFlash('success', sprintf('La promotion "%s" a été supprimée.', $nom));

        return $this->redirectToRoute('app_promotion_index');
    }

    private function denyUserMode(Request $request): ?Response
    {
        $uiMode = $request->hasSession()
            ? (string) $request->getSession()->get('ui_mode', 'admin')
            : 'admin';

        if ($uiMode === 'admin') {
            return null;
        }

        $this->addFlash('error', 'Cette section est réservée au mode Admin.');

        return $this->redirectToRoute('app_marketplace_index');
    }
}
