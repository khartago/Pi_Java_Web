<?php

namespace App\Entity;

use App\Repository\BlogRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: BlogRepository::class)]
#[ORM\Table(name: 'blog')]
class Blog
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idBlog')]
    private ?int $idBlog = null;

    #[ORM\Column(name: 'TitleBlog', length: 255)]
    private ?string $titleBlog = null;

    #[ORM\Column(name: 'BlogTag', length: 255)]
    private ?string $blogTag = null;

    #[ORM\ManyToOne(inversedBy: 'blogs')]
    #[ORM\JoinColumn(name: 'idutilisateur', referencedColumnName: 'id', nullable: false)]
    private ?Utilisateur $utilisateur = null; // <-- MUST be $utilisateur

    #[ORM\Column(name: 'DateBlog', type: Types::DATETIME_IMMUTABLE)]
    private ?\DateTimeInterface $dateBlog = null;



    public function __construct()
    {
        $this->articles = new ArrayCollection();
        $this->dateBlog = new \DateTimeImmutable();
    }

    public function getIdBlog(): ?int
    {
        return $this->idBlog;
    }

    public function getTitleBlog(): ?string
    {
        return $this->titleBlog;
    }

    public function setTitleBlog(?string $titleBlog): static
    {
        $this->titleBlog = $titleBlog;

        return $this;
    }

    public function getBlogTag(): ?string
    {
        return $this->blogTag;
    }

    public function setBlogTag(?string $blogTag): static
    {
        $this->blogTag = $blogTag;

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

    public function getDateBlog(): ?\DateTimeInterface
    {
        return $this->dateBlog;
    }


}