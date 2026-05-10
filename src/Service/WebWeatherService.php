<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class WebWeatherService
{
    public function __construct(
        private readonly HttpClientInterface $client,
        private readonly string $apiKey,
    ) {
    }

    /**
     * @return array<string, mixed>
     */
    public function getWeather(string $city): array
    {
        if (trim($this->apiKey) === '') {
            return [
                'name' => $city,
                'main' => ['temp' => null],
                'weather' => [],
            ];
        }

        return $this->client->request('GET', 'https://api.openweathermap.org/data/2.5/weather', [
            'query' => [
                'q' => $city,
                'appid' => $this->apiKey,
                'units' => 'metric',
            ],
        ])->toArray(false);
    }
}
