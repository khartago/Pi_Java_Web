<?php

namespace App\Repository;

use App\Entity\Plantation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Plantation>
 */
class PlantationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Plantation::class);
    }
    public function search(?string $keyword)
{
    $qb = $this->createQueryBuilder('p');

    if ($keyword) {
        $qb->andWhere('p.nomPlant LIKE :k OR p.variete LIKE :k OR p.saison LIKE :k')
           ->setParameter('k', '%' . $keyword . '%');
    }

    return $qb->getQuery()->getResult();
}
public function findSorted(?string $sort)
{
    $qb = $this->createQueryBuilder('p');

    if ($sort === 'quantite') {
        $qb->orderBy('p.quantite', 'ASC');
    } elseif ($sort === 'date') {
        $qb->orderBy('p.datePlante', 'DESC');
    }

    return $qb->getQuery()->getResult();
}
public function getStats()
{
    return [
        'total' => $this->count([]),
        'complete' => $this->count(['etat' => 'COMPLETE']),
        'attente' => $this->count(['etat' => 'EN_ATTENTE']),
    ];
}

//    /**
//     * @return Plantation[] Returns an array of Plantation objects
//     */
//    public function findByExampleField($value): array
//    {
//        return $this->createQueryBuilder('p')
//            ->andWhere('p.exampleField = :val')
//            ->setParameter('val', $value)
//            ->orderBy('p.id', 'ASC')
//            ->setMaxResults(10)
//            ->getQuery()
//            ->getResult()
//        ;
//    }

//    public function findOneBySomeField($value): ?Plantation
//    {
//        return $this->createQueryBuilder('p')
//            ->andWhere('p.exampleField = :val')
//            ->setParameter('val', $value)
//            ->getQuery()
//            ->getOneOrNullResult()
//        ;
//    }
}
