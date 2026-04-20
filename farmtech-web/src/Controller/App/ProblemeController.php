<?php

namespace App\Controller\App;

use App\Entity\Probleme;
use App\Entity\Utilisateur;
use App\Form\DiagnosticFeedbackType;
use App\Form\ProblemeType;
use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Service\ProblemeCatalogService;
use App\Service\ProblemePdfExportService;
use App\Service\ProblemePhotoPublicUrlResolver;
use App\Service\ProblemePhotoStorage;
use App\Service\ProblemeWorkflowService;
use App\Service\WeatherSnapshotService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/app/problemes')]
class ProblemeController extends AbstractController
{
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

    #[Route('', name: 'app_problemes_index', methods: ['GET'])]
    public function index(
        Request $request,
        ProblemeRepository $problemeRepository,
        ProblemePhotoPublicUrlResolver $photoPublicUrlResolver,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        $params = $this->listParams($request);
        $problemes = $problemeRepository->findForFarmer($user, $params);
        $stats = $problemeRepository->getStats($user);

        $thumbUrls = [];
        foreach ($problemes as $p) {
            $thumbUrls[$p->getId()] = $photoPublicUrlResolver->resolveForDisplay($p->getFirstPhotoPath());
        }

        return $this->render('app/probleme/index.html.twig', [
            'problemes' => $problemes,
            'stats' => $stats,
            'filters' => $params,
            'thumbUrls' => $thumbUrls,
        ]);
    }

    #[Route('/new', name: 'app_problemes_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $em,
        ProblemePhotoStorage $problemePhotoStorage,
        WeatherSnapshotService $weatherSnapshotService,
        ProblemeCatalogService $catalogService,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        $p = new Probleme();
        $p->setUtilisateur($user);
        $p->setEtat('EN_ATTENTE');
        $form = $this->createForm(ProblemeType::class, $p, [
            'include_etat' => false,
            'include_plantation_produit' => true,
            'plantation_choices' => $catalogService->getPlantationChoices(),
            'produit_choices' => $catalogService->getProduitChoices(),
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            try {
                $snap = $weatherSnapshotService->fetchSnapshotJson();
                if (null !== $snap) {
                    $p->setMeteoSnapshot($snap);
                }
            } catch (\Throwable) {
            }
            $problemePhotoStorage->appendUploadsToProbleme($p, $form->get('photoFiles')->getData());
            $p->setEtat('EN_ATTENTE');
            $em->persist($p);
            $em->flush();
            $this->addFlash('success', 'Problème enregistré.');

            return $this->redirectToRoute('app_problemes_show', ['id' => $p->getId()]);
        }

        return $this->render('app/probleme/new.html.twig', ['form' => $form]);
    }

    #[Route('/export/{id}', name: 'app_problemes_export_pdf', requirements: ['id' => '\d+'], methods: ['GET'])]
    public function exportPdf(
        int $id,
        ProblemeRepository $problemeRepository,
        DiagnostiqueRepository $diagnostiqueRepository,
        ProblemePdfExportService $pdfExportService,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        $probleme = $problemeRepository->find($id);
        if (!$probleme instanceof Probleme) {
            throw $this->createNotFoundException();
        }
        if ($probleme->getUtilisateur()?->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException();
        }
        $diag = $diagnostiqueRepository->findLatestApprovedForProbleme($id);
        $bytes = $pdfExportService->generateReportPdf($probleme, $diag);
        if (null === $bytes || '' === $bytes) {
            $this->addFlash('danger', 'Impossible de générer le PDF (réseau ou API indisponible).');

            return $this->redirectToRoute('app_problemes_show', ['id' => $id]);
        }

        $response = new Response($bytes);
        $response->headers->set('Content-Type', 'application/pdf');
        $response->headers->set('Content-Disposition', 'attachment; filename="rapport-probleme-'.$id.'.pdf"');

        return $response;
    }

    #[Route('/{id}', name: 'app_problemes_show', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function show(
        Probleme $probleme,
        Request $request,
        DiagnostiqueRepository $diagnostiqueRepository,
        EntityManagerInterface $em,
        ProblemeWorkflowService $problemeWorkflow,
        ProblemeCatalogService $catalogService,
        WeatherSnapshotService $weatherSnapshotService,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        if ($probleme->getUtilisateur()?->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException();
        }

        $diag = $diagnostiqueRepository->findLatestApprovedForProbleme((int) $probleme->getId());
        $allRevisions = $diagnostiqueRepository->findByProblemeOrderedByRevision($probleme);
        $feedbackFormView = null;
        if ($diag) {
            $fb = $this->createForm(DiagnosticFeedbackType::class, $diag);
            $fb->handleRequest($request);
            if ($fb->isSubmitted() && $fb->isValid()) {
                $diag->setDateFeedback(new \DateTimeImmutable());
                $problemeWorkflow->applyAfterFarmerFeedback($diag);
                $em->flush();
                $this->addFlash('success', 'Feedback enregistré.');

                return $this->redirectToRoute('app_problemes_show', ['id' => $probleme->getId()]);
            }
            $feedbackFormView = $fb->createView();
        }

        $meteoData = null;
        $snap = $probleme->getMeteoSnapshot();
        if ((null === $snap || '' === trim($snap))) {
            try {
                $live = $weatherSnapshotService->fetchSnapshotJson();
                if (null !== $live && '' !== trim($live)) {
                    $probleme->setMeteoSnapshot($live);
                    $em->flush();
                    $snap = $live;
                }
            } catch (\Throwable) {
                $snap = null;
            }
        }
        if (null !== $snap && '' !== trim($snap)) {
            try {
                $meteoData = json_decode($snap, true, 512, JSON_THROW_ON_ERROR);
            } catch (\Throwable) {
                $meteoData = null;
            }
        }

        return $this->render('app/probleme/show.html.twig', [
            'probleme' => $probleme,
            'diagnostic' => $diag,
            'allRevisions' => $allRevisions,
            'feedbackForm' => $feedbackFormView,
            'plantationLabel' => $catalogService->getPlantationLabel($probleme->getIdPlantation()),
            'produitLabel' => $catalogService->getProduitLabel($probleme->getIdProduit()),
            'meteoData' => $meteoData,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_problemes_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        Probleme $probleme,
        EntityManagerInterface $em,
        ProblemePhotoStorage $problemePhotoStorage,
        ProblemeCatalogService $catalogService,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        if ($probleme->getUtilisateur()?->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException();
        }
        if ('CLOTURE' === $probleme->getEtat()) {
            $this->addFlash('danger', 'Ce problème est clôturé et ne peut pas être modifié.');

            return $this->redirectToRoute('app_problemes_show', ['id' => $probleme->getId()]);
        }

        $form = $this->createForm(ProblemeType::class, $probleme, [
            'include_etat' => false,
            'include_plantation_produit' => true,
            'plantation_choices' => $catalogService->getPlantationChoices(),
            'produit_choices' => $catalogService->getProduitChoices(),
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $problemePhotoStorage->appendUploadsToProbleme($probleme, $form->get('photoFiles')->getData());
            $em->flush();
            $this->addFlash('success', 'Problème mis à jour.');

            return $this->redirectToRoute('app_problemes_show', ['id' => $probleme->getId()]);
        }

        return $this->render('app/probleme/edit.html.twig', ['form' => $form, 'probleme' => $probleme]);
    }
}
