<?php

namespace App\Service;

use App\Dto\AssistantIntent;

final class AssistantIntentRouter
{
    public function detect(string $message, string $language = 'auto'): AssistantIntent
    {
        $text = mb_strtolower(trim($message));

        // materiel.broken — check before generic stock to avoid overlap
        if ($this->matches($text, [
            'panne', 'cassé', 'cassee', 'en panne', 'matériel cassé', 'materiel casse',
            'broken', 'broken equipment', 'equipment down', 'out of order', 'damaged',
        ])) {
            return new AssistantIntent('materiel.broken');
        }

        // materiel.cost
        if ($this->matches($text, [
            'coût matériel', 'cout materiel', 'combien coûte', 'combien coute',
            'prix matériel', 'prix materiel', 'valeur matériel', 'valeur materiel',
            'equipment cost', 'cost of equipment', 'how much does the equipment',
        ])) {
            return new AssistantIntent('materiel.cost');
        }

        // stock.low — before stock.list so "stock bas" does not fall through
        if ($this->matches($text, [
            'rupture', 'stock bas', 'stock faible', 'alerte stock', 'en rupture',
            'manque', 'presque vide', 'bientôt épuisé', 'bientot epuise',
            'low stock', 'running out', 'almost out', 'out of stock', 'stock alert', 'critical stock',
        ])) {
            return new AssistantIntent('stock.low');
        }

        // stock.expiring
        if ($this->matches($text, [
            'périme', 'perime', 'expiration', 'expire', 'péremption', 'peremption',
            'bientôt', 'bientot', 'date limite', 'dlc', 'ddm',
            'expiring', 'expires', 'expiry', 'going bad', 'soon expire',
        ])) {
            $withinDays = $this->extractDays($text) ?? 30;
            return new AssistantIntent('stock.expiring', ['withinDays' => $withinDays]);
        }

        // stock.search — "do I have X" / "combien de X"
        if ($this->matchesPattern($text, [
            '/combien d[e\']\s+(.+)/u',
            '/est[- ]ce que j\'?ai d[e\'u]\s+(.+)/u',
            '/as[- ]tu d[e\'u]\s+(.+)/u',
            '/cherche\s+(.+)/u',
            '/trouve\s+(.+)/u',
            '/do (i|we) have\s+(.+)/u',
            '/how much\s+(.+)/u',
            '/search for\s+(.+)/u',
        ], $query)) {
            return new AssistantIntent('stock.search', ['query' => trim((string) $query)]);
        }

        // stock.list — generic stock / inventory overview
        if ($this->matches($text, [
            'stock', 'inventaire', 'produits', 'liste des produits', 'qu\'est-ce que j\'ai',
            "qu'est ce que j'ai", 'mes produits', 'mes stocks', 'en stock',
            'inventory', 'products', 'what do i have', 'what\'s in stock', 'whats in stock',
            'show me the stock', 'list products',
        ])) {
            return new AssistantIntent('stock.list');
        }

        return new AssistantIntent('unknown');
    }

    /** @param list<string> $keywords */
    private function matches(string $text, array $keywords): bool
    {
        foreach ($keywords as $kw) {
            if (str_contains($text, $kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param list<string> $patterns
     * @param-out string|null $capture
     */
    private function matchesPattern(string $text, array $patterns, ?string &$capture): bool
    {
        foreach ($patterns as $pattern) {
            if (preg_match($pattern, $text, $m)) {
                $capture = $m[array_key_last($m)] ?? null;
                return true;
            }
        }
        return false;
    }

    private function extractDays(string $text): ?int
    {
        if (preg_match('/(\d+)\s*(?:jours?|days?)/u', $text, $m)) {
            return (int) $m[1];
        }
        if (str_contains($text, 'semaine') || str_contains($text, 'week')) {
            return 7;
        }
        if (str_contains($text, 'mois') || str_contains($text, 'month')) {
            return 30;
        }
        return null;
    }
}
