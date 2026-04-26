<?php

namespace App\Controller;

use App\Entity\Promotion;
use App\Form\PromotionType;
use App\Repository\PromotionRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/promotions')]
class PromotionController extends AbstractController
{
    #[Route('', name: 'app_promotion_index', methods: ['GET'])]
    public function index(PromotionRepository $promotionRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('promotion/index.html.twig', ['promotions' => $promotionRepository->findAll()]);
    }

    #[Route('/nouvelle', name: 'app_promotion_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $promotion = new Promotion();
        $form = $this->createForm(PromotionType::class, $promotion);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($promotion);
            $em->flush();
            $this->addFlash('success', 'Promotion creee.');
            return $this->redirectToRoute('app_promotion_index');
        }
        return $this->render('promotion/form.html.twig', ['form' => $form, 'mode' => 'creation']);
    }

    #[Route('/{idPromotion<\d+>}/modifier', name: 'app_promotion_edit', methods: ['GET', 'POST'])]
    public function edit(Promotion $promotion, Request $request, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $form = $this->createForm(PromotionType::class, $promotion);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $this->addFlash('success', 'Promotion modifiee.');

            return $this->redirectToRoute('app_promotion_index');
        }

        return $this->render('promotion/form.html.twig', ['form' => $form, 'mode' => 'edition']);
    }

    #[Route('/{idPromotion<\d+>}/toggle', name: 'app_promotion_toggle', methods: ['POST'])]
    public function toggle(Promotion $promotion, Request $request, EntityManagerInterface $em): RedirectResponse
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if (!$this->isCsrfTokenValid('toggle_promotion_'.$promotion->getIdPromotion(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Token CSRF invalide.');
        }

        $promotion->setActif(!$promotion->isActif());
        $em->flush();
        $this->addFlash('success', $promotion->isActif() ? 'Promotion activee.' : 'Promotion desactivee.');

        return $this->redirectToRoute('app_promotion_index');
    }

    #[Route('/{idPromotion<\d+>}/supprimer', name: 'app_promotion_delete', methods: ['POST'])]
    public function delete(Promotion $promotion, Request $request, EntityManagerInterface $em): RedirectResponse
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if (!$this->isCsrfTokenValid('delete_promotion_'.$promotion->getIdPromotion(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Token CSRF invalide.');
        }

        $em->remove($promotion);
        $em->flush();
        $this->addFlash('success', 'Promotion supprimee.');

        return $this->redirectToRoute('app_promotion_index');
    }
}
