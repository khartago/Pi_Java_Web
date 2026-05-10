<?php

namespace App\Controller;

use App\Service\GameMailerService;
use App\Service\GameService;
use App\Service\WebWeatherService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class GameController extends AbstractController
{
    #[Route('/game', name: 'app_game', methods: ['GET'])]
    public function index(GameService $gameService, WebWeatherService $weatherService): Response
    {
        $cards = $gameService->getGameCards();
        $weather = $weatherService->getWeather('Tunis');

        return $this->render('game/index.html.twig', [
            'cards' => $cards,
            'weather' => $weather,
        ]);
    }

    #[Route('/plant/dead', name: 'plant_dead', methods: ['POST'])]
    public function plantDead(Request $request, GameMailerService $mailerService): JsonResponse
    {
        $payload = json_decode($request->getContent(), true) ?? [];
        $reason = $payload['reason'] ?? null;

        // Send email only for natural death (timer), not manual shovel action.
        if ($reason === 'timeout') {
            $plantName = is_string($payload['plantName'] ?? null) ? $payload['plantName'] : 'Parcelle';
            $mailerService->sendPlantDeathEmail(
                $plantName,
                null,
                'Timer epuise (plante non arrosee a temps)',
                new \DateTime()
            );
        }

        return $this->json(['ok' => true]);
    }
}
