<?php

namespace App\Repository;

use App\Entity\Comment;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Comment>
 */
class CommentRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Comment::class);
    }

    // Get only approved comments for an article
    public function findApprovedByArticle(Article $article)
    {
        return $this->createQueryBuilder('c')
            ->where('c.article = :article')
            ->andWhere('c.isApproved = :approved')
            ->setParameter('article', $article)
            ->setParameter('approved', true)
            ->orderBy('c.createdAt', 'ASC')
            ->getQuery()
            ->getResult();
    }

    // Get pending comments (for admin dashboard)
    public function findPendingComments()
    {
        return $this->createQueryBuilder('c')
            ->where('c.isApproved = :approved')
            ->setParameter('approved', false)
            ->orderBy('c.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }
}