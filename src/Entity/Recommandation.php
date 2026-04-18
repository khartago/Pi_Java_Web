<?php

namespace App\Entity;

use App\Repository\RecommandationRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: RecommandationRepository::class)]
#[ORM\Table(name: 'recommandation')]
#[ORM\UniqueConstraint(name: 'uniq_produit_materiel', columns: ['idProduit', 'idMateriel'])]
class Recommandation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idRecommandation', type: 'integer')]
    private ?int $idRecommandation = null;

    #[ORM\ManyToOne(targetEntity: Produit::class)]
    #[ORM\JoinColumn(name: 'idProduit', referencedColumnName: 'idProduit', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: 'Le produit est requis.')]
    private ?Produit $produit = null;

    #[ORM\ManyToOne(targetEntity: Materiel::class)]
    #[ORM\JoinColumn(name: 'idMateriel', referencedColumnName: 'idMateriel', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: 'Le matériel est requis.')]
    private ?Materiel $materiel = null;

    #[ORM\Column(name: 'priorite', type: 'integer')]
    #[Assert\Range(min: 1, max: 5, notInRangeMessage: 'La priorité doit être entre 1 et 5.')]
    private int $priorite = 3;

    #[ORM\Column(name: 'raison', type: 'text', nullable: true)]
    #[Assert\Length(max: 500)]
    private ?string $raison = null;

    #[ORM\Column(name: 'actif', type: 'boolean')]
    private bool $actif = true;

    public function getIdRecommandation(): ?int { return $this->idRecommandation; }

    public function getProduit(): ?Produit { return $this->produit; }
    public function setProduit(?Produit $p): self { $this->produit = $p; return $this; }

    public function getMateriel(): ?Materiel { return $this->materiel; }
    public function setMateriel(?Materiel $m): self { $this->materiel = $m; return $this; }

    public function getPriorite(): int { return $this->priorite; }
    public function setPriorite(int $p): self { $this->priorite = $p; return $this; }

    public function getRaison(): ?string { return $this->raison; }
    public function setRaison(?string $r): self { $this->raison = $r; return $this; }

    public function isActif(): bool { return $this->actif; }
    public function setActif(bool $a): self { $this->actif = $a; return $this; }

    public function getPrioriteLabel(): string
    {
        return match ($this->priorite) {
            1 => 'Optionnel',
            2 => 'Utile',
            3 => 'Recommandé',
            4 => 'Important',
            5 => 'Essentiel',
            default => 'Recommandé',
        };
    }
}
