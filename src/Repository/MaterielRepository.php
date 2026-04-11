<?php

namespace App\Repository;

use App\Entity\Materiel;
use App\Entity\Produit;
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
    public function findByProduitOrdered(Produit $produit): array
    {
        return $this->createQueryBuilder('m')
            ->andWhere('m.produit = :produit')
            ->setParameter('produit', $produit)
            ->orderBy('m.nom', 'ASC')
            ->getQuery()
            ->getResult();
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
}
