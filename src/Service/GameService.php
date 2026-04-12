<?php

namespace App\Service;

use App\Repository\PlantationRepository;
use App\Service\MailerService;
class GameService
{
    private PlantationRepository $repo;

    public function __construct(PlantationRepository $repo)
    {
        $this->repo = $repo;
    }
    public function checkPlant($plant, MailerService $mailerService)
{
    if ($plant->isDead() && !$plant->isNotified()) {
        

        $mailerService->sendPlantDeathEmail($plant->getName());

        $plant->setNotified(true);
    }
}

public function getGameCards(): array
{
    $plants = $this->repo->findAll();

    $cards = [];

    foreach ($plants as $i => $plant) {
        if ($i >= 12) break; // ✅ LIMIT TO 12

        $cards[] = [
            'name' => strtolower($plant->getNomPlant()),
            'index' => $i
        ];
    }

    return $cards;
}
}