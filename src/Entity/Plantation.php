<?php

namespace App\Entity;

use App\Repository\PlantationRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: PlantationRepository::class)]
class Plantation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    // 🌿 NOM (max 12, letters only)
    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: "Nom obligatoire")]
    #[Assert\Length(max: 12, maxMessage: "Max 12 caractères")]
    #[Assert\Regex(
        pattern: "/^[a-zA-Z]+$/",
        message: "Seulement des lettres (pas de chiffres ni symboles)"
    )]
    private ?string $nomPlant = null;

    // 🌱 VARIETE (max 8, format: abc-5)
    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: "Variété obligatoire")]
    #[Assert\Length(max: 8, maxMessage: "Max 8 caractères")]
    #[Assert\Regex(
        pattern: "/^[a-zA-Z]+-[0-9]+$/",
        message: "Format: lettres-nombre (ex: abc-5)"
    )]
    private ?string $variete = null;

    // 📦 QUANTITE (positive only)
    #[ORM\Column]
    #[Assert\NotBlank(message: "Quantité obligatoire")]
    #[Assert\Positive(message: "Quantité doit être positive")]
    private ?int $quantite = null;

    // 📅 DATE
    #[ORM\Column(type: 'date')]
    #[Assert\NotNull(message: "Date obligatoire")]
    private ?\DateTimeInterface $datePlante = null;

    // 🍂 SAISON (letters only)
    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: "Saison obligatoire")]
    #[Assert\Length(max: 12, maxMessage: "Max 8 caractères")]
    #[Assert\Regex(
        pattern: "/^[a-zA-Z]+$/",
        message: "Seulement des lettres"
    )]
    private ?string $saison = null;

    #[ORM\Column(length: 50)]
    private ?string $etat = 'EN_ATTENTE';

    // 🔗 RELATION
    #[ORM\OneToMany(mappedBy: 'plantation', targetEntity: Production::class, cascade: ['remove'])]
    private Collection $productions;

    public function __construct()
    {
        $this->productions = new ArrayCollection();
    }

    // GETTERS & SETTERS

    public function getId(): ?int { return $this->id; }

    public function getNomPlant(): ?string { return $this->nomPlant; }
    public function setNomPlant(string $nomPlant): self { $this->nomPlant = $nomPlant; return $this; }

    public function getVariete(): ?string { return $this->variete; }
    public function setVariete(string $variete): self { $this->variete = $variete; return $this; }

    public function getQuantite(): ?int { return $this->quantite; }
    public function setQuantite(int $quantite): self { $this->quantite = $quantite; return $this; }

    public function getDatePlante(): ?\DateTimeInterface { return $this->datePlante; }
    public function setDatePlante(\DateTimeInterface $datePlante): self { $this->datePlante = $datePlante; return $this; }

    public function getSaison(): ?string { return $this->saison; }
    public function setSaison(string $saison): self { $this->saison = $saison; return $this; }

    public function getEtat(): ?string { return $this->etat; }
    public function setEtat(string $etat): self { $this->etat = $etat; return $this; }

    public function getProductions(): Collection
    {
        return $this->productions;
    }
}