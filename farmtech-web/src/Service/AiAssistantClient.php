<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

final class AiAssistantClient
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $apiKey,
        private readonly string $model,
        private readonly string $baseUrl,
    ) {
    }

    /**
     * @param list<array{role: string, content: string}> $messages
     */
    public function chat(string $systemPrompt, array $messages): string
    {
        if (trim($this->apiKey) === '') {
            throw new \RuntimeException('Missing assistant API key.');
        }

        $response = $this->httpClient->request('POST', rtrim($this->baseUrl, '/') . '/chat/completions', [
            'headers' => [
                'Content-Type' => 'application/json',
                'Authorization' => 'Bearer ' . $this->apiKey,
            ],
            'json' => [
                'model' => $this->model,
                'messages' => array_merge([['role' => 'system', 'content' => $systemPrompt]], $messages),
                'temperature' => 0.4,
                'max_tokens' => 450,
            ],
            'timeout' => 20,
        ]);

        $data = $response->toArray(false);
        $status = $response->getStatusCode();
        if ($status >= 400) {
            throw new \RuntimeException($data['error']['message'] ?? ('Assistant request failed ('.$status.').'), $status);
        }

        $content = $data['choices'][0]['message']['content'] ?? null;
        if (!is_string($content) || trim($content) === '') {
            throw new \RuntimeException('Assistant returned an empty response.');
        }

        return trim($content);
    }
}
