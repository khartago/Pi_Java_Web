<?php

namespace App\Controller;

use App\Service\GameService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class GameController extends AbstractController
{
    #[Route('/game', name: 'app_game')]
    public function index(GameService $gameService): Response
    {
        $cards = $gameService->getGameCards();

        return $this->render('game/index.html.twig', [
            'cards' => $cards
        ]);
    }
}