<?php

namespace App\Service;

use App\Entity\Diagnostique;

class DiagnostiqueManager
{
    public function validate(Diagnostique $diagnostique): bool
    {
        if (empty(trim((string) $diagnostique->getCause())) || empty(trim((string) $diagnostique->getSolutionProposee()))) {
            throw new \InvalidArgumentException('La cause et la solution proposée sont obligatoires');
        }

        if (null !== $diagnostique->getDateFeedback()
            && null !== $diagnostique->getDateDiagnostique()
            && $diagnostique->getDateFeedback() < $diagnostique->getDateDiagnostique()) {
            throw new \InvalidArgumentException('La date du feedback doit être postérieure à la date du diagnostic');
        }

        return true;
    }
}
