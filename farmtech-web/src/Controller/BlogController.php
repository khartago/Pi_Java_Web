<?php

namespace App\Controller;

use App\Entity\Article;
use App\Entity\Comment;
use App\Form\CommentType;
use App\Repository\ArticleRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class BlogController extends AbstractController
{
    #[Route('/', name: 'app_blog_index', methods: ['GET'])]
    public function index(ArticleRepository $articleRepository): Response
    {
        // Only show published articles
        $articles = $articleRepository->findPublished();

        return $this->render('blog/index.html.twig', [
            'articles' => $articles,
        ]);
    }

    #[Route('/article/{slug}', name: 'app_blog_show', methods: ['GET', 'POST'])]
    public function show(Request $request, Article $article, EntityManagerInterface $entityManager): Response
    {
        // Create the comment form
        $comment = new Comment();
        $comment->setArticle($article); // Link comment to the article
        $form = $this->createForm(CommentType::class, $comment);

        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Comments are saved as un-approved (isApproved = false by default)
            // In a real app, you might send an email to the admin here.
            $entityManager->persist($comment);
            $entityManager->flush();

            $this->addFlash('success', 'Your comment has been submitted and is awaiting moderation.');

            // Redirect to prevent duplicate submissions on refresh
            return $this->redirectToRoute('app_blog_show', ['slug' => $article->getSlug()]);
        }

        // Get approved comments to display under the article
        $approvedComments = $entityManager->getRepository(Comment::class)
            ->findApprovedByArticle($article);

        return $this->render('blog/show.html.twig', [
            'article' => $article,
            'comments' => $approvedComments,
            'commentForm' => $form->createView(),
        ]);
    }
}