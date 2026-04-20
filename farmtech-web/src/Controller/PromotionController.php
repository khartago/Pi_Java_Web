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
}
