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

public class PdfService {

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
}