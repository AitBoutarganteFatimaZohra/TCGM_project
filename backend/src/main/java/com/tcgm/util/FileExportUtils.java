package com.tcgm.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileExportUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Exporte une liste de données en PDF
     */
    public static ResponseEntity<InputStreamResource> exportToPdf(
            String title,
            List<String> headers,
            List<List<Object>> rows,
            String filenamePrefix) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Titre
        document.add(new Paragraph(title).setBold().setFontSize(18));

        // Date d'export
        document.add(new Paragraph("Exporté le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            .setFontSize(10));

        document.add(new Paragraph(" ")); // Espace

        // Création du tableau
        Table table = new Table(UnitValue.createPercentArray(headers.size()));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        for (String header : headers) {
            table.addCell(new Paragraph(header).setBold());
        }

        // Données
        for (List<Object> row : rows) {
            for (Object cell : row) {
                table.addCell(new Paragraph(cell != null ? cell.toString() : ""));
            }
        }

        document.add(table);
        document.close();

        String filename = filenamePrefix + "_" + LocalDateTime.now().format(FORMATTER) + ".pdf";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }

    /**
     * Exporte une liste de données en Excel
     */
    public static ResponseEntity<InputStreamResource> exportToExcel(
            String sheetName,
            List<String> headers,
            List<List<Object>> rows,
            String filenamePrefix) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Création du style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Création du style pour les cellules
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Données
            int rowNum = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = rowData.get(i);
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                    cell.setCellStyle(cellStyle);
                }
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            String filename = filenamePrefix + "_" + LocalDateTime.now().format(FORMATTER) + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    /**
     * Exporte une liste de données en CSV
     */
    public static ResponseEntity<InputStreamResource> exportToCsv(
            List<String> headers,
            List<List<Object>> rows,
            String filenamePrefix) throws IOException {

        StringBuilder csvBuilder = new StringBuilder();

        // En-têtes
        csvBuilder.append(String.join(";", headers)).append("\n");

        // Données
        for (List<Object> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                Object value = row.get(i);
                if (value != null) {
                    String str = value.toString();
                    // Échapper les guillemets
                    str = str.replace("\"", "\"\"");
                    // Si la valeur contient des virgules ou des guillemets, l'encadrer de guillemets
                    if (str.contains(";") || str.contains("\"")) {
                        str = "\"" + str + "\"";
                    }
                    csvBuilder.append(str);
                }
                if (i < row.size() - 1) {
                    csvBuilder.append(";");
                }
            }
            csvBuilder.append("\n");
        }

        byte[] bytes = csvBuilder.toString().getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        String filename = filenamePrefix + "_" + LocalDateTime.now().format(FORMATTER) + ".csv";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(new InputStreamResource(in));
    }

    /**
     * Génère un nom de fichier avec timestamp
     */
    public static String generateFilename(String prefix, String extension) {
        return prefix + "_" + LocalDateTime.now().format(FORMATTER) + "." + extension;
    }
}