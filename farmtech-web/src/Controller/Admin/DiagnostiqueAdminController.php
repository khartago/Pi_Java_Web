<?php

namespace App\Controller\Admin;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use App\Entity\Utilisateur;
use App\Form\DiagnostiqueType;
use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Service\DiagnosticAIService;
use App\Service\DiagnosticApprovalNotifier;
use App\Service\ProblemeCatalogService;
use App\Service\ProblemeWorkflowService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/diagnostics')]
class DiagnostiqueAdminController extends AbstractController
{
    /** Redirige vers la liste des signalements (entrée unique menu admin). */
    #[Route('', name: 'admin_diagnostics_index', methods: ['GET'])]
    public function index(): RedirectResponse
    {
        return $this->redirectToRoute('admin_problemes_index', [], 302);
    }

    #[Route('/ai-suggest', name: 'admin_diagnostics_ai_suggest', methods: ['POST'])]
    public function aiSuggest(
        Request $request,
        ProblemeRepository $problemeRepository,
        DiagnosticAIService $diagnosticAIService,
    ): JsonResponse {
        if (!$this->isCsrfTokenValid('ai_diagnostic', (string) $request->request->get('_token'))) {
            return new JsonResponse(['ok' => false, 'error' => 'Jeton CSRF invalide.'], 400);
        }
        $pid = (int) $request->request->get('probleme');
        if ($pid <= 0) {
            return new JsonResponse(['ok' => false, 'error' => 'Problème manquant.'], 400);
        }
        $probleme = $problemeRepository->find($pid);
        if (!$probleme instanceof Probleme) {
            return new JsonResponse(['ok' => false, 'error' => 'Problème introuvable.'], 404);
        }
        try {
            $s = $diagnosticAIService->generateFromProbleme($probleme);

            return new JsonResponse([
                'ok' => true,
                'cause' => $s['cause'],
                'solutionProposee' => $s['solutionProposee'],
                'medicament' => $s['medicament'],
            ]);
        } catch (\Throwable $e) {
            return new JsonResponse(['ok' => false, 'error' => $e->getMessage()], 500);
        }
    }

    #[Route('/new-revision/{probleme}', name: 'admin_diagnostics_new_revision', requirements: ['probleme' => '\d+'], methods: ['GET', 'POST'])]
    public function newRevision(
        Request $request,
        Probleme $probleme,
        EntityManagerInterface $em,
        DiagnostiqueRepository $diagnostiqueRepository,
        ProblemeWorkflowService $problemeWorkflow,
        ProblemeCatalogService $catalogService,
    ): Response {
        $max = $diagnostiqueRepository->getMaxRevisionNumForProbleme((int) $probleme->getId());
        $d = new Diagnostique();
        $d->setProbleme($probleme);
        $d->setNumRevision($max + 1);
        $d->setApprouve(false);

        $revisions = $diagnostiqueRepository->findByProblemeOrderedByRevision($probleme);
        $similaires = [];
        $type = $probleme->getType();
        if (null !== $type && '' !== $type) {
            $similaires = $diagnostiqueRepository->findApprovedSimilarByProblemType($type, (int) $probleme->getId(), 5);
        }

        $form = $this->createForm(DiagnostiqueType::class, $d, [
            'include_feedback' => false,
            'probleme_locked_probleme' => $probleme,
            'hide_num_revision' => true,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $d->setNumRevision($diagnostiqueRepository->getMaxRevisionNumForProbleme((int) $probleme->getId()) + 1);
            $d->setProbleme($probleme);
            $d->setDateDiagnostique(new \DateTimeImmutable());
            $d->setFeedbackFermier(null);
            $d->setFeedbackCommentaire(null);
            $d->setDateFeedback(null);
            $admin = $this->getUser();
            \assert($admin instanceof Utilisateur);
            $d->setAdminDiagnostiqueur($admin);
            $em->persist($d);
            $em->flush();
            $problemeWorkflow->applyAfterDiagnosticChange($probleme);
            $em->flush();
            $this->addFlash('success', 'Nouvelle révision enregistrée.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/diagnostique/new.html.twig', [
            'form' => $form,
            'contextProbleme' => $probleme,
            'contextPlantationLabel' => $catalogService->getPlantationLabel($probleme->getIdPlantation()),
            'contextProduitLabel' => $catalogService->getProduitLabel($probleme->getIdProduit()),
            'revisionsList' => $revisions,
            'similaires' => $similaires,
            'isRevisionFlow' => true,
        ]);
    }

    #[Route('/new', name: 'admin_diagnostics_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $em,
        ProblemeRepository $problemeRepository,
        DiagnostiqueRepository $diagnostiqueRepository,
        ProblemeWorkflowService $problemeWorkflow,
        DiagnosticApprovalNotifier $approvalNotifier,
        ProblemeCatalogService $catalogService,
    ): Response {
        $d = new Diagnostique();
        $lockedProbleme = null;
        $qp = $request->query->get('probleme');
        if (null !== $qp && '' !== (string) $qp) {
            $lockedProbleme = $problemeRepository->find((int) $qp);
            if ($lockedProbleme) {
                $d->setProbleme($lockedProbleme);
                $d->setNumRevision($diagnostiqueRepository->getMaxRevisionNumForProbleme((int) $lockedProbleme->getId()) + 1);
            }
        }
        if (null === $lockedProbleme) {
            $d->setNumRevision(1);
        }

        $similaires = [];
        $revisions = [];
        if ($lockedProbleme) {
            $revisions = $diagnostiqueRepository->findByProblemeOrderedByRevision($lockedProbleme);
            $type = $lockedProbleme->getType();
            if (null !== $type && '' !== $type) {
                $similaires = $diagnostiqueRepository->findApprovedSimilarByProblemType($type, (int) $lockedProbleme->getId(), 5);
            }
        }

        $form = $this->createForm(DiagnostiqueType::class, $d, [
            'include_feedback' => false,
            'probleme_locked_probleme' => $lockedProbleme,
            'hide_num_revision' => true,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            if ($lockedProbleme) {
                $d->setProbleme($lockedProbleme);
            }
            $pb = $d->getProbleme();
            if ($pb && null === $d->getId()) {
                $d->setNumRevision($diagnostiqueRepository->getMaxRevisionNumForProbleme((int) $pb->getId()) + 1);
            }
            $d->setDateDiagnostique(new \DateTimeImmutable());
            $d->setFeedbackFermier(null);
            $d->setFeedbackCommentaire(null);
            $d->setDateFeedback(null);
            $admin = $this->getUser();
            \assert($admin instanceof Utilisateur);
            $d->setAdminDiagnostiqueur($admin);
            $em->persist($d);
            $em->flush();
            $pb = $d->getProbleme();
            if ($pb) {
                $problemeWorkflow->applyAfterDiagnosticChange($pb);
                $em->flush();
                if ($d->isApprouve()) {
                    $approvalNotifier->notifyFarmer($pb, $d);
                }
            }
            $this->addFlash('success', 'Diagnostic enregistré.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/diagnostique/new.html.twig', [
            'form' => $form,
            'contextProbleme' => $lockedProbleme,
            'contextPlantationLabel' => $lockedProbleme ? $catalogService->getPlantationLabel($lockedProbleme->getIdPlantation()) : null,
            'contextProduitLabel' => $lockedProbleme ? $catalogService->getProduitLabel($lockedProbleme->getIdProduit()) : null,
            'revisionsList' => $revisions,
            'similaires' => $similaires,
            'isRevisionFlow' => false,
        ]);
    }

    #[Route('/{id}/edit', name: 'admin_diagnostics_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        Diagnostique $diagnostique,
        EntityManagerInterface $em,
        DiagnostiqueRepository $diagnostiqueRepository,
        ProblemeWorkflowService $problemeWorkflow,
        DiagnosticApprovalNotifier $approvalNotifier,
        ProblemeCatalogService $catalogService,
    ): Response {
        $prevApproved = $diagnostique->isApprouve();
        $original = [
            'cause' => $diagnostique->getCause(),
            'solutionProposee' => $diagnostique->getSolutionProposee(),
            'dateDiagnostique' => $diagnostique->getDateDiagnostique(),
            'resultat' => $diagnostique->getResultat(),
            'medicament' => $diagnostique->getMedicament(),
            'approuve' => $diagnostique->isApprouve(),
            'feedbackFermier' => $diagnostique->getFeedbackFermier(),
            'feedbackCommentaire' => $diagnostique->getFeedbackCommentaire(),
            'dateFeedback' => $diagnostique->getDateFeedback(),
        ];
        $pb = $diagnostique->getProbleme();
        $similaires = [];
        $revisions = [];
        if ($pb) {
            $revisions = $diagnostiqueRepository->findByProblemeOrderedByRevision($pb);
            $type = $pb->getType();
            if (null !== $type && '' !== $type && null !== $pb->getId()) {
                $similaires = $diagnostiqueRepository->findApprovedSimilarByProblemType($type, (int) $pb->getId(), 5);
            }
        }

        $form = $this->createForm(DiagnostiqueType::class, $diagnostique, [
            'include_feedback' => false,
            'probleme_locked_probleme' => $diagnostique->getProbleme(),
            'hide_num_revision' => true,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $coreChanged = $diagnostique->getCause() !== $original['cause']
                || $diagnostique->getSolutionProposee() !== $original['solutionProposee']
                || $diagnostique->getResultat() !== $original['resultat']
                || $diagnostique->getMedicament() !== $original['medicament'];
            if ($coreChanged) {
                $pb = $diagnostique->getProbleme();
                if ($pb) {
                    // Préserve l'historique: une modification substantielle crée une nouvelle révision.
                    $newRevision = new Diagnostique();
                    $newRevision->setProbleme($pb);
                    $newRevision->setNumRevision($diagnostiqueRepository->getMaxRevisionNumForProbleme((int) $pb->getId()) + 1);
                    $newRevision->setCause($diagnostique->getCause());
                    $newRevision->setSolutionProposee($diagnostique->getSolutionProposee());
                    $newRevision->setDateDiagnostique(new \DateTimeImmutable());
                    $newRevision->setResultat($diagnostique->getResultat());
                    $newRevision->setMedicament($diagnostique->getMedicament());
                    $newRevision->setApprouve($diagnostique->isApprouve());
                    // Chaque nouvelle révision repart sans feedback fermier.
                    $newRevision->setFeedbackFermier(null);
                    $newRevision->setFeedbackCommentaire(null);
                    $newRevision->setDateFeedback(null);
                    $admin = $this->getUser();
                    if ($admin instanceof Utilisateur) {
                        $newRevision->setAdminDiagnostiqueur($admin);
                    } else {
                        $newRevision->setAdminDiagnostiqueur($diagnostique->getAdminDiagnostiqueur());
                    }

                    // Restaurer l'ancienne révision telle qu'elle était.
                    $diagnostique->setCause($original['cause']);
                    $diagnostique->setSolutionProposee($original['solutionProposee']);
                    $diagnostique->setDateDiagnostique($original['dateDiagnostique']);
                    $diagnostique->setResultat($original['resultat']);
                    $diagnostique->setMedicament($original['medicament']);
                    $diagnostique->setApprouve((bool) $original['approuve']);
                    $diagnostique->setFeedbackFermier($original['feedbackFermier']);
                    $diagnostique->setFeedbackCommentaire($original['feedbackCommentaire']);
                    $diagnostique->setDateFeedback($original['dateFeedback']);

                    $em->persist($newRevision);
                    $em->flush();
                    $problemeWorkflow->applyAfterDiagnosticChange($pb);
                    $em->flush();
                    if ($newRevision->isApprouve()) {
                        $approvalNotifier->notifyFarmer($pb, $newRevision);
                    }
                    $this->addFlash('success', 'Nouvelle révision créée à partir de la modification.');

                    return $this->redirectToRoute('admin_diagnostics_edit', ['id' => $newRevision->getId()]);
                }
            }
            $em->flush();
            $pb = $diagnostique->getProbleme();
            if ($pb) {
                $problemeWorkflow->applyAfterDiagnosticChange($pb);
                $em->flush();
                if (!$prevApproved && $diagnostique->isApprouve()) {
                    $approvalNotifier->notifyFarmer($pb, $diagnostique);
                }
            }
            $this->addFlash('success', 'Diagnostic mis à jour.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        $cp = $diagnostique->getProbleme();

        return $this->render('admin/diagnostique/edit.html.twig', [
            'form' => $form,
            'diagnostic' => $diagnostique,
            'contextProbleme' => $cp,
            'contextPlantationLabel' => $cp ? $catalogService->getPlantationLabel($cp->getIdPlantation()) : null,
            'contextProduitLabel' => $cp ? $catalogService->getProduitLabel($cp->getIdProduit()) : null,
            'revisionsList' => $revisions,
            'similaires' => $similaires,
        ]);
    }

    #[Route('/{id}/delete', name: 'admin_diagnostics_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(
        Request $request,
        Diagnostique $diagnostique,
        EntityManagerInterface $em,
        ProblemeWorkflowService $problemeWorkflow,
    ): Response {
        if ($this->isCsrfTokenValid('delete'.$diagnostique->getId(), (string) $request->request->get('_token'))) {
            $probleme = $diagnostique->getProbleme();
            $em->remove($diagnostique);
            $em->flush();
            if ($probleme) {
                $problemeWorkflow->applyAfterDiagnosticChange($probleme);
                $em->flush();
            }
            $this->addFlash('success', 'Diagnostic supprimé.');
        }

        return $this->redirectToRoute('admin_problemes_index');
    }
}
