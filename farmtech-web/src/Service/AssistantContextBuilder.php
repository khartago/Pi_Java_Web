<?php

namespace App\Service;

use App\Dto\AssistantIntent;
use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;

final class AssistantContextBuilder
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly int $stockThreshold,
    ) {
    }

    /**
     * @return array{context: string, summary: string}
     */
    public function build(AssistantIntent $intent): array
    {
        return match ($intent->name) {
            'stock.low' => $this->buildLowStock(),
            'stock.expiring' => $this->buildExpiring(),
            'materiel.broken' => $this->buildBroken(),
            'materiel.cost' => $this->buildCost(),
            default => $this->buildStock(),
        };
    }

    /**
     * @return array{context: string, summary: string}
     */
    private function buildStock(): array
    {
        $products = array_slice($this->produitRepository->findForList(), 0, 20);
        if ($products === []) {
            return ['context' => 'No stock data.', 'summary' => 'Stock empty.'];
        }

        $lines = ["| Produit | Quantite | Unite |", "|---|---|---|"];
        foreach ($products as $p) {
            $lines[] = sprintf('| %s | %d | %s |', $p->getNom(), (int) $p->getQuantite(), $p->getUnite());
        }

        return ['context' => implode("\n", $lines), 'summary' => 'Stock summary loaded'];
    }

    /**
     * @return array{context: string, summary: string}
     */
    private function buildLowStock(): array
    {
        $products = $this->produitRepository->findLowStock($this->stockThreshold);
        if ($products === []) {
            return ['context' => 'No low stock products.', 'summary' => 'No low stock'];
        }

        $lines = ["| Produit | Quantite |", "|---|---|"];
        foreach (array_slice($products, 0, 20) as $p) {
            $lines[] = sprintf('| %s | %d |', $p->getNom(), (int) $p->getQuantite());
        }

        return ['context' => implode("\n", $lines), 'summary' => 'Low stock products listed'];
    }

    /**
     * @return array{context: string, summary: string}
     */
    private function buildExpiring(): array
    {
        $products = $this->produitRepository->findExpiringBefore(new \DateTimeImmutable('+30 days'));
        if ($products === []) {
            return ['context' => 'No expiring products.', 'summary' => 'No expiration alert'];
        }

        $lines = ["| Produit | Expiration |", "|---|---|"];
        foreach (array_slice($products, 0, 20) as $p) {
            $lines[] = sprintf('| %s | %s |', $p->getNom(), $p->getDateExpiration()?->format('Y-m-d') ?? '-');
        }

        return ['context' => implode("\n", $lines), 'summary' => 'Expiring products listed'];
    }

    /**
     * @return array{context: string, summary: string}
     */
    private function buildBroken(): array
    {
        $items = $this->materielRepository->findByEtatWithProduit('panne');
        if ($items === []) {
            return ['context' => 'No broken equipment.', 'summary' => 'No panne equipment'];
        }

        $lines = ["| Materiel | Produit |", "|---|---|"];
        foreach (array_slice($items, 0, 20) as $m) {
            $lines[] = sprintf('| %s | %s |', $m->getNom(), $m->getProduit()?->getNom() ?? '-');
        }

        return ['context' => implode("\n", $lines), 'summary' => 'Broken equipment listed'];
    }

    /**
     * @return array{context: string, summary: string}
     */
    private function buildCost(): array
    {
        $rows = $this->materielRepository->sumCostByProduit();
        if ($rows === []) {
            return ['context' => 'No materiel cost data.', 'summary' => 'No cost data'];
        }

        $lines = ["| Produit | Cout total |", "|---|---|"];
        foreach (array_slice($rows, 0, 20) as $row) {
            $lines[] = sprintf('| %s | %.2f |', $row['produit_nom'], (float) $row['total_cout']);
        }

        return ['context' => implode("\n", $lines), 'summary' => 'Cost table generated'];
    }
}
