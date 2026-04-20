<?php

namespace App\Service;

use App\Repository\ProduitRepository;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Twig\Environment;

class StockNotificationService
{
    public function __construct(
        private MailerInterface $mailer,
        private Environment $twig,
        private ProduitRepository $produitRepository,
        private string $from,
        private string $to,
        private int $threshold,
    ) {
    }

    public function sendLowStockAlert(): void
    {
        $produits = $this->produitRepository->findLowStock($this->threshold);
        if ($produits === []) {
            return;
        }

        $html = $this->twig->render('emails/stock_alert.html.twig', [
            'produits' => $produits,
            'threshold' => $this->threshold,
        ]);

        $email = (new Email())
            ->from($this->from)
            ->to($this->to)
            ->subject('Alerte stock faible')
            ->html($html);
        $this->mailer->send($email);
    }
}
