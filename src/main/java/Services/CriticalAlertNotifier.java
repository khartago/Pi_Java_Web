package Services;

import model.*;
import java.util.*;

/**
 * Service pour gérer et envoyer les alertes critiques
 * (stock faible, matériels en panne)
 */
public class CriticalAlertNotifier {

    private final ProduitDAO produitDAO;
    private final MaterielDAO materielDAO;
    private final EmailService emailService;

    public CriticalAlertNotifier(ProduitDAO produitDAO, MaterielDAO materielDAO, EmailService emailService) {
        this.produitDAO = produitDAO;
        this.materielDAO = materielDAO;
        this.emailService = emailService;
    }

    /**
     * Envoie les alertes critiques (stock faible + matériels en panne)
     */
    public Map<String, Object> sendCriticalAlerts(String fromEmail, String toEmail, int stockThreshold) throws Exception {
        List<Produit> lowStockProducts = getLowStockProducts(stockThreshold);
        List<Materiel> brokenMateriels = getBrokenMateriels();

        Map<String, Object> result = new HashMap<>();
        result.put("sent", false);
        result.put("low_stock_count", lowStockProducts.size());
        result.put("panne_count", brokenMateriels.size());

        // N'envoyer que s'il y a des alertes
        if (lowStockProducts.isEmpty() && brokenMateriels.isEmpty()) {
            return result;
        }

        String htmlContent = buildAlertHtmlEmail(lowStockProducts, brokenMateriels, stockThreshold);
        String textContent = buildAlertTextEmail(lowStockProducts, brokenMateriels, stockThreshold);

        try {
            emailService.sendHtmlEmail(toEmail, "🚨 Alertes Critiques - FarmTech", htmlContent, textContent);
            result.put("sent", true);
        } catch (Exception e) {
            result.put("sent", false);
            result.put("error", e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * Récupère les produits avec stock faible
     */
    public List<Produit> getLowStockProducts(int threshold) {
        List<Produit> allProducts = produitDAO.getAll();
        List<Produit> lowStock = new ArrayList<>();

        if (allProducts != null) {
            for (Produit p : allProducts) {
                if (p.getQuantite() <= threshold) {
                    lowStock.add(p);
                }
            }
        }

        return lowStock;
    }

    /**
     * Récupère les matériels en panne
     */
    public List<Materiel> getBrokenMateriels() {
        List<Materiel> allMateriels = materielDAO.getAll();
        List<Materiel> broken = new ArrayList<>();

        if (allMateriels != null) {
            for (Materiel m : allMateriels) {
                if ("panne".equalsIgnoreCase(m.getEtat())) {
                    broken.add(m);
                }
            }
        }

        return broken;
    }

    /**
     * Génère le contenu HTML de l'email d'alerte
     */
    private String buildAlertHtmlEmail(List<Produit> lowStockProducts, List<Materiel> brokenMateriels, int threshold) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; }");
        html.append("h2 { color: #DC2626; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 15px 0; }");
        html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        html.append("th { background-color: #f5f5f5; font-weight: bold; }");
        html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append(".warning { color: #DC2626; font-weight: bold; }");
        html.append("</style></head><body>");

        html.append("<h1>🚨 Alertes Critiques - FarmTech</h1>");
        html.append("<p><strong>Date :</strong> ").append(new java.util.Date()).append("</p>");

        // Section Stock Faible
        if (!lowStockProducts.isEmpty()) {
            html.append("<h2>📦 Produits en Stock Faible (≤ ").append(threshold).append(")</h2>");
            html.append("<table>");
            html.append("<tr><th>ID</th><th>Nom</th><th>Quantité</th><th>Unité</th></tr>");

            for (Produit p : lowStockProducts) {
                html.append("<tr>");
                html.append("<td>").append(p.getIdProduit()).append("</td>");
                html.append("<td>").append(p.getNom()).append("</td>");
                html.append("<td class='warning'>").append(p.getQuantite()).append("</td>");
                html.append("<td>").append(p.getUnite()).append("</td>");
                html.append("</tr>");
            }

            html.append("</table>");
        }

        // Section Matériels en Panne
        if (!brokenMateriels.isEmpty()) {
            html.append("<h2>🔧 Matériels en Panne</h2>");
            html.append("<table>");
            html.append("<tr><th>ID</th><th>Nom</th><th>État</th><th>Produit</th></tr>");

            for (Materiel m : brokenMateriels) {
                html.append("<tr>");
                html.append("<td>").append(m.getIdMateriel()).append("</td>");
                html.append("<td>").append(m.getNom()).append("</td>");
                html.append("<td class='warning'>").append(m.getEtat()).append("</td>");
                Produit p = produitDAO.getById(m.getIdProduit());
                String produitNom = p != null ? p.getNom() : "N/A";
                html.append("<td>").append(produitNom).append("</td>");
                html.append("</tr>");
            }

            html.append("</table>");
        }

        html.append("<p style='color: #666; font-size: 12px;'>");
        html.append("Cet email a été généré automatiquement par FarmTech.<br/>");
        html.append("Merci de prendre les mesures nécessaires.");
        html.append("</p>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Génère le contenu texte de l'email d'alerte
     */
    private String buildAlertTextEmail(List<Produit> lowStockProducts, List<Materiel> brokenMateriels, int threshold) {
        StringBuilder text = new StringBuilder();
        text.append("🚨 ALERTES CRITIQUES - FarmTech\n");
        text.append("=================================\n\n");

        if (!lowStockProducts.isEmpty()) {
            text.append("📦 PRODUITS EN STOCK FAIBLE (≤ ").append(threshold).append(")\n");
            text.append("---------------------------------\n");
            for (Produit p : lowStockProducts) {
                text.append("• ").append(p.getNom()).append(" : ")
                    .append(p.getQuantite()).append(" ").append(p.getUnite()).append("\n");
            }
            text.append("\n");
        }

        if (!brokenMateriels.isEmpty()) {
            text.append("🔧 MATÉRIELS EN PANNE\n");
            text.append("---------------------------------\n");
            for (Materiel m : brokenMateriels) {
                Produit p = produitDAO.getById(m.getIdProduit());
                String produitNom = p != null ? p.getNom() : "N/A";
                text.append("• ").append(m.getNom()).append(" (Produit: ").append(produitNom).append(")\n");
            }
            text.append("\n");
        }

        text.append("Merci de prendre les mesures nécessaires.\n");
        return text.toString();
    }

    /**
     * Retourne un résumé des alertes sans envoyer d'email
     */
    public Map<String, Object> getAlertSummary(int stockThreshold) {
        List<Produit> lowStockProducts = getLowStockProducts(stockThreshold);
        List<Materiel> brokenMateriels = getBrokenMateriels();

        List<Map<String, Object>> lowStockData = new ArrayList<>();
        for (Produit p : lowStockProducts) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", p.getIdProduit());
            data.put("nom", p.getNom());
            data.put("quantite", p.getQuantite());
            data.put("unite", p.getUnite());
            lowStockData.add(data);
        }

        List<Map<String, Object>> brokenData = new ArrayList<>();
        for (Materiel m : brokenMateriels) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", m.getIdMateriel());
            data.put("nom", m.getNom());
            data.put("etat", m.getEtat());
            Produit p = produitDAO.getById(m.getIdProduit());
            data.put("produit", p != null ? p.getNom() : null);
            brokenData.add(data);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("threshold", stockThreshold);
        summary.put("low_stock_count", lowStockData.size());
        summary.put("broken_count", brokenData.size());
        summary.put("low_stock_products", lowStockData);
        summary.put("broken_materiels", brokenData);

        return summary;
    }
}




