package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ExportDocumentService {

    private static final DateTimeFormatter EXPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExportDocumentService() {
    }

    public static void exportPdf(String title, List<String> headers, List<List<String>> rows, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfStats stats = PdfStats.from(rows);
            int pageSize = 18;
            int totalPages = Math.max(1, (int) Math.ceil(rows.size() / (double) pageSize));

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                PDPage page = new PDPage(landscapeA4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    drawPdfHeader(content, title, rows.size(), stats);
                    drawPdfTableHeader(content);

                    int start = pageIndex * pageSize;
                    int end = Math.min(rows.size(), start + pageSize);
                    float y = 385;
                    for (int i = start; i < end; i++) {
                        drawPdfRow(content, rows.get(i), y, i % 2 == 0);
                        y -= 22;
                    }
                    drawPdfFooter(content, pageIndex + 1, totalPages);
                }
            }
            document.save(file);
        }
    }

    public static void exportSimplePdf(String title, List<String> headers, List<List<String>> rows, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            int pageSize = 24;
            int totalPages = Math.max(1, (int) Math.ceil(rows.size() / (double) pageSize));
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.setNonStrokingColor(new Color(18, 139, 87));
                    content.addRect(36, 770, 523, 42);
                    content.fill();
                    drawText(content, title, 52, 795, 16, true, Color.WHITE);
                    drawText(content, "Genere le " + EXPORT_DATE_FORMATTER.format(LocalDateTime.now()), 52, 780, 8, false, new Color(230, 255, 243));

                    float y = 730;
                    drawSimpleTableHeader(content, headers, y);
                    y -= 24;

                    int start = pageIndex * pageSize;
                    int end = Math.min(rows.size(), start + pageSize);
                    for (int i = start; i < end; i++) {
                        drawSimpleRow(content, rows.get(i), y, i % 2 == 0);
                        y -= 24;
                    }
                    drawText(content, "WasteWise TN", 36, 28, 8, true, new Color(18, 139, 87));
                    drawText(content, "Page " + (pageIndex + 1) + " / " + totalPages, 500, 28, 8, false, new Color(63, 109, 93));
                }
            }
            document.save(file);
        }
    }

    public static void exportImagePdf(BufferedImage image, File file) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDRectangle landscapeA4 = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscapeA4);
            document.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
            float margin = 20f;
            float maxWidth = landscapeA4.getWidth() - margin * 2;
            float maxHeight = landscapeA4.getHeight() - margin * 2;
            float scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
            float drawWidth = image.getWidth() * scale;
            float drawHeight = image.getHeight() * scale;
            float x = (landscapeA4.getWidth() - drawWidth) / 2f;
            float y = (landscapeA4.getHeight() - drawHeight) / 2f;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setNonStrokingColor(Color.WHITE);
                content.addRect(0, 0, landscapeA4.getWidth(), landscapeA4.getHeight());
                content.fill();
                content.drawImage(pdImage, x, y, drawWidth, drawHeight);
            }
            document.save(file);
        }
    }

    public static void exportXlsx(String title, List<String> headers, List<List<String>> rows, File file) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
            put(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                      <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                    </Types>
                    """);
            put(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            put(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                      <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                    </Relationships>
                    """);
            put(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                      <sheets><sheet name="Moderation" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>
                    """);
            put(zip, "xl/styles.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                      <fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts>
                      <fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FFE9F7EF"/><bgColor indexed="64"/></patternFill></fill></fills>
                      <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                      <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                      <cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/></cellXfs>
                    </styleSheet>
                    """);
            put(zip, "xl/worksheets/sheet1.xml", buildSheetXml(title, headers, rows));
        }
    }

    public static void exportDocx(String title, List<String> headers, List<List<String>> rows, File file) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
            put(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                    </Types>
                    """);
            put(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
                    </Relationships>
                    """);
            put(zip, "word/document.xml", buildDocumentXml(title, headers, rows));
        }
    }

    private static String buildSheetXml(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        int rowIndex = 1;
        xml.append("<row r=\"").append(rowIndex).append("\"><c r=\"A").append(rowIndex).append("\" t=\"inlineStr\" s=\"2\"><is><t>")
                .append(escapeXml(title)).append("</t></is></c></row>");
        rowIndex++;
        xml.append("<row r=\"").append(rowIndex).append("\"><c r=\"A").append(rowIndex).append("\" t=\"inlineStr\"><is><t>")
                .append(escapeXml("Genere le " + EXPORT_DATE_FORMATTER.format(LocalDateTime.now()) + " - " + rows.size() + " ligne(s)"))
                .append("</t></is></c></row>");
        rowIndex += 2;
        xml.append("<row r=\"").append(rowIndex).append("\">");
        for (int i = 0; i < headers.size(); i++) {
            xml.append(cellXml(columnName(i), rowIndex, headers.get(i), "1"));
        }
        xml.append("</row>");
        for (List<String> row : rows) {
            rowIndex++;
            xml.append("<row r=\"").append(rowIndex).append("\">");
            for (int i = 0; i < row.size(); i++) {
                xml.append(cellXml(columnName(i), rowIndex, row.get(i), "0"));
            }
            xml.append("</row>");
        }
        xml.append("</sheetData><autoFilter ref=\"A4:H").append(Math.max(4, rowIndex)).append("\"/>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private static String buildDocumentXml(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
        xml.append(paragraph(title, true));
        xml.append(paragraph("Genere le " + EXPORT_DATE_FORMATTER.format(LocalDateTime.now()) + " - " + rows.size() + " ligne(s)", false));
        xml.append("<w:tbl><w:tblPr><w:tblBorders>")
                .append(border("top")).append(border("left")).append(border("bottom")).append(border("right")).append(border("insideH")).append(border("insideV"))
                .append("</w:tblBorders></w:tblPr>");
        xml.append(tableRow(headers, true));
        for (List<String> row : rows) {
            xml.append(tableRow(row, false));
        }
        xml.append("</w:tbl><w:sectPr><w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/></w:sectPr></w:body></w:document>");
        return xml.toString();
    }

    private static void drawPdfHeader(PDPageContentStream content, String title, int totalRows, PdfStats stats) throws IOException {
        content.setNonStrokingColor(new Color(18, 139, 87));
        content.addRect(32, 515, 778, 54);
        content.fill();

        drawText(content, title, 48, 548, 18, true, Color.WHITE);
        drawText(content, "Back Office WasteWise - rapport de moderation", 48, 528, 9, false, new Color(230, 255, 243));
        drawText(content, "Genere le " + EXPORT_DATE_FORMATTER.format(LocalDateTime.now()), 630, 548, 9, false, Color.WHITE);
        drawText(content, totalRows + " reponse(s)", 690, 528, 12, true, Color.WHITE);

        drawKpi(content, "En attente", stats.pending(), 48, 455, new Color(255, 241, 199), new Color(154, 107, 0));
        drawKpi(content, "Validees", stats.validated(), 208, 455, new Color(216, 243, 229), new Color(8, 112, 68));
        drawKpi(content, "Refusees", stats.refused(), 368, 455, new Color(251, 228, 228), new Color(185, 28, 28));
        drawKpi(content, "Taux validation", Math.round(stats.validationRate()) + "%", 528, 455, new Color(232, 240, 255), new Color(30, 58, 138));
    }

    private static void drawKpi(PDPageContentStream content, String label, Object value, float x, float y, Color background, Color foreground) throws IOException {
        content.setNonStrokingColor(background);
        content.addRect(x, y, 140, 48);
        content.fill();
        content.setStrokingColor(new Color(203, 230, 216));
        content.addRect(x, y, 140, 48);
        content.stroke();
        drawText(content, label, x + 10, y + 31, 8, false, new Color(63, 109, 93));
        drawText(content, String.valueOf(value), x + 10, y + 12, 16, true, foreground);
    }

    private static void drawPdfTableHeader(PDPageContentStream content) throws IOException {
        float[] widths = columnWidths();
        String[] labels = {"Id", "Quantite", "Date soumis", "Statut", "Message", "Score", "Appel", "Citoyen"};
        float x = 36;
        float y = 407;
        content.setNonStrokingColor(new Color(233, 247, 239));
        content.addRect(x, y, totalWidth(widths), 24);
        content.fill();
        content.setStrokingColor(new Color(190, 224, 207));
        content.addRect(x, y, totalWidth(widths), 24);
        content.stroke();

        for (int i = 0; i < labels.length; i++) {
            drawText(content, labels[i], x + 6, y + 8, 8, true, new Color(7, 95, 62));
            x += widths[i];
        }
    }

    private static void drawSimpleTableHeader(PDPageContentStream content, List<String> headers, float y) throws IOException {
        float[] widths = simpleColumnWidths(headers.size());
        float x = 36;
        content.setNonStrokingColor(new Color(233, 247, 239));
        content.addRect(x, y, totalWidth(widths), 24);
        content.fill();
        content.setStrokingColor(new Color(190, 224, 207));
        content.addRect(x, y, totalWidth(widths), 24);
        content.stroke();
        for (int i = 0; i < widths.length; i++) {
            String label = i < headers.size() ? headers.get(i) : "";
            drawText(content, label, x + 8, y + 8, 9, true, new Color(7, 95, 62));
            x += widths[i];
        }
    }

    private static void drawSimpleRow(PDPageContentStream content, List<String> row, float y, boolean alternate) throws IOException {
        float[] widths = simpleColumnWidths(Math.max(1, row.size()));
        float x = 36;
        if (alternate) {
            content.setNonStrokingColor(new Color(250, 255, 252));
            content.addRect(x, y, totalWidth(widths), 24);
            content.fill();
        }
        content.setStrokingColor(new Color(215, 234, 224));
        content.addRect(x, y, totalWidth(widths), 24);
        content.stroke();
        for (int i = 0; i < widths.length; i++) {
            String value = i < row.size() ? row.get(i) : "";
            drawText(content, truncate(toPdfText(value), widthToChars(widths[i])), x + 8, y + 8, 8, false, new Color(6, 45, 40));
            x += widths[i];
        }
    }

    private static float[] simpleColumnWidths(int count) {
        int safeCount = Math.max(1, Math.min(5, count));
        float[] widths = new float[safeCount];
        float width = 523f / safeCount;
        for (int i = 0; i < safeCount; i++) {
            widths[i] = width;
        }
        return widths;
    }

    private static void drawPdfRow(PDPageContentStream content, List<String> row, float y, boolean alternate) throws IOException {
        float[] widths = columnWidths();
        float x = 36;
        if (alternate) {
            content.setNonStrokingColor(new Color(250, 255, 252));
            content.addRect(x, y, totalWidth(widths), 22);
            content.fill();
        }
        content.setStrokingColor(new Color(215, 234, 224));
        content.addRect(x, y, totalWidth(widths), 22);
        content.stroke();

        for (int i = 0; i < widths.length; i++) {
            String value = i < row.size() ? row.get(i) : "";
            Color color = i == 3 ? statusColor(value) : new Color(6, 45, 40);
            drawText(content, truncate(toPdfText(value), widthToChars(widths[i])), x + 6, y + 7, 7.5f, i == 3, color);
            x += widths[i];
        }
    }

    private static void drawPdfFooter(PDPageContentStream content, int page, int totalPages) throws IOException {
        content.setStrokingColor(new Color(203, 230, 216));
        content.moveTo(36, 28);
        content.lineTo(806, 28);
        content.stroke();
        drawText(content, "WasteWise TN", 36, 14, 8, true, new Color(18, 139, 87));
        drawText(content, "Page " + page + " / " + totalPages, 748, 14, 8, false, new Color(63, 109, 93));
    }

    private static void drawText(PDPageContentStream content, String text, float x, float y, float size, boolean bold, Color color) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        content.newLineAtOffset(x, y);
        content.showText(toPdfText(text));
        content.endText();
    }

    private static float[] columnWidths() {
        return new float[]{38, 78, 100, 74, 258, 60, 54, 54};
    }

    private static float totalWidth(float[] widths) {
        float total = 0;
        for (float width : widths) {
            total += width;
        }
        return total;
    }

    private static int widthToChars(float width) {
        return Math.max(4, (int) (width / 4.4));
    }

    private static String truncate(String value, int maxLength) {
        String normalized = nullToEmpty(value).replace('\n', ' ').replace('\r', ' ');
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 1)) + ".";
    }

    private static Color statusColor(String value) {
        String status = nullToEmpty(value).toLowerCase();
        if (status.contains("valide")) {
            return new Color(8, 112, 68);
        }
        if (status.contains("refuse")) {
            return new Color(185, 28, 28);
        }
        return new Color(154, 107, 0);
    }

    private static String tableRow(List<String> cells, boolean header) {
        StringBuilder xml = new StringBuilder("<w:tr>");
        for (String cell : cells) {
            xml.append("<w:tc><w:tcPr><w:tcW w:w=\"2200\" w:type=\"dxa\"/>");
            if (header) {
                xml.append("<w:shd w:fill=\"E9F7EF\"/>");
            }
            xml.append("</w:tcPr>").append(paragraph(nullToEmpty(cell), header)).append("</w:tc>");
        }
        xml.append("</w:tr>");
        return xml.toString();
    }

    private static String paragraph(String text, boolean bold) {
        return "<w:p><w:r>" + (bold ? "<w:rPr><w:b/></w:rPr>" : "") + "<w:t>" + escapeXml(nullToEmpty(text)) + "</w:t></w:r></w:p>";
    }

    private static String border(String side) {
        return "<w:" + side + " w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBE6D8\"/>";
    }

    private static String cellXml(String column, int rowIndex, String value, String style) {
        return "<c r=\"" + column + rowIndex + "\" t=\"inlineStr\" s=\"" + style + "\"><is><t>" + escapeXml(nullToEmpty(value)) + "</t></is></c>";
    }

    private static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int value = index + 1;
        while (value > 0) {
            int remainder = (value - 1) % 26;
            name.insert(0, (char) ('A' + remainder));
            value = (value - 1) / 26;
        }
        return name.toString();
    }

    private static String cell(List<String> values, int index, int width) {
        String value = index < values.size() ? toPdfText(values.get(index)) : "";
        value = value.replace('\n', ' ').replace('\r', ' ');
        if (value.length() > width) {
            return value.substring(0, Math.max(0, width - 1)) + ".";
        }
        return value;
    }

    private static void put(ZipOutputStream zip, String path, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String escapeXml(String value) {
        return nullToEmpty(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String toPdfText(String value) {
        return nullToEmpty(value)
                .replace('é', 'e')
                .replace('è', 'e')
                .replace('ê', 'e')
                .replace('à', 'a')
                .replace('ç', 'c')
                .replace('ù', 'u')
                .replace('î', 'i')
                .replace('ï', 'i')
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record PdfStats(long pending, long validated, long refused, double validationRate) {
        private static PdfStats from(List<List<String>> rows) {
            long pending = 0;
            long validated = 0;
            long refused = 0;
            for (List<String> row : rows) {
                String status = row.size() > 3 ? row.get(3).toLowerCase() : "";
                if (status.contains("valide")) {
                    validated++;
                } else if (status.contains("refuse")) {
                    refused++;
                } else {
                    pending++;
                }
            }
            double rate = rows.isEmpty() ? 0 : (validated * 100d) / rows.size();
            return new PdfStats(pending, validated, refused, rate);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
