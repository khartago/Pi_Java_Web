<?php

namespace App\Entity;

use App\Repository\PlantationRepository;
use Doctrine\DBAL\Types\Types;
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
    private string $nomPlant = '';

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Variete obligatoire')]
    private string $variete = '';

    #[ORM\Column]
    #[Assert\NotBlank(message: 'Quantite obligatoire')]
    #[Assert\Positive(message: 'Quantite doit etre positive')]
    private int $quantite = 0;

    #[ORM\Column(name: 'datePlante', type: Types::DATE_MUTABLE)]
    #[Assert\NotNull(message: 'Date obligatoire')]
    private \DateTimeInterface $datePlante;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: 'Saison obligatoire')]
    private string $saison = '';

    #[ORM\Column(length: 50)]
    private string $etat = 'EN_ATTENTE';

    /** Colonnes optionnelles partagées avec le mini-jeu JavaFX (valeurs par défaut = schéma migration). */
    #[ORM\Column(options: ['default' => 1])]
    private int $stage = 1;

    #[ORM\Column(name: 'water_count', options: ['default' => 0])]
    private int $waterCount = 0;

    #[ORM\Column(name: 'last_water_time', type: Types::BIGINT, options: ['default' => 0])]
    private string $lastWaterTime = '0';

    #[ORM\Column(length: 50, options: ['default' => 'ALIVE'])]
    private string $status = 'ALIVE';

    #[ORM\Column(name: 'growth_speed', options: ['default' => '1'])]
    private float $growthSpeed = 1.0;

    #[ORM\Column(name: 'slot_index', options: ['default' => '0'])]
    private int $slotIndex = 0;

    public function __construct()
    {
        $this->datePlante = new \DateTimeImmutable('today');
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNomPlant(): string
    {
        return $this->nomPlant;
    }

    public function setNomPlant(string $nomPlant): self
    {
        $this->nomPlant = $nomPlant;
        return $this;
    }

    public function getVariete(): string
    {
        return $this->variete;
    }

    public function setVariete(string $variete): self
    {
        $this->variete = $variete;
        return $this;
    }

    public function getQuantite(): int
    {
        return $this->quantite;
    }

    public function setQuantite(int $quantite): self
    {
        $this->quantite = $quantite;
        return $this;
    }

    public function getDatePlante(): \DateTimeInterface
    {
        return $this->datePlante;
    }

    public function setDatePlante(\DateTimeInterface $datePlante): self
    {
        $this->datePlante = $datePlante;
        return $this;
    }

    public function getSaison(): string
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

    public function getStage(): int
    {
        return $this->stage;
    }

    public function setStage(int $stage): self
    {
        $this->stage = $stage;
        return $this;
    }

    public function getWaterCount(): int
    {
        return $this->waterCount;
    }

    public function setWaterCount(int $waterCount): self
    {
        $this->waterCount = $waterCount;
        return $this;
    }

    public function getLastWaterTime(): string
    {
        return $this->lastWaterTime;
    }

    public function setLastWaterTime(int|string $lastWaterTime): self
    {
        $this->lastWaterTime = (string) $lastWaterTime;

        return $this;
    }

    public function getStatus(): string
    {
        return $this->status;
    }

    public function setStatus(string $status): self
    {
        $this->status = $status;
        return $this;
    }

    public function getGrowthSpeed(): float
    {
        return $this->growthSpeed;
    }

    public function setGrowthSpeed(float $growthSpeed): self
    {
        $this->growthSpeed = $growthSpeed;
        return $this;
    }

    public function getSlotIndex(): int
    {
        return $this->slotIndex;
    }

    public function setSlotIndex(int $slotIndex): self
    {
        $this->slotIndex = $slotIndex;
        return $this;
    }
}

