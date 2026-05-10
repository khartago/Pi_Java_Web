<?php

namespace App\Entity;

use App\Repository\UtilisateurRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
#[ORM\Table(name: 'utilisateur')]
#[UniqueEntity(fields: ['email'], message: 'Cet email est déjà utilisé.')]
class Utilisateur implements UserInterface, PasswordAuthenticatedUserInterface
{
    public const ROLE_ADMIN_DB = 'ADMIN';
    public const ROLE_FARMER_DB = 'FARMER';

    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Le nom est obligatoire.')]
    #[Assert\Length(max: 100)]
    private ?string $nom = null;

    #[ORM\Column(length: 150, unique: true)]
    #[Assert\NotBlank(message: 'L\'email est obligatoire.')]
    #[Assert\Email(message: 'Email invalide.')]
    #[Assert\Length(max: 150)]
    private ?string $email = null;

    #[ORM\Column(name: 'mot_de_passe', length: 255)]
    #[Assert\Length(max: 255)]
    private ?string $motDePasse = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank]
    #[Assert\Choice(choices: [self::ROLE_ADMIN_DB, self::ROLE_FARMER_DB])]
    private string $role = self::ROLE_FARMER_DB;

    /** @var Collection<int, Probleme> */
    #[ORM\OneToMany(targetEntity: Probleme::class, mappedBy: 'utilisateur')]
    private Collection $problemes;

    public function __construct()
    {
        $this->problemes = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(?string $nom): static
    {
        $this->nom = $nom;

        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(?string $email): static
    {
        $this->email = $email;

        return $this;
    }

    public function getMotDePasse(): ?string
    {
        return $this->motDePasse;
    }

    public function setMotDePasse(?string $motDePasse): static
    {
        $this->motDePasse = $motDePasse;

        return $this;
    }

    public function getRole(): string
    {
        return $this->role;
    }

    public function setRole(string $role): static
    {
        $this->role = $role;

        return $this;
    }

    /** @return Collection<int, Probleme> */
    public function getProblemes(): Collection
    {
        return $this->problemes;
    }

    public function getUserIdentifier(): string
    {
        return (string) $this->email;
    }

    public function getRoles(): array
    {
        $r = match ($this->role) {
            self::ROLE_ADMIN_DB => 'ROLE_ADMIN',
            default => 'ROLE_FARMER',
        };

        return [$r, 'ROLE_USER'];
    }

    public function eraseCredentials(): void
    {
    }

    public function getPassword(): ?string
    {
        return $this->motDePasse;
    }

    public function __toString(): string
    {
        return $this->nom ?? $this->email ?? '';
    }
}
