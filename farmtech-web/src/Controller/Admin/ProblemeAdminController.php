<?php

namespace App\Controller\Admin;

use App\Entity\Probleme;
use App\Form\ProblemeType;
use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Service\ProblemePhotoStorage;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/problemes')]
class ProblemeAdminController extends AbstractController
{
    private function listParams(Request $request): array
    {
        $q = $request->query->get('q');
        $q = \is_string($q) ? trim($q) : '';
        if (strlen($q) > 200) {
            $q = substr($q, 0, 200);
        }

        return [
            'q' => '' !== $q ? $q : null,
            'etat' => $request->query->get('etat') ?: null,
            'gravite' => $request->query->get('gravite') ?: null,
            'type' => $request->query->get('type') ?: null,
            'sort' => $request->query->get('sort') ?: 'dateDetection',
            'dir' => $request->query->get('dir') ?: 'DESC',
        ];
    }

    #[Route('', name: 'admin_problemes_index', methods: ['GET'])]
    public function index(
        Request $request,
        ProblemeRepository $repo,
        DiagnostiqueRepository $diagnostiqueRepository,
    ): Response {
        $params = $this->listParams($request);
        $problemes = $repo->findAdminFiltered($params);
        $stats = $repo->getStats(null);

        $latestDiagnosticByProbleme = [];
        foreach ($problemes as $p) {
            $pid = $p->getId();
            if (null !== $pid) {
                $latestDiagnosticByProbleme[$pid] = $diagnostiqueRepository->findLatestForProbleme((int) $pid);
            }
        }

        return $this->render('admin/probleme/index.html.twig', [
            'problemes' => $problemes,
            'stats' => $stats,
            'filters' => $params,
            'latestDiagnosticByProbleme' => $latestDiagnosticByProbleme,
        ]);
    }

    #[Route('/new', name: 'admin_problemes_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, ProblemePhotoStorage $problemePhotoStorage): Response
    {
        $p = new Probleme();
        $p->setEtat('EN_ATTENTE');
        $form = $this->createForm(ProblemeType::class, $p, ['include_etat' => true]);
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
    public function edit(Request $request, Probleme $probleme, EntityManagerInterface $em, ProblemePhotoStorage $problemePhotoStorage): Response
    {
        $form = $this->createForm(ProblemeType::class, $probleme, ['include_etat' => true]);
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
