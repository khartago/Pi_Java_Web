<?php

namespace App\Controller;

use App\Entity\Produit;
use App\Form\ProduitType;
use App\Repository\ProduitRepository;
use App\Service\ProduitManager;
use Endroid\QrCode\Builder\Builder;
use Endroid\QrCode\Encoding\Encoding;
use Endroid\QrCode\ErrorCorrectionLevel;
use Endroid\QrCode\RoundBlockSizeMode;
use Endroid\QrCode\Writer\SvgWriter;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

#[Route('/admin/produits')]
class ProduitController extends AbstractController
{
    #[Route('', name: 'app_produit_index', methods: ['GET'])]
    public function index(Request $request, ProduitRepository $produitRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $recherche = trim((string) $request->query->get('recherche', ''));
        $unite = trim((string) $request->query->get('unite', ''));
        $produits = $produitRepository->findForList($recherche, $unite);

        return $this->render('produit/index.html.twig', [
            'produits' => $produits,
            'recherche' => $recherche,
            'unite' => $unite,
            'unites' => $produitRepository->findDistinctUnites(),
        ]);
    }

    #[Route('/{idProduit<\d+>}', name: 'app_produit_show', methods: ['GET'])]
    public function show(#[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        return $this->render('produit/show.html.twig', [
            'produit' => $produit,
        ]);
    }

    #[Route('/nouveau', name: 'app_produit_new', methods: ['GET', 'POST'])]
    public function new(Request $request, ProduitManager $produitManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $produit = new Produit();
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            $produitManager->save($produit, $imageFile);
            $this->addFlash('success', 'Produit cree.');
            return $this->redirectToRoute('app_produit_index');
        }

        return $this->render('produit/form.html.twig', ['form' => $form, 'mode' => 'creation']);
    }

    #[Route('/{idProduit<\d+>}/modifier', name: 'app_produit_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        ProduitManager $produitManager,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            $produitManager->save($produit, $imageFile);
            $this->addFlash('success', 'Produit mis a jour.');
            return $this->redirectToRoute('app_produit_index');
        }

        return $this->render('produit/form.html.twig', [
            'form' => $form,
            'mode' => 'edition',
            'produit' => $produit,
            'current_image_path' => $produit->getImagePath(),
        ]);
    }

    #[Route('/{idProduit<\d+>}/qr-code', name: 'app_produit_qr_code', methods: ['GET'])]
    public function qrCode(#[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $url = $this->generateUrl('app_marketplace_show', ['idProduit' => $produit->getIdProduit()], UrlGeneratorInterface::ABSOLUTE_URL);
        $result = (new Builder(
            writer: new SvgWriter(),
            data: $url,
            encoding: new Encoding('UTF-8'),
            errorCorrectionLevel: ErrorCorrectionLevel::Medium,
            size: 200,
            margin: 8,
            roundBlockSizeMode: RoundBlockSizeMode::Margin,
        ))->build();

        return new Response($result->getString(), Response::HTTP_OK, ['Content-Type' => $result->getMimeType()]);
    }

    #[Route('/{idProduit<\d+>}/supprimer', name: 'app_produit_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        ProduitManager $produitManager,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        if ($this->isCsrfTokenValid('delete-produit-'.$produit->getIdProduit(), (string) $request->request->get('_token'))) {
            $produitManager->delete($produit);
            $this->addFlash('success', 'Produit supprime.');
        }

        return $this->redirectToRoute('app_produit_index');
    }
}
