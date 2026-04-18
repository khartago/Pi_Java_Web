<?php

namespace App\Entity;

use App\Repository\ProduitRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ProduitRepository::class)]
#[ORM\Table(name: 'produit')]
class Produit
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idProduit', type: 'integer')]
    private ?int $idProduit = null;

    #[ORM\Column(name: 'nom', length: 100)]
    #[Assert\NotBlank(message: 'Le nom du produit est requis.')]
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

    #[ORM\Column(name: 'quantite', type: 'integer')]
    #[Assert\NotNull(message: 'La quantité est requise.')]
    #[Assert\PositiveOrZero(message: 'La quantité doit être positive ou nulle.')]
    private ?int $quantite = null;

    #[ORM\Column(name: 'unite', length: 50)]
    #[Assert\NotBlank(message: 'L’unité est requise.')]
    #[Assert\Choice(
        choices: ['kg', 'l', 'piece'],
        message: 'L’unité doit être : kg, l ou piece.'
    )]
    private string $unite = '';

    #[ORM\Column(name: 'dateExpiration', type: 'date_immutable', nullable: true)]
    private ?\DateTimeImmutable $dateExpiration = null;

    #[ORM\Column(name: 'imagePath', length: 255, nullable: true)]
    private ?string $imagePath = null;

    #[ORM\Column(name: 'prix', type: 'float', nullable: true)]
    private ?float $prix = null;

    /**
     * @var Collection<int, Materiel>
     */
    #[ORM\OneToMany(mappedBy: 'produit', targetEntity: Materiel::class, orphanRemoval: true)]
    #[ORM\OrderBy(['nom' => 'ASC'])]
    private Collection $materiels;

    /**
     * @var Collection<int, Promotion>
     */
    #[ORM\ManyToMany(targetEntity: Promotion::class, mappedBy: 'produits')]
    private Collection $promotions;

    public function __construct()
    {
        $this->materiels = new ArrayCollection();
        $this->promotions = new ArrayCollection();
    }

    public function getIdProduit(): ?int
    {
        return $this->idProduit;
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

    public function getQuantite(): ?int
    {
        return $this->quantite;
    }

    public function setQuantite(?int $quantite): self
    {
        $this->quantite = $quantite;

        return $this;
    }

    public function getUnite(): string
    {
        return $this->unite;
    }

    public function setUnite(?string $unite): self
    {
        $this->unite = trim((string) $unite);

        return $this;
    }

    public function getDateExpiration(): ?\DateTimeImmutable
    {
        return $this->dateExpiration;
    }

    public function setDateExpiration(?\DateTimeImmutable $dateExpiration): self
    {
        $this->dateExpiration = $dateExpiration;

        return $this;
    }

    public function getImagePath(): ?string
    {
        return $this->imagePath;
    }

    public function setImagePath(?string $imagePath): self
    {
        $this->imagePath = $imagePath;

        return $this;
    }

    public function getPrix(): ?float
    {
        return $this->prix;
    }

    public function setPrix(?float $prix): self
    {
        $this->prix = $prix;

        return $this;
    }

    /**
     * @return Collection<int, Promotion>
     */
    public function getPromotions(): Collection
    {
        return $this->promotions;
    }

    public function addPromotion(Promotion $promotion): self
    {
        if (!$this->promotions->contains($promotion)) {
            $this->promotions->add($promotion);
            $promotion->addProduit($this);
        }

        return $this;
    }

    public function removePromotion(Promotion $promotion): self
    {
        if ($this->promotions->removeElement($promotion)) {
            $promotion->removeProduit($this);
        }

        return $this;
    }

    /**
     * @return Collection<int, Materiel>
     */
    public function getMateriels(): Collection
    {
        return $this->materiels;
    }

    public function addMateriel(Materiel $materiel): self
    {
        if (!$this->materiels->contains($materiel)) {
            $this->materiels->add($materiel);
            $materiel->setProduit($this);
        }

        return $this;
    }

    public function removeMateriel(Materiel $materiel): self
    {
        if ($this->materiels->removeElement($materiel) && $materiel->getProduit() === $this) {
            $materiel->setProduit(null);
        }

        return $this;
    }

    public function getDateExpirationLabel(): string
    {
        return $this->dateExpiration?->format('d/m/Y') ?? 'Non renseignée';
    }

    public function getImageAssetPath(): string
    {
        return $this->imagePath ?: 'images/product-placeholder.svg';
    }
}
