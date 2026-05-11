<?php

namespace App\Entity;

use App\Repository\ProduitHistoriqueRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

/**
 * Historique des événements stock (quantités) — aligné avec la table utilisée par JavaFX.
 */
#[ORM\Entity(repositoryClass: ProduitHistoriqueRepository::class)]
#[ORM\Table(name: 'produit_historique')]
class ProduitHistorique
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idHistorique')]
    private ?int $idHistorique = null;

    #[ORM\ManyToOne(targetEntity: Produit::class)]
    #[ORM\JoinColumn(name: 'idProduit', referencedColumnName: 'idProduit', nullable: false, onDelete: 'CASCADE')]
    private ?Produit $produit = null;

    #[ORM\Column(name: 'typeEvenement', length: 50)]
    private string $typeEvenement = '';

    #[ORM\Column(name: 'quantiteAvant', nullable: true)]
    private ?int $quantiteAvant = null;

    #[ORM\Column(name: 'quantiteApres', nullable: true)]
    private ?int $quantiteApres = null;

    #[ORM\Column(name: 'dateEvenement', type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $dateEvenement;

    #[ORM\Column(length: 500, nullable: true)]
    private ?string $commentaire = null;

    public function __construct()
    {
        $this->dateEvenement = new \DateTimeImmutable();
    }

    public function getIdHistorique(): ?int
    {
        return $this->idHistorique;
    }

    public function getProduit(): ?Produit
    {
        return $this->produit;
    }

    public function setProduit(?Produit $produit): static
    {
        $this->produit = $produit;

        return $this;
    }

    public function getTypeEvenement(): string
    {
        return $this->typeEvenement;
    }

    public function setTypeEvenement(string $typeEvenement): static
    {
        $this->typeEvenement = $typeEvenement;

        return $this;
    }

    public function getQuantiteAvant(): ?int
    {
        return $this->quantiteAvant;
    }

    public function setQuantiteAvant(?int $quantiteAvant): static
    {
        $this->quantiteAvant = $quantiteAvant;

        return $this;
    }

    public function getQuantiteApres(): ?int
    {
        return $this->quantiteApres;
    }

    public function setQuantiteApres(?int $quantiteApres): static
    {
        $this->quantiteApres = $quantiteApres;

        return $this;
    }

    public function getDateEvenement(): \DateTimeImmutable
    {
        return $this->dateEvenement;
    }

    public function setDateEvenement(\DateTimeImmutable $dateEvenement): static
    {
        $this->dateEvenement = $dateEvenement;

        return $this;
    }

    public function getCommentaire(): ?string
    {
        return $this->commentaire;
    }

    public function setCommentaire(?string $commentaire): static
    {
        $this->commentaire = $commentaire;

        return $this;
    }
}
