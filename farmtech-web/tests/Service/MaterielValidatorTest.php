<?php

namespace App\Tests\Service;

use App\Entity\Materiel;
use App\Service\MaterielValidator;
use PHPUnit\Framework\TestCase;

final class MaterielValidatorTest extends TestCase
{
    public function testValidMateriel(): void
    {
        $m = new Materiel();
        $m->setNom('Tracteur')->setCout(1500.0);
        $this->assertTrue((new MaterielValidator())->validate($m));
    }

    public function testMaterielWithoutName(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $m = new Materiel();
        $m->setNom('   ');
        (new MaterielValidator())->validate($m);
    }

    public function testMaterielWithNegativeCout(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $m = new Materiel();
        $m->setNom('Outil')->setCout(-10.0);
        (new MaterielValidator())->validate($m);
    }
}
