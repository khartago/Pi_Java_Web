<?php
namespace App\Service;

use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;
class MailerService
{
    private $mailer;

    public function __construct(MailerInterface $mailer)
    {
        $this->mailer = $mailer;
    }

    public function sendPlantDeathEmail($plantName)
    {
        $email = (new TemplatedEmail())
            ->from('tahajaballah07@gmail.com')
            ->to('tahabenjaballah@gmail.com') // you receive it
            ->subject('🌱 Plant Died Alert')
                ->htmlTemplate('email/plant_dead.html.twig')
    ->context([
        'plantName' => 'Tomato',
        'temp' => 28
        
    ]);

        $this->mailer->send($email);
    }



}