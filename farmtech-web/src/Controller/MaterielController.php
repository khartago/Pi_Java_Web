<?php

namespace App\Controller;

use App\Entity\Materiel;
use App\Entity\Produit;
use App\Form\MaterielType;
use App\Service\MaterielManager;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class MaterielController extends AbstractController
{
    #[Route('/admin/produits/{idProduit<\d+>}/materiels/nouveau', name: 'app_materiel_new', methods: ['GET', 'POST'])]
    public function new(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        MaterielManager $materielManager,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $materiel = (new Materiel())->setProduit($produit);
        $form = $this->createForm(MaterielType::class, $materiel);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $materielManager->save($materiel);
            $this->addFlash('success', 'Materiel ajoute.');
            return $this->redirectToRoute('app_produit_show', ['idProduit' => $produit->getIdProduit()]);
        }

        return $this->render('materiel/form.html.twig', ['form' => $form, 'produit' => $produit, 'mode' => 'creation']);
    }

    #[Route('/admin/materiels/{idMateriel<\d+>}/modifier', name: 'app_materiel_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idMateriel' => 'idMateriel'])] Materiel $materiel,
        Request $request,
        MaterielManager $materielManager,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $form = $this->createForm(MaterielType::class, $materiel);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $materielManager->save($materiel);
            $this->addFlash('success', 'Materiel mis a jour.');
            return $this->redirectToRoute('app_produit_show', ['idProduit' => $materiel->getProduit()?->getIdProduit()]);
        }

        return $this->render('materiel/form.html.twig', ['form' => $form, 'materiel' => $materiel, 'produit' => $materiel->getProduit(), 'mode' => 'edition']);
    }

    #[Route('/admin/materiels/{idMateriel<\d+>}/supprimer', name: 'app_materiel_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idMateriel' => 'idMateriel'])] Materiel $materiel,
        Request $request,
        MaterielManager $materielManager,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($this->isCsrfTokenValid('delete-materiel-'.$materiel->getIdMateriel(), (string) $request->request->get('_token'))) {
            $produitId = $materiel->getProduit()?->getIdProduit();
            $materielManager->delete($materiel);
            $this->addFlash('success', 'Materiel supprime.');
            return $this->redirectToRoute('app_produit_show', ['idProduit' => $produitId]);
        }

        return $this->redirectToRoute('app_produit_index');
    }
}
