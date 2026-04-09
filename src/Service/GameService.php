<?php

namespace App\Service;

use App\Repository\PlantationRepository;

class GameService
{
    private PlantationRepository $repo;

    public function __construct(PlantationRepository $repo)
    {
        $this->repo = $repo;
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