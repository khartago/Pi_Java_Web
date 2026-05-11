<?php

namespace App\Entity;

use App\Repository\ProductionRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ProductionRepository::class)]
class Production
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idProduction')]
    private ?int $idProduction = null;

    #[ORM\Column(name: 'quantiteProduite', type: Types::FLOAT)]
    #[Assert\Positive]
    private float $quantiteProduite = 1.0;

    #[ORM\Column(name: 'dateRecolte', type: Types::DATE_MUTABLE)]
    #[Assert\NotNull]
    private \DateTimeInterface $dateRecolte;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank]
    private string $qualite = '';

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank]
    private string $etat = '';

    public function __construct()
    {
        $this->dateRecolte = new \DateTimeImmutable('today');
        $this->quantiteProduite = 1.0;
    }

    public function getIdProduction(): ?int
    {
        return $this->idProduction;
    }

    public function getQuantiteProduite(): float
    {
        return $this->quantiteProduite;
    }

    public function setQuantiteProduite(float $quantiteProduite): self
    {
        $this->quantiteProduite = $quantiteProduite;
        return $this;
    }

    public function getDateRecolte(): \DateTimeInterface
    {
        return $this->dateRecolte;
    }

    public function setDateRecolte(\DateTimeInterface $dateRecolte): self
    {
        $this->dateRecolte = $dateRecolte;
        return $this;
    }

    public function getQualite(): string
    {
        return $this->qualite;
    }

    public function setQualite(string $qualite): self
    {
        $this->qualite = $qualite;
        return $this;
    }

    public function getEtat(): string
    {
        return $this->etat;
    }

    public function setEtat(string $etat): self
    {
        $this->etat = $etat;
        return $this;
    }

}
