<?php

namespace App\Entity;

use App\Repository\ProblemeRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ProblemeRepository::class)]
#[ORM\Table(name: 'probleme')]
class Probleme
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'problemes')]
    #[ORM\JoinColumn(name: 'id_utilisateur', referencedColumnName: 'id', nullable: true, onDelete: 'SET NULL')]
    private ?Utilisateur $utilisateur = null;

    #[ORM\Column(length: 100)]
    private ?string $type = null;

    #[ORM\Column(type: Types::TEXT)]
    private ?string $description = null;

    #[ORM\Column(length: 50)]
    private ?string $gravite = null;

    #[ORM\Column(name: 'date_detection', type: Types::DATETIME_IMMUTABLE)]
    private ?\DateTimeInterface $dateDetection = null;

    #[ORM\Column(length: 50)]
    private ?string $etat = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $photos = null;

    #[ORM\Column(name: 'id_plantation', nullable: true)]
    private ?int $idPlantation = null;

    #[ORM\Column(name: 'id_produit', nullable: true)]
    private ?int $idProduit = null;

    #[ORM\Column(name: 'meteo_snapshot', type: Types::TEXT, nullable: true)]
    private ?string $meteoSnapshot = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'id_admin_assignee', referencedColumnName: 'id', nullable: true, onDelete: 'SET NULL')]
    private ?Utilisateur $adminAssignee = null;

    /** @var Collection<int, Diagnostique> */
    #[ORM\OneToMany(targetEntity: Diagnostique::class, mappedBy: 'probleme', cascade: ['persist', 'remove'], orphanRemoval: false)]
    private Collection $diagnostiques;

    public function __construct()
    {
        $this->diagnostiques = new ArrayCollection();
        $this->dateDetection = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUtilisateur(): ?Utilisateur
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(?Utilisateur $utilisateur): static
    {
        $this->utilisateur = $utilisateur;

        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(?string $type): static
    {
        $this->type = $type;

        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): static
    {
        $this->description = $description;

        return $this;
    }

    public function getGravite(): ?string
    {
        return $this->gravite;
    }

    public function setGravite(?string $gravite): static
    {
        $this->gravite = $gravite;

        return $this;
    }

    public function getDateDetection(): ?\DateTimeInterface
    {
        return $this->dateDetection;
    }

    public function setDateDetection(?\DateTimeInterface $dateDetection): static
    {
        $this->dateDetection = $dateDetection;

        return $this;
    }

    public function getEtat(): ?string
    {
        return $this->etat;
    }

    public function setEtat(?string $etat): static
    {
        $this->etat = $etat;

        return $this;
    }

    public function getPhotos(): ?string
    {
        return $this->photos;
    }

    public function setPhotos(?string $photos): static
    {
        $this->photos = $photos;

        return $this;
    }

    /** Premier chemin photo (segments `;`) pour vignettes liste. */
    public function getFirstPhotoPath(): ?string
    {
        if ($this->photos === null || trim($this->photos) === '') {
            return null;
        }
        foreach (explode(';', $this->photos) as $seg) {
            $s = trim($seg);
            if ($s !== '') {
                return $s;
            }
        }

        return null;
    }

    public function getIdPlantation(): ?int
    {
        return $this->idPlantation;
    }

    public function setIdPlantation(?int $idPlantation): static
    {
        $this->idPlantation = $idPlantation;

        return $this;
    }

    public function getIdProduit(): ?int
    {
        return $this->idProduit;
    }

    public function setIdProduit(?int $idProduit): static
    {
        $this->idProduit = $idProduit;

        return $this;
    }

    public function getMeteoSnapshot(): ?string
    {
        return $this->meteoSnapshot;
    }

    public function setMeteoSnapshot(?string $meteoSnapshot): static
    {
        $this->meteoSnapshot = $meteoSnapshot;

        return $this;
    }

    public function getAdminAssignee(): ?Utilisateur
    {
        return $this->adminAssignee;
    }

    public function setAdminAssignee(?Utilisateur $adminAssignee): static
    {
        $this->adminAssignee = $adminAssignee;

        return $this;
    }

    /** @return Collection<int, Diagnostique> */
    public function getDiagnostiques(): Collection
    {
        return $this->diagnostiques;
    }

    public function addDiagnostique(Diagnostique $d): static
    {
        if (!$this->diagnostiques->contains($d)) {
            $this->diagnostiques->add($d);
            $d->setProbleme($this);
        }

        return $this;
    }

    public function removeDiagnostique(Diagnostique $d): static
    {
        if ($this->diagnostiques->removeElement($d) && $d->getProbleme() === $this) {
            $d->setProbleme(null);
        }

        return $this;
    }
}
