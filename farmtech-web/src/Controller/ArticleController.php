<?php

namespace App\Controller;

use App\Entity\Article;
use App\Form\ArticleType;
use App\Repository\ArticleRepository;
use App\Repository\BlogRepository;
use App\Repository\CommentaireRepository;
use App\Service\ArticleManager;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/articles')]
final class ArticleController extends AbstractController
{
    #[Route('/nouveau', name: 'app_article_new', methods: ['GET', 'POST'])]
    public function new(Request $request, ArticleManager $articleManager, BlogRepository $blogRepository): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $blogId = $request->query->get('blogId');
        $blog = $blogRepository->find($blogId);

        if (!$blog || $blog->getUtilisateur() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Vous ne pouvez pas publier dans ce blog.');
        }

        $article = new Article();
        // ADD THIS LINE RIGHT HERE:
        $article->setBlogId((int) $blogId);

        $form = $this->createForm(ArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $articleManager->save($article);
            $this->addFlash('success', 'Article créé.');
            return $this->redirectToRoute('app_blog_show', ['idBlog' => $blogId]);
        }

        return $this->render('article/form.html.twig', [
            'form' => $form,
            'blog' => $blog,
        ]);
    }

    #[Route('/{idArticle<\d+>}', name: 'app_article_show', methods: ['GET'])]
    public function show(
        #[MapEntity(mapping: ['idArticle' => 'id'])] Article $article,
        BlogRepository $blogRepository,
        CommentaireRepository $commentaireRepository
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // Find the blog to check ownership
        $blog = $blogRepository->find($article->getBlogId());

        // Determine if the current user is the owner of this article
        $isOwner = $blog && $blog->getUtilisateur() === $this->getUser();

        // Fetch comments linked to this article
        $comments = $commentaireRepository->findBy(['article' => $article], ['dateCommentaire' => 'DESC']);

        return $this->render('article/show.html.twig', [
            'article' => $article,
            'blog' => $blog,
            'isOwner' => $isOwner,
            'comments' => $comments,
        ]);
    }

    #[Route('/{idArticle<\d+>}/modifier', name: 'app_article_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idArticle' => 'id'])] Article $article,
        Request $request,
        ArticleManager $articleManager,
        BlogRepository $blogRepository
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // Check ownership before allowing edit
        $blog = $blogRepository->find($article->getBlogId());
        if (!$blog || $blog->getUtilisateur() !== $this->getUser()) {
            throw $this->createAccessDeniedException('Vous ne pouvez pas modifier cet article.');
        }

        $form = $this->createForm(ArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $articleManager->save($article);
            $this->addFlash('success', 'Article mis à jour.');
            return $this->redirectToRoute('app_article_show', ['idArticle' => $article->getId()]);
        }

        return $this->render('article/form.html.twig', [
            'form' => $form,
            'blog' => $blog,
            'article' => $article,
        ]);
    }

    #[Route('/{idArticle<\d+>}/supprimer', name: 'app_article_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idArticle' => 'id'])] Article $article,
        Request $request,
        ArticleManager $articleManager,
        BlogRepository $blogRepository
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $blog = $blogRepository->find($article->getBlogId());
        if (!$blog || $blog->getUtilisateur() !== $this->getUser()) {
            throw $this->createAccessDeniedException();
        }

        if ($this->isCsrfTokenValid('delete-article-'.$article->getId(), (string) $request->request->get('_token'))) {
            $articleManager->delete($article);
            $this->addFlash('success', 'Article supprimé.');
        }

        return $this->redirectToRoute('app_blog_show', ['idBlog' => $article->getBlogId()]);
    }
}