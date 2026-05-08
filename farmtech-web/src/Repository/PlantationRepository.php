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

    /**
     * @return Plantation[]
     */
    public function search(?string $keyword): array
    {
        $qb = $this->createQueryBuilder('p');

        if ($keyword) {
            $qb->andWhere('p.nomPlant LIKE :k OR p.variete LIKE :k OR p.saison LIKE :k')
                ->setParameter('k', '%' . $keyword . '%');
        }

        return $qb->orderBy('p.datePlante', 'DESC')->getQuery()->getResult();
    }

    /**
     * @return Plantation[]
     */
    public function findSorted(?string $sort): array
    {
        $qb = $this->createQueryBuilder('p');

        if ($sort === 'quantite') {
            $qb->orderBy('p.quantite', 'ASC');
        } elseif ($sort === 'date') {
            $qb->orderBy('p.datePlante', 'DESC');
        } else {
            $qb->orderBy('p.id', 'DESC');
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * @return array{total:int,complete:int,attente:int}
     */
    public function getStats(): array
    {
        return [
            'total' => $this->count([]),
            'complete' => $this->count(['etat' => 'COMPLETE']),
            'attente' => $this->count(['etat' => 'EN_ATTENTE']),
        ];
    }
}
