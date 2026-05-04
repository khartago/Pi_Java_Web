<?php

namespace App\Service;

use App\Entity\Materiel;

class MaterielValidator
{
    public function validate(Materiel $materiel): bool
    {
        if (empty(trim((string) $materiel->getNom()))) {
            throw new \InvalidArgumentException('Le nom du matériel est obligatoire');
        }

        if (null !== $materiel->getCout() && $materiel->getCout() < 0) {
            throw new \InvalidArgumentException('Le coût ne peut pas être négatif');
        }

        return true;
    }
}
