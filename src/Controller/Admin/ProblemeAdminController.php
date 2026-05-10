<?php

namespace App\Controller\Admin;

use App\Entity\Probleme;
use App\Form\ProblemeType;
use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Service\ProblemeCatalogService;
use App\Service\ProblemePhotoStorage;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/problemes')]
class ProblemeAdminController extends AbstractController
{
    /**
     * @return array<string, mixed>
     */
    private function listParams(Request $request): array
    {
        $q = $request->query->get('q');
        $q = \is_string($q) ? trim($q) : '';
        if (strlen($q) > 200) {
            $q = substr($q, 0, 200);
        }

        $dateFrom = $request->query->get('date_from');
        $dateFrom = \is_string($dateFrom) ? trim($dateFrom) : '';
        $dateTo = $request->query->get('date_to');
        $dateTo = \is_string($dateTo) ? trim($dateTo) : '';

        return [
            'q' => '' !== $q ? $q : null,
            'etat' => $request->query->get('etat') ?: null,
            'gravite' => $request->query->get('gravite') ?: null,
            'type' => $request->query->get('type') ?: null,
            'sort' => $request->query->get('sort') ?: 'dateDetection',
            'dir' => $request->query->get('dir') ?: 'DESC',
            'date_from' => '' !== $dateFrom ? $dateFrom : null,
            'date_to' => '' !== $dateTo ? $dateTo : null,
        ];
    }

    #[Route('', name: 'admin_problemes_index', methods: ['GET'])]
    public function index(
        Request $request,
        ProblemeRepository $repo,
        DiagnostiqueRepository $diagnostiqueRepository,
    ): Response {
        $params = $this->listParams($request);
        $limit = 10;
        $page = $request->query->get('page');
        $page = is_numeric($page) ? (int) $page : 1;
        if ($page < 1) {
            $page = 1;
        }

        $totalFiltered = $repo->countAdminFiltered($params);
        $pageCount = max(1, (int) ceil($totalFiltered / $limit));
        $page = min($page, $pageCount);
        $offset = ($page - 1) * $limit;

        $problemes = $repo->findAdminFilteredPage($params, $limit, $offset);
        $stats = $repo->getAdminIndexStats();

        $problemeIds = [];
        foreach ($problemes as $p) {
            $pid = $p->getId();
            if (null !== $pid) {
                $problemeIds[] = (int) $pid;
            }
        }
        $latestDiagnosticByProbleme = $diagnostiqueRepository->findLatestIndexedByProblemeIds($problemeIds);

        $baseQuery = [
            'sort' => $params['sort'],
            'dir' => $params['dir'],
        ];
        if (!empty($params['q'])) {
            $baseQuery['q'] = $params['q'];
        }
        if (!empty($params['etat'])) {
            $baseQuery['etat'] = $params['etat'];
        }
        if (!empty($params['gravite'])) {
            $baseQuery['gravite'] = $params['gravite'];
        }
        if (!empty($params['type'])) {
            $baseQuery['type'] = $params['type'];
        }
        if (!empty($params['date_from'])) {
            $baseQuery['date_from'] = $params['date_from'];
        }
        if (!empty($params['date_to'])) {
            $baseQuery['date_to'] = $params['date_to'];
        }

        $rangeStart = 0 === $totalFiltered ? 0 : $offset + 1;
        $rangeEnd = 0 === $totalFiltered ? 0 : $offset + \count($problemes);

        return $this->render('admin/probleme/index.html.twig', [
            'problemes' => $problemes,
            'stats' => $stats,
            'filters' => $params + ['page' => $page],
            'latestDiagnosticByProbleme' => $latestDiagnosticByProbleme,
            'pagination' => [
                'page' => $page,
                'page_count' => $pageCount,
                'limit' => $limit,
                'total_filtered' => $totalFiltered,
                'range_start' => $rangeStart,
                'range_end' => $rangeEnd,
                'prev_url' => $page > 1 ? $this->generateUrl('admin_problemes_index', array_merge($baseQuery, ['page' => $page - 1])) : null,
                'next_url' => $page < $pageCount ? $this->generateUrl('admin_problemes_index', array_merge($baseQuery, ['page' => $page + 1])) : null,
            ],
        ]);
    }

    #[Route('/new', name: 'admin_problemes_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $em,
        ProblemePhotoStorage $problemePhotoStorage,
        ProblemeCatalogService $catalogService,
    ): Response {
        $p = new Probleme();
        $p->setEtat('EN_ATTENTE');
        $form = $this->createForm(ProblemeType::class, $p, [
            'include_etat' => true,
            'include_plantation_produit' => true,
            'plantation_choices' => $catalogService->getPlantationChoices(),
            'produit_choices' => $catalogService->getProduitChoices(),
            'include_admin_assignee' => true,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $problemePhotoStorage->appendUploadsToProbleme($p, $form->get('photoFiles')->getData());
            $em->persist($p);
            $em->flush();
            $this->addFlash('success', 'Problème créé.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/probleme/new.html.twig', ['form' => $form]);
    }

    #[Route('/{id}/edit', name: 'admin_problemes_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        Probleme $probleme,
        EntityManagerInterface $em,
        ProblemePhotoStorage $problemePhotoStorage,
        ProblemeCatalogService $catalogService,
    ): Response {
        $form = $this->createForm(ProblemeType::class, $probleme, [
            'include_etat' => true,
            'include_plantation_produit' => true,
            'plantation_choices' => $catalogService->getPlantationChoices(),
            'produit_choices' => $catalogService->getProduitChoices(),
            'include_admin_assignee' => true,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $problemePhotoStorage->appendUploadsToProbleme($probleme, $form->get('photoFiles')->getData());
            $em->flush();
            $this->addFlash('success', 'Problème mis à jour.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/probleme/edit.html.twig', ['form' => $form, 'probleme' => $probleme]);
    }

    #[Route('/{id}/delete', name: 'admin_problemes_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, Probleme $probleme, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$probleme->getId(), (string) $request->request->get('_token'))) {
            $em->remove($probleme);
            $em->flush();
            $this->addFlash('success', 'Problème supprimé.');
        }

        return $this->redirectToRoute('admin_problemes_index');
    }
}
