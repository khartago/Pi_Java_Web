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
     * @return list<Recommandation>
     */
    public function findActiveForProduit(Produit $produit): array
    {
        return $this->createQueryBuilder('r')
            ->innerJoin('r.materiel', 'm')->addSelect('m')
            ->andWhere('r.produit = :produit')
            ->andWhere('r.actif = true')
            ->setParameter('produit', $produit)
            ->orderBy('r.priorite', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findOnePair(Produit $p, Materiel $m): ?Recommandation
    {
        return $this->findOneBy(['produit' => $p, 'materiel' => $m]);
    }
}
