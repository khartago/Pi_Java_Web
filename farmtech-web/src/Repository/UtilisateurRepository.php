<?php

namespace App\Repository;

use App\Dto\UserStatsDto;
use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\DBAL\ParameterType;
use Doctrine\ORM\QueryBuilder;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Security\Core\Exception\UnsupportedUserException;
use Symfony\Component\Security\Core\Exception\UserNotFoundException;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\PasswordUpgraderInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\UserProviderInterface;

/**
 * @extends ServiceEntityRepository<Utilisateur>
 *
 * @implements UserProviderInterface<Utilisateur>
 */
class UtilisateurRepository extends ServiceEntityRepository implements UserProviderInterface, PasswordUpgraderInterface
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Utilisateur::class);
    }

    public function loadUserByIdentifier(string $identifier): UserInterface
    {
        $user = $this->findOneBy(['email' => $identifier]);
        if (!$user) {
            throw new UserNotFoundException(sprintf('User "%s" not found.', $identifier));
        }

        return $user;
    }

    public function refreshUser(UserInterface $user): UserInterface
    {
        if (!$user instanceof Utilisateur) {
            throw new UnsupportedUserException(sprintf('Invalid user class "%s".', $user::class));
        }

        return $this->loadUserByIdentifier($user->getUserIdentifier());
    }

    public function supportsClass(string $class): bool
    {
        return Utilisateur::class === $class || is_subclass_of($class, Utilisateur::class);
    }

    public function upgradePassword(PasswordAuthenticatedUserInterface $user, string $newHashedPassword): void
    {
        // Plan: plain passwords — no upgrade to hashed passwords
    }

    /**
     * @param array{q?:string|null, role?:string|null, sort?:string, dir?:string} $params
     */
    private function applyListingFilters(QueryBuilder $qb, array $params): void
    {
        if (!empty($params['q'])) {
            $q = '%'.$params['q'].'%';
            $qb->andWhere('u.nom LIKE :q OR u.email LIKE :q')
                ->setParameter('q', $q);
        }

        if (!empty($params['role']) && \in_array($params['role'], [Utilisateur::ROLE_ADMIN_DB, Utilisateur::ROLE_FARMER_DB], true)) {
            $qb->andWhere('u.role = :role')->setParameter('role', $params['role']);
        }
    }

    /**
     * @param array{q?:string|null, role?:string|null, sort?:string, dir?:string} $params
     */
    private function applyListingSort(QueryBuilder $qb, array $params): void
    {
        $sort = $params['sort'] ?? 'email';
        $dir = strtoupper($params['dir'] ?? 'ASC');
        if (!\in_array($dir, ['ASC', 'DESC'], true)) {
            $dir = 'ASC';
        }
        $allowed = ['nom' => 'u.nom', 'email' => 'u.email', 'role' => 'u.role'];
        $col = $allowed[$sort] ?? 'u.email';
        $qb->orderBy($col, $dir);
    }

    /**
     * Count via DBAL so the request does not run ORM aggregate SQL (DoctrineDoctor flags scalar COUNT hydration).
     * Filter logic must stay aligned with {@see applyListingFilters()}.
     *
     * @param array{q?:string|null, role?:string|null, sort?:string, dir?:string} $params
     */
    public function countFiltered(array $params): int
    {
        $conn = $this->getEntityManager()->getConnection();
        $sql = 'SELECT COUNT(*) FROM utilisateur u WHERE 1=1';
        $bind = [];
        $types = [];

        if (!empty($params['q'])) {
            $sql .= ' AND (u.nom LIKE ? OR u.email LIKE ?)';
            $like = '%'.$params['q'].'%';
            $bind[] = $like;
            $bind[] = $like;
            $types[] = ParameterType::STRING;
            $types[] = ParameterType::STRING;
        }

        if (!empty($params['role']) && \in_array($params['role'], [Utilisateur::ROLE_ADMIN_DB, Utilisateur::ROLE_FARMER_DB], true)) {
            $sql .= ' AND u.role = ?';
            $bind[] = $params['role'];
            $types[] = ParameterType::STRING;
        }

        return (int) $conn->executeQuery($sql, $bind, $types)->fetchOne();
    }

    /**
     * @param array{q?:string|null, role?:string|null, sort?:string, dir?:string} $params
     *
     * @return Utilisateur[]
     */
    public function findFiltered(array $params, int $limit, int $offset): array
    {
        $qb = $this->createQueryBuilder('u');
        $this->applyListingFilters($qb, $params);
        $this->applyListingSort($qb, $params);
        $qb->setMaxResults($limit)
            ->setFirstResult($offset);

        return $qb->getQuery()->getResult();
    }

    /**
     * Global user stats via DBAL (single round-trip, no ORM aggregate queries for DoctrineDoctor).
     *
     * @return array{total:int, byRole: array<string,int>}
     */
    public function getStats(): array
    {
        $sql = 'SELECT COUNT(*) AS total, '
            .'SUM(CASE WHEN u.role = ? THEN 1 ELSE 0 END) AS adminCount, '
            .'SUM(CASE WHEN u.role = ? THEN 1 ELSE 0 END) AS farmerCount '
            .'FROM utilisateur u';

        $row = $this->getEntityManager()->getConnection()->executeQuery(
            $sql,
            [Utilisateur::ROLE_ADMIN_DB, Utilisateur::ROLE_FARMER_DB],
            [ParameterType::STRING, ParameterType::STRING],
        )->fetchAssociative();

        if (false === $row) {
            $dto = new UserStatsDto(0, 0, 0);
        } else {
            $dto = new UserStatsDto($row['total'], $row['adminCount'], $row['farmerCount']);
        }

        return [
            'total' => $dto->total,
            'byRole' => [
                Utilisateur::ROLE_ADMIN_DB => $dto->adminCount,
                Utilisateur::ROLE_FARMER_DB => $dto->farmerCount,
            ],
        ];
    }
}
