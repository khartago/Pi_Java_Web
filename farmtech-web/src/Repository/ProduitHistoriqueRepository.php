<?php

namespace App\Repository;

use App\Entity\ProduitHistorique;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<ProduitHistorique>
 */
class ProduitHistoriqueRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, ProduitHistorique::class);
    }

    /**
     * @return ProduitHistorique[]
     */
    public function findByProduitOrdered(int $idProduit): array
    {
        return $this->createQueryBuilder('h')
            ->innerJoin('h.produit', 'p')
            ->andWhere('p.idProduit = :pid')
            ->setParameter('pid', $idProduit)
            ->orderBy('h.dateEvenement', 'DESC')
            ->addOrderBy('h.idHistorique', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
