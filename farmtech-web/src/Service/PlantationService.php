<?php

namespace App\Service;

use App\Entity\Plantation;
use App\Entity\Production;
use Doctrine\ORM\EntityManagerInterface;

class PlantationService
{
    public function __construct(private readonly EntityManagerInterface $em)
    {
    }

    public function create(Plantation $plantation): void
    {
        $plantation->setEtat('EN_ATTENTE');
        $this->em->persist($plantation);
        $this->em->flush();
    }

    public function update(): void
    {
        $this->em->flush();
    }

    public function delete(Plantation $plantation): void
    {
        $this->em->remove($plantation);
        $this->em->flush();
    }

    public function accept(Plantation $plantation): void
    {
        $plantation->setEtat('COMPLETE');

        $production = new Production();
        $production->setQuantiteProduite((float) $plantation->getQuantite());
        $production->setDateRecolte(new \DateTime());
        $production->setQualite('Bonne');
        $production->setEtat('Recoltee');

        $this->em->persist($production);
        $this->em->flush();
    }
}
