<?php

namespace App\Util;

/**
 * Présentation des stats : répartition en % du total (pas du max par catégorie).
 */
final class StatsPresentation
{
    /**
     * @param array<string|int, int> $byKey
     *
     * @return array{total: int, items: array<string|int, array{count: int, pct: float}>}
     */
    public static function distribution(array $byKey): array
    {
        $total = array_sum($byKey);
        $items = [];
        foreach ($byKey as $k => $c) {
            $items[$k] = [
                'count' => $c,
                'pct' => $total > 0 ? round(100 * $c / $total, 1) : 0.0,
            ];
        }

        return ['total' => $total, 'items' => $items];
    }
}
