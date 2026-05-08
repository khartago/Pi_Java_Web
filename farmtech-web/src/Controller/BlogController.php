<?php

namespace App\Controller;

use App\Entity\Blog;
use App\Form\BlogType;
use App\Repository\BlogRepository;
use App\Service\BlogManager;
use App\Repository\ArticleRepository;
use Symfony\Bridge\Doctrine\Attribute\MapEntity;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/blogs')]
final class BlogController extends AbstractController
{
    #[Route('', name: 'app_blog_index', methods: ['GET'])]
    public function index(Request $request, BlogRepository $blogRepository): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $search = trim((string) $request->query->get('recherche', ''));
        $tag = trim((string) $request->query->get('tag', ''));
        $blogs = $blogRepository->findForList($search, $tag);

        return $this->render('blog/index.html.twig', [
            'blogs' => $blogs,
            'recherche' => $search,
            'tag' => $tag,
            'tags' => $blogRepository->findDistinctTags(),
        ]);
    }

    #[Route('/{idBlog<\d+>}', name: 'app_blog_show', methods: ['GET'])]
    public function show(
        #[MapEntity(mapping: ['idBlog' => 'idBlog'])] Blog $blog,
        ArticleRepository $articleRepository
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        // Fetch all articles that have this blog's ID
        $articles = $articleRepository->findBy(
            ['blogId' => $blog->getIdBlog()],
            ['createdAt' => 'DESC'] // Show newest articles first
        );

        return $this->render('blog/show.html.twig', [
            'blog' => $blog,
            'articles' => $articles,
        ]);
    }

    #[Route('/{idBlog<\d+>}/modifier', name: 'app_blog_edit', methods: ['GET', 'POST'])]
    public function edit(
        #[MapEntity(mapping: ['idBlog' => 'idBlog'])] Blog $blog,
        Request $request,
        BlogManager $blogManager,
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $form = $this->createForm(BlogType::class, $blog);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $blogManager->save($blog);
            $this->addFlash('success', 'Blog mis a jour.');
            return $this->redirectToRoute('app_news');
        }

        return $this->render('blog/form.html.twig', [
            'form' => $form,
            'mode' => 'edition',
            'blog' => $blog,
        ]);
    }

    #[Route('/{idBlog<\d+>}/supprimer', name: 'app_blog_delete', methods: ['POST'])]
    public function delete(
        #[MapEntity(mapping: ['idBlog' => 'idBlog'])] Blog $blog,
        Request $request,
        BlogManager $blogManager,
    ): Response {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        if ($this->isCsrfTokenValid('delete-blog-'.$blog->getIdBlog(), (string) $request->request->get('_token'))) {
            $blogManager->delete($blog);
            $this->addFlash('success', 'Blog supprime.');
        }

        return $this->redirectToRoute('app_news');
    }
    #[Route('/nouveau', name: 'app_blog_new', methods: ['GET', 'POST'])]
    public function new(Request $request, BlogManager $blogManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $blog = new Blog();
        $form = $this->createForm(BlogType::class, $blog);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // AUTOMATICALLY ASSIGN THE CONNECTED USER HERE
            $blog->setUtilisateur($this->getUser());

            $blogManager->save($blog);
            $this->addFlash('success', 'Blog cree.');
            return $this->redirectToRoute('app_news');
        }

        return $this->render('blog/form.html.twig', [
            'form' => $form,
            'mode' => 'creation',
        ]);
    }
}