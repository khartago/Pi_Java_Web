<?php

namespace App\Repository;

use App\Dto\PlantationStatsDto;
use App\Entity\Plantation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\DBAL\ParameterType;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Plantation>
 */
class PlantationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Plantation::class);
    }

    /**
     * One SQL round-trip for the admin index: global stat cards + list row count (full table, or filtered by keyword).
     *
     * @param ?string $keywordForList null or empty = all rows count toward list total; otherwise LIKE filter on nom/variete/saison.
     *
     * @return array{listTotal: int, stats: PlantationStatsDto}
     */
    public function getAdminIndexAggregates(?string $keywordForList): array
    {
        $includeAll = null === $keywordForList || '' === $keywordForList ? 1 : 0;
        $k = 1 === $includeAll ? '%' : '%'.$keywordForList.'%';

        $sql = <<<'SQL'
            SELECT
                COUNT(*) AS total,
                COALESCE(SUM(CASE WHEN p.etat = 'COMPLETE' THEN 1 ELSE 0 END), 0) AS complete,
                COALESCE(SUM(CASE WHEN p.etat = 'EN_ATTENTE' THEN 1 ELSE 0 END), 0) AS attente,
                COALESCE(SUM(CASE WHEN ? = 1 OR (p.nomPlant LIKE ? OR p.variete LIKE ? OR p.saison LIKE ?) THEN 1 ELSE 0 END), 0) AS list_total
            FROM plantation p
            SQL;

        $conn = $this->getEntityManager()->getConnection();
        $row = $conn->executeQuery(
            $sql,
            [$includeAll, $k, $k, $k],
            [ParameterType::INTEGER, ParameterType::STRING, ParameterType::STRING, ParameterType::STRING],
        )->fetchAssociative();

        if (false === $row) {
            $stats = new PlantationStatsDto(0, 0, 0);

            return ['listTotal' => 0, 'stats' => $stats];
        }

        $stats = new PlantationStatsDto($row['total'], $row['complete'], $row['attente']);

        return ['listTotal' => (int) $row['list_total'], 'stats' => $stats];
    }

    /**
     * @return list<Plantation>
     */
    public function findSearchPage(?string $keyword, int $limit, int $offset): array
    {
        $qb = $this->createQueryBuilder('p')
            ->orderBy('p.datePlante', 'DESC');
        $this->applyKeywordFilter($qb, $keyword);
        $qb->setMaxResults($limit)
            ->setFirstResult($offset);

        /** @var list<Plantation> */
        return $qb->getQuery()->getResult();
    }

    /**
     * @return list<Plantation>
     */
    public function findSortedPage(string $sort, int $limit, int $offset): array
    {
        $qb = $this->createQueryBuilder('p');
        $this->applySortOrder($qb, $sort);
        $qb->setMaxResults($limit)
            ->setFirstResult($offset);

        /** @var list<Plantation> */
        return $qb->getQuery()->getResult();
    }

    /**
     * @return Plantation[]
     */
    public function search(?string $keyword): array
    {
        $qb = $this->createQueryBuilder('p');
        $this->applyKeywordFilter($qb, $keyword);

        return $qb->orderBy('p.datePlante', 'DESC')->getQuery()->getResult();
    }

    /**
     * @return Plantation[]
     */
    public function findSorted(?string $sort): array
    {
        $qb = $this->createQueryBuilder('p');
        $this->applySortOrder($qb, $sort);

        return $qb->getQuery()->getResult();
    }

    public function getStats(): PlantationStatsDto
    {
        return $this->getAdminIndexAggregates(null)['stats'];
    }

    private function applyKeywordFilter(QueryBuilder $qb, ?string $keyword): void
    {
        if ($keyword !== null && $keyword !== '') {
            $qb->andWhere('p.nomPlant LIKE :k OR p.variete LIKE :k OR p.saison LIKE :k')
                ->setParameter('k', '%'.$keyword.'%');
        }
    }

    private function applySortOrder(QueryBuilder $qb, ?string $sort): void
    {
        if ('quantite' === $sort) {
            $qb->orderBy('p.quantite', 'ASC');
        } elseif ('date' === $sort) {
            $qb->orderBy('p.datePlante', 'DESC');
        } else {
            $qb->orderBy('p.id', 'DESC');
        }
    }
}
