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
    #[Assert\NotBlank]
    private string $nom = '';

    #[ORM\Column(name: 'prenom', length: 50)]
    #[Assert\NotBlank]
    private string $prenom = '';

    #[ORM\Column(name: 'poste', length: 50)]
    #[Assert\NotBlank]
    private string $poste = '';

    #[ORM\Column(name: 'email', length: 100, nullable: true)]
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
