<?php

namespace App\Controller;

use App\Entity\Plantation;
use App\Form\PlantationType;
use App\Repository\PlantationRepository;
use App\Service\PlantationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/admin/plantations')]
#[IsGranted('ROLE_ADMIN')]
class PlantationController extends AbstractController
{
    #[Route('', name: 'app_plantation_index', methods: ['GET'])]
    public function index(Request $request, PlantationRepository $repo): Response
    {
        $keyword = $request->query->get('q');
        $sort = $request->query->get('sort');

        $plantations = $sort ? $repo->findSorted($sort) : $repo->search($keyword);
        $stats = $repo->getStats();

        return $this->render('plantation/index.html.twig', [
            'plantations' => $plantations,
            'keyword' => $keyword,
            'sort' => $sort,
            'stats' => $stats,
        ]);
    }

    #[Route('/new', name: 'app_plantation_new', methods: ['GET', 'POST'])]
    public function new(Request $request, PlantationService $service): Response
    {
        $plantation = new Plantation();
        $form = $this->createForm(PlantationType::class, $plantation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->create($plantation);
            return $this->redirectToRoute('app_plantation_index');
        }

        return $this->render('plantation/form.html.twig', [
            'form' => $form->createView(),
            'mode' => 'creation',
        ]);
    }

    #[Route('/{id}/edit', name: 'app_plantation_edit', methods: ['GET', 'POST'])]
    public function edit(Plantation $plantation, Request $request, PlantationService $service): Response
    {
        $form = $this->createForm(PlantationType::class, $plantation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->update();
            return $this->redirectToRoute('app_plantation_index');
        }

        return $this->render('plantation/form.html.twig', [
            'form' => $form->createView(),
            'mode' => 'edition',
            'plantation' => $plantation,
        ]);
    }

    #[Route('/{id}/delete', name: 'app_plantation_delete', methods: ['POST'])]
    public function delete(Plantation $plantation, PlantationService $service): Response
    {
        $service->delete($plantation);
        return $this->redirectToRoute('app_plantation_index');
    }

    #[Route('/{id}/accept', name: 'app_plantation_accept', methods: ['POST'])]
    public function accept(Plantation $plantation, PlantationService $service): Response
    {
        $service->accept($plantation);
        return $this->redirectToRoute('app_plantation_index');
    }
}
