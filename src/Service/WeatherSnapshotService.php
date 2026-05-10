<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Snapshot JSON pour {@see \App\Entity\Probleme::setMeteoSnapshot} (aligné Open-Meteo / Java WeatherService).
 */
final class WeatherSnapshotService
{
    private const OPEN_METEO = 'https://api.open-meteo.com/v1/forecast';

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly LoggerInterface $logger,
        private readonly float $defaultLat,
        private readonly float $defaultLon,
    ) {
    }

    public function fetchSnapshotJson(): ?string
    {
        $url = self::OPEN_METEO.'?latitude='.$this->defaultLat.'&longitude='.$this->defaultLon
            .'&current=temperature_2m,weather_code,relative_humidity_2m';

        try {
            $response = $this->httpClient->request('GET', $url, ['timeout' => 10]);
            if (200 !== $response->getStatusCode()) {
                return null;
            }
            $data = $response->toArray(false);
            $current = $data['current'] ?? null;
            if (!\is_array($current)) {
                return null;
            }
            $temp = isset($current['temperature_2m']) ? (float) $current['temperature_2m'] : 0.0;
            $code = isset($current['weather_code']) ? (int) $current['weather_code'] : 0;
            $humidity = isset($current['relative_humidity_2m']) ? (int) $current['relative_humidity_2m'] : 0;
            $description = $this->weatherCodeToDescription($code);

            return json_encode([
                'lat' => $this->defaultLat,
                'lon' => $this->defaultLon,
                'temp' => $temp,
                'description' => $description,
                'humidity' => $humidity,
                'timestamp' => (new \DateTimeImmutable())->format(\DateTimeInterface::ATOM),
            ], JSON_THROW_ON_ERROR);
        } catch (\Throwable $e) {
            $this->logger->warning('Weather snapshot failed: '.$e->getMessage());

            return null;
        }
    }

    private function weatherCodeToDescription(int $code): string
    {
        if (0 === $code) {
            return 'Ciel dégagé';
        }
        if (1 === $code) {
            return 'Principalement dégagé';
        }
        if (2 === $code) {
            return 'Partiellement nuageux';
        }
        if (3 === $code) {
            return 'Couvert';
        }
        if ($code >= 45 && $code <= 48) {
            return 'Brouillard';
        }
        if ($code >= 51 && $code <= 57) {
            return 'Bruine';
        }
        if ($code >= 61 && $code <= 67) {
            return 'Pluie';
        }
        if ($code >= 71 && $code <= 77) {
            return 'Neige';
        }
        if ($code >= 80 && $code <= 82) {
            return 'Averses';
        }
        if ($code >= 85 && $code <= 86) {
            return 'Averses de neige';
        }
        if ($code >= 95 && $code <= 99) {
            return 'Orage';
        }

        return 'Variable';
    }
}
