<?php

namespace App\Service;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Génère un PDF via l’API html2pdf (parité client Java {@see PdfExportService}).
 */
final class ProblemePdfExportService
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $pdfRemoteUrl,
    ) {
    }

    /** @return non-empty-string|null octets PDF ou null si échec */
    public function generateReportPdf(Probleme $p, ?Diagnostique $d): ?string
    {
        $html = $this->buildReportHtml($p, $d);
        $body = json_encode(['html' => $html], JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE);

        try {
            $response = $this->httpClient->request('POST', $this->pdfRemoteUrl, [
                'headers' => ['Content-Type' => 'application/json'],
                'body' => $body,
                'timeout' => 60,
            ]);
            if (200 !== $response->getStatusCode()) {
                return null;
            }
            $content = $response->getContent();
            if ('' === $content) {
                return null;
            }

            return $content;
        } catch (\Throwable) {
            return null;
        }
    }

    private function buildReportHtml(Probleme $p, ?Diagnostique $d): string
    {
        $fmt = static fn (?\DateTimeInterface $dt) => $dt
            ? $dt->format('d/m/Y H:i')
            : '-';
        $now = (new \DateTimeImmutable())->format('d/m/Y H:i');

        $sb = '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Rapport FARMTECH</title>';
        $sb .= '<style>body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#1F2933;}';
        $sb .= 'h1{color:#1A4D2E;} .section{margin-top:16px;} .label{font-weight:600;} table{border-collapse:collapse;} td{padding:6px 12px;border:1px solid #E5EDE5;}</style></head><body>';
        $sb .= '<h1>Rapport FARMTECH</h1>';
        $sb .= '<p>Date du rapport : '.$this->escapeHtml($now).'</p>';

        $sb .= '<div class="section"><h2>Problème</h2><table>';
        $sb .= $this->row('Type', $p->getType());
        $sb .= $this->row('Description', $p->getDescription());
        $sb .= $this->row('Gravité', $p->getGravite());
        $dd = $p->getDateDetection();
        $sb .= $this->row('Date détection', $fmt($dd));
        $sb .= $this->row('État', $p->getEtat());
        $sb .= '</table></div>';

        $sb .= '<div class="section"><h2>Diagnostic</h2>';
        if (null !== $d) {
            $sb .= '<table>';
            $sb .= $this->row('Cause', $d->getCause());
            $sb .= $this->row('Solution proposée', $d->getSolutionProposee());
            $sb .= $this->row('Médicament', $d->getMedicament() ?? '-');
            $sb .= $this->row('Résultat', $d->getResultat());
            $sb .= '</table>';
        } else {
            $sb .= '<p>Aucun diagnostic</p>';
        }
        $sb .= '</div></body></html>';

        return $sb;
    }

    private function row(string $label, ?string $value): string
    {
        $v = null !== $value && '' !== $value ? $value : '-';

        return '<tr><td class="label">'.$this->escapeHtml($label).'</td><td>'.$this->escapeHtml($v).'</td></tr>';
    }

    private function escapeHtml(?string $s): string
    {
        if (null === $s || '' === $s) {
            return '';
        }

        return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
    }
}
