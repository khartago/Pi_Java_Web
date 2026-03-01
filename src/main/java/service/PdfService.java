package service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import model.Produit;
import model.ProduitDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service pour la génération de rapports PDF
 * Génère des rapports professionnels avec listes de produits et statistiques
 */
public class PdfService {
    private final ProduitDAO produitDAO;
    private final StatisticsService statisticsService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PdfService(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
        this.statisticsService = new StatisticsService(produitDAO);
    }

    /**
     * Génère un rapport PDF avec la liste complète des produits
     * @param outputPath Chemin du fichier PDF à créer
     * @throws IOException Si erreur lors de la création du fichier
     */
    public void generateProductListReport(String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // En-tête du rapport
        addReportHeader(document, "RAPPORT INVENTAIRE PRODUITS");
        addGenerationDate(document);

        // Résumé des statistiques
        addStatisticsSummary(document);

        // Table des produits
        addProductsTable(document, produitDAO.getAll());

        // Pied de page
        addFooter(document);

        document.close();
    }

    /**
     * Génère un rapport PDF des produits proches de l'expiration
     * @param outputPath Chemin du fichier PDF à créer
     * @throws IOException Si erreur lors de la création du fichier
     */
    public void generateExpirationReport(String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        addReportHeader(document, "RAPPORT EXPIRATION PRODUITS");
        addGenerationDate(document);

        List<Produit> expiringProducts = statisticsService.getExpiringProducts();
        List<Produit> expiredProducts = statisticsService.getExpiredProducts();

        // Section produits expirés
        if (!expiredProducts.isEmpty()) {
            addSectionTitle(document, "⚠️ PRODUITS EXPIRÉS (" + expiredProducts.size() + ")");
            addProductsTable(document, expiredProducts);
            document.add(new Paragraph("\n"));
        }

        // Section produits proches de l'expiration
        if (!expiringProducts.isEmpty()) {
            addSectionTitle(document, "⏰ PRODUITS EXPIRANT BIENTÔT (" + expiringProducts.size() + ")");
            addProductsTable(document, expiringProducts);
        } else if (expiredProducts.isEmpty()) {
            document.add(new Paragraph("✅ Aucun problème d'expiration détecté!")
                    .setFontSize(12).setTextAlignment(TextAlignment.CENTER));
        }

        addFooter(document);
        document.close();
    }

    /**
     * Génère un rapport de stock (faible stock, total, etc.)
     * @param outputPath Chemin du fichier PDF à créer
     * @throws IOException Si erreur lors de la création du fichier
     */
    public void generateStockReport(String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        addReportHeader(document, "RAPPORT GESTION STOCK");
        addGenerationDate(document);

        // KPIs principaux
        addStockKPIs(document);

        // Produits en rupture
        List<Produit> lowStockProducts = statisticsService.getLowStockProducts();
        if (!lowStockProducts.isEmpty()) {
            addSectionTitle(document, "⚠️ PRODUITS EN RUPTURE/FAIBLE STOCK");
            addProductsTable(document, lowStockProducts);
        }

        // Produits triés par quantité
        addSectionTitle(document, "📊 PRODUITS PAR QUANTITÉ DÉCROISSANTE");
        List<Produit> sorted = statisticsService.getProductsSortedByQuantity(true);
        addProductsTable(document, sorted);

        addFooter(document);
        document.close();
    }

    /**
     * Génère un rapport détaillé avec tous les détails et statistiques
     * @param outputPath Chemin du fichier PDF à créer
     * @throws IOException Si erreur lors de la création du fichier
     */
    public void generateComprehensiveReport(String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        addReportHeader(document, "RAPPORT COMPLET GESTION PRODUITS");
        addGenerationDate(document);

        // Page 1: Statistiques principales
        addStatisticsSummary(document);
        addStockKPIs(document);

        // Produits avec problèmes
        List<Produit> expiringProducts = statisticsService.getExpiringProducts();
        List<Produit> expiredProducts = statisticsService.getExpiredProducts();
        List<Produit> lowStockProducts = statisticsService.getLowStockProducts();

        if (!expiredProducts.isEmpty() || !expiringProducts.isEmpty()) {
            document.add(new AreaBreak());
            addSectionTitle(document, "SUIVI EXPIRATION");
            if (!expiredProducts.isEmpty()) {
                addSubSectionTitle(document, "Produits Expirés");
                addProductsTable(document, expiredProducts);
            }
            if (!expiringProducts.isEmpty()) {
                addSubSectionTitle(document, "Produits Expirant Bientôt");
                addProductsTable(document, expiringProducts);
            }
        }

        if (!lowStockProducts.isEmpty()) {
            document.add(new AreaBreak());
            addSectionTitle(document, "GESTION STOCK");
            addSubSectionTitle(document, "Produits en Rupture/Faible Stock");
            addProductsTable(document, lowStockProducts);
        }

        // Inventaire complet
        document.add(new AreaBreak());
        addSectionTitle(document, "INVENTAIRE COMPLET");
        addProductsTable(document, produitDAO.getAll());

        addFooter(document);
        document.close();
    }

    // ===== Méthodes privées d'aide =====

    private void addReportHeader(Document document, String title) throws IOException {
        Paragraph header = new Paragraph(title)
                .setFontSize(20)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(new DeviceRgb(33, 150, 243))  // Bleu #2196F3
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);
        document.add(new Paragraph("\n"));
    }

    private void addGenerationDate(Document document) {
        Paragraph date = new Paragraph("Généré le: " + LocalDate.now().format(DATE_FORMAT))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setItalic();
        document.add(date);
        document.add(new Paragraph("\n"));
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph section = new Paragraph(title)
                .setFontSize(14)
                .setBold()
                .setFontColor(new DeviceRgb(33, 150, 243))  // Bleu #2196F3
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(section);
    }

    private void addSubSectionTitle(Document document, String title) {
        Paragraph section = new Paragraph(title)
                .setFontSize(12)
                .setBold()
                .setFontColor(new DeviceRgb(102, 187, 255))  // Bleu clair #66BBFF
                .setMarginTop(8)
                .setMarginBottom(4);
        document.add(section);
    }

    private void addStatisticsSummary(Document document) {
        addSectionTitle(document, "RÉSUMÉ STATISTIQUES");

        Table statsTable = new Table(new float[]{1, 1, 1, 1});
        statsTable.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        addTableHeaderCell(statsTable, "Total Produits");
        addTableHeaderCell(statsTable, "Stock Total");
        addTableHeaderCell(statsTable, "Stock Moyen");
        addTableHeaderCell(statsTable, "Score Santé");

        // Valeurs
        addTableCell(statsTable, String.valueOf(statisticsService.getTotalProducts()));
        addTableCell(statsTable, String.valueOf(statisticsService.getTotalStock()));
        addTableCell(statsTable, String.format("%.1f", statisticsService.getAverageStock()));
        addTableCell(statsTable, String.format("%.1f%%", statisticsService.getHealthScore()));

        document.add(statsTable);
        document.add(new Paragraph("\n"));
    }

    private void addStockKPIs(Document document) {
        addSectionTitle(document, "INDICATEURS CLÉS (KPIs)");

        Table kpisTable = new Table(new float[]{1, 1, 1, 1});
        kpisTable.setWidth(UnitValue.createPercentValue(100));

        addTableHeaderCell(kpisTable, "Expirés");
        addTableHeaderCell(kpisTable, "Expirant Bientôt");
        addTableHeaderCell(kpisTable, "Faible Stock");
        addTableHeaderCell(kpisTable, "Valeur Stock");

        addTableCell(kpisTable, String.valueOf(statisticsService.getExpiredProductCount()));
        addTableCell(kpisTable, String.valueOf(statisticsService.getExpiringProductCount()));
        addTableCell(kpisTable, String.valueOf(statisticsService.getLowStockProductCount()));
        addTableCell(kpisTable, String.format("%.2f€", statisticsService.getTotalStockValue()));

        document.add(kpisTable);
        document.add(new Paragraph("\n"));
    }

    private void addProductsTable(Document document, List<Produit> products) {
        if (products.isEmpty()) {
            document.add(new Paragraph("Aucun produit à afficher.")
                    .setFontSize(11)
                    .setItalic());
            return;
        }

        Table table = new Table(new float[]{0.8f, 2, 1.2f, 1, 1.2f, 1.2f});
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        addTableHeaderCell(table, "ID");
        addTableHeaderCell(table, "Nom");
        addTableHeaderCell(table, "Quantité");
        addTableHeaderCell(table, "Unité");
        addTableHeaderCell(table, "Expiration");
        addTableHeaderCell(table, "Prix/U (€)");

        // Données
        for (Produit p : products) {
            addTableCell(table, String.valueOf(p.getIdProduit()));
            addTableCell(table, p.getNom());
            addTableCell(table, String.valueOf(p.getQuantite()));
            addTableCell(table, p.getUnite());

            String dateStr = p.getDateExpiration() != null
                    ? p.getDateExpiration().format(DATE_FORMAT)
                    : "-";
            addTableCell(table, dateStr);

            String priceStr = p.getPrixUnitaire() > 0
                    ? String.format("%.2f", p.getPrixUnitaire())
                    : "-";
            addTableCell(table, priceStr);
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTableHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(33, 150, 243))  // Bleu #2196F3
                .setFontColor(ColorConstants.WHITE)
                .setBorder(new SolidBorder(1));
        table.addCell(cell);
    }

    private void addTableCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text))
                .setBorder(new SolidBorder(0.5f));
        table.addCell(cell);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("© 2025 Gestion Produits Premium - Rapport Confidentiel")
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);
    }
}








