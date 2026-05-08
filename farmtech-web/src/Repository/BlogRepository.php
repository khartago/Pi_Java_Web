<?php

namespace App\Repository;

use App\Entity\Blog;
use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Blog>
 *
 * @method Blog|null find($id, $lockMode = null, $lockVersion = null)
 * @method Blog|null findOneBy(array $criteria, array $orderBy = null)
 * @method Blog[]    findAll()
 * @method Blog[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class BlogRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Blog::class);
    }

    public function findForList(string $search = '', string $tag = ''): array
    {
        $qb = $this->createQueryBuilder('b');

        if ($search !== '') {
            $qb->andWhere('b.titleBlog LIKE :search')
                ->setParameter('search', '%' . $search . '%');
        }

        if ($tag !== '') {
            $qb->andWhere('b.blogTag = :tag')
                ->setParameter('tag', $tag);
        }

        return $qb->orderBy('b.dateBlog', 'DESC')->getQuery()->getResult();
    }

    public function findDistinctTags(): array
    {
        return $this->createQueryBuilder('b')
            ->select('DISTINCT b.blogTag')
            ->orderBy('b.blogTag', 'ASC')
            ->getQuery()->getSingleColumnResult();
    }
    public function findByUser(Utilisateur $user): array
    {
        return $this->createQueryBuilder('b')
            ->where('b.utilisateur = :user') // <-- MUST be 'utilisateur', not 'user'
            ->setParameter('user', $user)
            ->orderBy('b.dateBlog', 'DESC')
            ->getQuery()
            ->getResult();
    }
}