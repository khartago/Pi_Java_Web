<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class FavorisController extends AbstractController
{
    #[Route('/marketplace/favoris', name: 'app_favoris_index', methods: ['GET'])]
    public function index(): Response
    {
        return $this->render('marketplace/favoris.html.twig');
    }
}
