<?php

namespace App\Service;

use App\Dto\AssistantIntent;

final class AssistantIntentRouter
{
    public function detect(string $message): AssistantIntent
    {
        $text = mb_strtolower(trim($message));
        if ($this->matches($text, ['rupture', 'stock bas', 'low stock', 'out of stock'])) {
            return new AssistantIntent('stock.low');
        }
        if ($this->matches($text, ['expire', 'expiration', 'expiring'])) {
            return new AssistantIntent('stock.expiring');
        }
        if ($this->matches($text, ['panne', 'broken', 'damaged'])) {
            return new AssistantIntent('materiel.broken');
        }
        if ($this->matches($text, ['cost', 'cout', 'prix materiel'])) {
            return new AssistantIntent('materiel.cost');
        }
        if ($this->matches($text, ['stock', 'inventaire', 'inventory'])) {
            return new AssistantIntent('stock.list');
        }

        return new AssistantIntent('unknown');
    }

    /**
     * @param list<string> $keywords
     */
    private function matches(string $text, array $keywords): bool
    {
        foreach ($keywords as $keyword) {
            if (str_contains($text, $keyword)) {
                return true;
            }
        }

        return false;
    }
}
