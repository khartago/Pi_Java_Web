<?php

namespace App\Service;

use App\Entity\Commentaire;
use Doctrine\ORM\EntityManagerInterface;

class CommentaireManager
{
    public function __construct(
        private readonly EntityManagerInterface $em
    ) {
    }

    public function save(Commentaire $commentaire): void
    {
        $this->em->persist($commentaire);
        $this->em->flush();
    }

    public function delete(Commentaire $commentaire): void
    {
        $this->em->remove($commentaire);
        $this->em->flush();
    }
}