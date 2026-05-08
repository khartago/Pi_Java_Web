<?php

namespace App\Twig;

use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

final class AppExtension extends AbstractExtension
{
    public function getFilters(): array
    {
        return [
            new TwigFilter('json_decode', static function (?string $json): mixed {
                if (null === $json || '' === trim($json)) {
                    return null;
                }
                try {
                    return json_decode($json, true, 512, JSON_THROW_ON_ERROR);
                } catch (\Throwable) {
                    return null;
                }
            }),
        ];
    }
}
