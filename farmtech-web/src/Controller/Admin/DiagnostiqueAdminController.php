<?php

namespace App\Controller\Admin;

use App\Entity\Diagnostique;
use App\Entity\Utilisateur;
use App\Form\DiagnostiqueType;
use App\Repository\ProblemeRepository;
use App\Service\ProblemeWorkflowService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
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

    #[Route('/new', name: 'admin_diagnostics_new', methods: ['GET', 'POST'])]
    public function new(
        Request $request,
        EntityManagerInterface $em,
        ProblemeRepository $problemeRepository,
        ProblemeWorkflowService $problemeWorkflow,
    ): Response {
        $d = new Diagnostique();
        $d->setNumRevision(1);
        $lockedProbleme = null;
        $qp = $request->query->get('probleme');
        if (null !== $qp && '' !== (string) $qp) {
            $lockedProbleme = $problemeRepository->find((int) $qp);
            if ($lockedProbleme) {
                $d->setProbleme($lockedProbleme);
            }
        }
        $form = $this->createForm(DiagnostiqueType::class, $d, [
            'include_feedback' => false,
            'probleme_locked_probleme' => $lockedProbleme,
        ]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            if ($lockedProbleme) {
                $d->setProbleme($lockedProbleme);
            }
            $admin = $this->getUser();
            \assert($admin instanceof Utilisateur);
            $d->setAdminDiagnostiqueur($admin);
            $em->persist($d);
            $em->flush();
            $pb = $d->getProbleme();
            if ($pb) {
                $problemeWorkflow->applyAfterDiagnosticChange($pb);
                $em->flush();
            }
            $this->addFlash('success', 'Diagnostic enregistré.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/diagnostique/new.html.twig', [
            'form' => $form,
            'contextProbleme' => $lockedProbleme,
        ]);
    }

    #[Route('/{id}/edit', name: 'admin_diagnostics_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(
        Request $request,
        Diagnostique $diagnostique,
        EntityManagerInterface $em,
        ProblemeWorkflowService $problemeWorkflow,
    ): Response {
        $form = $this->createForm(DiagnostiqueType::class, $diagnostique, ['include_feedback' => true]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->flush();
            $pb = $diagnostique->getProbleme();
            if ($pb) {
                $problemeWorkflow->applyAfterDiagnosticChange($pb);
                $em->flush();
            }
            $this->addFlash('success', 'Diagnostic mis à jour.');

            return $this->redirectToRoute('admin_problemes_index');
        }

        return $this->render('admin/diagnostique/edit.html.twig', [
            'form' => $form,
            'diagnostic' => $diagnostique,
            'contextProbleme' => $diagnostique->getProbleme(),
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
