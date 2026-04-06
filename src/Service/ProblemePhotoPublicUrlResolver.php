<?php

namespace App\Service;

/**
 * Ne renvoie une URL d’affichage que si le fichier existe — évite les vignettes cassées.
 */
final class ProblemePhotoPublicUrlResolver
{
    public function __construct(
        private readonly string $projectDir,
    ) {
    }

    /**
     * Retourne un chemin relatif à passer à asset(), une URL absolue, ou null.
     */
    public function resolveForDisplay(?string $path): ?string
    {
        if ($path === null || trim($path) === '') {
            return null;
        }
        $path = trim($path);
        if (preg_match('#^https?://#i', $path)) {
            return $path;
        }
        if (str_starts_with($path, '/')) {
            return $path;
        }
        $normalized = str_replace('\\', '/', $path);
        if (str_contains($normalized, '..')) {
            return null;
        }
        $full = $this->projectDir . '/public/' . $normalized;
        if (!is_file($full)) {
            return null;
        }

        return $normalized;
    }
}
