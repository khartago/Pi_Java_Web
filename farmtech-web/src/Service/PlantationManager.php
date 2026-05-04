<?php

namespace App\Service;

use App\Entity\Plantation;

class PlantationManager
{
    public function validate(Plantation $plantation): bool
    {
        if (null === $plantation->getQuantite() || $plantation->getQuantite() <= 0) {
            throw new \InvalidArgumentException('La quantité doit être strictement positive');
        }

        if (empty(trim((string) $plantation->getNomPlant()))) {
            throw new \InvalidArgumentException('Le nom de la plantation est obligatoire');
        }

        if (mb_strlen((string) $plantation->getNomPlant()) > 40) {
            throw new \InvalidArgumentException('Le nom de la plantation est limité à 40 caractères');
        }

        return true;
    }
}
