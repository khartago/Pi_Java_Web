<?php

namespace App\Repository;

use App\Entity\Produit;
use App\Entity\Promotion;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Promotion>
 */
class PromotionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Promotion::class);
    }

    /**
     * @return list<Promotion>
     */
    public function findActiveForProduct(Produit $produit): array
    {
        $today = new \DateTimeImmutable('today');

        return $this->createQueryBuilder('p')
            ->innerJoin('p.produits', 'pr')
            ->andWhere('pr = :produit')
            ->andWhere('p.actif = true')
            ->andWhere('p.dateDebut <= :today')
            ->andWhere('p.dateFin >= :today')
            ->setParameter('produit', $produit)
            ->setParameter('today', $today)
            ->orderBy('p.valeurReduction', 'DESC')
            ->getQuery()
            ->getResult();
    }
}
