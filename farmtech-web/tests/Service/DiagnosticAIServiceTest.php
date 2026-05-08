<?php

namespace App\Tests\Service;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use App\Repository\DiagnostiqueRepository;
use App\Service\DiagnosticAIService;
use PHPUnit\Framework\MockObject\MockObject;
use PHPUnit\Framework\TestCase;
use Symfony\Component\HttpClient\MockHttpClient;
use Symfony\Component\HttpClient\Response\MockResponse;

final class DiagnosticAIServiceTest extends TestCase
{
    public function testFirstResponseDoesNotTriggerRetry(): void
    {
        $requestCount = 0;
        $httpClient = new MockHttpClient(function (string $method, string $url, array $options) use (&$requestCount) {
            ++$requestCount;
            $this->assertSame('POST', $method);
            $this->assertStringContainsString('openrouter.ai', $url);

            return new MockResponse(json_encode([
                'choices' => [[
                    'message' => ['content' => '{"cause":"cause test","solutionProposee":"solution test","medicament":"med test"}'],
                ]],
            ], JSON_THROW_ON_ERROR));
        });

        $repo = $this->mockRepository([]);
        $service = new DiagnosticAIService($httpClient, 'or_test', 'openrouter/elephant-alpha', 'https://openrouter.ai/api/v1/chat/completions', dirname(__DIR__, 2), $repo);
        $probleme = $this->createProbleme('Feuilles jaunies.');

        $suggestion = $service->generateFromProbleme($probleme);

        $this->assertSame('cause test', $suggestion['cause']);
        $this->assertSame(1, $requestCount);
    }

    public function testSecondRevisionRetriesWhenSuggestionTooSimilar(): void
    {
        $requestCount = 0;
        $httpClient = new MockHttpClient(function () use (&$requestCount) {
            ++$requestCount;
            if (1 === $requestCount) {
                return new MockResponse(json_encode([
                    'choices' => [[
                        'message' => ['content' => '{"cause":"Infection persistante des feuilles par champignon.","solutionProposee":"Appliquer encore le même fongicide au même dosage.","medicament":"Produit A"}'],
                    ]],
                ], JSON_THROW_ON_ERROR));
            }

            return new MockResponse(json_encode([
                'choices' => [[
                    'message' => ['content' => '{"cause":"Nous sommes désolés que la solution précédente n’ait pas fonctionné, la maladie semble liée à une humidité localisée.","solutionProposee":"Nous vous proposons d’ajuster le protocole: alterner le mode d’action, corriger l’irrigation et renforcer l’assainissement.","medicament":"Produit B"}'],
                ]],
            ], JSON_THROW_ON_ERROR));
        });

        $latest = $this->createDiagnostique(
            1,
            'Infection persistante des feuilles par champignon.',
            'Appliquer encore le même fongicide au même dosage.'
        );
        $repo = $this->mockRepository([$latest]);
        $service = new DiagnosticAIService($httpClient, 'or_test', 'openrouter/elephant-alpha', 'https://openrouter.ai/api/v1/chat/completions', dirname(__DIR__, 2), $repo);
        $probleme = $this->createProbleme('Taches brunes sur feuilles.');

        $suggestion = $service->generateFromProbleme($probleme);

        $this->assertGreaterThanOrEqual(2, $requestCount);
        $this->assertStringContainsString('désol', strtolower($suggestion['cause']));
        $this->assertNotSame('Appliquer encore le même fongicide au même dosage.', $suggestion['solutionProposee']);
    }

    public function testCanParseLabeledNonJsonAnswer(): void
    {
        $httpClient = new MockHttpClient(function () {
            return new MockResponse(json_encode([
                'choices' => [[
                    'message' => ['content' => "Cause: Infection fongique persistante.\nSolution proposée: Ajuster le protocole avec rotation de matière active.\nTraitement: Produit C"],
                ]],
            ], JSON_THROW_ON_ERROR));
        });

        $repo = $this->mockRepository([]);
        $service = new DiagnosticAIService($httpClient, 'or_test', 'openrouter/elephant-alpha', 'https://openrouter.ai/api/v1/chat/completions', dirname(__DIR__, 2), $repo);
        $probleme = $this->createProbleme('Feuilles tachées.');

        $suggestion = $service->generateFromProbleme($probleme);

        $this->assertStringContainsString('Infection', $suggestion['cause']);
        $this->assertStringContainsString('Ajuster', $suggestion['solutionProposee']);
        $this->assertStringContainsString('Produit C', $suggestion['medicament']);
    }

    public function testCanParseToolCallArgumentsWhenContentIsNull(): void
    {
        $httpClient = new MockHttpClient(function () {
            return new MockResponse(json_encode([
                'choices' => [[
                    'message' => [
                        'content' => null,
                        'tool_calls' => [[
                            'type' => 'function',
                            'function' => [
                                'name' => 'diagnostic_output',
                                'arguments' => '{"cause":"Cause issue","solutionProposee":"Solution issue","medicament":"Produit D"}',
                            ],
                        ]],
                    ],
                ]],
            ], JSON_THROW_ON_ERROR));
        });

        $repo = $this->mockRepository([]);
        $service = new DiagnosticAIService($httpClient, 'or_test', 'openrouter/elephant-alpha', 'https://openrouter.ai/api/v1/chat/completions', dirname(__DIR__, 2), $repo);
        $probleme = $this->createProbleme('Stress hydrique.');

        $suggestion = $service->generateFromProbleme($probleme);

        $this->assertSame('Cause issue', $suggestion['cause']);
        $this->assertSame('Solution issue', $suggestion['solutionProposee']);
        $this->assertSame('Produit D', $suggestion['medicament']);
    }

    /**
     * @param Diagnostique[] $revisions
     */
    private function mockRepository(array $revisions): DiagnostiqueRepository
    {
        /** @var DiagnostiqueRepository&MockObject $repo */
        $repo = $this->createMock(DiagnostiqueRepository::class);
        $repo->method('findByProblemeOrderedByRevision')->willReturn($revisions);

        return $repo;
    }

    private function createProbleme(string $description): Probleme
    {
        $probleme = new Probleme();
        $probleme->setDescription($description);

        return $probleme;
    }

    private function createDiagnostique(int $numRevision, string $cause, string $solution): Diagnostique
    {
        $d = new Diagnostique();
        $d->setNumRevision($numRevision);
        $d->setCause($cause);
        $d->setSolutionProposee($solution);
        $d->setResultat('En attente');
        $d->setDateDiagnostique(new \DateTimeImmutable());

        return $d;
    }
}

