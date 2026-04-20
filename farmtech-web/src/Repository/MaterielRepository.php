<?php

namespace App\Repository;

use App\Entity\Materiel;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Materiel>
 */
class MaterielRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Materiel::class);
    }

    /**
     * @return list<Materiel>
     */
    public function findByEtatWithProduit(string $etat): array
    {
        return $this->createQueryBuilder('m')
            ->innerJoin('m.produit', 'p')
            ->addSelect('p')
            ->andWhere('m.etat = :etat')
            ->setParameter('etat', $etat)
            ->orderBy('m.nom', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return list<Materiel>
     */
    public function findAllWithProduit(): array
    {
        return $this->createQueryBuilder('m')
            ->innerJoin('m.produit', 'p')
            ->addSelect('p')
            ->orderBy('m.nom', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return list<array{produit_nom: string, total_cout: float, count: int}>
     */
    public function sumCostByProduit(): array
    {
        return $this->createQueryBuilder('m')
            ->select('p.nom AS produit_nom, SUM(m.cout) AS total_cout, COUNT(m.idMateriel) AS count')
            ->innerJoin('m.produit', 'p')
            ->groupBy('p.idProduit, p.nom')
            ->orderBy('total_cout', 'DESC')
            ->getQuery()
            ->getArrayResult();
    }
}
