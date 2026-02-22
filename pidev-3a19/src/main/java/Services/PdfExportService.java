package Services;

import model.Diagnostique;
import model.Probleme;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Export du rapport problème + diagnostic en PDF via une API externe (HTML → PDF).
 * Utilise html2pdf.fly.dev (sans clé) ou une URL configurée.
 */
public class PdfExportService {

    private static final String DEFAULT_PDF_API_URL = "https://html2pdf.fly.dev/api/generate";

    /**
     * Génère un PDF du rapport (problème + diagnostic si présent).
     *
     * @param p le problème (non null)
     * @param d le diagnostic (peut être null → "Aucun diagnostic" dans le rapport)
     * @return les octets du PDF, ou null en cas d'erreur réseau/API
     */
    public byte[] generateReportPdf(Probleme p, Diagnostique d) {
        String html = buildReportHtml(p, d);
        String jsonBody = "{\"html\": " + escapeJson(html) + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEFAULT_PDF_API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) return null;
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }

    private String buildReportHtml(Probleme p, Diagnostique d) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Rapport FARMTECH</title>");
        sb.append("<style>body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#1F2933;}");
        sb.append("h1{color:#1A4D2E;} .section{margin-top:16px;} .label{font-weight:600;} table{border-collapse:collapse;} td{padding:6px 12px;border:1px solid #E5EDE5;}</style></head><body>");
        sb.append("<h1>Rapport FARMTECH</h1>");
        sb.append("<p>Date du rapport : ").append(escapeHtml(java.time.LocalDateTime.now().format(dtf))).append("</p>");

        sb.append("<div class=\"section\"><h2>Problème</h2><table>");
        row(sb, "Type", p.getType());
        row(sb, "Description", p.getDescription());
        row(sb, "Gravité", p.getGravite());
        row(sb, "Date détection", p.getDateDetection() != null ? p.getDateDetection().format(dtf) : "-");
        row(sb, "État", p.getEtat());
        sb.append("</table></div>");

        sb.append("<div class=\"section\"><h2>Diagnostic</h2>");
        if (d != null) {
            sb.append("<table>");
            row(sb, "Cause", d.getCause());
            row(sb, "Solution proposée", d.getSolutionProposee());
            row(sb, "Médicament", d.getMedicament() != null ? d.getMedicament() : "-");
            row(sb, "Résultat", d.getResultat());
            sb.append("</table>");
        } else {
            sb.append("<p>Aucun diagnostic</p>");
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void row(StringBuilder sb, String label, String value) {
        sb.append("<tr><td class=\"label\">").append(escapeHtml(label)).append("</td><td>").append(escapeHtml(value != null ? value : "-")).append("</td></tr>");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escapeJson(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
}
