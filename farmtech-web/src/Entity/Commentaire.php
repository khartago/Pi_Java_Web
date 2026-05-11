<?php

namespace App\Entity;

use App\Repository\CommentaireRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CommentaireRepository::class)]
#[ORM\Table(name: 'commentaire')]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idComment')]
    private ?int $id = null;

    #[ORM\Column(name: 'texte' ,type: Types::TEXT)]
    private string $contenu = '';

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'idArticle', referencedColumnName: 'ArticleID', nullable: false)]
    private ?Article $article = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'idUser', referencedColumnName: 'id', nullable: false)]
    private ?Utilisateur $utilisateur = null;

    #[ORM\Column(name: 'datecomment', type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $dateCommentaire;

    public function __construct()
    {
        $this->dateCommentaire = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getContenu(): string
    {
        return $this->contenu;
    }

    public function setContenu(string $contenu): static
    {
        $this->contenu = $contenu;

        return $this;
    }

    public function getArticle(): ?Article
    {
        return $this->article;
    }

    public function setArticle(?Article $article): static
    {
        $this->article = $article;

        return $this;
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

    public function getDateCommentaire(): \DateTimeImmutable
    {
        return $this->dateCommentaire;
    }

    public function setDateCommentaire(\DateTimeImmutable $dateCommentaire): static
    {
        $this->dateCommentaire = $dateCommentaire;

        return $this;
    }
}