<?php

namespace App\Tests\Validation;

use App\Entity\Materiel;
use App\Entity\Produit;
use Symfony\Bundle\FrameworkBundle\Test\KernelTestCase;
use Symfony\Component\Validator\Validator\ValidatorInterface;

class MaterielValidationTest extends KernelTestCase
{
    private ValidatorInterface $validator;

    protected function setUp(): void
    {
        self::bootKernel();
        $this->validator = static::getContainer()->get(ValidatorInterface::class);
    }

    public function testMaterielRequiresExpectedFieldsAndConstraints(): void
    {
        $materiel = (new Materiel())
            ->setNom('!')
            ->setEtat('mauvais')
            ->setDateAchat(new \DateTimeImmutable('+1 day'))
            ->setCout(-5.2);

        $messages = array_map(
            static fn ($violation): string => $violation->getMessage(),
            iterator_to_array($this->validator->validate($materiel)),
        );

        self::assertContains('Le nom doit contenir au moins 2 caractères.', $messages);
        self::assertContains('Le nom ne doit contenir que des lettres, chiffres et espaces.', $messages);
        self::assertContains('L’état doit être : neuf, usagé, bon ou panne.', $messages);
        self::assertContains('La date d’achat doit être aujourd’hui ou passée.', $messages);
        self::assertContains('Le coût doit être positif ou nul.', $messages);
        self::assertContains('Le produit lié est requis.', $messages);
    }

    public function testMaterielIsValidWhenAttachedToAProduit(): void
    {
        $produit = (new Produit())
            ->setNom('Pompe')
            ->setQuantite(2)
            ->setUnite('piece');

        $materiel = (new Materiel())
            ->setNom('Pompe principale')
            ->setEtat('bon')
            ->setDateAchat(new \DateTimeImmutable('today'))
            ->setCout(149.99)
            ->setProduit($produit);

        self::assertCount(0, $this->validator->validate($materiel));
    }
}
