<?php

namespace App\Service;

use App\Entity\Probleme;
use Symfony\Component\HttpFoundation\File\UploadedFile;

final class ProblemePhotoStorage
{
    /** Préfixe des chemins stockés en base (relatif à public/). */
    private const PUBLIC_PREFIX = 'uploads/problemes';

    public function __construct(
        private readonly string $targetDirectory,
    ) {
    }

    /**
     * Enregistre les fichiers uploadés et ajoute leurs chemins web publics à {@see Probleme::photos} (séparateur ;).
     *
     * @param array<int, UploadedFile>|UploadedFile|null $uploaded
     */
    public function appendUploadsToProbleme(Probleme $probleme, array|UploadedFile|null $uploaded): void
    {
        $files = [];
        if ($uploaded instanceof UploadedFile) {
            $files = [$uploaded];
        } elseif (\is_array($uploaded)) {
            $files = $uploaded;
        }

        $paths = $this->storeUploads($files);
        if ($paths === []) {
            return;
        }

        $existing = $probleme->getPhotos();
        $segments = $existing ? array_filter(array_map('trim', explode(';', $existing))) : [];
        $merged = array_merge($segments, $paths);
        $probleme->setPhotos(implode(';', $merged));
    }

    /**
     * @param iterable<int, UploadedFile> $files
     *
     * @return list<string> chemins relatifs (ex. uploads/problemes/abc.jpg)
     */
    public function storeUploads(iterable $files): array
    {
        if (!is_dir($this->targetDirectory)) {
            mkdir($this->targetDirectory, 0775, true);
        }

        $paths = [];
        foreach ($files as $file) {
            if (!$file->isValid()) {
                continue;
            }

            $ext = $file->guessExtension();
            if ('' === (string) $ext) {
                $ext = strtolower((string) $file->getClientOriginalExtension()) ?: 'jpg';
            }
            $ext = preg_replace('/[^a-z0-9]/', '', strtolower($ext)) ?: 'jpg';
            $name = bin2hex(random_bytes(16)).'.'.$ext;
            $file->move($this->targetDirectory, $name);
            $paths[] = self::PUBLIC_PREFIX.'/'.$name;
        }

        return $paths;
    }
}
