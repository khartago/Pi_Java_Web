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
    #[Route('/assistant', name: 'app_assistant_index', methods: ['GET'])]
    public function index(): Response
    {
        return $this->render('assistant/index.html.twig');
    }

    #[Route('/assistant/chat', name: 'app_assistant_chat', methods: ['POST'])]
    public function chat(
        Request $request,
        AiAssistantClient $client,
        AssistantIntentRouter $router,
        AssistantContextBuilder $contextBuilder,
    ): JsonResponse {
        // Admin-only endpoint — same guard used across the back-office
        $uiMode = $request->hasSession() ? $request->getSession()->get('ui_mode', 'admin') : 'admin';
        if ($uiMode !== 'admin') {
            return $this->json(['error' => 'Assistant is only available in admin mode.'], Response::HTTP_FORBIDDEN);
        }

        try {
            $payload = $request->toArray();
        } catch (\JsonException) {
            return $this->json(['error' => 'Invalid JSON payload.'], Response::HTTP_BAD_REQUEST);
        }

        $language = is_string($payload['language'] ?? null) ? $payload['language'] : 'auto';
        $messages = is_array($payload['messages'] ?? null) ? $payload['messages'] : [];

        $cleanMessages = $this->sanitizeMessages($messages);

        if ($cleanMessages === []) {
            return $this->json(['error' => 'Message is required.'], Response::HTTP_BAD_REQUEST);
        }

        // Detect intent from the latest user message
        $lastUserMessage = '';
        foreach (array_reverse($cleanMessages) as $msg) {
            if ($msg['role'] === 'user') {
                $lastUserMessage = $msg['content'];
                break;
            }
        }

        $intent = $router->detect($lastUserMessage, $language);
        ['context' => $context, 'summary' => $summary] = $contextBuilder->build($intent);

        $systemPrompt = $this->buildSystemPrompt($language, $context);

        try {
            $reply = $client->chat($systemPrompt, $cleanMessages);
        } catch (\RuntimeException $exception) {
            $status = $exception->getCode();
            if (!is_int($status) || $status < 400 || $status > 599) {
                $status = Response::HTTP_BAD_REQUEST;
            }

            return $this->json(['error' => $exception->getMessage()], $status);
        } catch (\Throwable) {
            return $this->json(['error' => 'Assistant is unavailable right now.'], Response::HTTP_INTERNAL_SERVER_ERROR);
        }

        return $this->json([
            'reply'   => $reply,
            'intent'  => $intent->name,
            'summary' => $summary,
        ]);
    }

    /**
     * @param array<int, mixed> $messages
     *
     * @return list<array{role: string, content: string}>
     */
    private function sanitizeMessages(array $messages): array
    {
        $clean = [];

        foreach ($messages as $message) {
            if (!is_array($message)) {
                continue;
            }

            $role    = $message['role'] ?? '';
            $content = $message['content'] ?? '';

            if (!is_string($role) || !is_string($content)) {
                continue;
            }

            $role = strtolower(trim($role));
            if (!in_array($role, ['user', 'assistant'], true)) {
                continue;
            }

            $content = trim($content);
            if ($content === '') {
                continue;
            }

            $clean[] = [
                'role'    => $role,
                'content' => $this->limitText($content, 1200),
            ];
        }

        if (count($clean) > 8) {
            $clean = array_slice($clean, -8);
        }

        return $clean;
    }

    private function buildSystemPrompt(string $language, string $context): string
    {
        $language = strtolower($language);

        $languageHint = match ($language) {
            'fr'         => 'Reply in French.',
            'en'         => 'Reply in English.',
            'bilingual'  => 'Reply in French first, then English.',
            default      => 'Reply in the language used by the user. If mixed, use the most recent language.',
        };

        $base = 'You are the Farmtech Stock assistant for an agricultural management back-office. Be concise, practical, and friendly.';

        if (trim($context) !== '') {
            return $base . ' Answer using the STOCK_CONTEXT below. If the answer is not in the context, say you don\'t know but suggest checking the admin panel. ' . $languageHint . "\n\n" . $context;
        }

        return $base . ' You do not have live database access for this question — answer from general agricultural knowledge or say you don\'t know. ' . $languageHint;
    }

    private function limitText(string $text, int $max): string
    {
        if (function_exists('mb_strlen') && function_exists('mb_substr')) {
            if (mb_strlen($text) <= $max) {
                return $text;
            }

            return mb_substr($text, 0, $max);
        }

        if (strlen($text) <= $max) {
            return $text;
        }

        return substr($text, 0, $max);
    }
}
