<?php

namespace App\Service;

use App\Entity\Produit;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\File\UploadedFile;

class ProduitManager
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly ProductImageStorage $imageStorage,
    ) {
    }

    public function save(Produit $produit, ?UploadedFile $imageFile = null): void
    {
        $oldImagePath = $produit->getImagePath();

        if ($imageFile instanceof UploadedFile) {
            $produit->setImagePath($this->imageStorage->store($imageFile));
        }

        $this->entityManager->persist($produit);
        $this->entityManager->flush();

        if ($imageFile instanceof UploadedFile && $oldImagePath !== null && $oldImagePath !== $produit->getImagePath()) {
            $this->imageStorage->remove($oldImagePath);
        }
    }

    public function delete(Produit $produit): void
    {
        $imagePath = $produit->getImagePath();

        $this->entityManager->remove($produit);
        $this->entityManager->flush();

        $this->imageStorage->remove($imagePath);
    }
}
