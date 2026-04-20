<?php

namespace App\Repository;

use App\Entity\Employe;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Employe>
 */
class EmployeRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Employe::class);
    }

    /**
     * @return list<Employe>
     */
    public function findForList(?string $recherche): array
    {
        $qb = $this->createQueryBuilder('e')
            ->orderBy('e.nom', 'ASC')
            ->addOrderBy('e.prenom', 'ASC');

        if ($recherche !== null && $recherche !== '') {
            $qb->andWhere('LOWER(e.nom) LIKE :t OR LOWER(e.prenom) LIKE :t OR LOWER(e.poste) LIKE :t')
                ->setParameter('t', '%' . strtolower($recherche) . '%');
        }

        return $qb->getQuery()->getResult();
    }
}
