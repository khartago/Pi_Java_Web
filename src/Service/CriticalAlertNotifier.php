<?php

namespace App\Service;

use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Twig\Environment;

final class CriticalAlertNotifier
{
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly MailerInterface $mailer,
        private readonly Environment $twig,
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
            return ['sent' => false, 'low_stock_count' => 0, 'panne_count' => 0];
        }

        $html = $this->twig->render('emails/critical_alert.html.twig', [
            'threshold' => $stockThreshold,
            'low_stock_count' => $lowStockCount,
            'panne_count' => $panneCount,
            'low_stock_products' => $lowStockProducts,
            'broken_materiels' => $brokenMateriels,
        ]);

        $email = (new Email())
            ->from($fromEmail)
            ->to($toEmail)
            ->subject(sprintf('[Farmtech] Alertes critiques (%d stock bas, %d pannes)', $lowStockCount, $panneCount))
            ->html($html);

        $this->mailer->send($email);

        return ['sent' => true, 'low_stock_count' => $lowStockCount, 'panne_count' => $panneCount];
    }
}
