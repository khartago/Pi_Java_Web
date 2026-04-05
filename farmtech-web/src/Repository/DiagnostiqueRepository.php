<?php

namespace App\Repository;

use App\Entity\Diagnostique;
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
