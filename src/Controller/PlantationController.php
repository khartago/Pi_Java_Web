<?php

namespace App\Controller;

use App\Entity\Plantation;
use App\Form\PlantationType;
use App\Repository\PlantationRepository;
use App\Service\PlantationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class PlantationController extends AbstractController
{
    

#[Route('/plantation', name: 'app_plantation_index')]
public function index(Request $request, PlantationRepository $repo): Response
{
    $keyword = $request->query->get('q');
    $sort = $request->query->get('sort');

    if ($sort) {
        $plantations = $repo->findSorted($sort);
    } else {
        $plantations = $repo->search($keyword);
    }

    $stats = $repo->getStats();

    return $this->render('plantation/index.html.twig', [
        'plantations' => $plantations,
        'keyword' => $keyword,
        'stats' => $stats
    ]);
}


    // ✅ CREATE
    #[Route('/plantation/new', name: 'app_plantation_new')]
    public function new(Request $request, PlantationService $service): Response
    {
        $plantation = new Plantation();
        $form = $this->createForm(PlantationType::class, $plantation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->create($plantation);

            return $this->redirectToRoute('app_plantation_index');
        }

        return $this->render('plantation/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ✅ EDIT
    #[Route('/plantation/{id}/edit', name: 'app_plantation_edit')]
    public function edit(Plantation $plantation, Request $request, PlantationService $service): Response
    {
        $form = $this->createForm(PlantationType::class, $plantation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->update();

            return $this->redirectToRoute('app_plantation_index');
        }

        return $this->render('plantation/edit.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ✅ DELETE
    #[Route('/plantation/{id}/delete', name: 'app_plantation_delete')]
    public function delete(Plantation $plantation, PlantationService $service): Response
    {
        $service->delete($plantation);

        return $this->redirectToRoute('app_plantation_index');
    }

    // 🔥 ACCEPT → CREATE PRODUCTION
    #[Route('/plantation/{id}/accept', name: 'app_plantation_accept')]
    public function accept(Plantation $plantation, PlantationService $service): Response
    {
        $service->accept($plantation);

        return $this->redirectToRoute('app_plantation_index');
    }
}