<?php

namespace App\Service;

use Doctrine\DBAL\Connection;
use Psr\Log\LoggerInterface;

/**
 * Listes plantation / produit (tables non mappées en entités Doctrine).
 */
final class ProblemeCatalogService
{
    public function __construct(
        private readonly Connection $connection,
        private readonly LoggerInterface $logger,
    ) {
    }

    /**
     * @return array<int, string> id => libellé
     */
    public function getPlantationChoices(): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT id, nomPlant AS nom FROM plantation ORDER BY nomPlant'
            );
        } catch (\Throwable $e) {
            $this->logger->notice('Plantation table unavailable: '.$e->getMessage());

            return [];
        }

        $out = [];
        foreach ($rows as $row) {
            $id = (int) ($row['id'] ?? 0);
            if ($id <= 0) {
                continue;
            }
            $nom = (string) ($row['nom'] ?? '');
            $out[$id] = $nom !== '' ? $nom : ('#'.$id);
        }

        return $out;
    }

    /**
     * @return array<int, string> id => libellé
     */
    public function getProduitChoices(): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT idProduit AS id, nom FROM produit ORDER BY nom'
            );
        } catch (\Throwable $e) {
            $this->logger->notice('Produit table unavailable: '.$e->getMessage());

            return [];
        }

        $out = [];
        foreach ($rows as $row) {
            $id = (int) ($row['id'] ?? 0);
            if ($id <= 0) {
                continue;
            }
            $nom = (string) ($row['nom'] ?? '');
            $out[$id] = $nom !== '' ? $nom : ('#'.$id);
        }

        return $out;
    }

    public function getPlantationLabel(?int $id): ?string
    {
        if (null === $id) {
            return null;
        }
        $choices = $this->getPlantationChoices();

        return $choices[$id] ?? null;
    }

    public function getProduitLabel(?int $id): ?string
    {
        if (null === $id) {
            return null;
        }
        $choices = $this->getProduitChoices();

        return $choices[$id] ?? null;
    }
}
