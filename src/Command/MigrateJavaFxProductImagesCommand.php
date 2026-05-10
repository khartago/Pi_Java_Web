<?php

namespace App\Command;

use App\Repository\ProduitRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpKernel\KernelInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:images:migrate-javafx',
    description: 'Migrate JavaFX product image paths to web uploads/products paths.',
)]
class MigrateJavaFxProductImagesCommand extends Command
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly EntityManagerInterface $entityManager,
        KernelInterface $kernel,
    ) {
        $this->projectDir = $kernel->getProjectDir();
        parent::__construct();
    }

    private string $projectDir;

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $targetDir = $this->projectDir . '/public/uploads/products';
        if (!is_dir($targetDir) && !@mkdir($targetDir, 0775, true) && !is_dir($targetDir)) {
            $io->error('Cannot create target uploads directory.');
            return Command::FAILURE;
        }

        $updated = 0;
        $missing = 0;

        foreach ($this->produitRepository->findAll() as $produit) {
            $rawPath = trim((string) $produit->getImagePath());
            if ($rawPath === '') {
                continue;
            }

            $normalized = str_replace('\\', '/', $rawPath);

            // Already web-friendly
            if (str_starts_with($normalized, 'uploads/products/')) {
                continue;
            }

            // Normalize legacy French folder name
            if (str_starts_with($normalized, 'uploads/produits/')) {
                $newPath = 'uploads/products/' . basename($normalized);
                $produit->setImagePath($newPath);
                $updated++;
                continue;
            }

            $filename = basename($normalized);
            if ($filename === '' || $filename === '.' || $filename === '..') {
                continue;
            }

            $targetPath = $targetDir . '/' . $filename;
            if (!is_file($targetPath)) {
                if (is_file($normalized)) {
                    @copy($normalized, $targetPath);
                } else {
                    $missing++;
                    continue;
                }
            }

            $produit->setImagePath('uploads/products/' . $filename);
            $updated++;
        }

        $this->entityManager->flush();

        $io->success(sprintf('Image migration complete. Updated: %d, Missing source files: %d', $updated, $missing));
        return Command::SUCCESS;
    }
}
