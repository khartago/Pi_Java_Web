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

    public function sendPlantDeathEmail(string $plantName, ?float $temp = null): void
    {
        $email = (new TemplatedEmail())
            ->from($this->fromEmail)
            ->to($this->toEmail)
            ->subject('Plant Died Alert')
            ->htmlTemplate('email/plant_dead.html.twig')
            ->context([
                'plantName' => $plantName,
                'temp' => $temp,
            ]);

        $this->mailer->send($email);
    }
}
