<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class WebNewsService
{
    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {
    }

    /**
     * @return array<string, mixed>
     */
    public function getNews(): array
    {
        if (trim($this->apiKey) === '') {
            return ['articles' => []];
        }

        return $this->client->request('GET', 'https://newsapi.org/v2/everything', [
            'query' => [
                'q' => 'agriculture',
                'sortBy' => 'publishedAt',
                'apiKey' => $this->apiKey,
                'language' => 'fr',
                'pageSize' => 12,
            ],
        ])->toArray(false);
    }
}
