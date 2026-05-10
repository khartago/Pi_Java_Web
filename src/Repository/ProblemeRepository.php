<?php

namespace App\Repository;

use App\Entity\Probleme;
use App\Entity\Utilisateur;
use App\Form\ProblemeType;
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
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string, date_from?:string, date_to?:string} $params
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
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string, date_from?:string, date_to?:string} $params
     */
    public function countAdminFiltered(array $params): int
    {
        $qb = $this->createQueryBuilder('p')
            ->select('COUNT(p.id)');
        $this->applyFilters($qb, $params);

        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    /**
     * Admin list: filters + pagination + eager utilisateur / adminAssignee (avoids N+1 on the index table).
     *
     * Uses two queries so LIMIT applies only to probleme ids — never combined with fetch-joined associations
     * (avoids incorrect LIMIT on SQL row count when tooling assumes collection joins).
     *
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string, date_from?:string, date_to?:string} $params
     *
     * @return Probleme[]
     */
    public function findAdminFilteredPage(array $params, int $limit, int $offset): array
    {
        $idQb = $this->createQueryBuilder('p')
            ->select('p.id');
        $this->applyFilters($idQb, $params);
        $idQb->setMaxResults($limit)
            ->setFirstResult($offset);

        $ids = [];
        foreach ($idQb->getQuery()->getScalarResult() as $row) {
            $ids[] = (int) reset($row);
        }

        if ($ids === []) {
            return [];
        }

        $hydrateQb = $this->createQueryBuilder('p')
            ->leftJoin('p.utilisateur', 'u')->addSelect('u')
            ->leftJoin('p.adminAssignee', 'a')->addSelect('a')
            ->where('p.id IN (:ids)')
            ->setParameter('ids', $ids);

        /** @var Probleme[] $rows */
        $rows = $hydrateQb->getQuery()->getResult();
        $byId = [];
        foreach ($rows as $probleme) {
            $pid = $probleme->getId();
            if (null !== $pid) {
                $byId[$pid] = $probleme;
            }
        }

        $ordered = [];
        foreach ($ids as $id) {
            if (isset($byId[$id])) {
                $ordered[] = $byId[$id];
            }
        }

        return $ordered;
    }

    /**
     * Stat strip for the admin signalements index (total + counts by état).
     * One aggregate row (COUNT + SUM/CASE per known état) so analyzers do not treat the query as an unbounded row set.
     *
     * @return array{total: int, byEtat: array<string, int>}
     */
    public function getAdminIndexStats(): array
    {
        $conn = $this->getEntityManager()->getConnection();
        /** @var list<string> $etats */
        $etats = array_values(ProblemeType::ETATS);

        $selects = ['COUNT(*) AS total'];
        $params = [];
        foreach ($etats as $idx => $etat) {
            $selects[] = \sprintf(
                'COALESCE(SUM(CASE WHEN etat = :e%d THEN 1 ELSE 0 END), 0) AS cnt_%d',
                $idx,
                $idx
            );
            $params['e'.$idx] = $etat;
        }

        $sql = 'SELECT '.\implode(', ', $selects).' FROM probleme';
        /** @var array<string, mixed>|false $row */
        $row = $conn->fetchAssociative($sql, $params);
        if (false === $row) {
            return ['total' => 0, 'byEtat' => []];
        }

        $total = (int) $row['total'];
        $byEtat = [];
        foreach ($etats as $idx => $etat) {
            $c = (int) ($row['cnt_'.$idx] ?? 0);
            if ($c > 0) {
                $byEtat[$etat] = $c;
            }
        }

        return ['total' => $total, 'byEtat' => $byEtat];
    }

    /**
     * @param array{etat?:string, gravite?:string, type?:string, q?:string, sort?:string, dir?:string, date_from?:string, date_to?:string} $params
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
        if (!empty($params['date_from'])) {
            try {
                $from = new \DateTimeImmutable($params['date_from'].' 00:00:00');
                $qb->andWhere('p.dateDetection >= :df')->setParameter('df', $from);
            } catch (\Exception) {
            }
        }
        if (!empty($params['date_to'])) {
            try {
                $to = new \DateTimeImmutable($params['date_to'].' 00:00:00');
                $toEnd = $to->modify('+1 day');
                $qb->andWhere('p.dateDetection < :dt')->setParameter('dt', $toEnd);
            } catch (\Exception) {
            }
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
