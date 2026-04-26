<?php

namespace App\Entity;

use App\Repository\PromotionRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: PromotionRepository::class)]
#[ORM\Table(name: 'promotion')]
class Promotion
{
    public const TYPE_POURCENTAGE = 'pourcentage';
    public const TYPE_MONTANT_FIXE = 'montant_fixe';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idPromotion', type: 'integer')]
    private ?int $idPromotion = null;

    #[ORM\Column(name: 'nom', length: 100)]
    #[Assert\NotBlank]
    private string $nom = '';

    #[ORM\Column(name: 'description', type: 'text', nullable: true)]
    private ?string $description = null;

    #[ORM\Column(name: 'typeReduction', length: 20)]
    private string $typeReduction = self::TYPE_POURCENTAGE;

    #[ORM\Column(name: 'valeurReduction', type: 'float')]
    #[Assert\Positive]
    private float $valeurReduction = 0.0;

    #[ORM\Column(name: 'dateDebut', type: 'date_immutable')]
    private ?\DateTimeImmutable $dateDebut = null;

    #[ORM\Column(name: 'dateFin', type: 'date_immutable')]
    private ?\DateTimeImmutable $dateFin = null;

    #[ORM\Column(name: 'quantiteMin', type: 'integer', options: ['default' => 1])]
    private int $quantiteMin = 1;

    #[ORM\Column(name: 'cumulable', type: 'boolean', options: ['default' => false])]
    private bool $cumulable = false;

    #[ORM\Column(name: 'actif', type: 'boolean', options: ['default' => true])]
    private bool $actif = true;

    /**
     * @var Collection<int, Produit>
     */
    #[ORM\ManyToMany(targetEntity: Produit::class, inversedBy: 'promotions')]
    #[ORM\JoinTable(name: 'promotion_produit')]
    #[ORM\JoinColumn(name: 'promotion_id', referencedColumnName: 'idPromotion', onDelete: 'CASCADE')]
    #[ORM\InverseJoinColumn(name: 'produit_id', referencedColumnName: 'idProduit', onDelete: 'CASCADE')]
    private Collection $produits;

    public function __construct()
    {
        $this->produits = new ArrayCollection();
        $today = new \DateTimeImmutable('today');
        $this->dateDebut = $today;
        $this->dateFin = $today->modify('+7 days');
    }

    public function getIdPromotion(): ?int { return $this->idPromotion; }
    public function getNom(): string { return $this->nom; }
    public function setNom(?string $nom): self { $this->nom = trim((string)$nom); return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): self { $this->description = $description; return $this; }
    public function getTypeReduction(): string { return $this->typeReduction; }
    public function setTypeReduction(string $typeReduction): self { $this->typeReduction = $typeReduction; return $this; }
    public function getValeurReduction(): float { return $this->valeurReduction; }
    public function setValeurReduction(float $valeurReduction): self { $this->valeurReduction = $valeurReduction; return $this; }
    public function getDateDebut(): ?\DateTimeImmutable { return $this->dateDebut; }
    public function setDateDebut(?\DateTimeImmutable $dateDebut): self { $this->dateDebut = $dateDebut; return $this; }
    public function getDateFin(): ?\DateTimeImmutable { return $this->dateFin; }
    public function setDateFin(?\DateTimeImmutable $dateFin): self { $this->dateFin = $dateFin; return $this; }
    public function getQuantiteMin(): int { return $this->quantiteMin; }
    public function setQuantiteMin(int $quantiteMin): self { $this->quantiteMin = $quantiteMin; return $this; }
    public function isCumulable(): bool { return $this->cumulable; }
    public function setCumulable(bool $cumulable): self { $this->cumulable = $cumulable; return $this; }
    public function isActif(): bool { return $this->actif; }
    public function setActif(bool $actif): self { $this->actif = $actif; return $this; }
    public function getProduits(): Collection { return $this->produits; }
    public function addProduit(Produit $produit): self
    {
        if (!$this->produits->contains($produit)) {
            $this->produits->add($produit);
            if (!$produit->getPromotions()->contains($this)) {
                $produit->getPromotions()->add($this);
            }
        }

        return $this;
    }

    public function removeProduit(Produit $produit): self
    {
        if ($this->produits->removeElement($produit)) {
            $produit->getPromotions()->removeElement($this);
        }

        return $this;
    }

    public function isCurrentlyActive(): bool
    {
        if (!$this->actif || $this->dateDebut === null || $this->dateFin === null) {
            return false;
        }
        $today = new \DateTimeImmutable('today');
        return $today >= $this->dateDebut && $today <= $this->dateFin;
    }

    public function isUpcoming(): bool
    {
        return $this->actif && $this->dateDebut !== null && new \DateTimeImmutable('today') < $this->dateDebut;
    }

    public function isExpired(): bool
    {
        return $this->dateFin !== null && new \DateTimeImmutable('today') > $this->dateFin;
    }

    public function getLabel(): string
    {
        if ($this->typeReduction === self::TYPE_POURCENTAGE) {
            return '-' . rtrim(rtrim(number_format($this->valeurReduction, 2, ',', ''), '0'), ',') . '%';
        }

        return '-' . number_format($this->valeurReduction, 2, ',', ' ') . ' EUR';
    }

    public function applyTo(float $unitPrice, int $quantity): float
    {
        if ($quantity < $this->quantiteMin) {
            return $unitPrice;
        }
        if ($this->typeReduction === self::TYPE_POURCENTAGE) {
            return max(0.0, $unitPrice * (1 - ($this->valeurReduction / 100)));
        }

        return max(0.0, $unitPrice - $this->valeurReduction);
    }
}
