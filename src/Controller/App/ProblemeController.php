<?php

namespace App\Controller\App;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use App\Entity\Utilisateur;
use App\Form\DiagnosticFeedbackType;
use App\Form\ProblemeType;
use App\Repository\DiagnostiqueRepository;
use App\Repository\ProblemeRepository;
use App\Service\ProblemePhotoPublicUrlResolver;
use App\Service\ProblemePhotoStorage;
use App\Service\ProblemeWorkflowService;
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

        return [
            'q' => '' !== $q ? $q : null,
            'etat' => $request->query->get('etat') ?: null,
            'gravite' => $request->query->get('gravite') ?: null,
            'type' => $request->query->get('type') ?: null,
            'sort' => $request->query->get('sort') ?: 'dateDetection',
            'dir' => $request->query->get('dir') ?: 'DESC',
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
    public function new(Request $request, EntityManagerInterface $em, ProblemePhotoStorage $problemePhotoStorage): Response
    {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        $p = new Probleme();
        $p->setUtilisateur($user);
        $p->setEtat('EN_ATTENTE');
        $form = $this->createForm(ProblemeType::class, $p, ['include_etat' => false]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $problemePhotoStorage->appendUploadsToProbleme($p, $form->get('photoFiles')->getData());
            $p->setEtat('EN_ATTENTE');
            $em->persist($p);
            $em->flush();
            $this->addFlash('success', 'Problème enregistré.');

            return $this->redirectToRoute('app_problemes_show', ['id' => $p->getId()]);
        }

        return $this->render('app/probleme/new.html.twig', ['form' => $form]);
    }

    #[Route('/{id}', name: 'app_problemes_show', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function show(
        Probleme $probleme,
        Request $request,
        DiagnostiqueRepository $diagnostiqueRepository,
        EntityManagerInterface $em,
        ProblemeWorkflowService $problemeWorkflow,
    ): Response {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        if ($probleme->getUtilisateur()?->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException();
        }

        $diag = $diagnostiqueRepository->findLatestApprovedForProbleme((int) $probleme->getId());
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

        return $this->render('app/probleme/show.html.twig', [
            'probleme' => $probleme,
            'diagnostic' => $diag,
            'feedbackForm' => $feedbackFormView,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_problemes_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, Probleme $probleme, EntityManagerInterface $em, ProblemePhotoStorage $problemePhotoStorage): Response
    {
        $user = $this->getUser();
        \assert($user instanceof Utilisateur);
        if ($probleme->getUtilisateur()?->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException();
        }
        if ('CLOTURE' === $probleme->getEtat()) {
            $this->addFlash('danger', 'Ce problème est clôturé et ne peut pas être modifié.');

            return $this->redirectToRoute('app_problemes_show', ['id' => $probleme->getId()]);
        }

        $form = $this->createForm(ProblemeType::class, $probleme, ['include_etat' => false]);
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
