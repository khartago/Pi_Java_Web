<?php

namespace App\Service;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;

final class DiagnosticApprovalNotifier
{
    public function __construct(
        private readonly MailerInterface $mailer,
        private readonly string $fromEmail,
    ) {
    }

    public function notifyFarmer(Probleme $probleme, Diagnostique $diagnostique): void
    {
        $user = $probleme->getUtilisateur();
        if (null === $user || null === $user->getEmail()) {
            return;
        }

        $email = (new TemplatedEmail())
            ->from(new Address($this->fromEmail, 'FARMTECH'))
            ->to(new Address($user->getEmail(), $user->getNom() ?? ''))
            ->subject('FARMTECH – Votre diagnostic est disponible')
            ->htmlTemplate('emails/diagnostic_approved.html.twig')
            ->textTemplate('emails/diagnostic_approved.txt.twig')
            ->context([
                'farmerName' => $user->getNom() ?? 'Agriculteur',
                'probleme' => $probleme,
                'diagnostic' => $diagnostique,
            ]);

        $this->mailer->send($email);
    }
}
