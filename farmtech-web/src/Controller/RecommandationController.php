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
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('recommandation/index.html.twig', ['grouped' => $this->repo->findAllOrdered()]);
    }

    #[Route('/nouvelle', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $reco = new Recommandation();
        $form = $this->createForm(RecommandationType::class, $reco);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $this->em->persist($reco);
            $this->em->flush();
            $this->addFlash('success', 'Recommandation creee.');
            return $this->redirectToRoute('app_recommandation_index');
        }
        return $this->render('recommandation/form.html.twig', ['form' => $form, 'mode' => 'new']);
    }
}
