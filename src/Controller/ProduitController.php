<?php

namespace App\Controller;

use App\Entity\Produit;
use App\Form\ProduitType;
use App\Repository\ProduitRepository;
use App\Service\ProduitManager;
use Endroid\QrCode\Color\Color;
use Endroid\QrCode\ErrorCorrectionLevel;
use Endroid\QrCode\QrCode;
use Endroid\QrCode\RoundBlockSizeMode;
use Endroid\QrCode\Writer\SvgWriter;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Form\FormInterface;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;

#[Route('/produits')]
class ProduitController extends AbstractController
{
    #[Route('', name: 'app_produit_index', methods: ['GET'])]
    public function index(Request $request, ProduitRepository $produitRepository): Response
    {
        return $this->renderListing(
            $request,
            $produitRepository,
            null,
        );
    }

    #[Route('/{idProduit<\d+>}', name: 'app_produit_show', methods: ['GET'])]
    public function show(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        ProduitRepository $produitRepository,
    ): Response
    {
        return $this->renderListing(
            $request,
            $produitRepository,
            $produitRepository->findOneForDetail((int) $produit->getIdProduit()),
        );
    }

    #[Route('/nouveau', name: 'app_produit_new', methods: ['GET', 'POST'])]
    public function new(Request $request, ProduitManager $produitManager): Response
    {
        $produit = new Produit();
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            $produitManager->save($produit, $imageFile);

            $this->addFlash('success', 'Le produit a été créé.');

            return $this->redirectToRoute('app_produit_show', [
                'idProduit' => $produit->getIdProduit(),
            ]);
        }

        return $this->render('produit/form.html.twig', [
            'form' => $form,
            'produit' => $produit,
            'mode' => 'creation',
            'submit_label' => 'Créer le produit',
            'back_route' => 'app_produit_index',
            'current_image_path' => null,
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/{idProduit<\d+>}/modifier', name: 'app_produit_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        ProduitManager $produitManager,
    ): Response
    {
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            $produitManager->save($produit, $imageFile);

            $this->addFlash('success', 'Le produit a été mis à jour.');

            return $this->redirectToRoute('app_produit_show', [
                'idProduit' => $produit->getIdProduit(),
            ]);
        }

        return $this->render('produit/form.html.twig', [
            'form' => $form,
            'produit' => $produit,
            'mode' => 'edition',
            'submit_label' => 'Enregistrer les changements',
            'back_route' => 'app_produit_show',
            'back_route_params' => ['idProduit' => $produit->getIdProduit()],
            'current_image_path' => $produit->getImagePath(),
        ], $form->isSubmitted() ? new Response(status: Response::HTTP_UNPROCESSABLE_ENTITY) : null);
    }

    #[Route('/{idProduit<\d+>}/supprimer', name: 'app_produit_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit,
        Request $request,
        ProduitManager $produitManager,
    ): Response
    {
        if (!$this->isCsrfTokenValid('delete-produit-'.$produit->getIdProduit(), (string) $request->request->get('_token'))) {
            throw $this->createAccessDeniedException('Jeton CSRF invalide.');
        }

        $produitManager->delete($produit);
        $this->addFlash('success', 'Le produit a été supprimé.');

        return $this->redirectToRoute('app_produit_index');
    }

    #[Route('/{idProduit<\d+>}/qr-code', name: 'app_produit_qr_code', methods: ['GET'])]
    public function qrCode(#[MapEntity(mapping: ['idProduit' => 'idProduit'])] Produit $produit): Response
    {
        $absolutePath = $this->generateUrl('app_produit_show', [
            'idProduit' => $produit->getIdProduit(),
        ], UrlGeneratorInterface::ABSOLUTE_PATH);

        $publicBaseUrl = trim((string) ($_ENV['APP_PUBLIC_BASE_URL'] ?? $_SERVER['APP_PUBLIC_BASE_URL'] ?? ''));
        $targetUrl = $publicBaseUrl !== ''
            ? rtrim($publicBaseUrl, '/').$absolutePath
            : $this->generateUrl('app_produit_show', [
                'idProduit' => $produit->getIdProduit(),
            ], UrlGeneratorInterface::ABSOLUTE_URL);

        $qrCode = new QrCode(
            data: $targetUrl,
            errorCorrectionLevel: ErrorCorrectionLevel::Low,
            size: 240,
            margin: 10,
            roundBlockSizeMode: RoundBlockSizeMode::Margin,
            foregroundColor: new Color(30, 30, 30),
            backgroundColor: new Color(255, 255, 255),
        );

        $result = (new SvgWriter())->write($qrCode);

        $response = new Response($result->getString());
        $response->headers->set('Content-Type', $result->getMimeType());
        $response->setPublic();
        $response->setMaxAge(3600);

        return $response;
    }

    private function renderListing(
        Request $request,
        ProduitRepository $produitRepository,
        ?Produit $selectedProduit,
    ): Response {
        $recherche = trim((string) $request->query->get('recherche', ''));
        $unite = trim((string) $request->query->get('unite', ''));
        $produits = $produitRepository->findForList($recherche, $unite);

        if ($selectedProduit === null && $produits !== []) {
            $selectedProduit = $produitRepository->findOneForDetail((int) $produits[0]->getIdProduit());
        }

        if (
            $selectedProduit !== null
            && !array_filter(
                $produits,
                static fn (Produit $produit): bool => $produit->getIdProduit() === $selectedProduit->getIdProduit(),
            )
        ) {
            $selectedProduit = null;
        }

        return $this->render('produit/index.html.twig', [
            'produits' => $produits,
            'selected_produit' => $selectedProduit,
            'recherche' => $recherche,
            'unite' => $unite,
            'unites' => $produitRepository->findDistinctUnites(),
        ]);
    }
}
