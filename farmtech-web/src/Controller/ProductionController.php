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
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/admin/productions')]
#[IsGranted('ROLE_ADMIN')]
class ProductionController extends AbstractController
{
    #[Route('', name: 'app_production_index', methods: ['GET'])]
    public function index(Request $request, ProductionRepository $repo): Response
    {
        $id = $request->query->get('q');
        $sort = $request->query->get('sort');

        $productions = $sort ? $repo->findSorted($sort) : $repo->search($id);
        $stats = $repo->getStats();

        return $this->render('production/index.html.twig', [
            'productions' => $productions,
            'keyword' => $id,
            'sort' => $sort,
            'stats' => $stats,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_production_edit', methods: ['GET', 'POST'])]
    public function edit(Production $production, Request $request, ProductionService $service): Response
    {
        $form = $this->createForm(ProductionType::class, $production);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $service->update();
            return $this->redirectToRoute('app_production_index');
        }

        return $this->render('production/edit.html.twig', [
            'form' => $form->createView(),
            'production' => $production,
        ]);
    }

    #[Route('/{id}/delete', name: 'app_production_delete', methods: ['POST'])]
    public function delete(Production $production, ProductionService $service): Response
    {
        $service->delete($production);
        return $this->redirectToRoute('app_production_index');
    }
}
