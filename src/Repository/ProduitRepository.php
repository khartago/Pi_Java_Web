<?php

namespace App\Repository;

use App\Entity\Produit;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Produit>
 */
class ProduitRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Produit::class);
    }

    /**
     * @return list<Produit>
     */
    public function findForList(?string $recherche = null, ?string $unite = null): array
    {
        $qb = $this->createQueryBuilder('p')
            ->orderBy('p.nom', 'ASC');

        if ($recherche !== null && $recherche !== '') {
            $qb
                ->andWhere('LOWER(p.nom) LIKE :recherche')
                ->setParameter('recherche', '%'.mb_strtolower($recherche).'%');
        }

        if ($unite !== null && $unite !== '') {
            $qb
                ->andWhere('p.unite = :unite')
                ->setParameter('unite', $unite);
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * @return list<Produit>
     */
    public function findForMarketplace(?string $recherche = null, ?string $unite = null): array
    {
        $qb = $this->createQueryBuilder('p')
            ->leftJoin('p.materiels', 'm')
            ->addSelect('m')
            ->orderBy('p.nom', 'ASC')
            ->addOrderBy('m.nom', 'ASC');

        if ($recherche !== null && $recherche !== '') {
            $qb
                ->andWhere('LOWER(p.nom) LIKE :recherche')
                ->setParameter('recherche', '%'.mb_strtolower($recherche).'%');
        }

        if ($unite !== null && $unite !== '') {
            $qb
                ->andWhere('p.unite = :unite')
                ->setParameter('unite', $unite);
        }

        return $qb->getQuery()->getResult();
    }

    /**
     * @return list<string>
     */
    public function findDistinctUnites(): array
    {
        $rows = $this->createQueryBuilder('p')
            ->select('DISTINCT p.unite AS unite')
            ->where('p.unite IS NOT NULL')
            ->andWhere('p.unite <> :empty')
            ->setParameter('empty', '')
            ->orderBy('p.unite', 'ASC')
            ->getQuery()
            ->getArrayResult();

        return array_map(static fn (array $row): string => $row['unite'], $rows);
    }

    public function findOneForDetail(int $idProduit): ?Produit
    {
        return $this->createQueryBuilder('p')
            ->leftJoin('p.materiels', 'm')
            ->addSelect('m')
            ->andWhere('p.idProduit = :idProduit')
            ->setParameter('idProduit', $idProduit)
            ->orderBy('m.nom', 'ASC')
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * @return list<Produit>
     */
    public function findLowStock(int $threshold): array
    {
        return $this->createQueryBuilder('p')
            ->andWhere('p.quantite <= :threshold')
            ->setParameter('threshold', $threshold)
            ->orderBy('p.quantite', 'ASC')
            ->addOrderBy('p.nom', 'ASC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return list<Produit>
     */
    public function findExpiringBefore(\DateTimeInterface $date): array
    {
        return $this->createQueryBuilder('p')
            ->andWhere('p.dateExpiration IS NOT NULL')
            ->andWhere('p.dateExpiration <= :date')
            ->setParameter('date', $date)
            ->orderBy('p.dateExpiration', 'ASC')
            ->addOrderBy('p.nom', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
