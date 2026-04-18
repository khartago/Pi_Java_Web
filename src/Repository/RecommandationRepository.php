<?php

namespace App\Repository;

use App\Entity\Materiel;
use App\Entity\Produit;
use App\Entity\Recommandation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Recommandation>
 */
class RecommandationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Recommandation::class);
    }

    /**
     * @return list<Recommandation>
     */
    public function findAllOrdered(): array
    {
        return $this->createQueryBuilder('r')
            ->innerJoin('r.produit', 'p')->addSelect('p')
            ->innerJoin('r.materiel', 'm')->addSelect('m')
            ->orderBy('p.nom', 'ASC')
            ->addOrderBy('r.priorite', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Active recommendations for a specific produit, highest priority first.
     * @return list<Recommandation>
     */
    public function findActiveForProduit(Produit $produit): array
    {
        return $this->createQueryBuilder('r')
            ->innerJoin('r.materiel', 'm')->addSelect('m')
            ->innerJoin('m.produit', 'mp')->addSelect('mp')
            ->andWhere('r.produit = :produit')
            ->andWhere('r.actif = :actif')
            ->setParameter('produit', $produit)
            ->setParameter('actif', true)
            ->orderBy('r.priorite', 'DESC')
            ->addOrderBy('m.nom', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function findOnePair(Produit $p, Materiel $m): ?Recommandation
    {
        return $this->findOneBy(['produit' => $p, 'materiel' => $m]);
    }
}
