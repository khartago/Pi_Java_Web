<?php
namespace App\Service;

use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class MailerService
{
    private $mailer;

    public function __construct(MailerInterface $mailer)
    {
        $this->mailer = $mailer;
    }

    public function sendPlantDeathEmail($plantName)
    {
        $email = (new Email())
            ->from('tahajaballah07@gmail.com')
            ->to('tahabenjaballah@gmail.com') // you receive it
            ->subject('Plant Dead 🚨')
            ->text("Your plant '$plantName' has died.");

        $this->mailer->send($email);
    }
}