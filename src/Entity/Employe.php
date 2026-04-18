<?php

namespace App\Entity;

use App\Repository\EmployeRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: EmployeRepository::class)]
#[ORM\Table(name: 'employe')]
class Employe
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idEmploye', type: 'integer')]
    private ?int $idEmploye = null;

    #[ORM\Column(name: 'nom', length: 50)]
    #[Assert\NotBlank(message: 'Le nom est requis.')]
    #[Assert\Length(min: 2, max: 50, minMessage: 'Le nom doit contenir au moins {{ limit }} caractères.', maxMessage: 'Le nom doit contenir au maximum {{ limit }} caractères.')]
    private string $nom = '';

    #[ORM\Column(name: 'prenom', length: 50)]
    #[Assert\NotBlank(message: 'Le prénom est requis.')]
    #[Assert\Length(min: 2, max: 50, minMessage: 'Le prénom doit contenir au moins {{ limit }} caractères.', maxMessage: 'Le prénom doit contenir au maximum {{ limit }} caractères.')]
    private string $prenom = '';

    #[ORM\Column(name: 'poste', length: 50)]
    #[Assert\NotBlank(message: 'Le poste est requis.')]
    private string $poste = '';

    #[ORM\Column(name: 'email', length: 100, nullable: true)]
    #[Assert\Email(message: 'L\'adresse email n\'est pas valide.')]
    private ?string $email = null;

    public function getIdEmploye(): ?int
    {
        return $this->idEmploye;
    }

    public function getNom(): string
    {
        return $this->nom;
    }

    public function setNom(string $nom): self
    {
        $this->nom = trim($nom);

        return $this;
    }

    public function getPrenom(): string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): self
    {
        $this->prenom = trim($prenom);

        return $this;
    }

    public function getPoste(): string
    {
        return $this->poste;
    }

    public function setPoste(string $poste): self
    {
        $this->poste = trim($poste);

        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(?string $email): self
    {
        $this->email = $email !== null ? trim($email) : null;

        return $this;
    }

    public function getFullName(): string
    {
        return $this->prenom . ' ' . $this->nom;
    }
}
