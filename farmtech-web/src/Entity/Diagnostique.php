<?php

namespace App\Entity;

use App\Repository\DiagnostiqueRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: DiagnostiqueRepository::class)]
#[ORM\Table(name: 'diagnostique')]
class Diagnostique
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'diagnostiques')]
    #[ORM\JoinColumn(name: 'id_probleme', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    private ?Probleme $probleme = null;

    #[ORM\Column(type: Types::TEXT)]
    private ?string $cause = null;

    #[ORM\Column(name: 'solution_proposee', type: Types::TEXT)]
    private ?string $solutionProposee = null;

    #[ORM\Column(name: 'date_diagnostique', type: Types::DATETIME_IMMUTABLE)]
    private ?\DateTimeInterface $dateDiagnostique = null;

    #[ORM\Column(length: 100)]
    private ?string $resultat = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $medicament = null;

    #[ORM\Column(options: ['default' => false])]
    private bool $approuve = false;

    #[ORM\Column(name: 'num_revision', options: ['default' => 1])]
    private int $numRevision = 1;

    #[ORM\Column(name: 'feedback_fermier', length: 20, nullable: true)]
    private ?string $feedbackFermier = null;

    #[ORM\Column(name: 'feedback_commentaire', type: Types::TEXT, nullable: true)]
    private ?string $feedbackCommentaire = null;

    #[ORM\Column(name: 'date_feedback', type: Types::DATETIME_IMMUTABLE, nullable: true)]
    private ?\DateTimeInterface $dateFeedback = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'id_admin_diagnostiqueur', referencedColumnName: 'id', nullable: true, onDelete: 'SET NULL')]
    private ?Utilisateur $adminDiagnostiqueur = null;

    public function __construct()
    {
        $this->dateDiagnostique = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getProbleme(): ?Probleme
    {
        return $this->probleme;
    }

    public function setProbleme(?Probleme $probleme): static
    {
        $this->probleme = $probleme;

        return $this;
    }

    public function getCause(): ?string
    {
        return $this->cause;
    }

    public function setCause(?string $cause): static
    {
        $this->cause = $cause;

        return $this;
    }

    public function getSolutionProposee(): ?string
    {
        return $this->solutionProposee;
    }

    public function setSolutionProposee(?string $solutionProposee): static
    {
        $this->solutionProposee = $solutionProposee;

        return $this;
    }

    public function getDateDiagnostique(): ?\DateTimeInterface
    {
        return $this->dateDiagnostique;
    }

    public function setDateDiagnostique(?\DateTimeInterface $dateDiagnostique): static
    {
        $this->dateDiagnostique = $dateDiagnostique;

        return $this;
    }

    public function getResultat(): ?string
    {
        return $this->resultat;
    }

    public function setResultat(?string $resultat): static
    {
        $this->resultat = $resultat;

        return $this;
    }

    public function getMedicament(): ?string
    {
        return $this->medicament;
    }

    public function setMedicament(?string $medicament): static
    {
        $this->medicament = $medicament;

        return $this;
    }

    public function isApprouve(): bool
    {
        return $this->approuve;
    }

    public function setApprouve(bool $approuve): static
    {
        $this->approuve = $approuve;

        return $this;
    }

    public function getNumRevision(): int
    {
        return $this->numRevision;
    }

    public function setNumRevision(int $numRevision): static
    {
        $this->numRevision = $numRevision;

        return $this;
    }

    public function getFeedbackFermier(): ?string
    {
        return $this->feedbackFermier;
    }

    public function setFeedbackFermier(?string $feedbackFermier): static
    {
        $this->feedbackFermier = $feedbackFermier;

        return $this;
    }

    public function getFeedbackCommentaire(): ?string
    {
        return $this->feedbackCommentaire;
    }

    public function setFeedbackCommentaire(?string $feedbackCommentaire): static
    {
        $this->feedbackCommentaire = $feedbackCommentaire;

        return $this;
    }

    public function getDateFeedback(): ?\DateTimeInterface
    {
        return $this->dateFeedback;
    }

    public function setDateFeedback(?\DateTimeInterface $dateFeedback): static
    {
        $this->dateFeedback = $dateFeedback;

        return $this;
    }

    public function getAdminDiagnostiqueur(): ?Utilisateur
    {
        return $this->adminDiagnostiqueur;
    }

    public function setAdminDiagnostiqueur(?Utilisateur $adminDiagnostiqueur): static
    {
        $this->adminDiagnostiqueur = $adminDiagnostiqueur;

        return $this;
    }
}
