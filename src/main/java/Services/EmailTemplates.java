package Services;

import model.Diagnostique;
import model.Probleme;

import java.time.format.DateTimeFormatter;

/**
 * HTML email templates for FARMTECH notifications.
 * Inline CSS for maximum compatibility with email clients.
 */
public class EmailTemplates {

    private static final String BRAND_COLOR = "#1A4D2E";
    private static final String BRAND_LIGHT = "#E8F5E9";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Builds a well-styled HTML email for diagnostic approval notification.
     */
    public static String buildDiagnosticApprovedHtml(Probleme probleme, Diagnostique diagnostique, String farmerName) {
        String type = escape(probleme.getType());
        String description = escape(probleme.getDescription());
        String gravite = escape(probleme.getGravite());
        String dateDetection = probleme.getDateDetection() != null ? probleme.getDateDetection().format(DATE_FORMAT) : "-";
        String cause = escape(diagnostique.getCause());
        String solution = escape(diagnostique.getSolutionProposee());
        String medicament = diagnostique.getMedicament() != null && !diagnostique.getMedicament().isBlank()
                ? escape(diagnostique.getMedicament()) : "Non spécifié";
        String resultat = escape(diagnostique.getResultat());
        String nom = farmerName != null && !farmerName.isBlank() ? escape(farmerName) : "Agriculteur";

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"margin:0; padding:0; font-family: Arial, sans-serif; background-color:#f4f6f9;\">"
                + "<div style=\"max-width:600px; margin:0 auto; padding:20px;\">"
                + "<div style=\"background:white; border-radius:12px; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.08);\">"
                + "<div style=\"background:" + BRAND_COLOR + "; padding:24px; text-align:center;\">"
                + "<h1 style=\"color:white; margin:0; font-size:24px; font-weight:700;\">FARMTECH</h1>"
                + "<p style=\"color:#b8e0c0; margin:8px 0 0; font-size:14px;\">Votre diagnostic est disponible</p>"
                + "</div>"
                + "<div style=\"padding:28px;\">"
                + "<p style=\"color:#374151; font-size:15px; margin:0 0 16px;\">Bonjour " + nom + ",</p>"
                + "<p style=\"color:#6B7280; font-size:14px; line-height:1.6; margin:0 0 24px;\">"
                + "Un diagnostic a été approuvé pour votre problème. Voici les détails :</p>"
                + "<div style=\"background:" + BRAND_LIGHT + "; border-radius:8px; padding:16px; margin-bottom:20px; border-left:4px solid " + BRAND_COLOR + ";\">"
                + "<h3 style=\"color:" + BRAND_COLOR + "; margin:0 0 12px; font-size:16px;\">Votre problème</h3>"
                + "<p style=\"margin:4px 0; font-size:14px; color:#374151;\"><strong>Type :</strong> " + type + "</p>"
                + "<p style=\"margin:4px 0; font-size:14px; color:#374151;\"><strong>Gravité :</strong> " + gravite + "</p>"
                + "<p style=\"margin:4px 0; font-size:14px; color:#374151;\"><strong>Date :</strong> " + dateDetection + "</p>"
                + "<p style=\"margin:8px 0 0; font-size:14px; color:#4B5563; line-height:1.5;\">" + description + "</p>"
                + "</div>"
                + "<div style=\"background:#f8faf9; border-radius:8px; padding:16px; margin-bottom:20px;\">"
                + "<h3 style=\"color:" + BRAND_COLOR + "; margin:0 0 12px; font-size:16px;\">Diagnostic</h3>"
                + "<p style=\"margin:4px 0; font-size:14px; color:#374151;\"><strong>Cause identifiée :</strong></p>"
                + "<p style=\"margin:4px 0 0 12px; font-size:14px; color:#4B5563; line-height:1.5;\">" + cause + "</p>"
                + "<p style=\"margin:12px 0 4px; font-size:14px; color:#374151;\"><strong>Solution proposée :</strong></p>"
                + "<p style=\"margin:4px 0 0 12px; font-size:14px; color:#4B5563; line-height:1.5;\">" + solution + "</p>"
                + "<p style=\"margin:12px 0 4px; font-size:14px; color:#374151;\"><strong>Médicament :</strong> " + medicament + "</p>"
                + "<p style=\"margin:12px 0 0; font-size:14px; color:#374151;\"><strong>Résultat attendu :</strong> " + resultat + "</p>"
                + "</div>"
                + "<p style=\"text-align:center; margin:24px 0 0;\">"
                + "<span style=\"display:inline-block; background:" + BRAND_COLOR + "; color:white; padding:12px 24px; border-radius:8px; font-size:14px; font-weight:600;\">"
                + "Consultez l'application pour plus de détails</span></p>"
                + "</div>"
                + "<div style=\"background:#f1f5f9; padding:16px; text-align:center; font-size:12px; color:#64748b;\">"
                + "© FARMTECH – Application de gestion agricole"
                + "</div>"
                + "</div></div></body></html>";
    }

    /**
     * Builds plain text fallback for the diagnostic approval email.
     */
    public static String buildDiagnosticApprovedPlainText(Probleme probleme, Diagnostique diagnostique, String farmerName) {
        String nom = farmerName != null && !farmerName.isBlank() ? farmerName : "Agriculteur";
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(nom).append(",\n\n");
        sb.append("Votre diagnostic FARMTECH est disponible.\n\n");
        sb.append("--- Votre problème ---\n");
        sb.append("Type: ").append(probleme.getType()).append("\n");
        sb.append("Gravité: ").append(probleme.getGravite()).append("\n");
        sb.append("Description: ").append(probleme.getDescription()).append("\n\n");
        sb.append("--- Diagnostic ---\n");
        sb.append("Cause: ").append(diagnostique.getCause()).append("\n");
        sb.append("Solution: ").append(diagnostique.getSolutionProposee()).append("\n");
        sb.append("Médicament: ").append(diagnostique.getMedicament() != null ? diagnostique.getMedicament() : "Non spécifié").append("\n");
        sb.append("Résultat: ").append(diagnostique.getResultat()).append("\n\n");
        sb.append("Consultez l'application FARMTECH pour plus de détails.");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
    }
}
