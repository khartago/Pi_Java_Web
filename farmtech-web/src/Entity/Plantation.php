<?php

namespace App\Entity;

use App\Repository\PlantationRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: PlantationRepository::class)]
class Plantation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(name: 'nomPlant', length: 100)]
    #[Assert\NotBlank(message: 'Nom obligatoire')]
    #[Assert\Length(max: 40, maxMessage: 'Max 40 caracteres')]
    private ?string $nomPlant = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Variete obligatoire')]
    private ?string $variete = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: 'Quantite obligatoire')]
    #[Assert\Positive(message: 'Quantite doit etre positive')]
    private ?int $quantite = null;

    #[ORM\Column(name: 'datePlante', type: 'date')]
    #[Assert\NotNull(message: 'Date obligatoire')]
    private ?\DateTimeInterface $datePlante = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: 'Saison obligatoire')]
    private ?string $saison = null;

    #[ORM\Column(length: 50)]
    private string $etat = 'EN_ATTENTE';

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNomPlant(): ?string
    {
        return $this->nomPlant;
    }

    public function setNomPlant(string $nomPlant): self
    {
        $this->nomPlant = $nomPlant;
        return $this;
    }

    public function getVariete(): ?string
    {
        return $this->variete;
    }

    public function setVariete(string $variete): self
    {
        $this->variete = $variete;
        return $this;
    }

    public function getQuantite(): ?int
    {
        return $this->quantite;
    }

    public function setQuantite(int $quantite): self
    {
        $this->quantite = $quantite;
        return $this;
    }

    public function getDatePlante(): ?\DateTimeInterface
    {
        return $this->datePlante;
    }

    public function setDatePlante(\DateTimeInterface $datePlante): self
    {
        $this->datePlante = $datePlante;
        return $this;
    }

    public function getSaison(): ?string
    {
        return $this->saison;
    }

    public function setSaison(string $saison): self
    {
        $this->saison = $saison;
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
