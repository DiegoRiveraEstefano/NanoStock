package cl.diego.NanoStock.dataimport.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import cl.diego.NanoStock.common.data.DataFormat;

@Service
public class ImportTemplateService {

    public byte[] generateTemplate(String entityType, DataFormat format) throws Exception {
        if ("inventory".equalsIgnoreCase(entityType)) {
            return format == DataFormat.EXCEL ? generateInventoryExcel() : generateInventoryCsv();
        } else {
            return format == DataFormat.EXCEL ? generateProductExcel() : generateProductCsv();
        }
    }

    private byte[] generateProductExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                "Nombre", "Descripcion", "CodigoBarras", "SKU", "Categoria",
                "Marca", "UnidadMedida", "StockMinimo", "StockOptimo", "EsPerecible"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de ejemplo
            Row sample1 = sheet.createRow(1);
            sample1.createCell(0).setCellValue("Leche Entera 1L");
            sample1.createCell(1).setCellValue("Caja larga vida 1 Litro");
            sample1.createCell(2).setCellValue("7801234567890");
            sample1.createCell(3).setCellValue("LEC-ENT-01");
            sample1.createCell(4).setCellValue("Lácteos");
            sample1.createCell(5).setCellValue("Colun");
            sample1.createCell(6).setCellValue("LITER");
            sample1.createCell(7).setCellValue(2.0);
            sample1.createCell(8).setCellValue(6.0);
            sample1.createCell(9).setCellValue("SI");

            Row sample2 = sheet.createRow(2);
            sample2.createCell(0).setCellValue("Arroz Grado 1");
            sample2.createCell(1).setCellValue("Bolsa 1 Kilogramo");
            sample2.createCell(2).setCellValue("7809876543210");
            sample2.createCell(3).setCellValue("ARR-GR1-01");
            sample2.createCell(4).setCellValue("Despensa");
            sample2.createCell(5).setCellValue("Tucapel");
            sample2.createCell(6).setCellValue("KILOGRAM");
            sample2.createCell(7).setCellValue(1.0);
            sample2.createCell(8).setCellValue(4.0);
            sample2.createCell(9).setCellValue("NO");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateProductCsv() throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                 .setHeader("Nombre", "Descripcion", "CodigoBarras", "SKU", "Categoria",
                            "Marca", "UnidadMedida", "StockMinimo", "StockOptimo", "EsPerecible")
                 .build())) {

            csvPrinter.printRecord("Leche Entera 1L", "Caja larga vida 1 Litro", "7801234567890", "LEC-ENT-01", "Lácteos", "Colun", "LITER", "2.0", "6.0", "SI");
            csvPrinter.printRecord("Arroz Grado 1", "Bolsa 1 Kilogramo", "7809876543210", "ARR-GR1-01", "Despensa", "Tucapel", "KILOGRAM", "1.0", "4.0", "NO");
            csvPrinter.flush();
            return out.toByteArray();
        }
    }

    private byte[] generateInventoryExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                "CodigoBarras", "Ubicacion", "TipoAlmacenamiento", "Cantidad",
                "FechaVencimiento", "Estado", "Lote"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample1 = sheet.createRow(1);
            sample1.createCell(0).setCellValue("7801234567890");
            sample1.createCell(1).setCellValue("Refrigerador");
            sample1.createCell(2).setCellValue("REFRIGERATED");
            sample1.createCell(3).setCellValue(3.0);
            sample1.createCell(4).setCellValue("2026-12-31");
            sample1.createCell(5).setCellValue("SEALED");
            sample1.createCell(6).setCellValue("LOTE-2026A");

            Row sample2 = sheet.createRow(2);
            sample2.createCell(0).setCellValue("7809876543210");
            sample2.createCell(1).setCellValue("Despensa Principal");
            sample2.createCell(2).setCellValue("AMBIENT");
            sample2.createCell(3).setCellValue(5.0);
            sample2.createCell(4).setCellValue("2027-06-15");
            sample2.createCell(5).setCellValue("SEALED");
            sample2.createCell(6).setCellValue("LOTE-2026B");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generateInventoryCsv() throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                 .setHeader("CodigoBarras", "Ubicacion", "TipoAlmacenamiento", "Cantidad",
                            "FechaVencimiento", "Estado", "Lote")
                 .build())) {

            csvPrinter.printRecord("7801234567890", "Refrigerador", "REFRIGERATED", "3.0", "2026-12-31", "SEALED", "LOTE-2026A");
            csvPrinter.printRecord("7809876543210", "Despensa Principal", "AMBIENT", "5.0", "2027-06-15", "SEALED", "LOTE-2026B");
            csvPrinter.flush();
            return out.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
