<?php

namespace App\Service;

use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

final class CriticalAlertNotifier
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly MailerInterface $mailer,
    ) {
    }

    /**
     * @return array{sent: bool, low_stock_count: int, panne_count: int}
     */
    public function sendCriticalAlerts(string $fromEmail, string $toEmail, int $stockThreshold): array
    {
        $lowStockProducts = $this->produitRepository->findLowStock($stockThreshold);
        $brokenMateriels = $this->materielRepository->findByEtatWithProduit('panne');

        $lowStockCount = count($lowStockProducts);
        $panneCount = count($brokenMateriels);

        if ($lowStockCount === 0 && $panneCount === 0) {
            return [
                'sent' => false,
                'low_stock_count' => 0,
                'panne_count' => 0,
            ];
        }

        $lines = [
            'Alertes critiques Farmtech',
            '',
            sprintf('Seuil stock bas: %d', $stockThreshold),
            '',
            sprintf('Produits en stock bas: %d', $lowStockCount),
        ];

        foreach ($lowStockProducts as $produit) {
            $lines[] = sprintf(
                '- %s (ID #%d): %d %s',
                $produit->getNom(),
                (int) $produit->getIdProduit(),
                (int) $produit->getQuantite(),
                $produit->getUnite(),
            );
        }

        $lines[] = '';
        $lines[] = sprintf('Matériels en panne: %d', $panneCount);

        foreach ($brokenMateriels as $materiel) {
            $produit = $materiel->getProduit();
            $lines[] = sprintf(
                '- %s (ID #%d), produit: %s (ID #%d)',
                $materiel->getNom(),
                (int) $materiel->getIdMateriel(),
                $produit?->getNom() ?? 'N/A',
                (int) ($produit?->getIdProduit() ?? 0),
            );
        }

        $email = (new Email())
            ->from($fromEmail)
            ->to($toEmail)
            ->subject(sprintf('[Farmtech] Alertes critiques (%d stock bas, %d pannes)', $lowStockCount, $panneCount))
            ->text(implode("\n", $lines));

        $this->mailer->send($email);

        return [
            'sent' => true,
            'low_stock_count' => $lowStockCount,
            'panne_count' => $panneCount,
        ];
    }
}
