<?php

namespace App\Controller;

use App\Service\AiAssistantClient;
use App\Service\AssistantContextBuilder;
use App\Service\AssistantIntentRouter;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class AssistantController extends AbstractController
{
    #[Route('/admin/assistant', name: 'app_assistant_index', methods: ['GET'])]
    public function index(): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        return $this->render('assistant/index.html.twig');
    }

    #[Route('/admin/assistant/chat', name: 'app_assistant_chat', methods: ['POST'])]
    public function chat(
        Request $request,
        AiAssistantClient $client,
        AssistantIntentRouter $router,
        AssistantContextBuilder $contextBuilder,
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        $payload = $request->toArray();
        $messages = is_array($payload['messages'] ?? null) ? $payload['messages'] : [];
        $last = '';
        foreach (array_reverse($messages) as $message) {
            if (($message['role'] ?? null) === 'user' && is_string($message['content'] ?? null)) {
                $last = trim($message['content']);
                break;
            }
        }
        if ($last === '') {
            return $this->json(['error' => 'Message is required.'], Response::HTTP_BAD_REQUEST);
        }

        $intent = $router->detect($last);
        ['context' => $context, 'summary' => $summary] = $contextBuilder->build($intent);
        $system = "You are FarmTech assistant. Use STOCK_CONTEXT first. If unknown, say you do not know.\n\nSTOCK_CONTEXT:\n".$context;

        try {
            $reply = $client->chat($system, $messages);
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], Response::HTTP_BAD_REQUEST);
        }

        return $this->json(['reply' => $reply, 'intent' => $intent->name, 'summary' => $summary]);
    }
}
