<?php

namespace App\Tests\Validation;

use App\Entity\Produit;
use App\Form\ProduitType;
use Symfony\Bundle\FrameworkBundle\Test\KernelTestCase;
use Symfony\Component\Form\FormFactoryInterface;
use Symfony\Component\Validator\Constraints\File;
use Symfony\Component\Validator\Validator\ValidatorInterface;

class ProduitValidationTest extends KernelTestCase
{
    private ValidatorInterface $validator;
    private FormFactoryInterface $formFactory;

    protected function setUp(): void
    {
        self::bootKernel();
        $this->validator = static::getContainer()->get(ValidatorInterface::class);
        $this->formFactory = static::getContainer()->get(FormFactoryInterface::class);
    }

    public function testProduitRequiresExpectedFieldsAndValueRanges(): void
    {
        $produit = (new Produit())
            ->setNom('!')
            ->setQuantite(-1)
            ->setUnite('tonne');

        $messages = array_map(
            static fn ($violation): string => $violation->getMessage(),
            iterator_to_array($this->validator->validate($produit)),
        );

        self::assertContains('Le nom doit contenir au moins 2 caractères.', $messages);
        self::assertContains('Le nom ne doit contenir que des lettres, chiffres et espaces.', $messages);
        self::assertContains('La quantité doit être positive ou nulle.', $messages);
        self::assertContains('L’unité doit être : kg, l ou piece.', $messages);
    }

    public function testProduitAcceptsOptionalExpiration(): void
    {
        $produit = (new Produit())
            ->setNom('Semence bio')
            ->setQuantite(12)
            ->setUnite('kg')
            ->setDateExpiration(null);

        self::assertCount(0, $this->validator->validate($produit));
    }

    public function testProduitFormConfiguresImageFileConstraints(): void
    {
        $form = $this->formFactory->create(ProduitType::class, new Produit());
        $constraints = $form->get('imageFile')->getConfig()->getOption('constraints');

        self::assertCount(1, $constraints);
        self::assertInstanceOf(File::class, $constraints[0]);
        self::assertSame(5000000, $constraints[0]->maxSize);
        self::assertSame(['image/jpeg', 'image/png'], $constraints[0]->mimeTypes);
    }
}
