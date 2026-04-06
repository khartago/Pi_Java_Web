<?php

namespace App\Service;

use App\Entity\Production;
use Doctrine\ORM\EntityManagerInterface;

class ProductionService
{
    private EntityManagerInterface $em;

    public function __construct(EntityManagerInterface $em)
    {
        $this->em = $em;
    }

    // ✅ UPDATE
    public function update(): void
    {
        $this->em->flush();
    }

    // ✅ DELETE
    public function delete(Production $production): void
    {
        $this->em->remove($production);
        $this->em->flush();
    }
}