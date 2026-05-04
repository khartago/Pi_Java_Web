<?php

namespace App\Repository;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Diagnostique>
 */
class DiagnostiqueRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Diagnostique::class);
    }

    public function findLatestForProbleme(int $problemeId): ?Diagnostique
    {
        return $this->createQueryBuilder('d')
            ->andWhere('d.probleme = :pid')
            ->setParameter('pid', $problemeId)
            ->orderBy('d.numRevision', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    /**
     * Latest diagnostic per problem in one query (replaces N× findLatestForProbleme on admin lists).
     *
     * @param list<int> $problemeIds
     *
     * @return array<int, Diagnostique>
     */
    public function findLatestIndexedByProblemeIds(array $problemeIds): array
    {
        if ($problemeIds === []) {
            return [];
        }

        $rows = $this->createQueryBuilder('d')
            ->innerJoin('d.probleme', 'dp')->addSelect('dp')
            ->andWhere('dp.id IN (:ids)')
            ->andWhere('d.numRevision = (SELECT MAX(d2.numRevision) FROM '.Diagnostique::class.' d2 WHERE d2.probleme = d.probleme)')
            ->setParameter('ids', $problemeIds)
            ->getQuery()
            ->getResult();

        $map = [];
        foreach ($rows as $diag) {
            $pid = $diag->getProbleme()->getId();
            if (null !== $pid) {
                $map[$pid] = $diag;
            }
        }

        return $map;
    }

    public function findLatestApprovedForProbleme(int $problemeId): ?Diagnostique
    {
        return $this->createQueryBuilder('d')
            ->andWhere('d.probleme = :pid')
            ->andWhere('d.approuve = true')
            ->setParameter('pid', $problemeId)
            ->orderBy('d.numRevision', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    public function countApprovedForProbleme(int $problemeId): int
    {
        return (int) $this->createQueryBuilder('d')
            ->select('COUNT(d.id)')
            ->andWhere('d.probleme = :pid')
            ->andWhere('d.approuve = true')
            ->setParameter('pid', $problemeId)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Diagnostics approuvés du même type de problème (hors problème courant), les plus récents.
     *
     * @return Diagnostique[]
     */
    public function findApprovedSimilarByProblemType(string $type, int $excludeProblemeId, int $limit = 5): array
    {
        return $this->createQueryBuilder('d')
            ->join('d.probleme', 'p')
            ->andWhere('p.type = :t')
            ->andWhere('p.id != :pid')
            ->andWhere('d.approuve = true')
            ->setParameter('t', $type)
            ->setParameter('pid', $excludeProblemeId)
            ->orderBy('d.dateDiagnostique', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Diagnostique[]
     */
    public function findByProblemeOrderedByRevision(Probleme $probleme): array
    {
        return $this->createQueryBuilder('d')
            ->andWhere('d.probleme = :p')
            ->setParameter('p', $probleme)
            ->orderBy('d.numRevision', 'ASC')
            ->getQuery()
            ->getResult();
    }

    public function getMaxRevisionNumForProbleme(int $problemeId): int
    {
        $r = $this->createQueryBuilder('d')
            ->select('MAX(d.numRevision)')
            ->andWhere('d.probleme = :pid')
            ->setParameter('pid', $problemeId)
            ->getQuery()
            ->getSingleScalarResult();

        return (int) ($r ?? 0);
    }

    /**
     * @param array{approuve?:string, q?:string, sort?:string, dir?:string} $params
     *
     * @return Diagnostique[]
     */
    public function findAdminFiltered(array $params): array
    {
        $qb = $this->createQueryBuilder('d')
            ->leftJoin('d.probleme', 'pr')->addSelect('pr');

        if (isset($params['approuve']) && $params['approuve'] !== '') {
            $val = filter_var($params['approuve'], FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE);
            if (null !== $val) {
                $qb->andWhere('d.approuve = :ap')->setParameter('ap', $val);
            }
        }

        if (!empty($params['q'])) {
            $q = '%'.$params['q'].'%';
            $qb->andWhere('d.cause LIKE :q OR d.solutionProposee LIKE :q2 OR d.resultat LIKE :q3')
                ->setParameter('q', $q)
                ->setParameter('q2', $q)
                ->setParameter('q3', $q);
        }

        $sort = $params['sort'] ?? 'dateDiagnostique';
        $dir = strtoupper($params['dir'] ?? 'DESC');
        if (!\in_array($dir, ['ASC', 'DESC'], true)) {
            $dir = 'DESC';
        }
        $allowed = [
            'id' => 'd.id',
            'dateDiagnostique' => 'd.dateDiagnostique',
            'numRevision' => 'd.numRevision',
            'approuve' => 'd.approuve',
        ];
        $col = $allowed[$sort] ?? 'd.dateDiagnostique';
        $qb->orderBy($col, $dir);

        return $qb->getQuery()->getResult();
    }

    /** @return array{total:int, pendingApproval:int, approved:int} */
    public function getStats(): array
    {
        $em = $this->getEntityManager();
        $total = (int) $em->createQuery('SELECT COUNT(d.id) FROM App\Entity\Diagnostique d')->getSingleScalarResult();
        $approved = (int) $em->createQuery('SELECT COUNT(d.id) FROM App\Entity\Diagnostique d WHERE d.approuve = true')->getSingleScalarResult();
        $pendingApproval = (int) $em->createQuery('SELECT COUNT(d.id) FROM App\Entity\Diagnostique d WHERE d.approuve = false')->getSingleScalarResult();

        return ['total' => $total, 'pendingApproval' => $pendingApproval, 'approved' => $approved];
    }
}
