<?php

namespace App\Repository;

use App\Entity\Production;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Production>
 */
class ProductionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Production::class);
    }
public function search(?string $id)
{
    $qb = $this->createQueryBuilder('p');

    if ($id) {
        $qb->andWhere('p.idProduction = :id')
           ->setParameter('id', $id);
    }

    return $qb->getQuery()->getResult();
}
public function findSorted(?string $sort)
{
    $qb = $this->createQueryBuilder('p');

    if ($sort === 'quantite') {
        $qb->orderBy('p.quantiteProduite', 'ASC');
    } elseif ($sort === 'date') {
        $qb->orderBy('p.dateRecolte', 'DESC');
    }

    return $qb->getQuery()->getResult();
}
public function getStats()
{
    return [
        'total' => $this->count([]),
        'recoltee' => $this->count(['etat' => 'Recoltee']),
    ];
}
//    /**
//     * @return Production[] Returns an array of Production objects
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

//    public function findOneBySomeField($value): ?Production
//    {
//        return $this->createQueryBuilder('p')
//            ->andWhere('p.exampleField = :val')
//            ->setParameter('val', $value)
//            ->getQuery()
//            ->getOneOrNullResult()
//        ;
//    }
}
