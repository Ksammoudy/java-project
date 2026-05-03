package org.example.services;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.models.ZonePolluee;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PDFExportService {

    private static final Color PRIMARY_COLOR = new DeviceRgb(46, 125, 50);
    private static final Color CRITICAL_COLOR = new DeviceRgb(220, 53, 69);
    private static final Color WARNING_COLOR = new DeviceRgb(255, 193, 7);
    private static final Color SUCCESS_COLOR = new DeviceRgb(40, 167, 69);

    public static String generateReport(List<ZonePolluee> zones, String reportType) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "WasteWise_Report_" + reportType + "_" + timestamp + ".pdf";
            String filepath = System.getProperty("user.home") + "/Downloads/" + filename;

            PdfWriter writer = new PdfWriter(new FileOutputStream(filepath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // Use Helvetica font instead of StandardFonts
            PdfFont font = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Add header
            addHeader(document, boldFont, reportType);

            // Add content based on report type
            if (reportType.equals("FULL") || reportType.equals("EXECUTIVE")) {
                addSummaryStatistics(document, zones, boldFont, font);
                addRiskDistribution(document, zones, boldFont, font);
            }

            if (reportType.equals("FULL") || reportType.equals("STATISTICS")) {
                addDetailedStatistics(document, zones, boldFont, font);
            }

            if (reportType.equals("FULL") || reportType.equals("ZONES")) {
                addZonesTable(document, zones, boldFont, font);
            }

            if (reportType.equals("FULL") || reportType.equals("EXECUTIVE")) {
                addRecommendations(document, zones, boldFont, font);
            }

            addFooter(document, font);
            document.close();

            return filepath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addHeader(Document document, PdfFont boldFont, String reportType) throws Exception {
        Paragraph title = new Paragraph("🌱 WasteWise TN - Rapport Environnemental")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(PRIMARY_COLOR);
        document.add(title);

        String subtitle = getReportSubtitle(reportType);
        Paragraph subTitle = new Paragraph(subtitle)
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(100, 100, 100));
        document.add(subTitle);

        Paragraph date = new Paragraph("Généré le: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(150, 150, 150));
        document.add(date);

        // Line separator
        Paragraph line = new Paragraph("__________________________________________________")
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(line);
        document.add(new Paragraph("\n"));
    }

    private static String getReportSubtitle(String reportType) {
        switch (reportType) {
            case "FULL": return "Rapport Complet - Analyse Détaillée";
            case "EXECUTIVE": return "Résumé Exécutif pour la Direction";
            case "STATISTICS": return "Rapport Statistique Détaillé";
            case "ZONES": return "Liste Complète des Zones Polluées";
            default: return "Rapport Standard";
        }
    }

    private static void addSummaryStatistics(Document document, List<ZonePolluee> zones,
                                             PdfFont boldFont, PdfFont font) throws Exception {
        Paragraph sectionTitle = new Paragraph("📊 RÉSUMÉ STATISTIQUE")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        int total = zones.size();
        int critical = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        int medium = (int) zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
        int low = (int) zones.stream().filter(z -> z.getNiveauPollution() < 4).count();
        double avg = zones.stream().mapToInt(ZonePolluee::getNiveauPollution).average().orElse(0);

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
        statsTable.setWidth(UnitValue.createPercentValue(100));

        addStatRow(statsTable, "Total des zones:", String.valueOf(total), boldFont, font, PRIMARY_COLOR);
        addStatRow(statsTable, "Niveau moyen:", String.format("%.1f/10", avg), boldFont, font,
                avg >= 7 ? CRITICAL_COLOR : (avg >= 4 ? WARNING_COLOR : SUCCESS_COLOR));
        addStatRow(statsTable, "Zones critiques:", String.valueOf(critical), boldFont, font, CRITICAL_COLOR);
        addStatRow(statsTable, "Zones à risque moyen:", String.valueOf(medium), boldFont, font, WARNING_COLOR);
        addStatRow(statsTable, "Zones à faible risque:", String.valueOf(low), boldFont, font, SUCCESS_COLOR);

        document.add(statsTable);
        document.add(new Paragraph("\n"));
    }

    private static void addStatRow(Table table, String label, String value,
                                   PdfFont boldFont, PdfFont font, Color color) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(boldFont));
        labelCell.setBorder(null);

        Cell valueCell = new Cell().add(new Paragraph(value).setFont(font).setFontColor(color));
        valueCell.setBorder(null);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addRiskDistribution(Document document, List<ZonePolluee> zones,
                                            PdfFont boldFont, PdfFont font) throws Exception {
        Paragraph sectionTitle = new Paragraph("⚠️ ANALYSE DES RISQUES")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        List<ZonePolluee> criticalZones = zones.stream()
                .filter(z -> z.getNiveauPollution() >= 7)
                .toList();

        if (!criticalZones.isEmpty()) {
            Paragraph criticalTitle = new Paragraph("🔴 ZONES CRITIQUES (Action urgente requise)")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(CRITICAL_COLOR);
            document.add(criticalTitle);

            for (ZonePolluee zone : criticalZones) {
                Paragraph zoneInfo = new Paragraph("• " + zone.getNomZone() +
                        " - Niveau " + zone.getNiveauPollution() + "/10")
                        .setFont(font)
                        .setFontSize(10);
                document.add(zoneInfo);
            }
            document.add(new Paragraph("\n"));
        }
    }

    private static void addDetailedStatistics(Document document, List<ZonePolluee> zones,
                                              PdfFont boldFont, PdfFont font) throws Exception {
        Paragraph sectionTitle = new Paragraph("📈 STATISTIQUES DÉTAILLÉES")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        // Distribution by level
        for (int level = 1; level <= 10; level++) {
            final int currentLevel = level;
            long count = zones.stream().filter(z -> z.getNiveauPollution() == currentLevel).count();
            if (count > 0) {
                int barLength = (int) (count * 100.0 / zones.size() * 2);
                StringBuilder barBuilder = new StringBuilder();
                for (int i = 0; i < Math.min(barLength, 40); i++) {
                    barBuilder.append("█");
                }
                String bar = barBuilder.toString();

                Table levelRow = new Table(UnitValue.createPercentArray(new float[]{15, 15, 70}));
                levelRow.setWidth(UnitValue.createPercentValue(100));

                levelRow.addCell(new Cell().add(new Paragraph("Niveau " + level + ":").setFont(font)).setBorder(null));
                levelRow.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(font)).setBorder(null));
                levelRow.addCell(new Cell().add(new Paragraph(bar).setFont(font).setFontColor(getColorForLevel(level))).setBorder(null));

                document.add(levelRow);
            }
        }

        document.add(new Paragraph("\n"));
    }

    private static Color getColorForLevel(int level) {
        if (level >= 8) return CRITICAL_COLOR;
        if (level >= 6) return new DeviceRgb(255, 87, 34);
        if (level >= 4) return WARNING_COLOR;
        if (level >= 2) return new DeviceRgb(76, 175, 80);
        return SUCCESS_COLOR;
    }

    private static void addZonesTable(Document document, List<ZonePolluee> zones,
                                      PdfFont boldFont, PdfFont font) throws Exception {
        Paragraph sectionTitle = new Paragraph("📋 LISTE DES ZONES POLLUÉES")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        Table zonesTable = new Table(UnitValue.createPercentArray(new float[]{30, 15, 35, 20}));
        zonesTable.setWidth(UnitValue.createPercentValue(100));

        // Headers
        String[] headers = {"Nom de la zone", "Niveau", "Coordonnées GPS", "Statut"};
        for (String header : headers) {
            Cell headerCell = new Cell().add(new Paragraph(header).setFont(boldFont));
            headerCell.setBackgroundColor(PRIMARY_COLOR);
            headerCell.setFontColor(new DeviceRgb(255, 255, 255));
            headerCell.setPadding(5);
            zonesTable.addCell(headerCell);
        }

        // Data rows (limit to 15 for PDF readability)
        zones.stream().limit(15).forEach(zone -> {
            zonesTable.addCell(new Cell().add(new Paragraph(zone.getNomZone()).setFont(font)));

            String levelText = zone.getNiveauPollution() + "/10";
            Color levelColor = getColorForLevel(zone.getNiveauPollution());
            zonesTable.addCell(new Cell().add(new Paragraph(levelText).setFont(font).setFontColor(levelColor)));

            zonesTable.addCell(new Cell().add(new Paragraph(zone.getCoordonneesGps()).setFont(font)));

            String status = zone.getNiveauPollution() >= 7 ? "Critique" :
                    (zone.getNiveauPollution() >= 4 ? "À surveiller" : "Correct");
            zonesTable.addCell(new Cell().add(new Paragraph(status).setFont(font)));
        });

        document.add(zonesTable);
        document.add(new Paragraph("\n"));

        if (zones.size() > 15) {
            Paragraph note = new Paragraph("Note: Seules les 15 premières zones sont affichées. Le rapport complet inclut toutes les zones.")
                    .setFont(font)
                    .setFontSize(8)
                    .setFontColor(new DeviceRgb(150, 150, 150));
            document.add(note);
        }
    }

    private static void addRecommendations(Document document, List<ZonePolluee> zones,
                                           PdfFont boldFont, PdfFont font) throws Exception {
        Paragraph sectionTitle = new Paragraph("🌱 RECOMMANDATIONS")
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR);
        document.add(sectionTitle);
        document.add(new Paragraph("\n"));

        List<String> recommendations = new ArrayList<>();

        long criticalCount = zones.stream().filter(z -> z.getNiveauPollution() >= 7).count();
        if (criticalCount > 0) {
            recommendations.add("⚠️ Intervention immédiate requise dans " + criticalCount + " zone(s) critique(s)");
            recommendations.add("🏥 Équiper les équipes d'intervention d'équipements de protection");
        }

        long mediumCount = zones.stream().filter(z -> z.getNiveauPollution() >= 4 && z.getNiveauPollution() < 7).count();
        if (mediumCount > 0) {
            recommendations.add("📊 Surveillance renforcée des " + mediumCount + " zones à risque moyen");
        }

        recommendations.add("🌳 Planter des arbres pour améliorer la qualité de l'air");
        recommendations.add("♻️ Organiser des campagnes de sensibilisation");
        recommendations.add("📈 Mettre en place un système de surveillance continue");

        for (String rec : recommendations) {
            Paragraph recPara = new Paragraph("• " + rec).setFont(font).setFontSize(11);
            document.add(recPara);
        }

        document.add(new Paragraph("\n"));
    }

    private static void addFooter(Document document, PdfFont font) throws Exception {
        document.add(new Paragraph("\n"));

        Paragraph line = new Paragraph("__________________________________________________")
                .setFontColor(new DeviceRgb(150, 150, 150))
                .setTextAlignment(TextAlignment.CENTER);
        document.add(line);

        Paragraph footer = new Paragraph(
                "📧 support@wastewise.tn | 🌐 www.wastewise.tn\n" +
                        "Ce rapport est généré automatiquement par WasteWise TN Analytics Pro")
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(150, 150, 150));
        document.add(footer);
    }
}