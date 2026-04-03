<?php

namespace App\Repository;

use App\Entity\Probleme;
use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Probleme>
 */
class ProblemeRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Probleme::class);
    }

    /**
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string} $params
     *
     * @return Probleme[]
     */
    public function findForFarmer(Utilisateur $farmer, array $params): array
    {
        $qb = $this->createQueryBuilder('p')
            ->andWhere('p.utilisateur = :u')
            ->setParameter('u', $farmer);

        $this->applyFilters($qb, $params);

        return $qb->getQuery()->getResult();
    }

    /**
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string} $params
     *
     * @return Probleme[]
     */
    public function findAdminFiltered(array $params): array
    {
        $qb = $this->createQueryBuilder('p');
        $this->applyFilters($qb, $params);

        return $qb->getQuery()->getResult();
    }

    /**
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string} $params
     */
    private function applyFilters(\Doctrine\ORM\QueryBuilder $qb, array $params): void
    {
        if (!empty($params['etat'])) {
            $qb->andWhere('p.etat = :etat')->setParameter('etat', $params['etat']);
        }
        if (!empty($params['gravite'])) {
            $qb->andWhere('p.gravite = :gravite')->setParameter('gravite', $params['gravite']);
        }
        if (!empty($params['type'])) {
            $qb->andWhere('p.type = :type')->setParameter('type', $params['type']);
        }
        if (!empty($params['q'])) {
            $q = '%'.$params['q'].'%';
            $qb->andWhere('p.description LIKE :q OR p.type LIKE :q2')
                ->setParameter('q', $q)
                ->setParameter('q2', $q);
        }

        $sort = $params['sort'] ?? 'dateDetection';
        $dir = strtoupper($params['dir'] ?? 'DESC');
        if (!\in_array($dir, ['ASC', 'DESC'], true)) {
            $dir = 'DESC';
        }
        $allowed = [
            'id' => 'p.id',
            'dateDetection' => 'p.dateDetection',
            'gravite' => 'p.gravite',
            'etat' => 'p.etat',
            'type' => 'p.type',
        ];
        $col = $allowed[$sort] ?? 'p.dateDetection';
        $qb->orderBy($col, $dir);
    }

    /** @return array{total:int, byEtat: array<string,int>, byGravite: array<string,int>} */
    public function getStats(?Utilisateur $onlyFarmer = null): array
    {
        $em = $this->getEntityManager();
        $qb = $em->createQueryBuilder()
            ->select('COUNT(p.id)')
            ->from(Probleme::class, 'p');
        if ($onlyFarmer) {
            $qb->where('p.utilisateur = :u')->setParameter('u', $onlyFarmer);
        }
        $total = (int) $qb->getQuery()->getSingleScalarResult();

        $qbEtat = $em->createQueryBuilder()
            ->select('p.etat AS e, COUNT(p.id) AS c')
            ->from(Probleme::class, 'p')
            ->groupBy('p.etat');
        if ($onlyFarmer) {
            $qbEtat->where('p.utilisateur = :u')->setParameter('u', $onlyFarmer);
        }
        $byEtat = [];
        foreach ($qbEtat->getQuery()->getResult() as $row) {
            $byEtat[$row['e']] = (int) $row['c'];
        }

        $qbGr = $em->createQueryBuilder()
            ->select('p.gravite AS g, COUNT(p.id) AS c')
            ->from(Probleme::class, 'p')
            ->groupBy('p.gravite');
        if ($onlyFarmer) {
            $qbGr->where('p.utilisateur = :u')->setParameter('u', $onlyFarmer);
        }
        $byGravite = [];
        foreach ($qbGr->getQuery()->getResult() as $row) {
            $byGravite[$row['g']] = (int) $row['c'];
        }

        return ['total' => $total, 'byEtat' => $byEtat, 'byGravite' => $byGravite];
    }
}
