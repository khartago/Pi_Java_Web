<?php

namespace App\Repository;

use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Security\Core\Exception\UnsupportedUserException;
use Symfony\Component\Security\Core\Exception\UserNotFoundException;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\PasswordUpgraderInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\UserProviderInterface;

/**
 * @extends ServiceEntityRepository<Utilisateur>
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
     * @param array{q?:string, role?:string, sort?:string, dir?:string} $params
     *
     * @return Utilisateur[]
     */
    public function findFiltered(array $params): array
    {
        $qb = $this->createQueryBuilder('u');

        if (!empty($params['q'])) {
            $q = '%'.$params['q'].'%';
            $qb->andWhere('u.nom LIKE :q OR u.email LIKE :q')
                ->setParameter('q', $q);
        }

        if (!empty($params['role']) && \in_array($params['role'], [Utilisateur::ROLE_ADMIN_DB, Utilisateur::ROLE_FARMER_DB], true)) {
            $qb->andWhere('u.role = :role')->setParameter('role', $params['role']);
        }

        $sort = $params['sort'] ?? 'email';
        $dir = strtoupper($params['dir'] ?? 'ASC');
        if (!\in_array($dir, ['ASC', 'DESC'], true)) {
            $dir = 'ASC';
        }
        $allowed = ['nom' => 'u.nom', 'email' => 'u.email', 'role' => 'u.role'];
        $col = $allowed[$sort] ?? 'u.email';
        $qb->orderBy($col, $dir);

        return $qb->getQuery()->getResult();
    }

    /** @return array{total:int, byRole: array<string,int>} */
    public function getStats(): array
    {
        $em = $this->getEntityManager();
        $total = (int) $em->createQuery('SELECT COUNT(u.id) FROM App\Entity\Utilisateur u')->getSingleScalarResult();

        $byRole = [];
        $rows = $em->createQuery('SELECT u.role AS r, COUNT(u.id) AS c FROM App\Entity\Utilisateur u GROUP BY u.role')->getResult();
        foreach ($rows as $row) {
            $byRole[$row['r']] = (int) $row['c'];
        }

        return ['total' => $total, 'byRole' => $byRole];
    }
}
