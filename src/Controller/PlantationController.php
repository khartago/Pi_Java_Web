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
        $q = $request->query->get('q');
        $keyword = \is_string($q) ? trim($q) : '';
        $keyword = '' !== $keyword ? $keyword : null;

        $sortRaw = $request->query->get('sort');
        $sort = \is_string($sortRaw) ? trim($sortRaw) : '';
        $sort = '' !== $sort ? $sort : null;

        $limit = 10;
        $page = $request->query->get('page');
        $page = is_numeric($page) ? (int) $page : 1;
        if ($page < 1) {
            $page = 1;
        }

        $keywordForAggregates = null !== $sort ? null : $keyword;
        $aggregates = $repo->getAdminIndexAggregates($keywordForAggregates);
        $totalFiltered = $aggregates['listTotal'];
        $stats = $aggregates['stats'];

        $pageCount = max(1, (int) ceil($totalFiltered / $limit));
        $page = min($page, $pageCount);
        $offset = ($page - 1) * $limit;

        if (null !== $sort) {
            $plantations = $repo->findSortedPage($sort, $limit, $offset);
        } else {
            $plantations = $repo->findSearchPage($keyword, $limit, $offset);
        }

        $baseQuery = [];
        if (null !== $keyword) {
            $baseQuery['q'] = $keyword;
        }
        if (null !== $sort) {
            $baseQuery['sort'] = $sort;
        }

        $rangeStart = 0 === $totalFiltered ? 0 : $offset + 1;
        $rangeEnd = 0 === $totalFiltered ? 0 : $offset + \count($plantations);

        return $this->render('plantation/index.html.twig', [
            'plantations' => $plantations,
            'keyword' => $keyword ?? '',
            'sort' => $sort ?? '',
            'stats' => $stats,
            'pagination' => [
                'page' => $page,
                'page_count' => $pageCount,
                'limit' => $limit,
                'total_filtered' => $totalFiltered,
                'range_start' => $rangeStart,
                'range_end' => $rangeEnd,
                'prev_url' => $page > 1 ? $this->generateUrl('app_plantation_index', array_merge($baseQuery, ['page' => $page - 1])) : null,
                'next_url' => $page < $pageCount ? $this->generateUrl('app_plantation_index', array_merge($baseQuery, ['page' => $page + 1])) : null,
            ],
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
