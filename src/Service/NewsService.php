<?php
namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class NewsService
{
    private $client;
    private $apiKey;

    public function __construct(HttpClientInterface $client, string $apiKey)
    {
        $this->client = $client;
        $this->apiKey = $apiKey;
    }

    public function getNews()
    {
        $response = $this->client->request(
            'GET',
            'https://newsapi.org/v2/everything',
            [
                'query' => [
                    'q' => 'agriculture',
                    'sortBy' => 'publishedAt',
                    'apiKey' => $this->apiKey
                ]
            ]
        );

        return $response->toArray();
    }
}