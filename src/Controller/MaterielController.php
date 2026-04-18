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
    #[Route('/produits/{idProduit<\d+>}/materiels/nouveau', name: 'app_materiel_new', methods: ['GET', 'POST'])]
    public function new(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        MaterielManager $materielManager,
    ): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $materiel = (new Materiel())->setProduit($produit);
        $form = $this->createForm(MaterielType::class, $materiel);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $materielManager->save($materiel);
            $this->addFlash('success', 'Le matériel a été ajouté.');

            return $this->redirectToRoute('app_produit_show', [
                'idProduit' => $produit->getIdProduit(),
            ]);
        }

        return $this->render('materiel/form.html.twig', [
            'form' => $form,
            'materiel' => $materiel,
            'produit' => $produit,
            'mode' => 'creation',
            'submit_label' => 'Ajouter le matériel',
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/materiels/{idMateriel<\d+>}/modifier', name: 'app_materiel_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idMateriel' => 'idMateriel'])] Materiel $materiel,
        Request $request,
        MaterielManager $materielManager,
    ): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        $form = $this->createForm(MaterielType::class, $materiel);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $materielManager->save($materiel);
            $this->addFlash('success', 'Le matériel a été mis à jour.');

            return $this->redirectToRoute('app_produit_show', [
                'idProduit' => $materiel->getProduit()?->getIdProduit(),
            ]);
        }

        return $this->render('materiel/form.html.twig', [
            'form' => $form,
            'materiel' => $materiel,
            'produit' => $materiel->getProduit(),
            'mode' => 'edition',
            'submit_label' => 'Enregistrer les changements',
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/materiels/{idMateriel<\d+>}/supprimer', name: 'app_materiel_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idMateriel' => 'idMateriel'])] Materiel $materiel,
        Request $request,
        MaterielManager $materielManager,
    ): Response
    {
        if (($response = $this->denyUserMode($request)) !== null) {
            return $response;
        }

        if (!$this->isCsrfTokenValid('delete-materiel-'.$materiel->getIdMateriel(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $produitId = $materiel->getProduit()?->getIdProduit();
        $materielManager->delete($materiel);
        $this->addFlash('success', 'Le matériel a été supprimé.');

        return $this->redirectToRoute('app_produit_show', [
            'idProduit' => $produitId,
        ]);
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
