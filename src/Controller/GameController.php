<?php

namespace App\Controller;
use Symfony\Component\Mailer\MailerInterface;
use App\Service\GameService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Service\MailerService;
use Symfony\Component\Mime\Email;
use Symfony\Component\HttpFoundation\Request;
class GameController extends AbstractController
{
    #[Route('/game', name: 'app_game')]
    public function index(GameService $gameService): Response
    {
        $cards = $gameService->getGameCards();

        return $this->render('game/index.html.twig', [
            'cards' => $cards
        ]);
    }
#[Route('/plant/dead', name: 'plant_dead', methods: ['POST'])]
public function plantDead(MailerService $mailerService)
{
    $mailerService->sendPlantDeathEmail("Plant Dead");

    return new Response('ok');
}
#[Route('/test-mail', name: 'test_mail')]
public function testMail(MailerInterface $mailer): Response
{
    try {
        $email = (new Email())
            ->from('tahajaballah07@gmail.com')
            ->to('tahabenjaballah@gmail.com')
            ->subject('Test Email 🚀')
            ->text('Symfony mail is working!');

        $mailer->send($email);

        return new Response('✅ Email sent!');
    } catch (\Exception $e) {
        return new Response('❌ Error: ' . $e->getMessage());
    }
}
}