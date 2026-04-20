<?php

namespace App\Service;

use Doctrine\DBAL\Connection;

/**
 * KPI diagnostics (parité Java {@see DiagnosticAnalyticsService}).
 */
final class DiagnosticAnalyticsService
{
    public function __construct(
        private readonly Connection $connection,
    ) {
    }

    public function getTauxResolution(): float
    {
        try {
            $row = $this->connection->fetchAssociative(
                'SELECT SUM(CASE WHEN feedback_fermier = \'RESOLU\' THEN 1 ELSE 0 END) AS resolus, '
                .'SUM(CASE WHEN feedback_fermier IN (\'RESOLU\',\'NON_RESOLU\') THEN 1 ELSE 0 END) AS total_feedback '
                .'FROM diagnostique d WHERE d.approuve = 1'
            );
            if (!$row) {
                return 0.0;
            }
            $total = (int) ($row['total_feedback'] ?? 0);
            if ($total <= 0) {
                return 0.0;
            }

            return (int) ($row['resolus'] ?? 0) / $total;
        } catch (\Throwable) {
            return 0.0;
        }
    }

    /** @return array<string, int> */
    public function getProblemesParType(): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT p.type AS t, COUNT(*) AS cnt FROM probleme p GROUP BY p.type ORDER BY cnt DESC'
            );
        } catch (\Throwable) {
            return [];
        }
        $map = [];
        foreach ($rows as $row) {
            $map[(string) ($row['t'] ?? '')] = (int) ($row['cnt'] ?? 0);
        }

        return $map;
    }

    /** @return array<string, int> cause tronquée => count */
    public function getCausesFrequentes(int $limit = 10): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT COALESCE(SUBSTRING(d.cause, 1, 50), \'\') AS cause_trunc, COUNT(*) AS cnt '
                .'FROM diagnostique d WHERE d.approuve = 1 GROUP BY 1 ORDER BY cnt DESC LIMIT '.$limit
            );
        } catch (\Throwable) {
            return [];
        }
        $map = [];
        foreach ($rows as $row) {
            $map[(string) ($row['cause_trunc'] ?? '')] = (int) ($row['cnt'] ?? 0);
        }

        return $map;
    }

    /** Durée moyenne en heures entre détection et diagnostic (approuvé). */
    public function getDureeMoyenneDiagnosticHeures(): ?float
    {
        try {
            $v = $this->connection->fetchOne(
                'SELECT AVG(TIMESTAMPDIFF(HOUR, p.date_detection, d.date_diagnostique)) '
                .'FROM probleme p INNER JOIN diagnostique d ON d.id_probleme = p.id WHERE d.approuve = 1'
            );
            if (null === $v) {
                return null;
            }
            $f = (float) $v;

            return $f > 0 ? $f : null;
        } catch (\Throwable) {
            try {
                $v = $this->connection->fetchOne(
                    'SELECT AVG(UNIX_TIMESTAMP(d.date_diagnostique) - UNIX_TIMESTAMP(p.date_detection)) / 3600 '
                    .'FROM probleme p INNER JOIN diagnostique d ON d.id_probleme = p.id WHERE d.approuve = 1'
                );
                if (null === $v) {
                    return null;
                }
                $f = (float) $v;

                return $f > 0 ? $f : null;
            } catch (\Throwable) {
                return null;
            }
        }
    }

    /** @return array<string, int> nom plantation => count */
    public function getProblemesParPlantation(): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT COALESCE(p.nomPlant, \'Sans plantation\') AS nom, COUNT(*) AS cnt '
                .'FROM probleme pr LEFT JOIN plantation p ON pr.id_plantation = p.id '
                .'GROUP BY pr.id_plantation ORDER BY cnt DESC'
            );
        } catch (\Throwable) {
            return [];
        }
        $map = [];
        foreach ($rows as $row) {
            $map[(string) ($row['nom'] ?? '')] = (int) ($row['cnt'] ?? 0);
        }

        return $map;
    }

    /** @return array<string, int> nom produit => count */
    public function getProblemesParProduit(): array
    {
        try {
            $rows = $this->connection->fetchAllAssociative(
                'SELECT COALESCE(prod.nom, \'Sans produit\') AS nom, COUNT(*) AS cnt '
                .'FROM probleme pr LEFT JOIN produit prod ON pr.id_produit = prod.idProduit '
                .'GROUP BY pr.id_produit ORDER BY cnt DESC'
            );
        } catch (\Throwable) {
            return [];
        }
        $map = [];
        foreach ($rows as $row) {
            $map[(string) ($row['nom'] ?? '')] = (int) ($row['cnt'] ?? 0);
        }

        return $map;
    }
}
