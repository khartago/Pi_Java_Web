<?php

namespace App\Service;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use App\Repository\DiagnostiqueRepository;
use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Suggestion diagnostic via OpenRouter.
 */
final class DiagnosticAIService
{
    private const OPENROUTER_DEFAULT_CHAT_URL = 'https://openrouter.ai/api/v1/chat/completions';

    private const BASE_PROMPT = 'Tu es un expert en problèmes agricoles. À partir de la/les photo(s), de la description et, '
        .'si fourni, de l’historique des révisions et feedbacks fermier, '
        .'propose une suite cohérente (évite de contredire sans raison un diagnostic déjà approuvé sauf si les faits imposent une correction). '
        .'Fournis au format JSON strict uniquement, sans texte avant ou après, avec exactement ces clés : '
        .'"cause", "solutionProposee", "medicament". '
        .'cause = cause probable du problème, solutionProposee = solution recommandée, '
        .'medicament = nom du médicament/traitement recommandé et dosage si possible. '
        .'Réponds uniquement avec le JSON, pas de markdown.';

    private const HISTO_MAX_TOTAL_CHARS = 12000;

    private const HISTO_FIELD_CLIP = 900;

    private const MAX_RETRY_COUNT = 2;

    private const SIMILARITY_THRESHOLD = 0.84;

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $openRouterApiKey,
        private readonly string $openRouterModel,
        private readonly string $openRouterChatUrl,
        private readonly string $projectDir,
        private readonly DiagnostiqueRepository $diagnostiqueRepository,
    ) {
    }

    /**
     * @return array{cause: string, solutionProposee: string, medicament: string}
     */
    public function generateFromProbleme(Probleme $p): array
    {
        if ('' === trim($this->openRouterApiKey)) {
            throw new \RuntimeException('Clé OpenRouter manquante. Renseignez OPENROUTER_API_KEY dans .env.');
        }

        $model = '' !== trim($this->openRouterModel) ? $this->openRouterModel : 'google/gemma-4-26b-a4b-it';
        $desc = $p->getDescription() ?? '';
        $revisions = $this->diagnostiqueRepository->findByProblemeOrderedByRevision($p);
        $revisionCount = \count($revisions);
        $latest = $revisionCount > 0 ? $revisions[$revisionCount - 1] : null;
        $historique = $this->buildHistoriqueContext($revisions);
        $retryHint = null;

        for ($attempt = 0; $attempt <= self::MAX_RETRY_COUNT; ++$attempt) {
            $promptText = $this->buildPromptText($desc, $historique, $revisionCount, $retryHint);
            $raw = $this->requestCompletion($model, $p, $promptText);
            $suggestion = $this->parseSuggestionFromText($raw);
            if (!$latest instanceof Diagnostique || !$this->isTooSimilarToLatest($suggestion, $latest)) {
                return $suggestion;
            }
            $retryHint = 'La réponse précédente ressemble trop à la dernière révision. '
                .'Reformule de manière nettement différente la cause et la solution, en gardant la cohérence technique.';
        }

        throw new \RuntimeException('Impossible de générer une révision suffisamment différente. Réessayez.');
    }

    /**
     * Révisions existantes + feedbacks fermier pour contextualiser la suggestion (tronqué pour limiter les tokens).
     */
    private function buildHistoriqueContext(array $revs): string
    {
        if ($revs === []) {
            return '';
        }
        $blocks = ['--- Historique des révisions et retours fermier ---'];
        $total = 0;
        foreach ($revs as $d) {
            $chunk = $this->formatRevisionBlock($d);
            if ('' === $chunk) {
                continue;
            }
            if ($total + \strlen($chunk) > self::HISTO_MAX_TOTAL_CHARS) {
                $blocks[] = '… (historique tronqué pour limite de taille)';
                break;
            }
            $blocks[] = $chunk;
            $total += \strlen($chunk);
        }

        return implode("\n", $blocks);
    }

    private function buildPromptText(string $description, string $historique, int $revisionCount, ?string $retryHint): string
    {
        $promptText = 'Description du problème : '.$description."\n\n";
        if ('' !== $historique) {
            $promptText .= $historique."\n\n";
        }
        $promptText .= self::BASE_PROMPT."\n";
        if ($revisionCount === 0) {
            $promptText .= 'Ceci est la 1re réponse: ne pas présenter d’excuses et aller droit au diagnostic.';
        } else {
            $promptText .= 'Ceci est une révision (2e ou +): commencer la cause ou la solution par une phrase courte '
                .'d’empathie en français reconnaissant que la solution précédente n’a pas fonctionné, puis proposer '
                .'un plan ajusté et concret.';
        }
        if (null !== $retryHint && '' !== trim($retryHint)) {
            $promptText .= "\n".$retryHint;
        }

        return $promptText;
    }

    private function requestCompletion(string $model, Probleme $probleme, string $promptText): string
    {
        $chatUrl = '' !== trim($this->openRouterChatUrl) ? $this->openRouterChatUrl : self::OPENROUTER_DEFAULT_CHAT_URL;
        $contentList = $this->buildContentList($probleme, $promptText);
        $body = [
            'model' => $model,
            'messages' => [['role' => 'user', 'content' => $contentList]],
            'max_tokens' => 4096,
            'temperature' => 0.7,
            'top_p' => 0.95,
            'top_k' => 20,
        ];

        $response = $this->httpClient->request('POST', $chatUrl, [
            'headers' => [
                'Content-Type' => 'application/json',
                'Authorization' => 'Bearer '.$this->openRouterApiKey,
                'HTTP-Referer' => 'http://localhost:8000',
                'X-Title' => 'farmtech-web',
            ],
            'json' => $body,
            'timeout' => 120,
        ]);

        $status = $response->getStatusCode();
        $rawBody = $response->getContent(false);
        if (401 === $status) {
            throw new \RuntimeException('Clé OpenRouter invalide. Vérifiez OPENROUTER_API_KEY sur https://openrouter.ai/keys');
        }
        if (200 !== $status) {
            $preview = strlen($rawBody) > 250 ? substr($rawBody, 0, 250).'...' : $rawBody;
            throw new \RuntimeException('OpenRouter erreur '.$status.' : '.$preview);
        }
        $data = json_decode($rawBody, true);
        if (\is_array($data)) {
            $text = $this->extractHfRouterResponseText($data);
            if (null !== $text && '' !== trim($text)) {
                return $text;
            }
        }
        $fallback = $this->extractJsonObjectFromRawBody($rawBody);
        if (null !== $fallback) {
            return $fallback;
        }
        $preview = strlen($rawBody) > 250 ? substr($rawBody, 0, 250).'...' : $rawBody;
        throw new \RuntimeException('Réponse Hugging Face vide ou mal formée. Extrait: '.$preview);
    }

    /**
     * @return array<int, array{type:string, text?:string, image_url?:array{url:string}}>
     */
    private function buildContentList(Probleme $probleme, string $promptText): array
    {
        $contentList = [];
        $photos = $probleme->getPhotos();
        if (null !== $photos && '' !== trim($photos)) {
            foreach (explode(';', $photos) as $rel) {
                $trimmed = trim($rel);
                if ('' === $trimmed) {
                    continue;
                }
                $full = $this->projectDir.'/public/'.str_replace('\\', '/', $trimmed);
                if (str_contains($full, '..') || !is_file($full)) {
                    continue;
                }
                $bytes = @file_get_contents($full);
                if (false === $bytes) {
                    continue;
                }
                $mime = 'image/jpeg';
                $lower = strtolower($trimmed);
                if (str_ends_with($lower, '.png')) {
                    $mime = 'image/png';
                } elseif (str_ends_with($lower, '.gif')) {
                    $mime = 'image/gif';
                } elseif (str_ends_with($lower, '.webp')) {
                    $mime = 'image/webp';
                }
                $dataUrl = 'data:'.$mime.';base64,'.base64_encode($bytes);
                $contentList[] = [
                    'type' => 'image_url',
                    'image_url' => ['url' => $dataUrl],
                ];
            }
        }
        $contentList[] = ['type' => 'text', 'text' => $promptText];

        return $contentList;
    }

    /**
     * @param array{cause:string, solutionProposee:string, medicament:string} $candidate
     */
    private function isTooSimilarToLatest(array $candidate, Diagnostique $latest): bool
    {
        $latestCause = $this->normalizeText((string) ($latest->getCause() ?? ''));
        $latestSolution = $this->normalizeText((string) ($latest->getSolutionProposee() ?? ''));
        $newCause = $this->normalizeText($candidate['cause'] ?? '');
        $newSolution = $this->normalizeText($candidate['solutionProposee'] ?? '');
        if ('' === $latestCause || '' === $latestSolution || '' === $newCause || '' === $newSolution) {
            return false;
        }

        return $this->similarityRatio($latestCause, $newCause) >= self::SIMILARITY_THRESHOLD
            && $this->similarityRatio($latestSolution, $newSolution) >= self::SIMILARITY_THRESHOLD;
    }

    private function normalizeText(string $value): string
    {
        $value = strtolower(trim($value));
        if ('' === $value) {
            return '';
        }

        return (string) preg_replace('/\s+/', ' ', $value);
    }

    private function similarityRatio(string $left, string $right): float
    {
        similar_text($left, $right, $percent);

        return $percent / 100.0;
    }

    private function formatRevisionBlock(Diagnostique $d): string
    {
        $lines = [];
        $lines[] = sprintf(
            'Révision n°%d — %s — approuvé pour le fermier : %s',
            $d->getNumRevision(),
            $d->getDateDiagnostique()?->format('Y-m-d H:i') ?? '?',
            $d->isApprouve() ? 'oui' : 'non',
        );
        $lines[] = 'Résultat : '.$this->clipText((string) ($d->getResultat() ?? ''));
        $lines[] = 'Cause : '.$this->clipText((string) ($d->getCause() ?? ''));
        $lines[] = 'Solution proposée : '.$this->clipText((string) ($d->getSolutionProposee() ?? ''));
        $med = $d->getMedicament();
        if (null !== $med && '' !== trim($med)) {
            $lines[] = 'Médicament / traitement : '.$this->clipText($med);
        }
        $fb = $d->getFeedbackFermier();
        if (null !== $fb && '' !== $fb) {
            $label = 'RESOLU' === $fb ? 'Résolu' : ('NON_RESOLU' === $fb ? 'Non résolu' : $fb);
            $lines[] = 'Feedback fermier : '.$label;
            if (null !== $d->getDateFeedback()) {
                $lines[] = 'Date feedback : '.$d->getDateFeedback()->format('Y-m-d H:i');
            }
            $com = $d->getFeedbackCommentaire();
            if (null !== $com && '' !== trim($com)) {
                $lines[] = 'Commentaire fermier : '.$this->clipText($com);
            }
        }
        $lines[] = '---';

        return implode("\n", $lines);
    }

    private function clipText(string $text): string
    {
        $text = trim($text);
        if ('' === $text) {
            return '—';
        }
        if (\strlen($text) <= self::HISTO_FIELD_CLIP) {
            return $text;
        }

        return substr($text, 0, self::HISTO_FIELD_CLIP - 1).'…';
    }

    /**
     * @param array<string, mixed> $root
     */
    private function extractHfRouterResponseText(array $root): ?string
    {
        if (!isset($root['choices'][0])) {
            return null;
        }
        $choice = $root['choices'][0];
        if (!\is_array($choice)) {
            return null;
        }
        if (isset($choice['message']['content'])) {
            $content = $choice['message']['content'];
            if (\is_string($content)) {
                return $content;
            }
            if (\is_array($content)) {
                $sb = '';
                foreach ($content as $part) {
                    if (!\is_array($part)) {
                        continue;
                    }
                    if (isset($part['text']) && \is_string($part['text'])) {
                        $sb .= $part['text'];
                        continue;
                    }
                    if (isset($part['content']) && \is_string($part['content'])) {
                        $sb .= $part['content'];
                    }
                }

                return '' !== $sb ? $sb : null;
            }
        }
        if (isset($choice['message']['function_call']['arguments']) && \is_string($choice['message']['function_call']['arguments'])) {
            return $choice['message']['function_call']['arguments'];
        }
        if (isset($choice['message']['tool_calls']) && \is_array($choice['message']['tool_calls'])) {
            $sb = '';
            foreach ($choice['message']['tool_calls'] as $toolCall) {
                if (!\is_array($toolCall)) {
                    continue;
                }
                if (isset($toolCall['function']['arguments']) && \is_string($toolCall['function']['arguments'])) {
                    $sb .= $toolCall['function']['arguments']."\n";
                }
            }
            if ('' !== trim($sb)) {
                return trim($sb);
            }
        }
        if (isset($choice['message']['reasoning_content']) && \is_string($choice['message']['reasoning_content'])) {
            return $choice['message']['reasoning_content'];
        }
        if (isset($choice['text']) && \is_string($choice['text'])) {
            return $choice['text'];
        }
        if (isset($root['generated_text']) && \is_string($root['generated_text'])) {
            return $root['generated_text'];
        }

        return null;
    }

    private function extractJsonObjectFromRawBody(string $rawBody): ?string
    {
        $trimmed = trim($rawBody);
        if ('' === $trimmed) {
            return null;
        }
        if (str_starts_with($trimmed, '{') && str_contains($trimmed, '"cause"') && str_contains($trimmed, '"solutionProposee"')) {
            return $trimmed;
        }
        if (preg_match('/\{(?:[^{}]|(?R))*"cause"(?:[^{}]|(?R))*"solutionProposee"(?:[^{}]|(?R))*\}/s', $trimmed, $m)) {
            return $m[0];
        }

        return null;
    }

    /**
     * @return array{cause: string, solutionProposee: string, medicament: string}
     */
    private function parseSuggestionFromText(string $text): array
    {
        $payload = trim($text);
        if (preg_match('/(?s)```(?:json)?\s*(.*?)\s*```/', $payload, $m)) {
            $payload = trim($m[1]);
        }
        $obj = $this->decodeSuggestionObject($payload);
        if (!\is_array($obj)) {
            $fallback = $this->parseSuggestionFromLabeledText($payload);
            if (null !== $fallback) {
                return $fallback;
            }
            throw new \RuntimeException('Impossible de parser la réponse IA.');
        }
        $cause = $this->firstStringByKeys($obj, ['cause', 'causes', 'rootCause']);
        $solution = $this->firstStringByKeys($obj, ['solutionProposee', 'solution_proposee', 'solution', 'planAction']);
        $medicament = $this->firstStringByKeys($obj, ['medicament', 'traitement', 'treatment']);

        return [
            'cause' => $cause ?? '',
            'solutionProposee' => $solution ?? '',
            'medicament' => $medicament ?? '',
        ];
    }

    /**
     * @return array<string,mixed>|null
     */
    private function decodeSuggestionObject(string $payload): ?array
    {
        $try = trim($payload);
        $obj = json_decode($try, true);
        if (\is_array($obj)) {
            return $obj;
        }
        if (preg_match('/\{(?:[^{}]|(?R))*\}/s', $try, $m)) {
            $try = $m[0];
            $obj = json_decode($try, true);
            if (\is_array($obj)) {
                return $obj;
            }
        }
        $normalized = str_replace(["\u{201C}", "\u{201D}", "\u{2018}", "\u{2019}"], ['"', '"', "'", "'"], $try);
        $normalized = (string) preg_replace('/,\s*([}\]])/', '$1', $normalized);
        $obj = json_decode($normalized, true);
        if (\is_array($obj)) {
            return $obj;
        }

        return null;
    }

    private function firstStringByKeys(array $obj, array $keys): ?string
    {
        foreach ($keys as $key) {
            if (isset($obj[$key]) && \is_string($obj[$key]) && '' !== trim($obj[$key])) {
                return trim($obj[$key]);
            }
        }

        return null;
    }

    /**
     * @return array{cause:string, solutionProposee:string, medicament:string}|null
     */
    private function parseSuggestionFromLabeledText(string $payload): ?array
    {
        $cause = '';
        $solution = '';
        $medicament = '';
        $lines = preg_split('/\R+/', trim($payload)) ?: [];
        foreach ($lines as $line) {
            $line = trim((string) $line);
            if ('' === $line || !str_contains($line, ':')) {
                continue;
            }
            [$label, $value] = array_pad(explode(':', $line, 2), 2, '');
            $label = strtolower(trim($label));
            $value = trim($value);
            if ('' === $value) {
                continue;
            }
            if (str_contains($label, 'cause') && '' === $cause) {
                $cause = $value;
                continue;
            }
            if ((str_contains($label, 'solution') || str_contains($label, 'plan')) && '' === $solution) {
                $solution = $value;
                continue;
            }
            if ((str_contains($label, 'médicament') || str_contains($label, 'medicament') || str_contains($label, 'traitement')) && '' === $medicament) {
                $medicament = $value;
            }
        }
        if ('' === $cause && '' === $solution && '' === $medicament) {
            return null;
        }

        return ['cause' => $cause, 'solutionProposee' => $solution, 'medicament' => $medicament];
    }
}
