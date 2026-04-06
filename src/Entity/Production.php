<?php

namespace App\Entity;

use App\Repository\ProductionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ProductionRepository::class)]
class Production
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $idProduction = null;

    #[ORM\Column]
    private ?float $quantiteProduite = null;

    #[ORM\Column(type: 'date')]
    private ?\DateTimeInterface $dateRecolte = null;

    #[ORM\Column(length: 100)]
    private ?string $qualite = null;

    #[ORM\Column(length: 100)]
    private ?string $etat = null;

    // 🔗 RELATION
    #[ORM\ManyToOne(inversedBy: 'productions')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Plantation $plantation = null;

    // GETTERS & SETTERS

    public function getIdProduction(): ?int { return $this->idProduction; }

    public function getQuantiteProduite(): ?float { return $this->quantiteProduite; }
    public function setQuantiteProduite(float $q): self { $this->quantiteProduite = $q; return $this; }

    public function getDateRecolte(): ?\DateTimeInterface { return $this->dateRecolte; }
    public function setDateRecolte(\DateTimeInterface $d): self { $this->dateRecolte = $d; return $this; }

    public function getQualite(): ?string { return $this->qualite; }
    public function setQualite(string $q): self { $this->qualite = $q; return $this; }

    public function getEtat(): ?string { return $this->etat; }
    public function setEtat(string $e): self { $this->etat = $e; return $this; }

    public function getPlantation(): ?Plantation { return $this->plantation; }
    public function setPlantation(Plantation $p): self { $this->plantation = $p; return $this; }
}