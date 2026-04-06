<?php

namespace App\Service;

use App\Entity\Plantation;
use App\Entity\Production;
use Doctrine\ORM\EntityManagerInterface;

class PlantationService
{
    private EntityManagerInterface $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    // ✅ CREATE
    public function create(Plantation $plantation): void
    {
        $plantation->setEtat('EN_ATTENTE');

        $this->em->persist($plantation);
        $this->em->flush();
    }

    // ✅ UPDATE
    public function update(): void
    {
        $this->em->flush();
    }

    // ✅ DELETE
    public function delete(Plantation $plantation): void
    {
        $this->em->remove($plantation);
        $this->em->flush();
    }

    // 🔥 ACCEPT → CREATE PRODUCTION
    public function accept(Plantation $plantation): void
    {
        // 1. Change status
        $plantation->setEtat('COMPLETE');

        // 2. Create Production
        $production = new Production();
        $production->setQuantiteProduite($plantation->getQuantite());
        $production->setDateRecolte(new \DateTime());
        $production->setQualite('Bonne');
        $production->setEtat('Recoltee');
        $production->setPlantation($plantation);

        // 3. Save
        $this->em->persist($production);
        $this->em->flush();
    }
}