<?php

namespace App\Service;

use Symfony\Bridge\Twig\Mime\TemplatedEmail;
use Symfony\Component\Mailer\MailerInterface;

class GameMailerService
{
    public function __construct(
        private readonly MailerInterface $mailer,
        private readonly string $fromEmail,
        private readonly string $toEmail,
    ) {
    }

    public function sendPlantDeathEmail(
        string $plantName,
        ?float $temp = null,
        string $reasonLabel = 'Timer epuise (non arrose a temps)',
        ?\DateTimeInterface $occurredAt = null
    ): void
    {
        $occurredAt ??= new \DateTime();

        $email = (new TemplatedEmail())
            ->from($this->fromEmail)
            ->to($this->toEmail)
            ->subject('Alerte FarmTech: plante perdue dans le jeu')
            ->htmlTemplate('email/plant_dead.html.twig')
            ->context([
                'plantName' => $plantName,
                'temp' => $temp,
                'reasonLabel' => $reasonLabel,
                'occurredAt' => $occurredAt,
            ]);

        $this->mailer->send($email);
    }
}
