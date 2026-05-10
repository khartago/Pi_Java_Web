<?php

namespace App\Service;

use App\Entity\Production;
use Doctrine\ORM\EntityManagerInterface;

class ProductionService
{
    public function __construct(private readonly EntityManagerInterface $em)
    {
    }

    public function update(): void
    {
        $this->em->flush();
    }

    public function delete(Production $production): void
    {
        $this->em->remove($production);
        $this->em->flush();
    }
}
