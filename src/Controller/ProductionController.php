<?php

namespace App\Controller;

use App\Entity\Production;
use App\Form\ProductionType;
use App\Repository\ProductionRepository;
use App\Service\ProductionService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class ProductionController extends AbstractController
{
    // ✅ LIST
#[Route('/production', name: 'app_production_index')]
public function index(Request $request, ProductionRepository $repo): Response
{
    $id = $request->query->get('q');
    $sort = $request->query->get('sort');

    if ($sort) {
        $productions = $repo->findSorted($sort);
    } else {
        $productions = $repo->search($id);
    }

    $stats = $repo->getStats();

    return $this->render('production/index.html.twig', [
        'productions' => $productions,
        'keyword' => $id,
        'stats' => $stats
    ]);
}

    // ✅ EDIT
    #[Route('/production/{id}/edit', name: 'app_production_edit')]
    public function edit(
        Production $production,
        Request $request,
        ProductionService $service
    ): Response {
        $form = $this->createForm(ProductionType::class, $production);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->update();

            return $this->redirectToRoute('app_production_index');
        }

        return $this->render('production/edit.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    // ✅ DELETE
    #[Route('/production/{id}/delete', name: 'app_production_delete')]
    public function delete(
        Production $production,
        ProductionService $service
    ): Response {
        $service->delete($production);

        return $this->redirectToRoute('app_production_index');
    }
}