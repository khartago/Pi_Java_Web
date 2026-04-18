<?php

namespace App\Repository;

use App\Entity\Affectation;
use App\Entity\Materiel;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Affectation>
 */
class AffectationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Affectation::class);
    }

    public function findActiveByMateriel(Materiel $materiel): ?Affectation
    {
        return $this->createQueryBuilder('a')
            ->andWhere('a.materiel = :materiel')
            ->andWhere('a.dateRetour IS NULL')
            ->setParameter('materiel', $materiel)
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * @return list<Affectation>
     */
    public function findByMateriel(Materiel $materiel): array
    {
        return $this->createQueryBuilder('a')
            ->andWhere('a.materiel = :materiel')
            ->setParameter('materiel', $materiel)
            ->orderBy('a.dateAffectation', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return list<Affectation>
     */
    public function findAllActive(): array
    {
        return $this->createQueryBuilder('a')
            ->innerJoin('a.materiel', 'm')
            ->addSelect('m')
            ->innerJoin('a.employe', 'e')
            ->addSelect('e')
            ->andWhere('a.dateRetour IS NULL')
            ->orderBy('a.dateAffectation', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return list<Affectation>
     */
    public function findAllHistory(): array
    {
        return $this->createQueryBuilder('a')
            ->innerJoin('a.materiel', 'm')
            ->addSelect('m')
            ->innerJoin('a.employe', 'e')
            ->addSelect('e')
            ->orderBy('a.dateAffectation', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
