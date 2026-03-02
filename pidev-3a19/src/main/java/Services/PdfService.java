package Services;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.ColorConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.Production;
import model.Produit;
import model.ProduitDAO;

public class PdfService {

    private final ProduitDAO produitDAO;

    public PdfService() {
        this.produitDAO = null;
    }

    public PdfService(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    public void generateProductionPdf(List<Production> productions, String path) {

        try {

            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ===== FARMTECH GREEN COLOR =====
            Color green = new DeviceRgb(46, 125, 50);

            // ===== TITLE =====
            Paragraph title = new Paragraph("FARMTECH - Production Report")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(green)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(title);

            // Date
            Paragraph date = new Paragraph(
                    "Generated on: " +
                            LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            )
            ).setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(date);
            document.add(new Paragraph("\n"));

            // ===== TABLE =====
            Table table = new Table(UnitValue.createPercentArray(7))
                    .useAllAvailableWidth();

            // Header Style
            String[] headers = {
                    "ID", "Plant", "Variete",
                    "Quantite", "Date",
                    "Saison", "Etat"
            };

            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(green)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5);

                table.addHeaderCell(headerCell);
            }

            // ===== TABLE DATA =====
            for (Production p : productions) {

                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getId()))));
                table.addCell(new Cell().add(new Paragraph(p.getNomPlant())));
                table.addCell(new Cell().add(new Paragraph(p.getVariete())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getQuantite()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getDatePlante()))));
                table.addCell(new Cell().add(new Paragraph(p.getSaison())));
                table.addCell(new Cell().add(new Paragraph(p.getEtat())));
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("\n\nTotal Productions: " + productions.size())
                    .setBold());

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Generates a statistics report (requires PdfService(ProduitDAO) constructor). */
    public void generateComprehensiveReport(String path) {
        if (produitDAO == null) {
            throw new IllegalStateException("PdfService must be constructed with ProduitDAO for stats report");
        }
        try {
            StatisticsService stats = new StatisticsService(produitDAO);
            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            Color green = new DeviceRgb(46, 125, 50);

            document.add(new Paragraph("Rapport statistiques - Produits")
                    .setFontSize(20).setBold().setFontColor(green).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Indicateurs").setBold());
            document.add(new Paragraph("Nombre de produits: " + stats.getTotalProducts()));
            document.add(new Paragraph("Stock total: " + stats.getTotalStock()));
            document.add(new Paragraph("Stock moyen: " + String.format("%.1f", stats.getAverageStock())));
            document.add(new Paragraph("Score santé: " + String.format("%.1f%%", stats.getHealthScore())));
            document.add(new Paragraph("Produits expirés: " + stats.getExpiredProductCount()));
            document.add(new Paragraph("Expire bientôt (7j): " + stats.getExpiringProductCount()));
            document.add(new Paragraph("Stock faible (<5): " + stats.getLowStockProductCount()));
            document.add(new Paragraph("Valeur totale stock: " + String.format("%.2f €", stats.getTotalStockValue())));
            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
            String[] headers = { "ID", "Nom", "Quantité", "Unité", "Expiration" };
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(green).setTextAlignment(TextAlignment.CENTER).setPadding(5));
            }
            for (Produit p : stats.getProductsSortedByQuantity(true)) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getIdProduit()))));
                table.addCell(new Cell().add(new Paragraph(p.getNom() != null ? p.getNom() : "")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getQuantite()))));
                table.addCell(new Cell().add(new Paragraph(p.getUnite() != null ? p.getUnite() : "")));
                table.addCell(new Cell().add(new Paragraph(p.getDateExpiration() != null ? p.getDateExpiration().toString() : "-")));
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF: " + e.getMessage(), e);
        }
    }
}