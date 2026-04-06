<?php

namespace App\Service;

use App\Entity\Materiel;
use Doctrine\ORM\EntityManagerInterface;

class MaterielManager
{
    public function __construct(private readonly EntityManagerInterface $entityManager)
    {
    }

    public function save(Materiel $materiel): void
    {
        $this->entityManager->persist($materiel);
        $this->entityManager->flush();
    }

    public function delete(Materiel $materiel): void
    {
        $this->entityManager->remove($materiel);
        $this->entityManager->flush();
    }
}
