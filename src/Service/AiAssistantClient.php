<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

final class AiAssistantClient
{
    private const DEFAULT_TIMEOUT = 20;

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
        if (trim($this->apiKey) === '' && !$this->isLocalBaseUrl()) {
            throw new \RuntimeException('Missing OPENAI_API_KEY.');
        }

        $payload = [
            'model' => $this->model,
            'messages' => array_merge(
                [['role' => 'system', 'content' => $systemPrompt]],
                $messages,
            ),
            'temperature' => 0.4,
            'max_tokens' => 450,
        ];

        $headers = [
            'Content-Type' => 'application/json',
        ];

        if (trim($this->apiKey) !== '') {
            $headers['Authorization'] = 'Bearer ' . $this->apiKey;
            $headers['X-API-Key'] = $this->apiKey;
        }

        $response = $this->httpClient->request('POST', rtrim($this->baseUrl, '/') . '/chat/completions', [
            'headers' => $headers,
            'json' => $payload,
            'timeout' => self::DEFAULT_TIMEOUT,
        ]);

        $status = $response->getStatusCode();
        $rawContent = $response->getContent(false);
        $data = json_decode($rawContent, true);

        if ($status >= 400) {
            $message = 'Assistant request failed (status ' . $status . ').';
            if (is_array($data)) {
                if (isset($data['error']['message']) && is_string($data['error']['message'])) {
                    $message = $data['error']['message'];
                } elseif (isset($data['message']) && is_string($data['message'])) {
                    $message = $data['message'];
                } elseif (isset($data['detail']) && is_string($data['detail'])) {
                    $message = $data['detail'];
                }
            }
            throw new \RuntimeException($message, $status);
        }

        if (!is_array($data)) {
            throw new \RuntimeException('Assistant returned invalid JSON.');
        }

        $content = $data['choices'][0]['message']['content'] ?? null;
        if (!is_string($content)) {
            throw new \RuntimeException('Assistant returned an empty response.');
        }

        return trim($content);
    }

    private function isLocalBaseUrl(): bool
    {
        $host = parse_url($this->baseUrl, PHP_URL_HOST);
        if (!is_string($host)) {
            return false;
        }

        return in_array(strtolower($host), ['localhost', '127.0.0.1'], true);
    }
}
