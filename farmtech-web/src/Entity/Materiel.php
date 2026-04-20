<?php

namespace App\Entity;

use App\Repository\MaterielRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
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
    #[Assert\NotBlank]
    private string $nom = '';

    #[ORM\Column(name: 'etat', length: 50)]
    #[Assert\NotBlank]
    private string $etat = '';

    #[ORM\Column(name: 'dateAchat', type: 'date_immutable')]
    private ?\DateTimeImmutable $dateAchat = null;

    #[ORM\Column(name: 'cout', type: 'float')]
    #[Assert\NotNull]
    #[Assert\PositiveOrZero]
    private ?float $cout = null;

    #[ORM\ManyToOne(targetEntity: Produit::class, inversedBy: 'materiels')]
    #[ORM\JoinColumn(name: 'idProduit', referencedColumnName: 'idProduit', nullable: false, onDelete: 'CASCADE')]
    private ?Produit $produit = null;

    /**
     * @var Collection<int, Affectation>
     */
    #[ORM\OneToMany(mappedBy: 'materiel', targetEntity: Affectation::class, orphanRemoval: true)]
    private Collection $affectations;

    public function __construct()
    {
        $this->affectations = new ArrayCollection();
    }

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
        return $this->dateAchat?->format('d/m/Y') ?? 'Non renseignee';
    }

    /**
     * @return Collection<int, Affectation>
     */
    public function getAffectations(): Collection
    {
        return $this->affectations;
    }

    public function getAffectationActive(): ?Affectation
    {
        foreach ($this->affectations as $affectation) {
            if ($affectation->isActive()) {
                return $affectation;
            }
        }

        return null;
    }
}
