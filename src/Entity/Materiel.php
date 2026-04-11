<?php

namespace App\Entity;

use App\Repository\MaterielRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: MaterielRepository::class)]
#[ORM\Table(name: 'materiel')]
class Materiel
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idMateriel', type: 'integer')]
    private ?int $idMateriel = null;

    #[ORM\Column(name: 'nom', length: 100)]
    #[Assert\NotBlank(message: 'Le nom du matériel est requis.')]
    #[Assert\Length(
        min: 2,
        max: 50,
        minMessage: 'Le nom doit contenir au moins {{ limit }} caractères.',
        maxMessage: 'Le nom doit contenir au maximum {{ limit }} caractères.'
    )]
    #[Assert\Regex(
        pattern: '/^[\p{L}\d ]+$/u',
        message: 'Le nom ne doit contenir que des lettres, chiffres et espaces.'
    )]
    private string $nom = '';

    #[ORM\Column(name: 'etat', length: 50)]
    #[Assert\NotBlank(message: 'L’état du matériel est requis.')]
    #[Assert\Choice(
        choices: ['neuf', 'usagé', 'bon', 'panne'],
        message: 'L’état doit être : neuf, usagé, bon ou panne.'
    )]
    private string $etat = '';

    #[ORM\Column(name: 'dateAchat', type: 'date_immutable')]
    #[Assert\NotNull(message: 'La date d’achat est requise.')]
    #[Assert\LessThanOrEqual('today', message: 'La date d’achat doit être aujourd’hui ou passée.')]
    private ?\DateTimeImmutable $dateAchat = null;

    #[ORM\Column(name: 'cout', type: 'float')]
    #[Assert\NotNull(message: 'Le coût est requis.')]
    #[Assert\PositiveOrZero(message: 'Le coût doit être positif ou nul.')]
    private ?float $cout = null;

    #[ORM\ManyToOne(targetEntity: Produit::class, inversedBy: 'materiels')]
    #[ORM\JoinColumn(name: 'idProduit', referencedColumnName: 'idProduit', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: 'Le produit lié est requis.')]
    private ?Produit $produit = null;

    public function getIdMateriel(): ?int
    {
        return $this->idMateriel;
    }

    public function getNom(): string
    {
        return $this->nom;
    }

    public function setNom(?string $nom): self
    {
        $this->nom = trim((string) $nom);

        return $this;
    }

    public function getEtat(): string
    {
        return $this->etat;
    }

    public function setEtat(?string $etat): self
    {
        $this->etat = trim((string) $etat);

        return $this;
    }

    public function getDateAchat(): ?\DateTimeImmutable
    {
        return $this->dateAchat;
    }

    public function setDateAchat(?\DateTimeImmutable $dateAchat): self
    {
        $this->dateAchat = $dateAchat;

        return $this;
    }

    public function getCout(): ?float
    {
        return $this->cout;
    }

    public function setCout(?float $cout): self
    {
        $this->cout = $cout;

        return $this;
    }

    public function getProduit(): ?Produit
    {
        return $this->produit;
    }

    public function setProduit(?Produit $produit): self
    {
        $this->produit = $produit;

        return $this;
    }

    public function getDateAchatLabel(): string
    {
        return $this->dateAchat?->format('d/m/Y') ?? 'Non renseignée';
    }
}
