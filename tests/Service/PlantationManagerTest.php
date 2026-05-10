<?php

namespace App\Tests\Service;

use App\Entity\Plantation;
use App\Service\PlantationManager;
use PHPUnit\Framework\TestCase;

final class PlantationManagerTest extends TestCase
{
    public function testValidPlantation(): void
    {
        $p = new Plantation();
        $p->setNomPlant('Parcelle Nord')->setQuantite(100);
        $this->assertTrue((new PlantationManager())->validate($p));
    }

    public function testPlantationWithNonPositiveQuantite(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $p = new Plantation();
        $p->setNomPlant('Parcelle')->setQuantite(0);
        (new PlantationManager())->validate($p);
    }

    public function testPlantationWithoutNomPlant(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $p = new Plantation();
        $p->setQuantite(10);
        (new PlantationManager())->validate($p);
    }

    public function testPlantationWithTooLongNomPlant(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $p = new Plantation();
        $p->setNomPlant(str_repeat('a', 41))->setQuantite(5);
        (new PlantationManager())->validate($p);
    }
}
