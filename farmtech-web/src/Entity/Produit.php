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
    #[Assert\NotBlank]
    private string $nom = '';

    #[ORM\Column(name: 'quantite', type: 'integer')]
    #[Assert\NotNull]
    #[Assert\PositiveOrZero]
    private ?int $quantite = null;

    #[ORM\Column(name: 'unite', length: 50)]
    #[Assert\NotBlank]
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
     * @return Collection<int, Materiel>
     */
    public function getMateriels(): Collection
    {
        return $this->materiels;
    }

    /**
     * @return Collection<int, Promotion>
     */
    public function getPromotions(): Collection
    {
        return $this->promotions;
    }

    public function getDateExpirationLabel(): string
    {
        return $this->dateExpiration?->format('d/m/Y') ?? 'Non renseignee';
    }

    public function getImageAssetPath(): string
    {
        $path = trim((string) $this->imagePath);
        if ($path === '') {
            return 'images/product-placeholder.svg';
        }

        $normalized = str_replace('\\', '/', $path);
        if (str_starts_with($normalized, 'http://') || str_starts_with($normalized, 'https://')) {
            return $normalized;
        }
        if (str_starts_with($normalized, '/')) {
            return ltrim($normalized, '/');
        }
        if (str_starts_with($normalized, 'uploads/')) {
            return $normalized;
        }

        return 'uploads/products/' . basename($normalized);
    }
}
