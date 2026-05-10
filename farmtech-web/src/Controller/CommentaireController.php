<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Commentaire;
use App\Form\CommentaireType;
use App\Service\CommentaireManager;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/commentaires')]
final class CommentaireController extends AbstractController
{
    #[Route('/article/{idArticle<\d+>}/nouveau', name: 'app_commentaire_new', methods: ['GET', 'POST'])]
    public function new(
        #[MapEntity(mapping: ['idArticle' => 'id'])] Article $article,
        Request $request,
        CommentaireManager $commentaireManager
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $commentaire = new Commentaire();
        $form = $this->createForm(CommentaireType::class, $commentaire);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $commentaire->setArticle($article);
            $commentaire->setUtilisateur($this->getUser());

            $commentaireManager->save($commentaire);
            $this->addFlash('success', 'Commentaire ajouté.');

            return $this->redirectToRoute('app_article_show', ['idArticle' => $article->getId()]);
        }

        return $this->render('commentaire/form.html.twig', [
            'form' => $form,
            'article' => $article,
        ]);
    }

    #[Route('/{id<\d+>}/modifier', name: 'app_commentaire_edit', methods: ['GET', 'POST'])]
    public function edit(
        Commentaire $commentaire,
        Request $request,
        CommentaireManager $commentaireManager,
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // SECURITY CHECK: Ensure the logged-in user is the author of the comment
        if ($commentaire->getUtilisateur() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Vous ne pouvez pas modifier ce commentaire.');
        }

        $form = $this->createForm(CommentaireType::class, $commentaire);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $commentaireManager->save($commentaire);
            $this->addFlash('success', 'Commentaire modifié.');

            return $this->redirectToRoute('app_article_show', ['idArticle' => $commentaire->getArticle()->getId()]);
        }

        return $this->render('commentaire/form.html.twig', [
            'form' => $form,
            'article' => $commentaire->getArticle(),
        ]);
    }

    #[Route('/{id<\d+>}/supprimer', name: 'app_commentaire_delete', methods: ['POST'])]
    public function delete(
        Commentaire $commentaire,
        Request $request,
        CommentaireManager $commentaireManager,
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // SECURITY CHECK: Ensure the logged-in user is the author of the comment
        if ($commentaire->getUtilisateur() !== $this->getUser()) {
            throw $this->createAccessDeniedException();
        }

        if ($this->isCsrfTokenValid('delete-commentaire-'.$commentaire->getId(), (string) $request->request->get('_token'))) {
            $articleId = $commentaire->getArticle()->getId();
            $commentaireManager->delete($commentaire);
            $this->addFlash('success', 'Commentaire supprimé.');

            return $this->redirectToRoute('app_article_show', ['idArticle' => $articleId]);
        }

        return $this->redirectToRoute('app_news');
    }
}