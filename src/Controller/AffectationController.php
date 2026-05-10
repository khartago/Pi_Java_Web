<?php

namespace App\Controller;

use App\Entity\Affectation;
use App\Entity\Employe;
use App\Form\AffectationType;
use App\Form\EmployeType;
use App\Repository\AffectationRepository;
use App\Repository\EmployeRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/affectations')]
class AffectationController extends AbstractController
{
    #[Route('', name: 'app_affectation_index', methods: ['GET'])]
    public function index(AffectationRepository $affectationRepo, EmployeRepository $employeRepo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('affectation/index.html.twig', [
            'affectations' => $affectationRepo->findAllActive(),
            'total_employes' => count($employeRepo->findForList(null)),
        ]);
    }

    #[Route('/nouveau', name: 'app_affectation_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $affectation = new Affectation();
        $form = $this->createForm(AffectationType::class, $affectation);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($affectation);
            $em->flush();
            $this->addFlash('success', 'Affectation creee.');
            return $this->redirectToRoute('app_affectation_index');
        }
        return $this->render('affectation/form.html.twig', ['form' => $form]);
    }

    #[Route('/historique', name: 'app_affectation_historique', methods: ['GET'])]
    public function historique(AffectationRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('affectation/historique.html.twig', ['affectations' => $repo->findAllHistory()]);
    }

    #[Route('/employes', name: 'app_affectation_employes', methods: ['GET'])]
    public function employes(EmployeRepository $repo): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        return $this->render('affectation/employes.html.twig', ['employes' => $repo->findForList(null), 'actif_par_employe' => []]);
    }

    #[Route('/employes/nouveau', name: 'app_employe_new', methods: ['GET', 'POST'])]
    public function newEmploye(Request $request, EntityManagerInterface $em): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $employe = new Employe();
        $form = $this->createForm(EmployeType::class, $employe);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($employe);
            $em->flush();
            $this->addFlash('success', 'Employe ajoute.');
            return $this->redirectToRoute('app_affectation_employes');
        }
        return $this->render('affectation/employe_form.html.twig', ['form' => $form]);
    }
}
