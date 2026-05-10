<?php

namespace App\Service;

use App\Repository\PlantationRepository;

class GameService
{
    public function __construct(private readonly PlantationRepository $plantationRepository)
    {
    }

    /**
     * @return array<int, array{name:string,index:int}>
     */
    public function getGameCards(): array
    {
        $plants = $this->plantationRepository->findSorted('date');
        $cards = [];

        foreach ($plants as $i => $plant) {
            if ($i >= 12) {
                break;
            }

            $cards[] = [
                'name' => strtolower((string) $plant->getNomPlant()),
                'index' => $i,
            ];
        }

        return $cards;
    }
}
