<?php

namespace App\Service;

use App\Entity\Utilisateur;

class UtilisateurManager
{
    public function validate(Utilisateur $user): bool
    {
        if (empty(trim((string) $user->getNom()))) {
            throw new \InvalidArgumentException('Le nom est obligatoire');
        }

        if (!filter_var($user->getEmail(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException('Email invalide');
        }

        return true;
    }
}
