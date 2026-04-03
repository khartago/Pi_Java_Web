<?php

namespace App\Controller\Admin;

use App\Entity\Utilisateur;
use App\Form\UtilisateurType;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin/users')]
class UserController extends AbstractController
{
    private function filterParams(Request $request): array
    {
        $q = $request->query->get('q');
        $q = \is_string($q) ? trim($q) : '';
        if (strlen($q) > 200) {
            $q = substr($q, 0, 200);
        }

        return [
            'q' => '' !== $q ? $q : null,
            'role' => $request->query->get('role') ?: null,
            'sort' => $request->query->get('sort') ?: 'id',
            'dir' => $request->query->get('dir') ?: 'ASC',
        ];
    }

    #[Route('', name: 'admin_users_index', methods: ['GET'])]
    public function index(Request $request, UtilisateurRepository $repo): Response
    {
        $params = $this->filterParams($request);
        $users = $repo->findFiltered($params);
        $stats = $repo->getStats();

        return $this->render('admin/user/index.html.twig', [
            'users' => $users,
            'stats' => $stats,
            'filters' => $params,
        ]);
    }

    #[Route('/new', name: 'admin_users_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, UserPasswordHasherInterface $hasher): Response
    {
        $u = new Utilisateur();
        $form = $this->createForm(UtilisateurType::class, $u, ['require_password' => true]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $plain = $form->get('motDePasse')->getData();
            $plain = \is_string($plain) ? trim($plain) : '';
            $u->setMotDePasse($hasher->hashPassword($u, $plain));
            $em->persist($u);
            $em->flush();
            $this->addFlash('success', 'Utilisateur créé.');

            return $this->redirectToRoute('admin_users_index');
        }

        return $this->render('admin/user/new.html.twig', ['form' => $form]);
    }

    #[Route('/{id}/edit', name: 'admin_users_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function edit(Request $request, Utilisateur $u, EntityManagerInterface $em, UserPasswordHasherInterface $hasher): Response
    {
        $form = $this->createForm(UtilisateurType::class, $u, ['require_password' => false]);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $plain = $form->get('motDePasse')->getData();
            $plain = \is_string($plain) ? trim($plain) : '';
            if ('' !== $plain) {
                $u->setMotDePasse($hasher->hashPassword($u, $plain));
            }
            $em->flush();
            $this->addFlash('success', 'Utilisateur mis à jour.');

            return $this->redirectToRoute('admin_users_index');
        }

        return $this->render('admin/user/edit.html.twig', ['form' => $form, 'user' => $u]);
    }

    #[Route('/{id}/delete', name: 'admin_users_delete', requirements: ['id' => '\d+'], methods: ['POST'])]
    public function delete(Request $request, Utilisateur $u, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete'.$u->getId(), (string) $request->request->get('_token'))) {
            $me = $this->getUser();
            \assert($me instanceof Utilisateur);
            if ($u->getId() === $me->getId()) {
                $this->addFlash('danger', 'Vous ne pouvez pas supprimer votre propre compte.');

                return $this->redirectToRoute('admin_users_index');
            }
            $em->remove($u);
            $em->flush();
            $this->addFlash('success', 'Utilisateur supprimé.');
        }

        return $this->redirectToRoute('admin_users_index');
    }
}
