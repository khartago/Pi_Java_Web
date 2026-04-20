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

    /**
     * @return Production[]
     */
    public function search(?string $id): array
    {
        $qb = $this->createQueryBuilder('p');

        if ($id !== null && $id !== '') {
            $qb->andWhere('p.idProduction = :id')
                ->setParameter('id', (int) $id);
        }

        return $qb->orderBy('p.dateRecolte', 'DESC')->getQuery()->getResult();
    }

    /**
     * @return Production[]
     */
    public function findSorted(?string $sort): array
    {
        $qb = $this->createQueryBuilder('p');

        if ($sort === 'quantite') {
            $qb->orderBy('p.quantiteProduite', 'ASC');
        } elseif ($sort === 'date') {
            $qb->orderBy('p.dateRecolte', 'DESC');
        } else {
            $qb->orderBy('p.idProduction', 'DESC');
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * @return array{total:int,recoltee:int}
     */
    public function getStats(): array
    {
        return [
            'total' => $this->count([]),
            'recoltee' => $this->count(['etat' => 'Recoltee']),
        ];
    }
}
