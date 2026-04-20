<?php

namespace App\Controller;

use App\Service\GameMailerService;
use App\Service\WebWeatherService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class GameController extends AbstractController
{
    #[Route('/game', name: 'app_game', methods: ['GET'])]
    public function index(WebWeatherService $weatherService): Response
    {
        $weather = $weatherService->getWeather('Tunis');

        return $this->render('game/index.html.twig', [
            'weather' => $weather,
        ]);
    }

    #[Route('/plant/dead', name: 'plant_dead', methods: ['POST'])]
    public function plantDead(GameMailerService $mailerService): JsonResponse
    {
        $mailerService->sendPlantDeathEmail('Plant');
        return $this->json(['ok' => true]);
    }
}
