<?php

namespace App\Entity;

use App\Repository\ArticleRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ArticleRepository::class)]
#[ORM\Table(name: 'article')]
class Article
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'ArticleID', type: Types::INTEGER)]
    private ?int $id = null;

    #[ORM\Column(name:'Titre',length: 255)]
    private string $title = '';

    #[ORM\Column(name:'texte',type: Types::TEXT)]
    private string $text = '';

    #[ORM\Column(name:'Likes',nullable: true)]
    private ?int $likes = null;

    #[ORM\Column(name:'Dislikes',nullable: true)]
    private ?int $dislikes = null;

    #[ORM\Column]
    private bool $edited = false;

    #[ORM\Column(name: 'BlogID')]
    private int $blogId = 0;

    #[ORM\Column(name: 'CreationDate', type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $createdAt;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getTitle(): string
    {
        return $this->title;
    }

    public function setTitle(string $title): static
    {
        $this->title = $title;

        return $this;
    }

    public function getText(): string
    {
        return $this->text;
    }

    public function setText(string $text): static
    {
        $this->text = $text;

        return $this;
    }

    public function getLikes(): ?int
    {
        return $this->likes;
    }

    public function setLikes(?int $likes): static
    {
        $this->likes = $likes;

        return $this;
    }

    public function getDislikes(): ?int
    {
        return $this->dislikes;
    }

    public function setDislikes(?int $dislikes): static
    {
        $this->dislikes = $dislikes;

        return $this;
    }

    public function isEdited(): bool
    {
        return $this->edited;
    }

    public function setEdited(bool $edited): static
    {
        $this->edited = $edited;

        return $this;
    }

    public function getBlogId(): int
    {
        return $this->blogId;
    }

    public function setBlogId(int $blogId): static
    {
        $this->blogId = $blogId;

        return $this;
    }

    public function getCreatedAt(): \DateTimeImmutable
    {
        return $this->createdAt;
    }
}