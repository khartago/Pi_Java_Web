<?php

namespace App\Entity;

use App\Repository\AffectationRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: AffectationRepository::class)]
#[ORM\Table(name: 'affectation')]
class Affectation
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idAffectation', type: 'integer')]
    private ?int $idAffectation = null;

    #[ORM\ManyToOne(targetEntity: Materiel::class, inversedBy: 'affectations')]
    #[ORM\JoinColumn(name: 'idMateriel', referencedColumnName: 'idMateriel', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: 'Le matériel est requis.')]
    private ?Materiel $materiel = null;

    #[ORM\ManyToOne(targetEntity: Employe::class)]
    #[ORM\JoinColumn(name: 'idEmploye', referencedColumnName: 'idEmploye', nullable: false, onDelete: 'CASCADE')]
    #[Assert\NotNull(message: 'L\'employé est requis.')]
    private ?Employe $employe = null;

    #[ORM\Column(name: 'dateAffectation', type: 'date_immutable')]
    private \DateTimeImmutable $dateAffectation;

    #[ORM\Column(name: 'dateRetour', type: 'date_immutable', nullable: true)]
    private ?\DateTimeImmutable $dateRetour = null;

    #[ORM\Column(name: 'note', type: 'text', nullable: true)]
    private ?string $note = null;

    public function __construct()
    {
        $this->dateAffectation = new \DateTimeImmutable();
    }

    public function isActive(): bool
    {
        return $this->dateRetour === null;
    }

    public function getIdAffectation(): ?int
    {
        return $this->idAffectation;
    }

    public function getMateriel(): ?Materiel
    {
        return $this->materiel;
    }

    public function setMateriel(?Materiel $materiel): self
    {
        $this->materiel = $materiel;

        return $this;
    }

    public function getEmploye(): ?Employe
    {
        return $this->employe;
    }

    public function setEmploye(?Employe $employe): self
    {
        $this->employe = $employe;

        return $this;
    }

    public function getDateAffectation(): \DateTimeImmutable
    {
        return $this->dateAffectation;
    }

    public function setDateAffectation(\DateTimeImmutable $dateAffectation): self
    {
        $this->dateAffectation = $dateAffectation;

        return $this;
    }

    public function getDateRetour(): ?\DateTimeImmutable
    {
        return $this->dateRetour;
    }

    public function setDateRetour(?\DateTimeImmutable $dateRetour): self
    {
        $this->dateRetour = $dateRetour;

        return $this;
    }

    public function getNote(): ?string
    {
        return $this->note;
    }

    public function setNote(?string $note): self
    {
        $this->note = $note;

        return $this;
    }

    public function getDuree(): ?string
    {
        $end = $this->dateRetour ?? new \DateTimeImmutable();
        $diff = $this->dateAffectation->diff($end);

        if ($diff->days === 0) {
            return 'Aujourd\'hui';
        }

        if ($diff->days < 7) {
            return $diff->days . ' jour' . ($diff->days > 1 ? 's' : '');
        }

        if ($diff->days < 30) {
            $weeks = (int) floor($diff->days / 7);

            return $weeks . ' semaine' . ($weeks > 1 ? 's' : '');
        }

        $months = (int) floor($diff->days / 30);

        return $months . ' mois';
    }
}
