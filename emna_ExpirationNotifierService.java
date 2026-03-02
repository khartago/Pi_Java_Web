package service;

import model.Produit;
import model.ProduitDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpirationNotifierService {

    private final ProduitDAO produitDAO;
    private final EmailService emailService;

    public ExpirationNotifierService(ProduitDAO produitDAO, EmailService emailService) {
        this.produitDAO = produitDAO;
        this.emailService = emailService;
    }

    public int notifyByEmail(String recipientEmail, int daysBefore) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(daysBefore);

        List<Produit> expiringSoon = produitDAO.getExpiringBetween(today, end);
        List<Produit> expired = produitDAO.getExpiredBefore(today);

        if (expiringSoon.isEmpty() && expired.isEmpty()) return 0;

        String subject = "TechFarm | Alerte expiration produits (J+" + daysBefore + ")";
        String textBody = buildTextBody(today, daysBefore, expiringSoon, expired);
        String htmlBody = buildHtmlBody(today, daysBefore, expiringSoon, expired);

        emailService.sendHtmlEmail(recipientEmail, subject, htmlBody, textBody);
        return expiringSoon.size() + expired.size();
    }

    // Ô£à Fallback texte (au cas o├╣)
    private String buildTextBody(LocalDate today, int daysBefore,
                                 List<Produit> soon, List<Produit> expired) {

        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour,\n\n");
        sb.append("TechFarm - Contr├┤le des expirations\n");
        sb.append("Date du contr├┤le: ").append(today).append("\n\n");

        if (!soon.isEmpty()) {
            sb.append("Produits qui expirent dans les ").append(daysBefore).append(" prochains jours:\n");
            for (Produit p : soon) {
                sb.append(" - ").append(p.getNom())
                        .append(" | exp: ").append(p.getDateExpiration())
                        .append(" | qty: ").append(p.getQuantite()).append(" ").append(p.getUnite())
                        .append("\n");
            }
            sb.append("\n");
        }

        if (!expired.isEmpty()) {
            sb.append("Produits d├®j├á expir├®s:\n");
            for (Produit p : expired) {
                sb.append(" - ").append(p.getNom())
                        .append(" | exp: ").append(p.getDateExpiration())
                        .append(" | qty: ").append(p.getQuantite()).append(" ").append(p.getUnite())
                        .append("\n");
            }
            sb.append("\n");
        }

        sb.append("Cordialement,\n");
        sb.append("TechFarm (Syst├¿me de gestion des stocks)\n");
        return sb.toString();
    }

    // Ô£à Email HTML design pro "TechFarm"
    private String buildHtmlBody(LocalDate today, int daysBefore,
                                 List<Produit> soon, List<Produit> expired) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String primary = "#1A4D2E";
        String light = "#F5F7F5";
        String danger = "#DC2626";
        String warn = "#E67E22";
        String text = "#1F2933";

        StringBuilder html = new StringBuilder();

        html.append("""
                <!doctype html>
                <html lang="fr">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>TechFarm - Alerte expiration</title>
                </head>
                <body style="margin:0; padding:0; background:%s; font-family:Segoe UI, Arial, sans-serif; color:%s;">
                  <div style="max-width:760px; margin:0 auto; padding:24px;">
                    
                    <!-- Header -->
                    <div style="background:%s; color:white; border-radius:14px 14px 0 0; padding:20px 22px;">
                      <div style="font-size:18px; font-weight:700; letter-spacing:0.4px;">TECHFARM</div>
                      <div style="opacity:0.95; margin-top:6px;">Alerte expiration produits</div>
                    </div>
                    
                    <!-- Content -->
                    <div style="background:white; border-radius:0 0 14px 14px; padding:22px; box-shadow:0 6px 18px rgba(0,0,0,0.06);">
                      
                      <p style="margin:0 0 10px 0; font-size:14px;">Bonjour,</p>
                      <p style="margin:0 0 16px 0; font-size:14px;">
                        Voici le rapport dÔÇÖexpiration du <b>%s</b> (fen├¬tre <b>J+%d</b>).
                      </p>
                      
                      <!-- Summary cards -->
                      <div style="display:flex; gap:12px; flex-wrap:wrap; margin:16px 0 18px 0;">
                        <div style="flex:1; min-width:220px; border:1px solid #E8E8E8; border-radius:12px; padding:12px;">
                          <div style="font-size:12px; color:#667085;">Expire bient├┤t</div>
                          <div style="font-size:22px; font-weight:700; color:%s;">%d</div>
                        </div>
                        <div style="flex:1; min-width:220px; border:1px solid #E8E8E8; border-radius:12px; padding:12px;">
                          <div style="font-size:12px; color:#667085;">D├®j├á expir├®s</div>
                          <div style="font-size:22px; font-weight:700; color:%s;">%d</div>
                        </div>
                      </div>
                """.formatted(light, text, primary, today.format(fmt), daysBefore, warn, soon.size(), danger, expired.size()));

        if (!soon.isEmpty()) {
            html.append("""
                    <h3 style="margin:0 0 10px 0; font-size:16px; color:%s;">Produits qui expirent bient├┤t</h3>
                    <table style="width:100%%; border-collapse:collapse; font-size:13px; margin-bottom:18px;">
                      <thead>
                        <tr>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Produit</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Expiration</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Quantit├®</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Statut</th>
                        </tr>
                      </thead>
                      <tbody>
                    """.formatted(primary, light, light, light, light));

            for (Produit p : soon) {
                String exp = p.getDateExpiration() == null ? "-" : p.getDateExpiration().format(fmt);
                String qty = p.getQuantite() + " " + (p.getUnite() == null ? "" : p.getUnite());

                html.append("""
                        <tr>
                          <td style="padding:10px; border:1px solid #E8E8E8;"><b>%s</b></td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">%s</td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">%s</td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">
                            <span style="display:inline-block; padding:4px 10px; border-radius:999px; background:%s; color:white; font-weight:600; font-size:12px;">
                              Expire bient├┤t
                            </span>
                          </td>
                        </tr>
                        """.formatted(escapeHtml(p.getNom()), exp, escapeHtml(qty), warn));
            }

            html.append("""
                      </tbody>
                    </table>
                    """);
        }

        if (!expired.isEmpty()) {
            html.append("""
                    <h3 style="margin:0 0 10px 0; font-size:16px; color:%s;">Produits d├®j├á expir├®s</h3>
                    <table style="width:100%%; border-collapse:collapse; font-size:13px; margin-bottom:10px;">
                      <thead>
                        <tr>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Produit</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Expiration</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Quantit├®</th>
                          <th align="left" style="padding:10px; background:%s; border:1px solid #E8E8E8;">Statut</th>
                        </tr>
                      </thead>
                      <tbody>
                    """.formatted(danger, light, light, light, light));

            for (Produit p : expired) {
                String exp = p.getDateExpiration() == null ? "-" : p.getDateExpiration().format(fmt);
                String qty = p.getQuantite() + " " + (p.getUnite() == null ? "" : p.getUnite());

                html.append("""
                        <tr>
                          <td style="padding:10px; border:1px solid #E8E8E8;"><b>%s</b></td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">%s</td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">%s</td>
                          <td style="padding:10px; border:1px solid #E8E8E8;">
                            <span style="display:inline-block; padding:4px 10px; border-radius:999px; background:%s; color:white; font-weight:600; font-size:12px;">
                              Expir├®
                            </span>
                          </td>
                        </tr>
                        """.formatted(escapeHtml(p.getNom()), exp, escapeHtml(qty), danger));
            }

            html.append("""
                      </tbody>
                    </table>
                    """);
        }

        html.append("""
                      <div style="margin-top:16px; padding-top:14px; border-top:1px solid #E8E8E8; font-size:12px; color:#667085;">
                        Cet email a ├®t├® g├®n├®r├® automatiquement par <b>TechFarm</b> (Gestion Stock & Expirations).<br/>
                        Merci de ne pas r├®pondre ├á ce message.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """);

        return html.toString();
    }

    // Ô£à s├®curit├® minimale HTML
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
