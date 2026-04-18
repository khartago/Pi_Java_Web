<?php

namespace App\Service;

use App\Dto\AssistantIntent;
use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;

final class AssistantContextBuilder
{
    public function __construct(
        private readonly ProduitRepository $produitRepo,
        private readonly MaterielRepository $materielRepo,
        private readonly int $stockThreshold,
    ) {
    }

    /**
     * @return array{context: string, summary: string}
     */
    public function build(AssistantIntent $intent): array
    {
        return match ($intent->name) {
            'stock.list'     => $this->buildStockList(),
            'stock.search'   => $this->buildStockSearch((string) $intent->param('query', '')),
            'stock.low'      => $this->buildLowStock(),
            'stock.expiring' => $this->buildExpiring((int) $intent->param('withinDays', 30)),
            'materiel.broken' => $this->buildBrokenEquipment(),
            'materiel.cost'  => $this->buildEquipmentCost(),
            default          => ['context' => '', 'summary' => ''],
        };
    }

    /** @return array{context: string, summary: string} */
    private function buildStockList(): array
    {
        $products = $this->produitRepo->findForList();
        if ($products === []) {
            return ['context' => 'STOCK_CONTEXT: No products in stock.', 'summary' => 'Stock is empty.'];
        }

        $rows = ["| Produit | Quantité | Unité | Expiration |", "|---|---|---|---|"];
        $capped = array_slice($products, 0, 30);
        foreach ($capped as $p) {
            $rows[] = sprintf(
                '| %s | %d | %s | %s |',
                $p->getNom(),
                (int) $p->getQuantite(),
                $p->getUnite(),
                $p->getDateExpiration()?->format('Y-m-d') ?? '—',
            );
        }

        $total = count($products);
        $summary = sprintf('%d produit(s) en stock', $total);
        if ($total > 30) {
            $summary .= ' (top 30 shown)';
        }

        return [
            'context' => "STOCK_CONTEXT:\n" . implode("\n", $rows),
            'summary' => $summary,
        ];
    }

    /** @return array{context: string, summary: string} */
    private function buildStockSearch(string $query): array
    {
        if (trim($query) === '') {
            return $this->buildStockList();
        }

        $products = $this->produitRepo->findForList($query);
        if ($products === []) {
            return [
                'context' => sprintf('STOCK_CONTEXT: No product matching "%s" found.', $query),
                'summary' => sprintf('No match for "%s".', $query),
            ];
        }

        $rows = ["| Produit | Quantité | Unité | Expiration |", "|---|---|---|---|"];
        foreach (array_slice($products, 0, 10) as $p) {
            $rows[] = sprintf(
                '| %s | %d | %s | %s |',
                $p->getNom(),
                (int) $p->getQuantite(),
                $p->getUnite(),
                $p->getDateExpiration()?->format('Y-m-d') ?? '—',
            );
        }

        return [
            'context' => "STOCK_CONTEXT (search: \"$query\"):\n" . implode("\n", $rows),
            'summary' => sprintf('%d result(s) for "%s"', count($products), $query),
        ];
    }

    /** @return array{context: string, summary: string} */
    private function buildLowStock(): array
    {
        $products = $this->produitRepo->findLowStock($this->stockThreshold);
        if ($products === []) {
            return [
                'context' => sprintf('STOCK_CONTEXT: No products below the alert threshold (%d).', $this->stockThreshold),
                'summary' => 'No low-stock products.',
            ];
        }

        $rows = [
            sprintf("| Produit | Quantité | Unité | Seuil alerte |"),
            "|---|---|---|---|",
        ];
        foreach (array_slice($products, 0, 20) as $p) {
            $rows[] = sprintf(
                '| %s | **%d** | %s | %d |',
                $p->getNom(),
                (int) $p->getQuantite(),
                $p->getUnite(),
                $this->stockThreshold,
            );
        }

        return [
            'context' => "STOCK_CONTEXT (low stock, threshold={$this->stockThreshold}):\n" . implode("\n", $rows),
            'summary' => sprintf('%d produit(s) en rupture ou sous le seuil (%d)', count($products), $this->stockThreshold),
        ];
    }

    /** @return array{context: string, summary: string} */
    private function buildExpiring(int $withinDays): array
    {
        $until = new \DateTimeImmutable("+{$withinDays} days");
        $products = $this->produitRepo->findExpiringBefore($until);

        if ($products === []) {
            return [
                'context' => sprintf('STOCK_CONTEXT: No products expiring within %d days.', $withinDays),
                'summary' => sprintf('Nothing expiring in the next %d days.', $withinDays),
            ];
        }

        $rows = ["| Produit | Quantité | Unité | Expire le |", "|---|---|---|---|"];
        foreach (array_slice($products, 0, 20) as $p) {
            $rows[] = sprintf(
                '| %s | %d | %s | **%s** |',
                $p->getNom(),
                (int) $p->getQuantite(),
                $p->getUnite(),
                $p->getDateExpiration()?->format('Y-m-d') ?? '—',
            );
        }

        return [
            'context' => "STOCK_CONTEXT (expiring within {$withinDays} days):\n" . implode("\n", $rows),
            'summary' => sprintf('%d produit(s) expire(nt) dans les %d prochains jours', count($products), $withinDays),
        ];
    }

    /** @return array{context: string, summary: string} */
    private function buildBrokenEquipment(): array
    {
        $items = $this->materielRepo->findByEtatWithProduit('panne');
        if ($items === []) {
            return [
                'context' => 'STOCK_CONTEXT: No equipment in "panne" state.',
                'summary' => 'No broken equipment.',
            ];
        }

        $rows = ["| Matériel | Produit lié | Date achat | Coût |", "|---|---|---|---|"];
        foreach (array_slice($items, 0, 20) as $m) {
            $rows[] = sprintf(
                '| **%s** | %s | %s | %.2f € |',
                $m->getNom(),
                $m->getProduit()?->getNom() ?? '—',
                $m->getDateAchat()?->format('Y-m-d') ?? '—',
                (float) $m->getCout(),
            );
        }

        return [
            'context' => "STOCK_CONTEXT (matériel en panne):\n" . implode("\n", $rows),
            'summary' => sprintf('%d matériel(s) en panne', count($items)),
        ];
    }

    /** @return array{context: string, summary: string} */
    private function buildEquipmentCost(): array
    {
        $rows_data = $this->materielRepo->sumCostByProduit();
        if ($rows_data === []) {
            return [
                'context' => 'STOCK_CONTEXT: No equipment cost data available.',
                'summary' => 'No equipment recorded.',
            ];
        }

        $totalGlobal = array_sum(array_column($rows_data, 'total_cout'));
        $rows = ["| Produit | Nb matériels | Coût total |", "|---|---|---|"];
        foreach (array_slice($rows_data, 0, 20) as $row) {
            $rows[] = sprintf(
                '| %s | %d | %.2f € |',
                $row['produit_nom'],
                (int) $row['count'],
                (float) $row['total_cout'],
            );
        }
        $rows[] = sprintf('| **TOTAL** | — | **%.2f €** |', $totalGlobal);

        return [
            'context' => "STOCK_CONTEXT (coûts matériel par produit):\n" . implode("\n", $rows),
            'summary' => sprintf('Coût total matériel : %.2f €', $totalGlobal),
        ];
    }
}
