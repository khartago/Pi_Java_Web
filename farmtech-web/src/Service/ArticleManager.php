<?php


namespace App\Service;

use App\Entity\Article;
use Doctrine\ORM\EntityManagerInterface;

class ArticleManager
{
    public function __construct(
        private readonly EntityManagerInterface $em
    )
    {
    }

    public function save(Article $article): void
    {
        // If the article has an ID, it means it's being updated, not created.
        // We automatically flag it as edited.
        if ($article->getId() !== null) {
            $article->setEdited(true);
        }

        $this->em->persist($article);
        $this->em->flush();
    }

    public function delete(Article $article): void
    {
        $this->em->remove($article);
        $this->em->flush();
    }
}