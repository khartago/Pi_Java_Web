<?php

namespace App\Controller;

use App\Entity\Affectation;
use App\Entity\Employe;
use App\Form\AffectationType;
use App\Form\EmployeType;
use App\Repository\AffectationRepository;
use App\Repository\EmployeRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/affectations')]
class AffectationController extends AbstractController
{
    #[Route('', name: 'app_affectation_index', methods: ['GET'])]
    public function index(AffectationRepository $affectationRepo, EmployeRepository $employeRepo): Response
    {
        return $this->render('affectation/index.html.twig', [
            'affectations' => $affectationRepo->findAllActive(),
            'total_employes' => count($employeRepo->findForList(null)),
        ]);
    }

    #[Route('/nouveau', name: 'app_affectation_new', methods: ['GET', 'POST'])]
    public function new(Request $request, AffectationRepository $affectationRepo, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $affectation = new Affectation();
        $form = $this->createForm(AffectationType::class, $affectation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $materiel = $affectation->getMateriel();
            if ($materiel !== null) {
                $existing = $affectationRepo->findActiveByMateriel($materiel);
                if ($existing !== null) {
                    $form->get('materiel')->addError(
                        new \Symfony\Component\Form\FormError(
                            sprintf(
                                'Ce matériel est déjà affecté à %s. Veuillez d\'abord enregistrer le retour.',
                                $existing->getEmploye()?->getFullName() ?? 'un employé'
                            )
                        )
                    );
                } else {
                    $em->persist($affectation);
                    $em->flush();
                    $this->addFlash('success', 'L\'affectation a été créée avec succès.');

                    return $this->redirectToRoute('app_affectation_index');
                }
            }
        }

        return $this->render('affectation/form.html.twig', [
            'form' => $form,
            'affectation' => $affectation,
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/{id}/retourner', name: 'app_affectation_retourner', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function retourner(
        int $id,
        Request $request,
        AffectationRepository $affectationRepo,
        EntityManagerInterface $em
    ): Response {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $affectation = $affectationRepo->find($id);
        if ($affectation === null) {
            throw $this->createNotFoundException('Affectation introuvable.');
        }

        if (!$this->isCsrfTokenValid('retourner-affectation-' . $id, (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        if (!$affectation->isActive()) {
            $this->addFlash('error', 'Cette affectation a déjà été retournée.');

            return $this->redirectToRoute('app_affectation_index');
        }

        $affectation->setDateRetour(new \DateTimeImmutable());
        $em->flush();

        $this->addFlash('success', sprintf(
            'Retour enregistré pour %s.',
            $affectation->getMateriel()?->getNom() ?? 'le matériel'
        ));

        return $this->redirectToRoute('app_affectation_index');
    }

    #[Route('/historique', name: 'app_affectation_historique', methods: ['GET'])]
    public function historique(Request $request, AffectationRepository $affectationRepo): Response
    {
        $recherche = (string) $request->query->get('recherche', '');
        $allHistory = $affectationRepo->findAllHistory();

        if ($recherche !== '') {
            $term = strtolower($recherche);
            $allHistory = array_filter($allHistory, static function (Affectation $a) use ($term): bool {
                $materielNom = strtolower($a->getMateriel()?->getNom() ?? '');
                $employeNom = strtolower($a->getEmploye()?->getFullName() ?? '');

                return str_contains($materielNom, $term) || str_contains($employeNom, $term);
            });
        }

        return $this->render('affectation/historique.html.twig', [
            'affectations' => array_values($allHistory),
            'recherche' => $recherche,
        ]);
    }

    #[Route('/employes', name: 'app_affectation_employes', methods: ['GET'])]
    public function employes(Request $request, EmployeRepository $employeRepo, AffectationRepository $affectationRepo): Response
    {
        $recherche = (string) $request->query->get('recherche', '');
        $employes = $employeRepo->findForList($recherche !== '' ? $recherche : null);

        $actifParEmploye = [];
        $actives = $affectationRepo->findAllActive();
        foreach ($actives as $aff) {
            $eid = $aff->getEmploye()?->getIdEmploye();
            if ($eid !== null) {
                $actifParEmploye[$eid] = ($actifParEmploye[$eid] ?? 0) + 1;
            }
        }

        return $this->render('affectation/employes.html.twig', [
            'employes' => $employes,
            'recherche' => $recherche,
            'actif_par_employe' => $actifParEmploye,
        ]);
    }

    #[Route('/employes/nouveau', name: 'app_employe_new', methods: ['GET', 'POST'])]
    public function newEmploye(Request $request, EntityManagerInterface $em): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $employe = new Employe();
        $form = $this->createForm(EmployeType::class, $employe);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($employe);
            $em->flush();
            $this->addFlash('success', sprintf('L\'employé %s a été ajouté.', $employe->getFullName()));

            return $this->redirectToRoute('app_affectation_employes');
        }

        return $this->render('affectation/employe_form.html.twig', [
            'form' => $form,
            'employe' => $employe,
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/employes/{id}/supprimer', name: 'app_employe_delete', methods: ['POST'], requirements: ['id' => '\d+'])]
    public function deleteEmploye(
        int $id,
        Request $request,
        EmployeRepository $employeRepo,
        EntityManagerInterface $em
    ): Response {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $employe = $employeRepo->find($id);
        if ($employe === null) {
            throw $this->createNotFoundException('Employé introuvable.');
        }

        if (!$this->isCsrfTokenValid('delete-employe-' . $id, (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $nom = $employe->getFullName();
        $em->remove($employe);
        $em->flush();

        $this->addFlash('success', sprintf('L\'employé %s a été supprimé.', $nom));

        return $this->redirectToRoute('app_affectation_employes');
    }

    private function denyUserMode(Request $request): ?Response
    {
        $uiMode = $request->hasSession()
            ? (string) $request->getSession()->get('ui_mode', 'admin')
            : 'admin';

        if ($uiMode === 'admin') {
            return null;
        }

        $this->addFlash('error', 'Cette section est réservée au mode Admin.');

        return $this->redirectToRoute('app_marketplace_index');
    }
}
