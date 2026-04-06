<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\String\Slugger\SluggerInterface;

class ProductImageStorage
{
    public function __construct(
        private readonly string $productUploadDir,
        private readonly string $productUploadPath,
        private readonly SluggerInterface $slugger,
    ) {
    }

    public function store(UploadedFile $uploadedFile): string
    {
        $originalName = pathinfo($uploadedFile->getClientOriginalName(), PATHINFO_FILENAME);
        $safeName = $this->slugger->slug($originalName)->lower()->toString();
        $extension = $uploadedFile->guessExtension() ?: $uploadedFile->getClientOriginalExtension() ?: 'bin';
        $filename = sprintf('%s-%s.%s', $safeName ?: 'produit', bin2hex(random_bytes(6)), $extension);

        $uploadedFile->move($this->productUploadDir, $filename);

        return sprintf('%s/%s', trim($this->productUploadPath, '/'), $filename);
    }

    public function remove(?string $relativePath): void
    {
        if ($relativePath === null || $relativePath === '') {
            return;
        }

        $filename = basename($relativePath);
        $absolutePath = $this->productUploadDir.DIRECTORY_SEPARATOR.$filename;

        if (is_file($absolutePath)) {
            @unlink($absolutePath);
        }
    }
}
