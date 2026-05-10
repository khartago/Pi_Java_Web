<?php

namespace App\Tests\Service;

use App\Entity\Utilisateur;
use App\Service\UtilisateurManager;
use PHPUnit\Framework\TestCase;

final class UtilisateurManagerTest extends TestCase
{
    public function testValidUtilisateur(): void
    {
        $u = (new Utilisateur())->setNom('Victor Hugo')->setEmail('victor.hugo@gmail.com');
        $this->assertTrue((new UtilisateurManager())->validate($u));
    }

    public function testUtilisateurWithoutName(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        (new UtilisateurManager())->validate((new Utilisateur())->setEmail('test@gmail.com'));
    }

    public function testUtilisateurWithInvalidEmail(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        (new UtilisateurManager())->validate((new Utilisateur())->setNom('Test')->setEmail('email_invalide'));
    }
}
